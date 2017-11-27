/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.attachment;

import gnext.bean.Company;
import gnext.dbutils.util.EncryptDecrypt;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
@Entity
@Table(name = "crm_server")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Server.findByCompanyIdAndFlag"
            , query = " SELECT c FROM Server c "
                    + " WHERE c.serverDeleted = :serverDeleted "
                    + " AND c.serverType = :serverType "
                    + " AND c.serverFlag = :serverFlag "
                    + " AND c.company.companyId = :companyId ")
})
public class Server implements Serializable {
    private static final long serialVersionUID = 7591035225677798805L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Setter @Getter
    @Column(name = "server_id")
    private Integer serverId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Setter @Getter
    @Column(name = "server_name")
    private String serverName;
    
    @Size(max = 150)
    @Setter @Getter
    @Column(name = "server_type")
    private String serverType;
    
    @Setter @Getter
    @Column(name = "server_flag")
    private Integer serverFlag;
    
    @Size(max = 150)
    @Setter @Getter
    @Column(name = "server_folder")
    private String serverFolder;
    
    @Size(max = 150)
    @Setter @Getter
    @Column(name = "server_host")
    private String serverHost;
    
    @Setter @Getter
    @Column(name = "server_port")
    private Integer serverPort;
    
    @Size(max = 150)
    @Setter @Getter
    @Column(name = "server_username")
    private String serverUsername;
    
    @Size(max = 150)
    @Column(name = "server_password")
    private String serverPassword;
    
    @Setter @Getter
    @Column(name = "server_ssl")
    private Short serverSsl;
    
    @Column(name = "server_protocol")
    @Setter @Getter
    private String serverProtocol;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "server_deleted")
    @Setter @Getter
    private Short serverDeleted;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "creator_id")
    @Setter @Getter
    private Integer creatorId;
    
    @Basic(optional = false)
    @NotNull
    @Setter @Getter
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    
    @Setter @Getter
    @Column(name = "updated_id")
    private Integer updatedId;
    
    @Setter @Getter
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;
    
    @JoinColumn(name = "company_id",
            referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter private Company company;

    @Column(name = "server_gnext")
    @Setter @Getter private Short serverGnext;
    
    @Column(name = "server_memo")
    @Setter @Getter
    private String serverMemo;

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String p_serverPassword) {
        if(StringUtils.isEmpty(this.serverPassword) && StringUtils.isEmpty(p_serverPassword)) return;
        if(StringUtils.isEmpty(this.serverPassword) && !StringUtils.isEmpty(p_serverPassword)) {
            this.serverPassword = EncryptDecrypt.encrypt(p_serverPassword);
            return;
        }
        if(this.serverPassword.equals(p_serverPassword)) return;
        
        this.serverPassword = EncryptDecrypt.encrypt(p_serverPassword);
    }
    
    public String getDecryptServerPassword() {
        return EncryptDecrypt.decrypt(serverPassword);
    }
    
    public Server() { }
    public Server(Integer serverId) { this.serverId = serverId; }
    public Server(Integer serverId, String serverName, short serverDeleted, int creatorId, Date createdTime) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.serverDeleted = serverDeleted;
        this.creatorId = creatorId;
        this.createdTime = createdTime;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (serverId != null ? serverId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Server)) {
            return false;
        }
        Server other = (Server) object;
        return !((this.serverId == null && other.serverId != null) || (this.serverId != null && !this.serverId.equals(other.serverId)));
    }

    @Override
    public String toString() {
        return "gnext.bean.attachment.Server[ serverId=" + serverId + " ]";
    }
    
}
