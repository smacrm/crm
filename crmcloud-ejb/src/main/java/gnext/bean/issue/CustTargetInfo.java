package gnext.bean.issue;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import gnext.bean.mente.MenteItem;
import gnext.interceptors.annotation.QuickSearchField;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author gnextadmin
 */
@Entity
@Table(name = "crm_cust_target_info")
@XmlRootElement
public class CustTargetInfo  extends BaseEntity {

    private static final long serialVersionUID = 4082346047728653955L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "target_id")
    private Integer targetId;
    
    @Transient
    @Getter @Setter
    private Long targetLongId = System.currentTimeMillis(); //Su dung de luu du lieu tam thoi luc tao CustTargetInfo

    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "cust_flag_type")
    private Short custFlagType;

    @Getter @Setter
    @JoinColumn(name = "cust_target_class", referencedColumnName = "item_id")
    @ManyToOne(optional = false)
    @QuickSearchField(name = "cust_target_class")
    private MenteItem custTargetClass;

    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;

    @Basic(optional = false)
    @Size(min = 1, max = 120)
    @Getter @Setter
    @Column(name = "cust_target_data")
    @QuickSearchField(name = "cust_target_data")
    private String custTargetData;

    @Getter @Setter
    @JoinColumn(name = "cust_id", nullable = false, referencedColumnName = "cust_id")
    @ManyToOne(optional = false)
    private Customer customer;

    public CustTargetInfo() {
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (targetId != null ? targetId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CustTargetInfo)) {
            return false;
        }
        CustTargetInfo other = (CustTargetInfo) object;
        if(this.targetId != null && other.targetId != null){
            return this.targetId.equals(other.targetId);
        }else{
            return this.targetLongId.equals(other.targetLongId);
        }
    }
    
    public boolean isValid(){
        return !StringUtils.isEmpty(this.custTargetData);
    }

    @Override
    public String toString() {
        return "gnext.bean.CrmCustTargetInfo[ targetId=" + targetId + " ]";
    }

    @Transient
    @Getter @Setter
    @QuickSearchField(name = "cust_flag_type_name")
    private String custFlagTypeName;

}
