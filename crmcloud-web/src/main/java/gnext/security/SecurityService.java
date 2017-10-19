/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.security;

import gnext.bean.Company;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.model.authority.UserModel;
import gnext.multitenancy.service.MultitenancyService;
import gnext.service.MemberService;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Check member's authority
 *
 * @author hungpham
 * @since 2016/09
 */
@ManagedBean(name = "sec")
@SessionScoped
public class SecurityService implements Serializable{
    private static final long serialVersionUID = 2004517189316740089L;
    final private Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);
    
    @EJB private MemberService memberService;
    @EJB private MultitenancyService multitenancyService;
    
    public static enum Role{
        ROLE_LOGGED_IN, 
        ROLE_GRANT_ACCESS,
        ROLE_BASIC_AUTH, 
        ROLE_MASTER
    };
    
    private List<GrantedAuthority> roles;
    private Map<String, Boolean> cache;
    
    @PostConstruct
    public void init(){
        cache =  new HashMap<>();
        roles = (List<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    }
    
    /**
     * Trường hợp là group dùng để admin vào hoặc phân member sang công ty thì không được xóa.
     * @param group
     * @return 
     */
    public boolean isAllowDeleteGroup(final Group group) {
        if(Group.TARGET_CUSTOMER.equals(group.getTarget()) || Group.TARGET_ADMIN.equals(group.getTarget())) return false;
        return true;
    }
    
    /**
     * Kiểm tra memberId có trùng với user đang đăng nhập ở công ty khác.
     * 
     * @param memberId
     * @return Boolean
     */
    public boolean isLoggedInOtherCompany(Integer memberId) {
        if(memberId == null) return false;
        try {
            UserModel userLogined = UserModel.getLogined();
            
            Member memberOnMaster = memberService.find(memberId);
            Member memberOnSlaveOrGroup = multitenancyService.findMemberOnSlaveById(userLogined.getCompanyId(), memberId);
            if(memberOnSlaveOrGroup == null) return false;
            
            if(memberOnMaster.getGroupId().intValue() != memberOnSlaveOrGroup.getGroupId().intValue()) return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
    
    /**
     * Check member is logged in 
     * 
     * @return Boolean
     */
    public boolean isLoggedIn(){
        return hasRole(Role.ROLE_LOGGED_IN.toString());
    }
    
    /**
     * Check member is master
     * 
     * @return Boolean
     */
    public boolean isMaster(){
        return hasRole(Role.ROLE_MASTER.toString());
    }
    
    public boolean isLoginedCompanyAdmin() {
        return UserModel.getLogined().getCompanyId() == Company.MASTER_COMPANY_ID;
    }
    
    /**
     * Kiểm tra có quyền ROLE_MASTER và member-id = 1;
     * @param memberId
     * @return 
     */
    public boolean isSuperAdmin(int memberId) {
        return isMaster() && checkMemberIdIsSuperAdmin(memberId);
    }
    
    public static boolean checkMemberIdIsSuperAdmin(int memberId) {
        return memberId == Member.SUPER_ADMIN_MEMBER_ID;
    }
    
    /**
     * Check member has role
     * 
     * @param role
     * @return Boolean
     */
    public boolean hasRole(String role){
        if(roles == null) return false;
        if(cache.containsKey(role)) return cache.get(role);
        Boolean grant = roles.stream().anyMatch( (g) -> ( g.getAuthority().equals(role) || g.getAuthority().equals(Role.ROLE_MASTER.toString()) ) );
        cache.put(role, grant);
        return grant;
    }
    
    /**
     * Check member has role as regular expression
     * 
     * @param regex
     * @return Boolean
     */
    public boolean hasRoleRegex(String regex){
        if(cache.containsKey(regex)) return cache.get(regex);
        Boolean grant = roles.stream().anyMatch( (g) -> ( g.getAuthority().startsWith(regex) || g.getAuthority().equals(Role.ROLE_MASTER.toString()) ) );
        cache.put(regex, grant);
        return grant;
    }
    
    /**
     * Check member has role with module
     * 
     * @param module
     * @return Boolean
     */
    public boolean hasModule(String module){
        if(roles == null) return false;
        String regex = String.format("ROLE_%s_", StringUtils.upperCase(module));
        return hasRoleRegex(regex);
    }
    
    /**
     * Check member has role with page and module
     * 
     * @param module
     * @param page
     * @return Boolean
     */
    public boolean hasPage(String module, String page){
        if(roles == null) return false;
        String regex = StringUtils.upperCase(String.format("ROLE_%s_%s_", module, page));
        return hasRoleRegex(regex);
    }
    
    /**
     * Check member has role with page
     * 
     * @param page
     * @return Boolean
     */
    public boolean hasPage(String page){
        if(roles == null) return false;
        String regex = StringUtils.upperCase(String.format("ROLE_%s_", page));
        return hasRoleRegex(regex);
    }
    
    /**
     * Check member has role with method and module
     * 
     * @param module
     * @param page
     * @param method
     * @return Boolean
     */
    public boolean hasMethod(String module, String page, String method){
        if(roles == null) return false;
        String regex = StringUtils.upperCase(String.format("ROLE_%s_%s_%s", module, page, method));
        return hasRoleRegex(regex);
    }
    
    /**
     * Check member has role with method
     * 
     * @param page
     * @param method
     * @return Boolean
     */
    public boolean hasMethod(String page, String method){
        if(roles == null) return false;
        String regex = StringUtils.upperCase(String.format("ROLE_%s_%s", page, method));
        return hasRoleRegex(regex);
    }
}
