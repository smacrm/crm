/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.mail;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class ReadEmail extends Email {
    private static final long serialVersionUID = 7566177796973635147L;
    
    @Getter @Setter private String prior;
    
    public ReadEmail() { this(null); }
    public ReadEmail(String prior) { this.prior = prior; }
}
