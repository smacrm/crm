/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author daind
 */
@Entity
@Table(name = "crm_database_server")
@XmlRootElement
public class DatabaseServer implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int LOCAL_DATA_SOURCE = 1;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @NotNull
    @Column(name = "database_server_id")
    private Integer databaseServerId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "database_server_name")
    private String databaseServerName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "database_server_host")
    private String databaseServerHost;
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
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "database_server_port")
    private String databaseServerPort;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "database_server_driver")
    private String databaseServerDriver;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "database_server_username")
    private String databaseServerUsername;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "database_server_password")
    private String databaseServerPassword;

    public DatabaseServer() { } 

    public Integer getDatabaseServerId() {
        return databaseServerId;
    }

    public void setDatabaseServerId(Integer databaseServerId) {
        this.databaseServerId = databaseServerId;
    }

    public String getDatabaseServerName() {
        return databaseServerName;
    }

    public void setDatabaseServerName(String databaseServerName) {
        this.databaseServerName = databaseServerName;
    }

    public String getDatabaseServerHost() {
        return databaseServerHost;
    }

    public void setDatabaseServerHost(String databaseServerHost) {
        this.databaseServerHost = databaseServerHost;
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

    public String getDatabaseServerPort() {
        return databaseServerPort;
    }

    public void setDatabaseServerPort(String databaseServerPort) {
        this.databaseServerPort = databaseServerPort;
    }

    public String getDatabaseServerDriver() {
        return databaseServerDriver;
    }

    public void setDatabaseServerDriver(String databaseServerDriver) {
        this.databaseServerDriver = databaseServerDriver;
    }

    public String getDatabaseServerUsername() {
        return databaseServerUsername;
    }

    public void setDatabaseServerUsername(String databaseServerUsername) {
        this.databaseServerUsername = databaseServerUsername;
    }

    public String getDatabaseServerPassword() {
        return databaseServerPassword;
    }

    public void setDatabaseServerPassword(String databaseServerPassword) {
        this.databaseServerPassword = databaseServerPassword;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (databaseServerId != null ? databaseServerId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatabaseServer)) {
            return false;
        }
        DatabaseServer other = (DatabaseServer) object;
        if ((this.databaseServerId == null && other.databaseServerId != null)
                || (this.databaseServerId != null && !this.databaseServerId.equals(other.databaseServerId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.DatabaseServer[ id=" + databaseServerId + " ]";
    }
    
}
