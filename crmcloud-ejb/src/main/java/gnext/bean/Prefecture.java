/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import gnext.interceptors.annotation.QuickSearchField;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
@Entity
@Table(name = "crm_prefecture")
@XmlRootElement
@NamedQueries({})
public class Prefecture implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "prefecture_id")
    @SequenceGenerator(name = "seq_prefectureId", sequenceName = "seq_prefectureId")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    @Setter @Getter
    private Integer prefectureId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 3)
    @Column(name = "prefecture_locale_code")
    @Setter @Getter
    private String prefectureLocaleCode;
    
    @Size(max = 32)
    @Column(name = "prefecture_code")
    @Getter @Setter
    private String prefectureCode;
    
    @Size(max = 64)
    @Column(name = "prefecture_name")
    @Getter @Setter
    @QuickSearchField(name = "prefecture_name")
    private String prefectureName;
    
    @Column(name = "prefecture_order")
    @Getter @Setter
    private Integer prefectureOrder;
    
    @Size(max = 32)
    @Column(name = "prefecture_area_code")
    @Getter @Setter
    private String prefectureAreaCode;
    
    @Size(max = 64)
    @Column(name = "prefecture_area_name")
    @Getter @Setter
    @QuickSearchField(name = "prefecture_area_name")
    private String prefectureAreaName;
    
    @Column(name = "prefecture_area_order")
    @Getter @Setter
    private Integer prefectureAreaOrder;
    
    @Column(name = "prefecture_is_deleted")
    @Getter @Setter
    private short prefectureIsDeleted;
    
    @Column(name = "creator_id")
    @Getter @Setter
    private Integer creatorId;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date createdTime;
    
    @Column(name = "updated_id")
    @Getter @Setter
    private Integer updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date updatedTime;

    public Prefecture() {
    }

    public Prefecture(Integer prefectureId, String prefectureCode, String prefectureName) {
        this.prefectureId = prefectureId;
        this.prefectureCode = prefectureCode;
        this.prefectureName = prefectureName;
    }
    
    public Prefecture(Integer prefectureId, Short prefectureCode, String prefectureName) {
        this.prefectureId = prefectureId;
        this.prefectureCode = String.valueOf(prefectureCode);
        this.prefectureName = prefectureName;
    }
    
    public Prefecture(String prefectureCode, String prefectureName) {
        this.prefectureCode = prefectureCode;
        this.prefectureName = prefectureName;
    }
    
    public Prefecture(Short prefectureCode, String prefectureName) {
        this.prefectureCode = String.valueOf(prefectureCode);
        this.prefectureName = prefectureName;
    }
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (prefectureId != null ? prefectureId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Prefecture)) {
            return false;
        }
        Prefecture other = (Prefecture) object;
        if ((this.prefectureId == null && other.prefectureId != null) || (this.prefectureId != null && !this.prefectureId.equals(other.prefectureId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return prefectureId.toString();
    }

}
