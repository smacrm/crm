package gnext.bean.label;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Oct 27, 2016
 */
@Entity
@Cacheable(true)
@Table(name = "crm_property_item_label")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "PropertyItemLabel.findAll", 
        query = "SELECT o FROM PropertyItemLabel o",
        hints = {
            @QueryHint(name="eclipselink.query-results-cache", value="true"),
            @QueryHint(name="eclipselink.query-results-cache.size", value="100")
        }
    ),
    @NamedQuery(
        name = "PropertyItemLabel.findByKey", 
        query = "SELECT o.labelName FROM PropertyItemLabel o WHERE o.pk.companyId = :companyId and o.pk.itemCode = :key",
        hints = {
            @QueryHint(name="eclipselink.query-results-cache", value="true"),
            @QueryHint(name="eclipselink.query-results-cache.size", value="100")
        }
    ),
})
public class PropertyItemLabel  extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    @Setter @Getter
    protected PropertyItemLabelPK pk = new PropertyItemLabelPK();
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 3)
    @Column(name = "label_language")
    @Setter @Getter
    private String labelLanguage;
    
    @Size(max = 255)
    @Column(name = "label_name")
    @Setter @Getter
    private String labelName;
    
    @Setter @Getter
    @Transient
    private String module;
    
    @JoinColumn(name="company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    @ManyToOne
    @Setter @Getter
    private Company company;

    public PropertyItemLabel() {
    }

    public PropertyItemLabel(PropertyItemLabelPK propertyItemLabelPK) {
        this.pk = propertyItemLabelPK;
    }

    public PropertyItemLabel(String itemCode, int companyId) {
        this.pk = new PropertyItemLabelPK(itemCode, companyId);
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pk != null ? pk.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PropertyItemLabel)) {
            return false;
        }
        PropertyItemLabel other = (PropertyItemLabel) object;
        if ((this.pk == null && other.pk != null) || (this.pk != null && !this.pk.equals(other.pk))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.label.PropertyItemLabel[ propertyItemLabelPK=" + pk + " ]";
    }

}
