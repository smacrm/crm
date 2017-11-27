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
public class AutoFormPageTabDivItemRelPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "page_id")
    @Setter @Getter
    private int pageId;
    
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
    
    @Basic(optional = false)
    @Column(name = "tab_id")
    @Setter @Getter
    private Integer tabId;
    
    @Column(name = "div_id")
    @Setter @Getter
    private Integer divId;

    public AutoFormPageTabDivItemRelPK() {
    }

    public AutoFormPageTabDivItemRelPK(int pageId, int divId, int tabId, int itemId, int companyId) {
        this.pageId = pageId;
        this.divId = divId;
        this.tabId = tabId;
        this.itemId = itemId;
        this.companyId = companyId;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) pageId;
        hash += (int) itemId;
        hash += (int) companyId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoFormPageTabDivItemRelPK)) {
            return false;
        }
        AutoFormPageTabDivItemRelPK other = (AutoFormPageTabDivItemRelPK) object;
        if (this.pageId != other.pageId) {
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
        return "gnext.bean.customize.AutoFormPageTabDivItemRelPK[ pageId=" + pageId + ", itemId=" + itemId + ", companyId=" + companyId + " ]";
    }
    
}
