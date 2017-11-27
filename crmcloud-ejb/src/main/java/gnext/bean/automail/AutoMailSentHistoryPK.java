package gnext.bean.automail;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Aug 29, 2017
 */
@Embeddable
public class AutoMailSentHistoryPK implements Serializable {

    private static final long serialVersionUID = 6799557884807803511L;

    @Basic(optional = false)
    @NotNull
    @Column(name = "auto_id")
    @Getter @Setter
    private int autoId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "issue_id")
    @Getter @Setter
    private int issueId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "sent_date")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date sentDate;

    public AutoMailSentHistoryPK() {
    }

    public AutoMailSentHistoryPK(int autoId, int issueId, Date sentDate) {
        this.autoId = autoId;
        this.issueId = issueId;
        this.sentDate = sentDate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) autoId;
        hash += (int) issueId;
        hash += (sentDate != null ? sentDate.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoMailSentHistoryPK)) {
            return false;
        }
        AutoMailSentHistoryPK other = (AutoMailSentHistoryPK) object;
        if (this.autoId != other.autoId) {
            return false;
        }
        if (this.issueId != other.issueId) {
            return false;
        }
        if ((this.sentDate == null && other.sentDate != null) || (this.sentDate != null && !this.sentDate.equals(other.sentDate))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.automail.AutoMailSentHistoryPK[ autoId=" + autoId + ", issueId=" + issueId + ", sentDate="+sentDate+" ]";
    }

}
