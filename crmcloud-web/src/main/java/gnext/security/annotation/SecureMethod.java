/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.security.annotation;

import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 *
 * @author hungpham
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface SecureMethod {
    public enum Method {
        INDEX
        , VIEW
        , SEARCH
        , CREATE
        , UPDATE
        , DELETE
        , UPLOAD
        , DOWNLOAD
        , PRINT
        , COPY
        , NONE
        , RELATED
        , SUPPORTMAIL
        , REQUESTMAIL
        , DELETEFILE
        , CUSTOMER_HISTORY
        , CUSTOMER_SPECIAL
    }
    
    /**
     * Secure method name
     * 
     * @return String
     */
    public Method value(); //Action name code
    
    /**
     * Is require for checking security
     * 
     * @return Boolean
     */
    public boolean require() default true; // require to checking by security
}
