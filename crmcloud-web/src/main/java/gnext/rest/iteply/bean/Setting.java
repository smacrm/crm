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
public class Setting implements Serializable{
    @Getter @Setter private int UDPMessagePort;
    @Getter @Setter private List<ServiceNumber> ServiceNumberList = new ArrayList<>();
    @Getter @Setter private List<KeyValue> TelePhoneDirectory = new ArrayList<>();

    public Setting() {
    }
    
    public Setting(int UDPMessagePort) {
        this.UDPMessagePort = UDPMessagePort;
    }

    public Setting(int UDPMessagePort, List<ServiceNumber> ServiceNumberList) {
        this.UDPMessagePort = UDPMessagePort;
        this.ServiceNumberList = ServiceNumberList;
    }
    
}
