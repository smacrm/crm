/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.security.service;

import gnext.bean.DatabaseServer;
import gnext.dbutils.util.StringUtil;
import gnext.model.authority.UserModel;
import gnext.service.config.ConfigService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.utils.EncoderUtil;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 *
 * @author hungpham
 */
@EJB(name = "configService", beanInterface = ConfigService.class)
@Component
public class AuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {
    final private Logger logger = LoggerFactory.getLogger(AuthenticationProvider.class);
    private static final String CONFIG_EJB_LOOKUP_PATH = "java:comp/env/configService";
    public static ThreadLocal<ConfigService> configServiceThreadLocal = new ThreadLocal<>();

    @Autowired private AuthenticationService authenticationService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        String companyBasicLoginId = SecurityContextHolder.getContext().getAuthentication().getName();

        WebAuthenticationDetails wad = (WebAuthenticationDetails) authentication.getDetails();
        @SuppressWarnings("UnusedAssignment")
        String userIPAddress = wad.getRemoteAddress();
//        userIPAddress = "180.131.116.84";//118.238.204.101";
        boolean isAuthErrorByIP = authenticationService.isGlobalIpPass(userIPAddress, companyBasicLoginId, username);
        if (!isAuthErrorByIP) {
            JsfUtil.getResource().putErrors(NumberUtils.toInt(companyBasicLoginId), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_ERROR_NAME, "label.errors.not_login_outside", false);
            throw new BadCredentialsException("Can not login from outside !!!");
        }

        // kiểm tra sự tồn tại của cơ sở dữ liệu của công ty logined.
        DatabaseServer ds = authenticationService.getDatabaseServerOfCompany(companyBasicLoginId);
        if(ds == null) {
            JsfUtil.getResource().putErrors(NumberUtils.toInt(companyBasicLoginId), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_ERROR_NAME, "label.errors.database.not_found", false);
            throw new BadCredentialsException("Database not found.");
        }
        
        if(!authenticationService.testConnectionToSlaveDb(ds)) {
            JsfUtil.getResource().putErrors(NumberUtils.toInt(companyBasicLoginId), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_ERROR_NAME, "label.errors.database.refuse", false);
            throw new BadCredentialsException("Error when try test connection to slave Database.");
        }
        
        UserModel user = authenticationService.loadUserByUsername(username+":"+companyBasicLoginId);
        if (user == null) {
            JsfUtil.getResource().putErrors(NumberUtils.toInt(companyBasicLoginId), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_ERROR_NAME, "label.errors.username", false);
            throw new BadCredentialsException("Username not found.");
        }
        
        // Kiểm tra trạng thái member đã bị delete chưa
        if(user.getMember().getMemberDeleted() == null || user.getMember().getMemberDeleted() == 1){
            JsfUtil.getResource().putErrors(NumberUtils.toInt(companyBasicLoginId), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_ERROR_NAME, "label.errors.user.deleted", false);
            throw new BadCredentialsException("User has been deleted.");
        }
        
        if (!EncoderUtil.getPassEncoder().matches(password, user.getPassword())) {
            JsfUtil.getResource().putErrors(NumberUtils.toInt(companyBasicLoginId), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_ERROR_NAME, "label.errors.password", false);
            throw new BadCredentialsException("Wrong password.");
        }
        
        ConfigService configService = lookupConfigEJBService();
        int overDayPassMember = 1; //default 1 day.
        String overDayPassMemberFromConf = configService.get("OVER_DAY_PASS_MEMBER");
        if(!StringUtil.isEmpty(overDayPassMemberFromConf)) overDayPassMember = Integer.parseInt(overDayPassMemberFromConf);
        if(user.getMember().getMemberResetPwdDatetime()!=null && DateUtil.timeToResetPasswordDate(overDayPassMember, user.getMember().getMemberResetPwdDatetime()) < 0) {
            JsfUtil.getResource().putErrors(NumberUtils.toInt(companyBasicLoginId), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_ERROR_NAME, "label.errors.resetexpired", false);
            throw new BadCredentialsException("Please generate password again. Because valuable time is over!!.");
        }
        
        //Forward to 2nd checking action
        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }
    
    private ConfigService lookupConfigEJBService() {
        if (null != configServiceThreadLocal.get()) return configServiceThreadLocal.get();
        try {
            InitialContext initialContext = new InitialContext();
            ConfigService configService = (ConfigService) initialContext.lookup(CONFIG_EJB_LOOKUP_PATH);
            configServiceThreadLocal.set(configService);
            return configService;
        } catch (NamingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
