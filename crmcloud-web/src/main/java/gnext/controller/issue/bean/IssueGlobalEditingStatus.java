package gnext.controller.issue.bean;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Jul 6, 2017
 */
public class IssueGlobalEditingStatus implements Serializable{
    
    private static final long serialVersionUID = -424189700650759702L;
    
    @Getter @Setter private Integer memberId;
    @Getter @Setter private String memberFullName;
    @Getter final private Date timestamp = new Date();

    public IssueGlobalEditingStatus(Integer memberId, String memberFullName) {
        this.memberId = memberId;
        this.memberFullName = memberFullName;
    }
    
    
}
