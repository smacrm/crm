/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util;

import gnext.mailapi.authentication.SMTPAuthentication;
import java.util.Properties;
import javax.mail.Session;

/**
 *
 * @author daind
 */
public class SessionUtil {
    public static Session getSession(Properties conf, boolean auth, String u, String p, Boolean debug) {
        Session session = null;
        if (!auth)
            session = Session.getInstance(conf);
        else
            session = Session.getInstance(conf, new SMTPAuthentication(u, p));
        
        if(debug != null) session.setDebug(debug);
        return session;
    }
}