/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.issue;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author gnextadmin
 */
@Embeddable
public class CustTargetInfoPK implements Serializable {

    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    @Column(name = "target_id")
    private int targetId;
    @Basic(optional = false)

    @NotNull
    @Getter @Setter
    @Column(name = "cust_id")
    private int custId;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "cust_flag_type")
    private short custFlagType;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "company_id")
    private int companyId;

    public CustTargetInfoPK() {
    }

    public CustTargetInfoPK(int targetId, int custId, short custFlagType, int companyId) {
        this.targetId = targetId;
        this.custId = custId;
        this.custFlagType = custFlagType;
        this.companyId = companyId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) targetId;
        hash += (int) custId;
        hash += (int) custFlagType;
        hash += (int) companyId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CustTargetInfoPK)) {
            return false;
        }
        CustTargetInfoPK other = (CustTargetInfoPK) object;
        if (this.targetId != other.targetId) {
            return false;
        }
        if (this.custId != other.custId) {
            return false;
        }
        if (this.custFlagType != other.custFlagType) {
            return false;
        }
        if (this.companyId != other.companyId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.CrmCustTargetInfoPK[ targetId=" + targetId + ", custId=" + custId + ", custFlagType=" + custFlagType + ", companyId=" + companyId + " ]";
    }
    
}
