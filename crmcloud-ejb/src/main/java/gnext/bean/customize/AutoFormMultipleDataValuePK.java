/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.customize;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Embeddable
public class AutoFormMultipleDataValuePK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "target_id")
    @Setter @Getter
    private int targetId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "page_id")
    @Setter @Getter
    private int pageId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "page_type")
    @Setter @Getter
    private int pageType;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "item_id")
    @Setter @Getter
    private int itemId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id")
    @Setter @Getter
    private int companyId;

    public AutoFormMultipleDataValuePK() {
    }

    public AutoFormMultipleDataValuePK(int targetId, int pageId, int pageType, int itemId, int companyId) {
        this.targetId = targetId;
        this.pageId = pageId;
        this.pageType = pageType;
        this.itemId = itemId;
        this.companyId = companyId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) targetId;
        hash += (int) pageId;
        hash += (int) pageType;
        hash += (int) itemId;
        hash += (int) companyId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoFormMultipleDataValuePK)) {
            return false;
        }
        AutoFormMultipleDataValuePK other = (AutoFormMultipleDataValuePK) object;
        if (this.targetId != other.targetId) {
            return false;
        }
        if (this.pageId != other.pageId) {
            return false;
        }
        if (this.pageType != other.pageType) {
            return false;
        }
        if (this.itemId != other.itemId) {
            return false;
        }
        if (this.companyId != other.companyId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.customize.AutoFormMultipleDataValuePK[ targetId=" + targetId + ", pageId=" + pageId + ", pageType=" + pageType + ", itemId=" + itemId + ", companyId=" + companyId + " ]";
    }
    
}
