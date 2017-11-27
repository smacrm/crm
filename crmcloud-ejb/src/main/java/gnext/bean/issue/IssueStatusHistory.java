package gnext.bean.issue;

import gnext.bean.Member;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author gnextadmin
 */
@Entity
@Table(name = "crm_issue_status_history")
@XmlRootElement
@NamedQueries({})
public class IssueStatusHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "history_id")
    private Integer historyId;
    
    @Getter @Setter
    @JoinColumn(name = "issue_id", referencedColumnName = "issue_id", nullable = false)
    @ManyToOne
    private Issue issueId;

    @Getter @Setter
    @NotNull
    @Column(name = "from_status_id")
    private Integer fromStatusId;
    
    @Getter @Setter
    @NotNull
    @Column(name = "to_status_id")
    private Integer toStatusId;
    
    @Getter @Setter
    @JoinColumn(name = "creator_id", referencedColumnName = "member_id")
    @ManyToOne(optional = false)
    private Member creatorId;
    
    @Getter @Setter
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime = new Date();

    public IssueStatusHistory() {
    }
    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (historyId != null ? historyId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof IssueStatusHistory)) {
            return false;
        }
        IssueStatusHistory other = (IssueStatusHistory) object;
        return !((this.historyId == null && other.historyId != null) || (this.historyId != null && !this.historyId.equals(other.historyId)));
    }

    @Override
    public String toString() {
        return "gnext.issue.CrmIssueStatusHistory[ historyId=" + historyId + " ]";
    }

}
