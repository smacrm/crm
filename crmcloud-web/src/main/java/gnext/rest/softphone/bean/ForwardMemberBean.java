package gnext.rest.softphone.bean;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Feb 23, 2017
 */
public class ForwardMemberBean implements Serializable{
    @Getter @Setter private String name;
    @Getter @Setter private boolean online;
    @Getter @Setter private String account;

    public ForwardMemberBean() {
    }

}
