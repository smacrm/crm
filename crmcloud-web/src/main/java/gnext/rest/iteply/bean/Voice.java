package gnext.rest.iteply.bean;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Dec 26, 2016
 */
public class Voice implements Serializable{
    @Getter @Setter private String password;
    @Getter @Setter private String url;
    @Getter @Setter private String uniqueId;

    public Voice() {
    }
    
}
