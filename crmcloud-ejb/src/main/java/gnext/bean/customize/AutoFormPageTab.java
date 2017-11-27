/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.customize;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import java.util.ArrayList;
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
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
// @Cacheable(true)
@Table(name = "crm_page_tab")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AutoFormPageTab.findByCompanyId", query = "SELECT DISTINCT p FROM AutoFormPageTab p INNER JOIN FETCH p.company c WHERE c.companyId = :companyId")
})
public class AutoFormPageTab  extends BaseEntity {

    public static final short PAGE_TYPE_CORRELATIVE_DYNAMIC_FORM = 2;
    public static final short PAGE_TYPE_IRRELEVANTIVE_DYNAMIC_FORM = 1;
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "page_id")
    @Setter @Getter
    private Integer pageId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 70)
    @Column(name = "page_name")
    @Setter @Getter
    private String pageName;
    
    @Column(name = "page_type")
    @Setter @Getter
    private Short pageType;
    
    @Column(name = "page_deleted")
    @Setter @Getter
    private Boolean pageDeleted;
    
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
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id")
    @ManyToOne(optional = false)
    @Setter @Getter
    private Company company;
    
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "page")
    @Setter @Getter
    private AutoFormMultipleDataValue autoFormMultipleDataValue;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "page", orphanRemoval = true, fetch = FetchType.EAGER)
    @Setter @Getter
    @XmlTransient
    @OrderBy("tabOrder ASC, divOrder ASC, itemOrder ASC")
    private List<AutoFormPageTabDivItemRel> autoFormPageTabDivItemRelList = new ArrayList<>();
    
    @Transient
    @Setter
    private boolean dynamic = false;

    public AutoFormPageTab() {
    }

    public AutoFormPageTab(Integer pageId) {
        this.pageId = pageId;
    }

    public AutoFormPageTab(Integer pageId, String pageName) {
        this.pageId = pageId;
        this.pageName = pageName;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pageId != null ? pageId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoFormPageTab)) {
            return false;
        }
        AutoFormPageTab other = (AutoFormPageTab) object;
        if ((this.pageId == null && other.pageId != null) || (this.pageId != null && !this.pageId.equals(other.pageId))) {
            return false;
        }
        return true;
    }
    
    public boolean isDynamic(){
        return pageType == PAGE_TYPE_CORRELATIVE_DYNAMIC_FORM;
    }
    
    @Override
    public String toString() {
        return "gnext.bean.customize.AutoFormPageTab[ pageId=" + pageId + " ]";
    }
    
}
