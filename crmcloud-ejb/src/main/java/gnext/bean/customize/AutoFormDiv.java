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
import javax.persistence.NamedQuery;
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
// @Cacheable(true)
@Table(name = "crm_div")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AutoFormDiv.findAll", query = "SELECT a FROM AutoFormDiv a"),
    @NamedQuery(name = "AutoFormDiv.findByDivId", query = "SELECT a FROM AutoFormDiv a WHERE a.divId = :divId"),
    @NamedQuery(name = "AutoFormDiv.findByDivName", query = "SELECT a FROM AutoFormDiv a WHERE a.divName = :divName"),
    @NamedQuery(name = "AutoFormDiv.findByDivCol", query = "SELECT a FROM AutoFormDiv a WHERE a.divCol = :divCol"),
    @NamedQuery(name = "AutoFormDiv.findByDivDeleted", query = "SELECT a FROM AutoFormDiv a WHERE a.divDeleted = :divDeleted"),
    @NamedQuery(name = "AutoFormDiv.findByCreatorId", query = "SELECT a FROM AutoFormDiv a WHERE a.creatorId = :creatorId"),
    @NamedQuery(name = "AutoFormDiv.findByCreatedTime", query = "SELECT a FROM AutoFormDiv a WHERE a.createdTime = :createdTime"),
    @NamedQuery(name = "AutoFormDiv.findByUpdatedId", query = "SELECT a FROM AutoFormDiv a WHERE a.updatedId = :updatedId"),
    @NamedQuery(name = "AutoFormDiv.findByUpdatedTime", query = "SELECT a FROM AutoFormDiv a WHERE a.updatedTime = :updatedTime")})
public class AutoFormDiv  extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "div_id")
    @Setter @Getter
    private Integer divId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 70)
    @Column(name = "div_name")
    @Setter @Getter
    private String divName;
    
    @Column(name = "div_col")
    @Setter @Getter
    private Short divCol;
    
    @Column(name = "div_deleted")
    @Setter @Getter
    private Boolean divDeleted;
    
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
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;

    public AutoFormDiv() {
    }

    public AutoFormDiv(Integer divId) {
        this.divId = divId;
    }

    public AutoFormDiv(Integer divId, String divName) {
        this.divId = divId;
        this.divName = divName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (divId != null ? divId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoFormDiv)) {
            return false;
        }
        AutoFormDiv other = (AutoFormDiv) object;
        if ((this.divId == null && other.divId != null) || (this.divId != null && !this.divId.equals(other.divId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.customize.AutoFormDiv[ divId=" + divId + " ]";
    }
}
