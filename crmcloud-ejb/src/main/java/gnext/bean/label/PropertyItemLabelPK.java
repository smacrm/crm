package gnext.bean.label;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Oct 27, 2016
 */
@Embeddable
public class PropertyItemLabelPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "item_code")
    @Setter @Getter
    private String itemCode;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id")
    @Setter @Getter
    private int companyId;

    public PropertyItemLabelPK() {
    }

    public PropertyItemLabelPK(String itemCode, int companyId) {
        this.itemCode = itemCode;
        this.companyId = companyId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (itemCode != null ? itemCode.hashCode() : 0);
        hash += (int) companyId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PropertyItemLabelPK)) {
            return false;
        }
        PropertyItemLabelPK other = (PropertyItemLabelPK) object;
        if ((this.itemCode == null && other.itemCode != null) || (this.itemCode != null && !this.itemCode.equals(other.itemCode))) {
            return false;
        }
        if (this.companyId != other.companyId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.label.PropertyItemLabelPK[ itemCode=" + itemCode + ", companyId=" + companyId + " ]";
    }

}
