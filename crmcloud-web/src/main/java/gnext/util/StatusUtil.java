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
public interface StatusUtil {
    public static final short DELETED = 1;
    public static final short UN_DELETED = 0;

    public static final boolean EXISTS = false;
    public static final boolean NOT_EXISTS = true;

    public static boolean getBoolean(Short flag) {
        if (flag != null && flag == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean getBoolean(String flag) {
        if ("true".equalsIgnoreCase(flag)) {
            return true;
        } else {
            return false;
        }
    }

    public static Short getShort(Boolean flag) {
        if (flag) {
            return (short) 1;
        } else {
            return (short) 0;
        }
    }

    public static String getString(Boolean flag) {
        if (flag) {
            return "true";
        } else {
            return "false";
        }
    }

}
