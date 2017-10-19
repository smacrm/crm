package gnext.rest.customize.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Nov 1, 2016
 */
public class KeyValueBean implements Serializable{

    private static final long serialVersionUID = 2619409100676728055L;
    
    @Setter @Getter
    public int value;
    
    @Setter @Getter
    public String label;
    
    /**
     * Type 1: Group
     * Type 2: Member
     * Type 3: Mente
     */
    @Setter @Getter
    public Integer type;
    
    @Setter @Getter
    public List<KeyValueBean> items = new ArrayList<>();

    public KeyValueBean(int type, int value, String label) {
        this.type = type;
        this.value = value;
        this.label = label;
    }
    
}
