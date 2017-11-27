package gnext.bean.automail;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
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
@Entity
@Table(name = "crm_auto_mail_sent_history")
@NamedQueries({
    @NamedQuery(name = "AutoMailSentHistory.findAll", query = "SELECT a FROM AutoMailSentHistory a")})
public class AutoMailSentHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    @Getter @Setter
    protected AutoMailSentHistoryPK autoMailSentHistoryPK;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "history_data")
    @Getter @Setter
    private String historyData;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id")
    @Getter @Setter
    private int companyId;

    public AutoMailSentHistory() {
    }

    public AutoMailSentHistory(AutoMailSentHistoryPK autoMailSentHistoryPK) {
        this.autoMailSentHistoryPK = autoMailSentHistoryPK;
    }

    public AutoMailSentHistory(AutoMailSentHistoryPK autoMailSentHistoryPK, int companyId) {
        this.autoMailSentHistoryPK = autoMailSentHistoryPK;
        this.companyId = companyId;
    }

    public AutoMailSentHistory(int autoId, int issueId, Date sentDate) {
        this.autoMailSentHistoryPK = new AutoMailSentHistoryPK(autoId, issueId, sentDate);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (autoMailSentHistoryPK != null ? autoMailSentHistoryPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoMailSentHistory)) {
            return false;
        }
        AutoMailSentHistory other = (AutoMailSentHistory) object;
        if ((this.autoMailSentHistoryPK == null && other.autoMailSentHistoryPK != null) || (this.autoMailSentHistoryPK != null && !this.autoMailSentHistoryPK.equals(other.autoMailSentHistoryPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.automail.AutoMailSentHistory[ autoMailSentHistoryPK=" + autoMailSentHistoryPK + " ]";
    }

}
