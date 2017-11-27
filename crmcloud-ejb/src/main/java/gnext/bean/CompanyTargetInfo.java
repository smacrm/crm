/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_company_target_info")
@XmlRootElement
public class CompanyTargetInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final short COMPANY_TARGET_COMPANY = 1;
    public static final short COMPANY_TARGET_MEMBER = 2;
    
    public static final short COMPANY_FLAG_TYPE_EMAIL = 1;
    public static final short COMPANY_FLAG_TYPE_PHONE = 2;
    public static final short COMPANY_FLAG_TYPE_MOBILE = 3;
    public static final short COMPANY_FLAG_TYPE_FAX = 4;
    public static final short COMPANY_FLAG_TYPE_HOMEPAGE = 5;
    
    @EmbeddedId
    protected CompanyTargetInfoPK crmCompanyTargetInfoPK;
    
    @Size(max = 120)
    @Column(name = "company_target_data", length = 120)
    private String companyTargetData;
    
    @Column(name = "company_target_deleted")
    private Short companyTargetDeleted;
    
    @Column(name = "creator_id")
    private Integer creatorId;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    
    @Column(name = "updated_id")
    private Integer updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;

    public CompanyTargetInfo() {
    }

    public CompanyTargetInfo(CompanyTargetInfoPK crmCompanyTargetInfoPK) {
        this.crmCompanyTargetInfoPK = crmCompanyTargetInfoPK;
    }

    public CompanyTargetInfo(short companyTarget, int companyTargetId, short companyFlagType) {
        this.crmCompanyTargetInfoPK = new CompanyTargetInfoPK(companyTarget, companyTargetId, companyFlagType);
    }

    public CompanyTargetInfoPK getCompanyTargetInfoPK() {
        return crmCompanyTargetInfoPK;
    }

    public void setCompanyTargetInfoPK(CompanyTargetInfoPK crmCompanyTargetInfoPK) {
        this.crmCompanyTargetInfoPK = crmCompanyTargetInfoPK;
    }

    public String getCompanyTargetData() {
        return companyTargetData;
    }

    public void setCompanyTargetData(String companyTargetData) {
        this.companyTargetData = companyTargetData;
    }

    public Short getCompanyTargetDeleted() {
        return companyTargetDeleted;
    }

    public void setCompanyTargetDeleted(Short companyTargetDeleted) {
        this.companyTargetDeleted = companyTargetDeleted;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getUpdatedId() {
        return updatedId;
    }

    public void setUpdatedId(Integer updatedId) {
        this.updatedId = updatedId;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (crmCompanyTargetInfoPK != null ? crmCompanyTargetInfoPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CompanyTargetInfo)) {
            return false;
        }
        CompanyTargetInfo other = (CompanyTargetInfo) object;
        if ((this.crmCompanyTargetInfoPK == null && other.crmCompanyTargetInfoPK != null) || (this.crmCompanyTargetInfoPK != null && !this.crmCompanyTargetInfoPK.equals(other.crmCompanyTargetInfoPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.CompanyTargetInfo[ crmCompanyTargetInfoPK=" + crmCompanyTargetInfoPK + " ]";
    }

}
