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
public class DeletedMail extends ReadEmail {
    private static final long serialVersionUID = -7869258310311883044L;
    @Getter @Setter private String messageId;
}
