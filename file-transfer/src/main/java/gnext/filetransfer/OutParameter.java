/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer;

import gnext.dbutils.model.Attachment;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class OutParameter {
    /** Trả về id của attachment trong bảng crm_attachment. */
    @Getter @Setter private Attachment attachment;
}
