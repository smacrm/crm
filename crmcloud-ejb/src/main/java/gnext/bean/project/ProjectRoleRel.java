/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.project;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_project_role_rel", schema = "")
@XmlRootElement
public class ProjectRoleRel  extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    @Setter @Getter
    protected ProjectRoleRelPK projectRoleRelPK;
    
    @Column(name = "creator_id")
    @Setter @Getter
    private Integer creatorId;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date createdTime;
    
    @Column(name = "updated_id")
    @Setter @Getter
    private Integer updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date updatedTime;
    
    @JoinColumn(name = "list_id", referencedColumnName = "list_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Setter @Getter
    private ProjectCustSearch project;
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Getter @Setter
    private Company company;


    public ProjectRoleRel() {
    }

    public ProjectRoleRel(ProjectRoleRelPK projectRoleRelPK) {
        this.projectRoleRelPK = projectRoleRelPK;
    }

    public ProjectRoleRel(int listId, int companyId, short groupMemberFlag, int groupMemberId) {
        this.projectRoleRelPK = new ProjectRoleRelPK(listId, companyId, groupMemberFlag, groupMemberId);
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (projectRoleRelPK != null ? projectRoleRelPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProjectRoleRelPK)) {
            return false;
        }
        ProjectRoleRel other = (ProjectRoleRel) object;
        if ((this.projectRoleRelPK == null && other.projectRoleRelPK != null) || (this.projectRoleRelPK != null && !this.projectRoleRelPK.equals(other.projectRoleRelPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.SystemUseAuthRel[ projectRoleRelPK=" + projectRoleRelPK + " ]";
    }
    
}
