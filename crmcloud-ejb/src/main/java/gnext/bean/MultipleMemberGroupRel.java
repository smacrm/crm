/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_multiple_member_group_rel")
@XmlRootElement
public class MultipleMemberGroupRel implements CloneSelfDataToDbChild {
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    protected MultipleMemberGroupRelPK crmMultipleMemberGroupRelPK;
    
    public MultipleMemberGroupRel() { }

    public MultipleMemberGroupRel(MultipleMemberGroupRelPK crmMultipleMemberGroupRelPK) {
        this.crmMultipleMemberGroupRelPK = crmMultipleMemberGroupRelPK;
    }

    public MultipleMemberGroupRel(int memberId, int groupId) {
        this.crmMultipleMemberGroupRelPK = new MultipleMemberGroupRelPK(memberId, groupId);
    }

    public MultipleMemberGroupRelPK getMultipleMemberGroupRelPK() {
        return crmMultipleMemberGroupRelPK;
    }

    public void setMultipleMemberGroupRelPK(MultipleMemberGroupRelPK crmMultipleMemberGroupRelPK) {
        this.crmMultipleMemberGroupRelPK = crmMultipleMemberGroupRelPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (crmMultipleMemberGroupRelPK != null ? crmMultipleMemberGroupRelPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MultipleMemberGroupRel)) {
            return false;
        }
        MultipleMemberGroupRel other = (MultipleMemberGroupRel) object;
        if ((this.crmMultipleMemberGroupRelPK == null && other.crmMultipleMemberGroupRelPK != null) || (this.crmMultipleMemberGroupRelPK != null && !this.crmMultipleMemberGroupRelPK.equals(other.crmMultipleMemberGroupRelPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.MultipleMemberGroupRel[ crmMultipleMemberGroupRelPK=" + crmMultipleMemberGroupRelPK + " ]";
    }
    
}
