package gnext.rest.common.bean;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Mar 17, 2017
 */
public class Issue implements Serializable{

    private static final long serialVersionUID = 7702275076964889767L;
    
    @Getter @Setter private Integer id;
    @Getter @Setter private String code;
    @Getter @Setter private Date receiveDate;
    @Getter @Setter private String updatedPersonName;
    @Getter @Setter private String status;
    @Getter @Setter private Date closedDate;
    
    public Issue(gnext.bean.issue.Issue data){
        this.id = data.getIssueId();
        this.code = data.getIssueViewCode();
        this.receiveDate = data.getIssueReceiveDate();
        this.updatedPersonName = data.getUpdatedId() != null ? data.getUpdatedId().getMemberNameFull() : (data.getCreatorId() != null ? data.getCreatorId().getMemberNameFull() : "");
        this.status = data.getIssueStatusId() != null ? data.getIssueStatusId().getItemViewData("jp") : "";
        this.closedDate = data.getIssueClosedDate();
    }
}
