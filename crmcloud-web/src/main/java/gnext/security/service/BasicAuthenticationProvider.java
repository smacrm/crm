/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.security.service;

import gnext.model.authority.RoleModel;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 *
 * @author hungpham
 */
@Component
public class BasicAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {
    
    final private Logger logger = LoggerFactory.getLogger(BasicAuthenticationProvider.class);
    
    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        WebAuthenticationDetails wad = null;
        String userIPAddress         = null;
        boolean isAuthenticatedByIP  = false;
        
        String username = authentication.getPrincipal().toString();
        String password = authentication.getCredentials().toString();
        boolean basicLoginSuccess = authenticationService.basicAuthCheck(username, password);
        if(basicLoginSuccess){
            wad = (WebAuthenticationDetails) authentication.getDetails();
            userIPAddress = wad.getRemoteAddress();
            isAuthenticatedByIP = authenticationService.isGlobalIpPass(userIPAddress, username);
            logger.debug("AUTH WITH IP: " + userIPAddress);
            // Authenticated, the user's IP address matches one in the database
            if (!isAuthenticatedByIP){
                return new UsernamePasswordAuthenticationToken(username, password, Arrays.asList(new RoleModel("ROLE_RESTRICT")));
                // throw new LockedException("IP has ben restricted!");
            }
            logger.debug("[SEC] Basic Auth success !");
        }else{
            logger.debug("[SEC] Basic Auth failed with username "+username+"!");
            throw new BadCredentialsException(null);
        }
        
        //Forward to 2nd checking action
        return new UsernamePasswordAuthenticationToken(username, password, Arrays.asList(new RoleModel("ROLE_BASIC_AUTH")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
