package gnext.utils;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Nov 1, 2016
 */
public class FieldOption implements Serializable{
    @Setter @Getter
    private String label;
    
    @Setter @Getter
    private String value;
    
    @Setter @Getter
    private boolean selected;

    public FieldOption(String label, String value, boolean selected) {
        this.label = label;
        this.value = value;
        this.selected = selected;
    }
}
