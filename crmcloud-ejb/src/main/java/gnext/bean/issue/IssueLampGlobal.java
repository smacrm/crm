package gnext.bean.issue;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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
@Table(name = "crm_issue_lamp_global")
@XmlRootElement
@NamedQueries({ })
public class IssueLampGlobal implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @Getter @Setter
    protected IssueLampGlobalPK crmIssueLampGlobalPK = new IssueLampGlobalPK();

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Getter @Setter
    @Column(name = "item_name")
    private String itemName;

    @JoinColumn(name = "item_id", referencedColumnName = "lamp_id", insertable = false, updatable = false)
    @ManyToOne(cascade=CascadeType.ALL)
    @Getter @Setter
    private IssueLamp crmIssueLamp;

    public IssueLampGlobal() {
    }

    public IssueLampGlobal(IssueLampGlobalPK crmIssueLampGlobalPK) {
        this.crmIssueLampGlobalPK = crmIssueLampGlobalPK;
    }

    public IssueLampGlobal(IssueLampGlobalPK crmIssueLampGlobalPK, String itemName) {
        this.crmIssueLampGlobalPK = crmIssueLampGlobalPK;
        this.itemName = itemName;
    }

    public IssueLampGlobal(int itemId, String itemLang) {
        this.crmIssueLampGlobalPK = new IssueLampGlobalPK(itemId, itemLang);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (crmIssueLampGlobalPK != null ? crmIssueLampGlobalPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IssueLampGlobal)) {
            return false;
        }
        IssueLampGlobal other = (IssueLampGlobal) object;
        return !((this.crmIssueLampGlobalPK == null && other.crmIssueLampGlobalPK != null) || (this.crmIssueLampGlobalPK != null && !this.crmIssueLampGlobalPK.equals(other.crmIssueLampGlobalPK)));
    }

    @Override
    public String toString() {
        return "gnext.issue.IssueLampGlobal[ crmIssueLampGlobalPK=" + crmIssueLampGlobalPK + " ]";
    }
    
}
