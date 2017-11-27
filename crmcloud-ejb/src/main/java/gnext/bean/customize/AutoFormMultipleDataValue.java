/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.customize;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
@Cacheable(true)
@Table(name = "crm_multiple_data_value")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AutoFormMultipleDataValue.findByKey", query = "SELECT a FROM AutoFormMultipleDataValue a WHERE a.autoFormMultipleDataValuePK.companyId = :companyId and  a.autoFormMultipleDataValuePK.itemId = :itemId and  a.autoFormMultipleDataValuePK.pageId = :pageId and  a.autoFormMultipleDataValuePK.targetId = :targetId"),
    @NamedQuery(name = "AutoFormMultipleDataValue.deleteByExcludedKey", query = "DELETE FROM AutoFormMultipleDataValue a WHERE a.autoFormMultipleDataValuePK.companyId = :companyId and  a.autoFormMultipleDataValuePK.itemId NOT IN :itemId and  a.autoFormMultipleDataValuePK.pageId = :pageId and a.autoFormMultipleDataValuePK.targetId = :targetId"),
    @NamedQuery(name = "AutoFormMultipleDataValue.findItemData", query = "SELECT a FROM AutoFormMultipleDataValue a WHERE a.autoFormMultipleDataValuePK.companyId = :companyId and  a.autoFormMultipleDataValuePK.pageId = :pageId and  a.autoFormMultipleDataValuePK.targetId = :targetId")
})
public class AutoFormMultipleDataValue  extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    @Setter @Getter
    protected AutoFormMultipleDataValuePK autoFormMultipleDataValuePK;
    
    @Size(max = 2000)
    @Column(name = "item_data")
    @Setter @Getter
    private String itemData;
    
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
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private Company company;
    
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private AutoFormItem item;
    
    @JoinColumn(name = "page_id", referencedColumnName = "page_id", insertable = false, updatable = false)
    @OneToOne(optional = false)
    @Setter @Getter
    private AutoFormPageTab page;

    public AutoFormMultipleDataValue() {
    }

    public AutoFormMultipleDataValue(AutoFormMultipleDataValuePK autoFormMultipleDataValuePK) {
        this.autoFormMultipleDataValuePK = autoFormMultipleDataValuePK;
    }

    public AutoFormMultipleDataValue(int refId, int pageId, int pageType, int itemId, int companyId) {
        this.autoFormMultipleDataValuePK = new AutoFormMultipleDataValuePK(refId, pageId, pageType, itemId, companyId);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (autoFormMultipleDataValuePK != null ? autoFormMultipleDataValuePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoFormMultipleDataValue)) {
            return false;
        }
        AutoFormMultipleDataValue other = (AutoFormMultipleDataValue) object;
        if ((this.autoFormMultipleDataValuePK == null && other.autoFormMultipleDataValuePK != null) || (this.autoFormMultipleDataValuePK != null && !this.autoFormMultipleDataValuePK.equals(other.autoFormMultipleDataValuePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.customize.AutoFormMultipleDataValue[ autoFormMultipleDataValuePK=" + autoFormMultipleDataValuePK + " ]";
    }
    
}
