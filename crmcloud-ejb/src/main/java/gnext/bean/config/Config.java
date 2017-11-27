package gnext.bean.config;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
@Cacheable(true)
@Table(name = "crm_config")
@XmlRootElement
public class Config implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "config_id", nullable = false)
    @Setter @Getter
    private Integer configId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "config_key")
    @Setter @Getter
    private String configKey;
    
    @Lob
    @Size(max = 65535)
    @Column(name = "config_value")
    @Setter @Getter
    private String configValue;
    
    @Lob
    @Size(max = 200)
    @Column(name = "config_group")
    @Setter @Getter
    private String configGroup;
    
    @Column(name = "config_type")
    @Setter @Getter
    private Short configType = 0;
    
    @Lob
    @Size(max = 65535)
    @Column(name = "config_note")
    @Setter @Getter
    private String configNote;
    
    @Column(name = "config_deleted")
    @Setter @Getter
    private Short configDeleted;
    
    @Transient
    private String configDeletedLabel;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "creator_id")
    @Setter @Getter
    private int creatorId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date createdTime;
    
    @Column(name = "updated_id")
    @Setter @Getter
    private Integer updatedId;
    
    @Basic(optional = false)
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date updatedTime;

    public Config() {
    }

    public Config(Integer configId) {
        this.configId = configId;
    }

    public Config(Integer configId, String configKey, int creatorId, Date createdTime, Date updatedTime) {
        this.configId = configId;
        this.configKey = configKey;
        this.creatorId = creatorId;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }
    
    public boolean isDeleted(){
        return configDeleted == 1;
    }

    public void setConfigDeletedLabel(String configDeletedLabel) {
        this.configDeletedLabel = configDeletedLabel;
        this.setConfigDeleted(this.configDeletedLabel.equalsIgnoreCase("false") ? (short)1: (short)0);
    }

    public String getConfigDeletedLabel() {
        return this.getConfigDeleted() == 1 ? "false" : "true";
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (configId != null ? configId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Config)) {
            return false;
        }
        Config other = (Config) object;
        if ((this.configId == null && other.configId != null) || (this.configId != null && !this.configId.equals(other.configId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.config.Crm[ configId=" + configId + " ]";
    }
    
}
