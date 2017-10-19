/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

/**
 *
 * @author daind
 */
public class ExceptionUtils {
    /**
     * 
     * @param cause
     * @return 
     */
    public static Throwable getRootCause(Throwable cause) {
        if (cause != null) {
            Throwable source = cause.getCause();
            if (source != null) {
                return getRootCause(source);
            } else {
                return cause;
            }
        }
        return null;
    }
    
    /**
     * Hàm xử lí kiểm tra exception.
     * @param e
     * @param klass
     * @return 
     */
    public static boolean checkIsThrowable(Exception e, Class<?> klass) {
        Throwable throwable = ExceptionUtils.getRootCause(e);
        if (throwable.getClass().isAssignableFrom(klass)) return true;
        
//        StringBuilder message_error = new StringBuilder(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
//        for(String sz : org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseStackTrace(e))
//            message_error.append(sz);
//        
//        if(message_error.indexOf(klass.getName())>=0) return true;
        
        return false;
    }
    
}
