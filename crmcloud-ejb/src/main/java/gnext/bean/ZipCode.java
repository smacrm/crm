package gnext.bean;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_zip_code")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ZipCode.findAll", query = "SELECT c FROM ZipCode c"),
    @NamedQuery(name = "ZipCode.findByCodeId", query = "SELECT c FROM ZipCode c WHERE c.codeId = :codeId"),
    @NamedQuery(name = "ZipCode.findByZipCode", query = "SELECT c FROM ZipCode c WHERE c.zipCode = :zipCode"),    
    @NamedQuery(name = "ZipCode.findByLocaleCode", query = "SELECT c FROM ZipCode c WHERE c.zipCode = :zipCode AND c.localeCode = :localeCode"),
})
public class ZipCode implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "code_id", nullable = false)
    private Integer codeId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 8)
    @Column(name = "zip_code", nullable = false, length = 8)
    private String zipCode;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "city_kana", nullable = false, length = 50)
    private String cityKana;
    @Size(max = 250)
    @Column(name = "address_kana", length = 250)
    private String addressKana;
    @Size(max = 200)
    @Column(name = "district_kana", length = 200)
    private String districtKana;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "city_kannji", nullable = false, length = 50)
    private String cityKannji;
    @Size(max = 250)
    @Column(name = "address_kannji", length = 250)
    private String addressKannji;
    @Size(max = 200)
    @Column(name = "district_kanj", length = 200)
    private String districtKanj;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 3)
    @Column(name = "locale_code", nullable = false, length = 3)
    private String localeCode;
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

    public ZipCode() {
    }

    public ZipCode(Integer codeId) {
        this.codeId = codeId;
    }

    public ZipCode(Integer codeId, String zipCode, String cityKana, String cityKannji, String localeCode) {
        this.codeId = codeId;
        this.zipCode = zipCode;
        this.cityKana = cityKana;
        this.cityKannji = cityKannji;
        this.localeCode = localeCode;
    }

    public Integer getCodeId() {
        return codeId;
    }

    public void setCodeId(Integer codeId) {
        this.codeId = codeId;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCityKana() {
        return cityKana;
    }

    public void setCityKana(String cityKana) {
        this.cityKana = cityKana;
    }

    public String getAddressKana() {
        return addressKana;
    }

    public void setAddressKana(String addressKana) {
        this.addressKana = addressKana;
    }

    public String getDistrictKana() {
        return districtKana;
    }

    public void setDistrictKana(String districtKana) {
        this.districtKana = districtKana;
    }

    public String getCityKannji() {
        return cityKannji;
    }

    public void setCityKannji(String cityKannji) {
        this.cityKannji = cityKannji;
    }

    public String getAddressKannji() {
        return addressKannji;
    }

    public void setAddressKannji(String addressKannji) {
        this.addressKannji = addressKannji;
    }

    public String getDistrictKanj() {
        return districtKanj;
    }

    public void setDistrictKanj(String districtKanj) {
        this.districtKanj = districtKanj;
    }

    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
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
        hash += (codeId != null ? codeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ZipCode)) {
            return false;
        }
        ZipCode other = (ZipCode) object;
        if ((this.codeId == null && other.codeId != null) || (this.codeId != null && !this.codeId.equals(other.codeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.ZipCode[ codeId=" + codeId + " ]";
    }
    
}
