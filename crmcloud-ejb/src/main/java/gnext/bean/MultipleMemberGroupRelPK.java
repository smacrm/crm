package gnext.bean;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author hungpham
 */
@Embeddable
public class MultipleMemberGroupRelPK implements Serializable {

    private static final long serialVersionUID = 7545118476870305442L;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "member_id", nullable = false)
    private int memberId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "group_id", nullable = false)
    private int groupId;

    public MultipleMemberGroupRelPK() {
    }

    public MultipleMemberGroupRelPK(int memberId, int groupId) {
        this.memberId = memberId;
        this.groupId = groupId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) memberId;
        hash += (int) groupId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MultipleMemberGroupRelPK)) {
            return false;
        }
        MultipleMemberGroupRelPK other = (MultipleMemberGroupRelPK) object;
        if (this.memberId != other.memberId) {
            return false;
        }
        if (this.groupId != other.groupId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.CrmMultipleMemberGroupRelPK[ memberId=" + memberId + ", groupId=" + groupId + " ]";
    }
    
}
