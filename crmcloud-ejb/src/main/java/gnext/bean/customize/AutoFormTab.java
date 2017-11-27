/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.customize;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
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
@Table(name = "crm_tab")
// @Cacheable(true)
@XmlRootElement
@NamedQueries({})
public class AutoFormTab  extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "tab_id")
    @Setter @Getter
    private Integer tabId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 70)
    @Column(name = "tab_name")
    @Setter @Getter
    private String tabName;
    
    @Column(name = "tab_col")
    @Setter @Getter
    private Boolean tabCol;
    
    @Column(name = "tab_deleted")
    @Setter @Getter
    private Boolean tabDeleted;
    
    @Column(name = "creator_id")
    @Setter @Getter
    private Integer creatorId;
    
    @Column(name = "created_time")
    @Setter @Getter
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    
    @Column(name = "updated_id")
    @Setter @Getter
    private Integer updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date updatedTime;
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;

    public AutoFormTab() {
    }

    public AutoFormTab(Integer tabId) {
        this.tabId = tabId;
    }

    public AutoFormTab(Integer tabId, String tabName) {
        this.tabId = tabId;
        this.tabName = tabName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tabId != null ? tabId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoFormTab)) {
            return false;
        }
        AutoFormTab other = (AutoFormTab) object;
        if ((this.tabId == null && other.tabId != null) || (this.tabId != null && !this.tabId.equals(other.tabId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.customize.AutoFormTab[ tabId=" + tabId + " ]";
    }
}
