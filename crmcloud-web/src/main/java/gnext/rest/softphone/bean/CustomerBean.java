package gnext.rest.softphone.bean;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Feb 23, 2017
 */
public class CustomerBean implements Serializable{
    @Getter @Setter private String name;
    @Getter @Setter private String phone;
    @Getter @Setter private Date lastCall;

    public CustomerBean() {
    }

    public CustomerBean(String name, String phone, Date lastCall) {
        this.name = name;
        this.phone = phone;
        this.lastCall = lastCall;
    }
    
}
