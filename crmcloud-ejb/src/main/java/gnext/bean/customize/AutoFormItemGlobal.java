package gnext.bean.customize;

import gnext.bean.BaseEntity;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Oct 31, 2016
 */
@Entity
@Cacheable(true)
@Table(name = "crm_item_global")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AutoFormItemGlobal.findAll", query = "SELECT a FROM AutoFormItemGlobal a")
    ,@NamedQuery(
            name = "AutoFormItemGlobal.findItemLabelById",
            query = " SELECT a.itemName FROM AutoFormItemGlobal a "
                    + " WHERE a.autoFormItem.company.companyId = :companyId "
                    + " AND a.autoFormItemGlobalPK.itemId = :itemId "
                    + " AND a.autoFormItemGlobalPK.itemLang = :itemLang ",
            hints = {
                @QueryHint(name="eclipselink.query-results-cache", value="true"),
                @QueryHint(name="eclipselink.query-results-cache.size", value="100")
            }
    )
    ,@NamedQuery(name = "AutoFormItemGlobal.findAllSelectItem"
            , query = " SELECT a.itemDataDefault FROM AutoFormItemGlobal a "
                    + " WHERE a.autoFormItem.company.companyId = :companyId "
                    + " AND a.autoFormItemGlobalPK.itemId IN :itemId ")
    ,@NamedQuery(name = "AutoFormItemGlobal.findAllSelectItemName"
            , query = " SELECT a.autoFormItem.itemType, a.itemDataDefault FROM AutoFormItemGlobal a "
                    + " WHERE a.autoFormItem.company.companyId = :companyId "
                    + " AND a.autoFormItemGlobalPK.itemId = :itemId "
                    + " AND a.autoFormItemGlobalPK.itemLang = :itemLang ")
})
public class AutoFormItemGlobal  extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    @Setter @Getter
    protected AutoFormItemGlobalPK autoFormItemGlobalPK = new AutoFormItemGlobalPK();
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 70)
    @Column(name = "item_name")
    @Setter @Getter
    private String itemName;
    
    @Size(max = 45)
    @Column(name = "item_description")
    @Setter @Getter
    private String itemDescription;
    
    @Size(max = 10000)
    @Column(name = "item_data_default")
    @Setter @Getter
    private String itemDataDefault;

    @JoinColumn(name = "item_id", referencedColumnName = "item_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private AutoFormItem autoFormItem;

    public AutoFormItemGlobal() {
    }

    public AutoFormItemGlobal(AutoFormItemGlobalPK autoFormItemGlobalPK) {
        this.autoFormItemGlobalPK = autoFormItemGlobalPK;
    }

    public AutoFormItemGlobal(AutoFormItemGlobalPK autoFormItemGlobalPK, String itemName) {
        this.autoFormItemGlobalPK = autoFormItemGlobalPK;
        this.itemName = itemName;
    }

    public AutoFormItemGlobal(int itemId, String itemLang) {
        this.autoFormItemGlobalPK = new AutoFormItemGlobalPK(itemId, itemLang);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (autoFormItemGlobalPK != null ? autoFormItemGlobalPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoFormItemGlobal)) {
            return false;
        }
        AutoFormItemGlobal other = (AutoFormItemGlobal) object;
        if ((this.autoFormItemGlobalPK == null && other.autoFormItemGlobalPK != null) || (this.autoFormItemGlobalPK != null && !this.autoFormItemGlobalPK.equals(other.autoFormItemGlobalPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.customize.AutoFormItemGlobal[ autoFormItemGlobalPK=" + autoFormItemGlobalPK + " ]";
    }
}
