/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.authentication;

import javax.mail.PasswordAuthentication;
import lombok.Getter;

/**
 *
 * @author daind
 */
public class SMTPAuthentication extends javax.mail.Authenticator {

    @Getter private final String username;
    @Getter private final String password;

    public SMTPAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
}
