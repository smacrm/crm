package gnext.model;

import gnext.bean.mente.MenteItem;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since May 16, 2017
 */
public class MenteSelectItem extends SelectItem{
    private static final long serialVersionUID = 197030100351082708L;
    
    @Getter @Setter
    private MenteItem source;
}
