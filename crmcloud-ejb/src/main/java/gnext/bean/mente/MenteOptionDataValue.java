package gnext.bean.mente;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import gnext.interceptors.annotation.QuickSearchField;
import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author gnextadmin
 */
@Entity
@Cacheable(true)
@Table(name = "crm_mente_option_data_value")
@XmlRootElement
public class MenteOptionDataValue extends BaseEntity {

    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    @Setter @Getter
    protected MenteOptionDataValuePK menteOptionDataValuePK = new MenteOptionDataValuePK();
    
    @Size(max = 2000)
    @Column(name = "item_data")
    @Setter @Getter
    @QuickSearchField(name = "item_data")
    private String itemData;
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;
    
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
    
    @MapsId("itemId")
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", nullable = false, insertable = true, updatable = true)
    @ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @Setter @Getter
    private MenteItem menteItem;

    public MenteOptionDataValue() {
    }
    
    public MenteOptionDataValue(String lang, String value, MenteItem belong) {
        this.menteOptionDataValuePK.setItemLanguage(lang);
        this.setItemData(value);
        this.setMenteItem(menteItem);
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (menteOptionDataValuePK != null ? menteOptionDataValuePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MenteOptionDataValue)) {
            return false;
        }
        MenteOptionDataValue other = (MenteOptionDataValue) object;
        if ((this.menteOptionDataValuePK == null && other.menteOptionDataValuePK != null) || (this.menteOptionDataValuePK != null && !this.menteOptionDataValuePK.equals(other.menteOptionDataValuePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.MenteOptionDataValue[ crmMenteOptionDataValuePK=" + menteOptionDataValuePK + " ]";
    }
    
}
