package gnext.controller.system.bean;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Apr 6, 2017
 */
public class GroupMemberKeyValueBean implements Serializable  {
    @Setter @Getter
    public Integer id;
    
    @Setter @Getter
    public String name;

    public GroupMemberKeyValueBean(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
