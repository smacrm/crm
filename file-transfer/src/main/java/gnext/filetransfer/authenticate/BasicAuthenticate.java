/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer.authenticate;

import lombok.Getter;

/**
 *
 * @author daind
 */
public class BasicAuthenticate {
    @Getter private final String username;
    @Getter private final String password;
    public BasicAuthenticate(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
