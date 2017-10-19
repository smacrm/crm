package gnext.security.service;

import gnext.bean.Company;
import gnext.bean.DatabaseServer;
import gnext.bean.Member;
import gnext.model.authority.RoleModel;
import gnext.model.authority.UserModel;
import gnext.security.SecurityService;
import gnext.service.DatabaseServerService;
import gnext.service.MemberService;
import gnext.utils.EncoderUtil;
import gnext.validator.IpValidator;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 *
 * @author hungpham
 */
@EJB(name = "memberService", beanInterface = MemberService.class)
@Component
public class AuthenticationService implements UserDetailsService {
    final private Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final String MEMBER_EJB_LOOKUP_PATH = "java:comp/env/memberService";
    private static final String DS_EJB_LOOKUP_PATH = "java:comp/env/databaseServerService";
    
    @Setter private MemberService memberService;
    @Setter private DatabaseServerService databaseServerService;
    
    @Override
    public UserModel loadUserByUsername(final String loginExp) throws UsernameNotFoundException {
        String[] arrLoginExp = StringUtils.split(loginExp, ":");
        String companyBasicLoginId = arrLoginExp[1];
        String loginId = arrLoginExp[0];
        Member bean = null;
        
        logger.debug("[SEC] Getting access details from MemberController " + loginId + " !!");
        memberService = this.lookupMemberEJBService();
        try {
            Object[] authInfo = memberService.findBasicAuth(companyBasicLoginId);
            bean = memberService.findByUsername(loginId, Integer.parseInt(authInfo[2].toString()));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            bean = null;
        }
        UserModel user = null;
        if (bean != null) user = this.convertMemberObject(bean);

        return user;
    }
    
    public boolean basicAuthCheck(final String loginId, final String password) throws UsernameNotFoundException {
        logger.debug("[SEC] Getting Basic Auth checking " + loginId + " !!");
        memberService = this.lookupMemberEJBService();
        Object[] obj = memberService.findBasicAuth(loginId);
        if (obj != null && EncoderUtil.getPassEncoder().matches(password, String.valueOf(obj[1]))) {
            memberService.persitCompanyIdAfterBasicAuth(NumberUtils.toInt(String.valueOf(obj[2]), 0));
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    
    public boolean isGlobalIpPass(String globalIp, String basicLoginId){
        logger.debug("[SEC] Checking Ip restrict " + globalIp + " - " + basicLoginId + " !!");
        memberService = this.lookupMemberEJBService();
        try {
            if (!StringUtils.isEmpty(globalIp) && !"127.0.0.1".equals(globalIp) && !"0.0.0.0".equals(globalIp) && new IpValidator().doValidate(globalIp)) {
                return memberService.isGlobalIpPass(globalIp, basicLoginId);
            }
            return true; //Trong truong hop dia chi IP khong dung, hoac la tu localhost thi luon luon cho pass
        }catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    // @Transactional(readOnly = true)
    public boolean isGlobalIpPass(String globalIp, String basicLoginId, String loginId){
        logger.debug("[SEC] Checking User Ip restrict " + globalIp + " - " + basicLoginId + " !!");
        memberService = this.lookupMemberEJBService();
        try{
            Object[] authInfo = memberService.findBasicAuth(basicLoginId);
            if(authInfo == null || authInfo.length < 2) return false;
            loginId = String.format("GN%03d%s", Integer.parseInt(authInfo[2].toString()), loginId);
            if(!StringUtils.isEmpty(globalIp) 
                    && !"127.0.0.1".equals(globalIp) && !"0.0.0.0".equals(globalIp) 
                    && new IpValidator().doValidate(globalIp)){
                return memberService.isGlobalIpUserPass(globalIp, basicLoginId, loginId);
            }
            return true; //Trong truong hop dia chi IP khong dung, hoac la tu localhost thi luon luon cho pass
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        return false;
    }
    
    public UserModel convertMemberObject(Member bean) {
        try {
            UserModel user = new UserModel(bean);
            List<RoleModel> roleList = new ArrayList<>();
            roleList.add(new RoleModel(SecurityService.Role.ROLE_BASIC_AUTH.toString()));
            roleList.add(new RoleModel(SecurityService.Role.ROLE_LOGGED_IN.toString()));
            if (SecurityService.checkMemberIdIsSuperAdmin(user.getUserId())) roleList.add(new RoleModel(SecurityService.Role.ROLE_MASTER.toString()));
            user.setAuthorities(roleList);
            
            return user;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private DatabaseServerService lookupDsEJBService() {
        if (null != databaseServerService) return databaseServerService;
        try {
            InitialContext initialContext = new InitialContext();
            databaseServerService = (DatabaseServerService) initialContext.lookup(DS_EJB_LOOKUP_PATH);
            return databaseServerService;
        } catch (NamingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    private MemberService lookupMemberEJBService() {
        if (null != memberService) return memberService;
        try {
            InitialContext initialContext = new InitialContext();
            memberService = (MemberService) initialContext.lookup(MEMBER_EJB_LOOKUP_PATH);
            return memberService;
        } catch (NamingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public DatabaseServer getDatabaseServerOfCompany(String companyBasicLoginId) {
        try {
            if(StringUtils.isEmpty(companyBasicLoginId)) return null;

            memberService = this.lookupMemberEJBService();
            if(memberService == null) return null;

            databaseServerService = lookupDsEJBService();
            if(databaseServerService == null) return null;

            Object[] authInfo = memberService.findBasicAuth(companyBasicLoginId);
            Integer companyLoginedId = Integer.parseInt(authInfo[2].toString());
            if(companyLoginedId == Company.MASTER_COMPANY_ID) return new DatabaseServer();
            
            return databaseServerService.findOneDatabaseServer(companyLoginedId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    public boolean testConnectionToSlaveDb(DatabaseServer databaseServer) {
        if(databaseServer == null) return false;
        
        // thử cố gắng kết nối tới cơ sở dữ liệu slave.
        // nếu không thành công thì báo lỗi.
        // ngược lại cho qua tiếp tục authentication.
        
        
        return true;
    }
}
