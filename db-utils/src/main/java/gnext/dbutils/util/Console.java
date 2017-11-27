/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public final class Console {
    private static final Logger LOGGER = LoggerFactory.getLogger(Console.class);
    public static boolean showConsole = true;
    private Console() {}
    
    public static void success() {
        if((!showConsole)) return;
        System.out.println("success");
    }
    
    public static void error(String message) {
        if((!showConsole)) return;
        System.out.println(message);
    }
    
    public static void error(Exception e) {
        if((!showConsole)) return;
        e.printStackTrace();
    }
    
    public static void log(String message) {
        if((!showConsole)) return;
        System.out.println(message);
    }
}