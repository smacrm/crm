/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.role;

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
public class SystemUseAuthRelPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "role_id", nullable = false)
    private int roleId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id", nullable = false)
    private int companyId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "group_member_flag", nullable = false)
    private short groupMemberFlag;
    @Basic(optional = false)
    @NotNull
    @Column(name = "group_member_id", nullable = false)
    private int groupMemberId;

    public SystemUseAuthRelPK() {
    }

    public SystemUseAuthRelPK(int roleId, int companyId, short groupMemberFlag, int groupMemberId) {
        this.roleId = roleId;
        this.companyId = companyId;
        this.groupMemberFlag = groupMemberFlag;
        this.groupMemberId = groupMemberId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public short getGroupMemberFlag() {
        return groupMemberFlag;
    }

    public void setGroupMemberFlag(short groupMemberFlag) {
        this.groupMemberFlag = groupMemberFlag;
    }

    public int getGroupMemberId() {
        return groupMemberId;
    }

    public void setGroupMemberId(int groupMemberId) {
        this.groupMemberId = groupMemberId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) roleId;
        hash += (int) companyId;
        hash += (int) groupMemberFlag;
        hash += (int) groupMemberId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SystemUseAuthRelPK)) {
            return false;
        }
        SystemUseAuthRelPK other = (SystemUseAuthRelPK) object;
        if (this.roleId != other.roleId) {
            return false;
        }
        if (this.companyId != other.companyId) {
            return false;
        }
        if (this.groupMemberFlag != other.groupMemberFlag) {
            return false;
        }
        if (this.groupMemberId != other.groupMemberId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.CrmSystemUseAuthRelPK[ roleId=" + roleId + ", companyId=" + companyId + ", groupMemberFlag=" + groupMemberFlag + ", groupMemberId=" + groupMemberId + " ]";
    }
    
}
