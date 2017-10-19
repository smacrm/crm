/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.security.annotation;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 *
 * @author hungpham
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface SecurePage {
    public enum Module {
        COMPANY,
        ISSUE,
        MAIL,
        MESSAGE,
        AUTHENTICATION,
        CUSTOMIZE,
        SPEECHAPI,
        SYSTEM,
        REPORT,
        NONE
    }

    /**
     * Secure module belong to
     * 
     * @return String
     */
    public Module module(); //Module name
    
    /**
     * Secure page name
     * 
     * @return String
     */
    public String value() default ""; //Page name code
    
    /**
     * Is require for checking security
     * 
     * @return Boolean
     */
    public boolean require() default true; // require to checking by security
}
