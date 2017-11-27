package gnext.rest.iteply.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Dec 26, 2016
 */
public class ServiceNumber implements Serializable{
    @Getter @Setter private String Name;
    @Getter @Setter private List<KeyValue> TelNumbers = new ArrayList<>();

    public ServiceNumber() {
    }
    
    public ServiceNumber(String Name) {
        this.Name = Name;
    }

    public ServiceNumber(String Name, List<KeyValue> TelNumbers) {
        this.Name = Name;
        this.TelNumbers = TelNumbers;
    }
    
    
}
