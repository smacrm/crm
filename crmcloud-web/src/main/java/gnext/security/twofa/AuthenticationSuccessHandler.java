package gnext.security.twofa;

import gnext.controller.system.ForgetpassController;
import gnext.dbutils.util.StringUtil;
import gnext.model.authority.UserModel;
import gnext.security.SecurityConfig;
import gnext.security.service.AuthenticationProvider;
import gnext.service.config.ConfigService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;

/**
 * Process after login to system
 * Redirect to 2FA or not #determineTargetUrl
 *
 * @author hungpham
 * @since Dec 15, 2016
 */
public class AuthenticationSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler{
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    private final int MAX_TIME_INTERACTIVE = 24;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserModel user = UserModel.getLogined();
        String targetUrl = determineTargetUrl(user, request);
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }
    
    public long getHoursFromLastInteractive(UserModel user){
        Date lastLogin = user.getMember().getLastLoginTime();
        Date lastLogout = user.getMember().getLastLoginTime();
        Date interactiveTime = lastLogin;
        
        if(lastLogout != null && lastLogout.after(lastLogin)) interactiveTime = lastLogout;
        if(interactiveTime != null){
            long secs = ((new Date()).getTime() - interactiveTime.getTime()) / 1000;
            long hours = secs / 3600; 
            return hours;
        }
        
        return MAX_TIME_INTERACTIVE + 1;
    }
    
    protected String determineTargetUrl(UserModel user, HttpServletRequest request) {
        ConfigService configService = getConfigService();
        int overDayPassMember = 1; //default 1 day.
        if(configService != null) {
            String overDayPassMemberFromConf = configService.get("OVER_DAY_PASS_MEMBER");
            if(!StringUtil.isEmpty(overDayPassMemberFromConf)) overDayPassMember = Integer.parseInt(overDayPassMemberFromConf);
        }
        
        if (user.getMember().getMemberResetPwdDatetime() != null && DateUtil.timeToResetPasswordDate(overDayPassMember, user.getMember().getMemberResetPwdDatetime()) > 0) {
            ForgetpassController forgetpassController = JsfUtil.getManagedBean("forgetpassController", ForgetpassController.class);
            if(forgetpassController != null) forgetpassController.setUsername(user.getUsername().replaceAll("GN[0-9]{3}", ""));
            return "/resetpassword.xhtml";
        } else if(user.isUsing2FA() && !StringUtils.isEmpty(user.getSecret()) && getHoursFromLastInteractive(user) > MAX_TIME_INTERACTIVE ) { // //active time in 24 hours
            return "/twofactor.xhtml";
        }
        
        String redirectCode = request.getParameter("r");
        return SecurityConfig.PAGE_GRANT_URL + (StringUtils.isEmpty(redirectCode) ? "" : "?r=" + redirectCode);
    }
    
    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
    
    private ConfigService getConfigService() {
        if (null != AuthenticationProvider.configServiceThreadLocal.get())
            return AuthenticationProvider.configServiceThreadLocal.get();
        return null;
    }
}
