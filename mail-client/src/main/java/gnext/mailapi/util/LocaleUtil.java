/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author daind
 */
public final class LocaleUtil {
    private String lang;
    private ResourceBundle labels;
    
    public LocaleUtil() {
        this("en");
    }
    
    public LocaleUtil(String lang) {
        if(lang == null || lang.isEmpty()) return;
        this.lang = lang;
        this.labels = ResourceBundle.getBundle("messages.mail", new Locale(lang));
    }
    
    public String get(String key, String... arguments) {
        if(labels == null) return key;
        if(!labels.containsKey(key)) return "!!" + key + "!!";
        return MessageFormat.format(labels.getString(key), arguments);
    }
    
    public static void main(String args[]) {
        LocaleUtil locale = new LocaleUtil("ja");
        System.out.println("gnext.mailapi.util.LocaleUtil.main()" + locale.get("mail.alert.support.subject"));
    }
}
