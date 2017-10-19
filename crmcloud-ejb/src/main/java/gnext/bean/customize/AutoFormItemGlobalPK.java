package gnext.bean.customize;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author hungpham
 * @since Oct 31, 2016
 */
@Embeddable
public class AutoFormItemGlobalPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "item_id")
    private int itemId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 3)
    @Column(name = "item_lang")
    private String itemLang;

    public AutoFormItemGlobalPK() {
    }

    public AutoFormItemGlobalPK(int itemId, String itemLang) {
        this.itemId = itemId;
        this.itemLang = itemLang;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemLang() {
        return itemLang;
    }

    public void setItemLang(String itemLang) {
        this.itemLang = itemLang;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) itemId;
        hash += (itemLang != null ? itemLang.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoFormItemGlobalPK)) {
            return false;
        }
        AutoFormItemGlobalPK other = (AutoFormItemGlobalPK) object;
        if (this.itemId != other.itemId) {
            return false;
        }
        if ((this.itemLang == null && other.itemLang != null) || (this.itemLang != null && !this.itemLang.equals(other.itemLang))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.customize.AutoFormItemGlobalPK[ itemId=" + itemId + ", itemLang=" + itemLang + " ]";
    }

}
