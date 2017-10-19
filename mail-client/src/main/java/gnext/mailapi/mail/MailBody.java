/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.mail;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public class MailBody implements Serializable {
    private static final long serialVersionUID = 704640281863791182L;
    @Getter @Setter private String text;
    @Getter @Setter private String html;
    
    public String getContent() {
        if(!StringUtils.isEmpty(this.html)) return this.html;
        return this.text;
    }
}
