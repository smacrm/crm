/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.role;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_system_use_auth_rel")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SystemUseAuthRel.findAll", query = "SELECT c FROM SystemUseAuthRel c"),
    @NamedQuery(name = "SystemUseAuthRel.findByRoleId", query = "SELECT c FROM SystemUseAuthRel c WHERE c.crmSystemUseAuthRelPK.roleId = :roleId"),
    @NamedQuery(name = "SystemUseAuthRel.findByCompanyId", query = "SELECT c FROM SystemUseAuthRel c WHERE c.crmSystemUseAuthRelPK.companyId = :companyId"),
    @NamedQuery(name = "SystemUseAuthRel.findByGroupMemberFlag", query = "SELECT c FROM SystemUseAuthRel c WHERE c.crmSystemUseAuthRelPK.groupMemberFlag = :groupMemberFlag"),
    @NamedQuery(name = "SystemUseAuthRel.findByGroupMemberId", query = "SELECT c FROM SystemUseAuthRel c WHERE c.crmSystemUseAuthRelPK.groupMemberId = :groupMemberId")
})
public class SystemUseAuthRel implements Serializable {
    
    public static final short GROUP_FLAG = 0;
    
    public static final short MEMBER_FLAG = 1;
    
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected SystemUseAuthRelPK crmSystemUseAuthRelPK;
    @Column(name = "creator_id")
    private Integer creatorId;
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    @Column(name = "updated_id")
    private Integer updatedId;
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;

    public SystemUseAuthRel() {
    }

    public SystemUseAuthRel(SystemUseAuthRelPK crmSystemUseAuthRelPK) {
        this.crmSystemUseAuthRelPK = crmSystemUseAuthRelPK;
    }

    public SystemUseAuthRel(int roleId, int companyId, short groupMemberFlag, int groupMemberId) {
        this.crmSystemUseAuthRelPK = new SystemUseAuthRelPK(roleId, companyId, groupMemberFlag, groupMemberId);
    }

    public SystemUseAuthRelPK getSystemUseAuthRelPK() {
        return crmSystemUseAuthRelPK;
    }

    public void setSystemUseAuthRelPK(SystemUseAuthRelPK crmSystemUseAuthRelPK) {
        this.crmSystemUseAuthRelPK = crmSystemUseAuthRelPK;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getUpdatedId() {
        return updatedId;
    }

    public void setUpdatedId(Integer updatedId) {
        this.updatedId = updatedId;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (crmSystemUseAuthRelPK != null ? crmSystemUseAuthRelPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SystemUseAuthRel)) {
            return false;
        }
        SystemUseAuthRel other = (SystemUseAuthRel) object;
        if ((this.crmSystemUseAuthRelPK == null && other.crmSystemUseAuthRelPK != null) || (this.crmSystemUseAuthRelPK != null && !this.crmSystemUseAuthRelPK.equals(other.crmSystemUseAuthRelPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.SystemUseAuthRel[ crmSystemUseAuthRelPK=" + crmSystemUseAuthRelPK + " ]";
    }
    
}
