/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.issue;

import gnext.interceptors.annotation.QuickSearchDb;
import gnext.interceptors.annotation.QuickSearchField;
import gnext.interceptors.annotation.enums.Module;
import java.io.Serializable;
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

/**
 *
 * @author daind
 */
@Entity
@Table(name = "crm_cust_data_special")
@XmlRootElement
@QuickSearchDb(module = Module.CUSTOMER, fieldTargetId = "custDataSpecialId", disable = false)
public class CustDataSpecial implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static int CUST_SPECIAL_TYPE_NAME = 1;
    public static int CUST_SPECIAL_TYPE_ADDRESS = 2;
    public static int CUST_SPECIAL_TYPE_HISTORY = 3;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "cust_data_special_id")
    @QuickSearchField(name = "cust_data_special_id")
    private Integer custDataSpecialId;
    
    @JoinColumn(name = "cust_id", nullable = false, referencedColumnName = "cust_id")
    @ManyToOne(optional = false)
    private Customer customer;
    
    @Column(name = "cust_type")
    @QuickSearchField(name = "cust_type")
    private Integer custType;
    
    @Size(max = 45)
    @Column(name = "cust_data_1")
    @QuickSearchField(name = "cust_data_1")
    private String custData1;
    
    @Size(max = 45)
    @Column(name = "cust_data_2")
    @QuickSearchField(name = "cust_data_2")
    private String custData2;
    
    // trường hợp nếu là field tương ứng với city trong address type.
    @Transient
    @Getter @Setter private String custCityNameData2;
    
    @Size(max = 45)
    @Column(name = "cust_data_3")
    @QuickSearchField(name = "cust_data_3")
    private String custData3;
    
    @Size(max = 45)
    @Column(name = "cust_data_4")
    @QuickSearchField(name = "cust_data_4")
    private String custData4;
    
    @Size(max = 45)
    @Column(name = "cust_data_5")
    @QuickSearchField(name = "cust_data_5")
    private String custData5;
    
    public CustDataSpecial() {
    }

    public CustDataSpecial(int custDataSpecialId) {
        this.custDataSpecialId = custDataSpecialId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public Integer getCustType() {
        return custType;
    }

    public void setCustType(Integer custType) {
        this.custType = custType;
    }

    public String getCustData1() {
        return custData1;
    }

    public void setCustData1(String custData1) {
        this.custData1 = custData1;
    }

    public String getCustData2() {
        return custData2;
    }

    public void setCustData2(String custData2) {
        this.custData2 = custData2;
    }

    public String getCustData3() {
        return custData3;
    }

    public void setCustData3(String custData3) {
        this.custData3 = custData3;
    }

    public String getCustData4() {
        return custData4;
    }

    public void setCustData4(String custData4) {
        this.custData4 = custData4;
    }

    public String getCustData5() {
        return custData5;
    }

    public void setCustData5(String custData5) {
        this.custData5 = custData5;
    }

    public Integer getCustDataSpecialId() {
        return custDataSpecialId;
    }

    public void setCustDataSpecialId(Integer custDataSpecialId) {
        this.custDataSpecialId = custDataSpecialId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (custDataSpecialId != null ? custDataSpecialId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CustDataSpecial)) {
            return false;
        }
        CustDataSpecial other = (CustDataSpecial) object;
        if ((this.custDataSpecialId == null && other.custDataSpecialId != null) || (this.custDataSpecialId != null && !this.custDataSpecialId.equals(other.custDataSpecialId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.issue.CustDataSpecial[ custDataSpecialId=" + custDataSpecialId + " ]";
    }
    
}
