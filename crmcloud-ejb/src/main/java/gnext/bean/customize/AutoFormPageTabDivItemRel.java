package gnext.bean.customize;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
// @Cacheable(true)
@Table(name = "crm_page_tab_div_item_rel")
@XmlRootElement
public class AutoFormPageTabDivItemRel  extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    @Setter @Getter
    protected AutoFormPageTabDivItemRelPK autoFormPageTabDivItemRelPK;
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private Company company;
    
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private AutoFormItem item;
    
    @JoinColumn(name = "page_id", referencedColumnName = "page_id", insertable = false, updatable = false)
    @ManyToOne(optional = true)
    @Setter @Getter
    private AutoFormPageTab page;
    
    @Column(name = "item_order")
    @Setter @Getter
    private Integer itemOrder;
    
    @Column(name = "div_order")
    @Setter @Getter
    private Integer divOrder;
    
    @Column(name = "tab_order")
    @Setter @Getter
    private Integer tabOrder;
    
    @JoinColumn(name = "tab_id", referencedColumnName = "tab_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private AutoFormTab tab;
    
    @JoinColumn(name = "div_id", referencedColumnName = "div_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private AutoFormDiv div;

    public AutoFormPageTabDivItemRel() {
    }

    public AutoFormPageTabDivItemRel(AutoFormPageTabDivItemRelPK autoFormPageTabDivItemRelPK) {
        this.autoFormPageTabDivItemRelPK = autoFormPageTabDivItemRelPK;
    }

    public AutoFormPageTabDivItemRel(int pageId, int divId, int tabId, int itemId, int companyId) {
        this.autoFormPageTabDivItemRelPK = new AutoFormPageTabDivItemRelPK(pageId, divId, tabId, itemId, companyId);
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (autoFormPageTabDivItemRelPK != null ? autoFormPageTabDivItemRelPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoFormPageTabDivItemRel)) {
            return false;
        }
        AutoFormPageTabDivItemRel other = (AutoFormPageTabDivItemRel) object;
        if ((this.autoFormPageTabDivItemRelPK == null && other.autoFormPageTabDivItemRelPK != null) || (this.autoFormPageTabDivItemRelPK != null && !this.autoFormPageTabDivItemRelPK.equals(other.autoFormPageTabDivItemRelPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.customize.AutoFormPageTabDivItemRel[ autoFormPageTabDivItemRelPK=" + autoFormPageTabDivItemRelPK + " ]";
    }
    
}
