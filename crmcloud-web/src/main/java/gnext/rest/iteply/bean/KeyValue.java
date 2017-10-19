package gnext.rest.iteply.bean;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Dec 29, 2016
 */
public class KeyValue {
    @Getter @Setter private String Key;
    @Getter @Setter private String Value;

    public KeyValue(String Key, String Value) {
        this.Key = Key;
        this.Value = Value;
    }
    
}
