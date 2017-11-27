package gnext.model.system.maintenance;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Nov 7, 2016
 */
public class Row implements Serializable{
    
    @Getter @Setter
    private int id;
    
    @Getter @Setter
    private String label;

    public Row(int id, String label) {
        this.id = id;
        this.label = label;
    }
    
}
