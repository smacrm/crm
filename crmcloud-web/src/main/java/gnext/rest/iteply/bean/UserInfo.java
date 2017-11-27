package gnext.rest.iteply.bean;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Dec 26, 2016
 */
public class UserInfo implements Serializable{
    @Getter @Setter private String FirstName;
    @Getter @Setter private String FamilyName;
}
