package gnext.rest.twilio.bean;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Mar 6, 2017
 */
public class WaitingCall implements Serializable{
    @Getter @Setter private Boolean privateCall = false;
    @Getter @Setter private Integer size = 0;
    @Getter @Setter private String name;
    @Getter @Setter private String phoneNumber;
    @Getter @Setter private String callsid;

    public WaitingCall() {
    }
    
}
