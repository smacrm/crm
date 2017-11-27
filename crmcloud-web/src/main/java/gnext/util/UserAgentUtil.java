/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

/**
 *
 * @author gnextadmin
 */
public class UserAgentUtil {
    private static final String[] USER_AGENT = {"Chrome/"};
    private static final String[] SPEECH_SUPORT_LOCALE = {"ja","en"};

    /** Speech APIがサポートプラウザを判断
     * @param userAgent
     * @return 「true、false」 */
    public static boolean getUserAgentSpeechSuport(String userAgent) {
        if(userAgent == null || userAgent.isEmpty() ) return false;
        for(String u:USER_AGENT) {
            if(userAgent.indexOf(u)>0) return true;
        }
        return false;
    }

    /** Speech APIがサポート言語を判断
     * @param locale
     * @return 「true、false」 */
    public static boolean getSpeechSuportLocale(String locale) {
        if(locale == null || locale.isEmpty() ) return false;
        for(String u:SPEECH_SUPORT_LOCALE) {
            if(locale.equals(u)) return true;
        }
        return false;
    }

}
