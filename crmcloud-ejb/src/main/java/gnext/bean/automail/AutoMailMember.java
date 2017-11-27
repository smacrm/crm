package gnext.bean.automail;

import gnext.bean.Member;
import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Aug 24, 2017
 */
@Entity
@Table(name = "crm_auto_mail_member")
@NamedQueries({
    @NamedQuery(name = "AutoMailMember.findAll", query = "SELECT a FROM AutoMailMember a")})
public class AutoMailMember implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    @Setter @Getter
    protected AutoMailMemberPK autoMailMemberPK = new AutoMailMemberPK();
    
    @JoinColumn(name = "auto_id", referencedColumnName = "auto_config_id", insertable = false, updatable = false)
    @Setter @Getter
    @ManyToOne(optional = false)
    private AutoMail autoMail;
    
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", insertable = false, updatable = false)
    @OneToOne(optional = false)
    @Setter @Getter
    private Member member;

    public AutoMailMember() {
    }

    public AutoMailMember(AutoMailMemberPK autoMailMemberPK) {
        this.autoMailMemberPK = autoMailMemberPK;
    }

    public AutoMailMember(Integer autoId, int memberId, String type) {
        this.autoMailMemberPK = new AutoMailMemberPK(autoId, memberId, type);
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (autoMailMemberPK != null ? autoMailMemberPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoMailMember)) {
            return false;
        }
        AutoMailMember other = (AutoMailMember) object;
        if ((this.autoMailMemberPK == null && other.autoMailMemberPK != null) || (this.autoMailMemberPK != null && !this.autoMailMemberPK.equals(other.autoMailMemberPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.automail.AutoMailMember[ autoMailMemberPK=" + autoMailMemberPK + " ]";
    }

}
