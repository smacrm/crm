package gnext.security;

import static gnext.security.SecurityService.Role.ROLE_GRANT_ACCESS;
import static gnext.security.SecurityService.Role.ROLE_LOGGED_IN;
import gnext.security.service.AuthenticationProvider;
import gnext.security.service.BasicAuthenticationProvider;
import gnext.security.twofa.AuthenticationSuccessHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Spring security configuration
 * 
 * @author hungpham
 * @since 2016/10
 */
@EnableWebSecurity
@ComponentScan("gnext.security")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    public static final String PAGE_INDEX_URL = "/index.xhtml";
    public static final String PAGE_IP_DENIED_URL = "/errors/index_403_IPRestricted.xhtml";
    public static final String PAGE_GRANT_URL = "/grant.xhtml";
    public static final String PAGE_LOGIN_URL = "/login.xhtml";
    private static final String PAGE_REST_URL = "/rest/**";
    private static final String PAGE_ITELPY_URL = "/rest/itelpy/**";
    private static final String PAGE_TWILIO_URL = "/rest/twilio/**";
    private static final String PAGE_ISSUE_VIEW = "/issue/**";
    public static final String PAGE_500_URL = "/errors/index_500.xhtml";
    public static final String PAGE_404_URL = "/errors/index_404.xhtml";
    public static final String PAGE_403_URL = "/errors/index_403.xhtml";
    
    public static final String PAGE_LOGIN_PROCESS = "/appLogin";
    public static final String PAGE_LOGOUT_PROCESS = "/appLogout";
    
    private static final String RESOURCE_MATCHER_PATH = "/*resource/**";
    
    @Configuration
    @Order(1)
    public static class BasicSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Autowired
        private BasicAuthenticationProvider basicAuthenticationProvider;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable()
                .antMatcher(PAGE_LOGIN_URL)
                .authorizeRequests()
                    .antMatchers(PAGE_REST_URL).permitAll()
                    .anyRequest().hasRole("BASIC_AUTH")
                .and().httpBasic()
                .and().exceptionHandling().accessDeniedPage(PAGE_IP_DENIED_URL);
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(basicAuthenticationProvider);
        }
        
        @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }
    }
    
    @Configuration
    @Order(2)
    public static class FormWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private AuthenticationProvider formAuthenticationProvider;
        
        @Autowired
        private gnext.security.twofa.CustomWebAuthenticationDetailsSource authenticationDetailsSource;
        
        //https://github.com/molgenis/molgenis
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable();

            // do not write cache control headers for static resources 
            RequestMatcher matcher = new NegatedRequestMatcher(new OrRequestMatcher(new AntPathRequestMatcher(RESOURCE_MATCHER_PATH)));

            DelegatingRequestMatcherHeaderWriter cacheControlHeaderWriter = new DelegatingRequestMatcherHeaderWriter(matcher, new CacheControlHeadersWriter());

            // add default header options but use custom cache control header writer 
            http.headers().frameOptions().sameOrigin().
                    contentTypeOptions().
                    and().xssProtection().
                    and().httpStrictTransportSecurity().
                    and().frameOptions().
                    and().addHeaderWriter(cacheControlHeaderWriter);

            http.formLogin(). //login configuration
                    loginPage(PAGE_LOGIN_URL).
                    loginProcessingUrl(PAGE_LOGIN_PROCESS).
                    usernameParameter("j_username").
                    passwordParameter("j_password").
                    successHandler(new AuthenticationSuccessHandler()).
                and().
                logout(). //logout configuration
                    logoutUrl(PAGE_LOGOUT_PROCESS).
                    logoutSuccessHandler(new SimpleUrlLogoutSuccessHandler(){
                        @Override
                        public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                            StringBuilder targetUrl = new StringBuilder(PAGE_LOGIN_URL);
                            List<String> params = new ArrayList<>();
                            request.getParameterMap().forEach((key, values) -> {
                                if(values == null || values.length == 0 || (values.length == 1 && StringUtils.isEmpty(values[0]))){
                                    params.add(key);
                                }else{
                                    for(String value : values){
                                        params.add(String.format("%s=%s", key, value));
                                    }
                                }
                            });
                            if(!params.isEmpty()){
                                targetUrl.append("?").append(StringUtils.join(params, "&"));
                            }
                            setDefaultTargetUrl(targetUrl.toString());
                            super.onLogoutSuccess(request, response, authentication); //To change body of generated methods, choose Tools | Templates.
                        }
                    }).
                    invalidateHttpSession(true).
                    deleteCookies("JSESSIONID", "twilio_allow_incomming", "twilio_is_incomming", "twilio_is_calling" //remove twilio cookie
                    ).and().rememberMe().tokenValiditySeconds(1209600).rememberMeParameter("j_rememberme");
            
            http.formLogin(). //2FA
                    authenticationDetailsSource(authenticationDetailsSource).
                    failureHandler(new SimpleUrlAuthenticationFailureHandler() {
                        @Override
                        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                            String redirectCode = request.getParameter("r");
                            super.setDefaultFailureUrl(request.getContextPath() + PAGE_LOGIN_URL);
                            if(!StringUtils.isEmpty(redirectCode)){
                                super.setDefaultFailureUrl(request.getContextPath() + PAGE_LOGIN_URL + "?r=" + redirectCode);
                            }
                            super.onAuthenticationFailure(request, response, exception);
                        }
                    });

            http.authorizeRequests().
                    antMatchers(PAGE_LOGIN_URL, PAGE_ITELPY_URL, PAGE_TWILIO_URL, PAGE_ISSUE_VIEW).permitAll(). //allow URLs for anyone
                    antMatchers(PAGE_GRANT_URL).access("hasRole('"+ROLE_LOGGED_IN+"')").
                    antMatchers(PAGE_INDEX_URL).access("hasRole('"+ROLE_LOGGED_IN+"') and hasRole('"+ROLE_GRANT_ACCESS+"')").
                    anyRequest().authenticated();

            http.exceptionHandling()
                .accessDeniedHandler(new AccessDeniedHandler() {
                    private static final String FACES_REQUEST_HEADER = "faces-request";
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ade) throws IOException, ServletException {
                        boolean ajaxRedirect = "partial/ajax".equals(request.getHeader(FACES_REQUEST_HEADER));
                        if(ajaxRedirect) {
                            String contextPath = request.getContextPath();
                            String redirectUrl = contextPath + PAGE_LOGIN_URL;
                            logger.info("Session expired due to ajax request, redirecting to '"+redirectUrl+"'");
                            String ajaxRedirectXml = createAjaxRedirectXml(redirectUrl);
                            response.setContentType("text/xml");
                            response.getWriter().write(ajaxRedirectXml);
                        } else {
                            request.getSession(true);
                            response.sendRedirect(request.getContextPath() + PAGE_LOGIN_URL);
                        }
                    }

                    private String createAjaxRedirectXml(String redirectUrl) {
                        return new StringBuilder()
                                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                                .append("<partial-response><redirect url=\"")
                                .append(redirectUrl)
                                .append("\"></redirect></partial-response>")
                                .toString();
                    }
                })
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException, ServletException {
                        request.getSession(true);
                        response.sendRedirect(request.getContextPath() + PAGE_LOGIN_URL);
                    }
                });
        }
        
        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers(RESOURCE_MATCHER_PATH);
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(formAuthenticationProvider);
        }
        
        @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }
    }
}
