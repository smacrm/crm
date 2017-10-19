/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.project;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import gnext.bean.Member;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */

@Entity
@Table(name = "crm_project_cust_search_list")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "ProjectCustSearch.findByMember", 
        query = "SELECT o FROM ProjectCustSearch o WHERE o.listDeleted = 0 AND o.listType = 1"
                + " AND o.company.companyId = :companyId"
                + " AND ("
                    + "o.creator.memberId = :memberId"
                    + " OR o.listId in (SELECT r.projectRoleRelPK.listId FROM ProjectRoleRel r WHERE r.projectRoleRelPK.groupMemberFlag = 1 AND r.projectRoleRelPK.groupMemberId = :memberId)"
                    + " OR o.listId in (SELECT r.projectRoleRelPK.listId FROM ProjectRoleRel r WHERE r.projectRoleRelPK.groupMemberFlag = 0 AND r.projectRoleRelPK.groupMemberId = :groupId)"
                + ") ORDER BY o.listId ASC",
        hints = {
            @QueryHint(name="eclipselink.query-results-cache", value="true"),
            @QueryHint(name="eclipselink.query-results-cache.size", value="200")
        }
    ),
})
public class ProjectCustSearch extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "list_id")
    @Setter @Getter
    private Integer listId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 70)
    @Column(name = "list_name")
    @Setter @Getter
    private String listName;
    
    @Column(name = "list_type")
    @Setter @Getter
    private Short listType;
    
    @Column(name = "list_deleted")
    @Setter @Getter
    private Short listDeleted;
    
    @Column(name = "list_search_data")
    @Setter @Getter
    private String listSearchData;
    
    @Column(name = "list_view_data")
    @Setter @Getter
    private String listViewData;
    
    @Column(name = "date_from")
    @Temporal(TemporalType.DATE)
    @Setter @Getter
    private Date dateFrom;
    
    @Column(name = "date_to")
    @Temporal(TemporalType.DATE)
    @Setter @Getter
    private Date dateTo;

    @Column(name = "list_search_period_flag")
    @Setter @Getter
    private Short listSearchPeriodFlag;

    @Size(max = 50)
    @Column(name = "list_search_period")
    @Setter @Getter
    private String listSearchPeriod; 

    @JoinColumn(name = "company_id")
    @ManyToOne
    @Setter @Getter
    private Company company;
    
    @JoinColumn(name = "creator_id", referencedColumnName = "member_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Member creator;
    
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

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "project", orphanRemoval = true)
    @Setter @Getter
    private List<ProjectRoleRel> projectRoleRel = new ArrayList<>();
    
    public ProjectCustSearch() {
        
        Calendar c = Calendar.getInstance();
        this.setDateTo(c.getTime());
        c.add(Calendar.MONTH, -6);
        this.setDateFrom(c.getTime());
    }

    public boolean isDeleted(){
        return listDeleted == 1;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (listId != null ? listId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProjectCustSearch)) {
            return false;
        }
        ProjectCustSearch other = (ProjectCustSearch) object;
        if ((this.listId == null && other.listId != null) || (this.listId != null && !this.listId.equals(other.listId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.customize.project.ProjectCustSearch[ listId=" + listId + " ]";
    }
    
}
