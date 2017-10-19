package gnext.bean.automail;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author hungpham
 * @since Aug 24, 2017
 */
@Embeddable
public class AutoMailMemberPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "auto_id")
    private Integer autoId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "member_id")
    private int memberId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 4)
    @Column(name = "type")
    private String type; // to, cc

    public AutoMailMemberPK() {
    }

    public AutoMailMemberPK(Integer autoId, int memberId, String type) {
        this.autoId = autoId;
        this.memberId = memberId;
        this.type = type;
    }

    public int getAutoId() {
        return autoId;
    }

    public void setAutoId(int autoId) {
        this.autoId = autoId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) autoId;
        hash += (int) memberId;
        hash += (type != null ? type.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoMailMemberPK)) {
            return false;
        }
        AutoMailMemberPK other = (AutoMailMemberPK) object;
        if (this.autoId != other.autoId) {
            return false;
        }
        if (this.memberId != other.memberId) {
            return false;
        }
        if ((this.type == null && other.type != null) || (this.type != null && !this.type.equals(other.type))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.automail.AutoMailMemberPK[ autoId=" + autoId + ", memberId=" + memberId + ", type=" + type + " ]";
    }

}
