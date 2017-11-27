/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.authority;

import lombok.Getter;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author hungpham
 */
public class RoleModel implements GrantedAuthority {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String authority;

    public RoleModel(String name) {
        this.name = name;
        this.authority = name;
    }

    public RoleModel(String name, String authority) {
        this.name = name;
        this.authority = authority;
    }
}
