/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Embeddable
public class CompanyTargetInfoPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "target_id", nullable = false)
    @SequenceGenerator(name = "seq_targetId", sequenceName = "seq_targetId")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    @Getter private Integer targetId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_target", nullable = false)
    @Getter @Setter private Short companyTarget;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_target_id", nullable = false)
    @Getter @Setter private Integer companyTargetId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_flag_type", nullable = false)
    @Getter @Setter private Short companyFlagType;

    public CompanyTargetInfoPK() {
        
    }
    
    public CompanyTargetInfoPK(Integer targetId) {
        this.targetId = targetId;
    }

    public CompanyTargetInfoPK(Short companyTarget, Integer companyTargetId, Short companyFlagType) {
        this.companyTarget = companyTarget;
        this.companyTargetId = companyTargetId;
        this.companyFlagType = companyFlagType;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += targetId == null ? 0 : targetId.intValue();
        hash += companyTarget == null ? 0 : (int) companyTarget;
        hash += companyTargetId == null ? 0 : (int) companyTargetId;
        hash += companyFlagType == null ? 0 : (int) companyFlagType;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CompanyTargetInfoPK)) {
            return false;
        }
        CompanyTargetInfoPK other = (CompanyTargetInfoPK) object;
        if (this.targetId != null && other.targetId != null
                && this.targetId.intValue() != other.targetId.intValue()) {
            return false;
        }
        if (this.companyTarget != null && other.companyTarget != null
                && this.companyTarget.shortValue()!= other.companyTarget.shortValue()) {
            return false;
        }
        if (this.companyTargetId != null && other.companyTargetId != null
                && this.companyTargetId.intValue() != other.companyTargetId.intValue()) {
            return false;
        }
        if (this.companyFlagType != null && other.companyFlagType != null
                && this.companyFlagType.shortValue()!= other.companyFlagType.shortValue()) {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.CrmCompanyTargetInfoPK[ companyTarget=" + companyTarget + ", companyTargetId=" + companyTargetId + ", companyFlagType=" + companyFlagType + " ]";
    }

}
