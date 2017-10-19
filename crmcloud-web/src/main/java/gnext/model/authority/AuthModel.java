/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.authority;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
public class AuthModel implements Serializable {

    @Setter
    @Getter
    protected int id;

    @Setter
    @Getter
    protected String name;

    @Setter
    @Getter
    protected boolean selected = false;

    @Setter
    @Getter
    protected String source;
}
