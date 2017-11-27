/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.project;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Embeddable
public class ProjectRoleRelPK implements Serializable {

    private static final long serialVersionUID = 4062399795642560198L;
    @Basic(optional = false)
    @NotNull
    @Column(name = "list_id", nullable = false)
    @Setter @Getter
    private Integer listId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id", nullable = false)
    @Setter @Getter
    private Integer companyId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "group_member_flag", nullable = false)
    @Setter @Getter
    private Short groupMemberFlag;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "group_member_id", nullable = false)
    @Setter @Getter
    private Integer groupMemberId;

    public ProjectRoleRelPK() {
    }

    public ProjectRoleRelPK(Integer listId, Integer companyId, Short groupMemberFlag, Integer groupMemberId) {
        this.listId = listId;
        this.companyId = companyId;
        this.groupMemberFlag = groupMemberFlag;
        this.groupMemberId = groupMemberId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += listId==null?0:(int) listId;
        hash += companyId==null?0:(int) companyId;
        hash += groupMemberFlag==null?0:(int) groupMemberFlag;
        hash += groupMemberId==null?0:(int) groupMemberId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProjectRoleRelPK)) {
            return false;
        }
        ProjectRoleRelPK other = (ProjectRoleRelPK) object;
        
        if (!this.listId.equals(other.listId)) {
            return false;
        }
        
        if (!Objects.equals(this.companyId, other.companyId)) {
            return false;
        }
        
        if (!Objects.equals(this.groupMemberFlag, other.groupMemberFlag)) {
            return false;
        }
        
        if (!Objects.equals(this.groupMemberId, other.groupMemberId)) {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.ProjectRoleRelPK[ listId=" + listId + ", companyId=" + companyId + ", groupMemberFlag=" + groupMemberFlag + ", groupMemberId=" + groupMemberId + " ]";
    }
    
}
