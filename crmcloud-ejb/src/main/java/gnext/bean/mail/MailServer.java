/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.mail;

import gnext.bean.Company;
import gnext.bean.Member;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_mail_server")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MailServer.findAll", query = "SELECT c FROM MailServer c"),
    @NamedQuery(name = "MailServer.findByServerId", query = "SELECT c FROM MailServer c WHERE c.serverId = :serverId"),})
public class MailServer implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "server_id")
    private Integer serverId;
    @Size(max = 100)
    @Column(name = "server_name")
    private String serverName;
    @Size(max = 100)
    @Column(name = "server_host")
    private String serverHost;
    @Column(name = "server_port")
    private Integer serverPort;
    @Size(max = 100)
    @Column(name = "server_smtp")
    private String serverSmtp;
    @Column(name = "server_smtp_port")
    private Integer serverSmtpPort;
    @Size(max = 5)
    @Column(name = "server_auth")
    private String serverAuth;
    @Size(max = 5)
    @Column(name = "server_ssl")
    private String serverSsl;
    @Size(max = 255)
    @Column(name = "server_charset")
    private String serverCharset;
    @Size(max = 255)
    @Column(name = "server_header")
    private String serverHeader;
    @Size(max = 255)
    @Column(name = "server_format")
    private String serverFormat;
    @Column(name = "server_same_receive_mail")
    private Boolean serverSameReceiveMail;
    @Size(max = 6)
    @Column(name = "server_type")
    private String serverType;
    @Size(max = 500)
    @Column(name = "server_memo")
    private String serverMemo;
    @Column(name = "server_deleted")
    private Short serverDeleted;
    
    @JoinColumn(name = "creator_id", referencedColumnName = "member_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Member creator;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    
    @JoinColumn(name = "updated_id", referencedColumnName = "member_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Member updator;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;

    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    private Company company;

    public MailServer() {
    }

    public MailServer(Integer serverId) {
        this.serverId = serverId;
    }

    public MailServer(Integer serverId, Company company) {
        this.serverId = serverId;
        this.company = company;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerAuth() {
        return serverAuth;
    }

    public void setServerAuth(String serverAuth) {
        this.serverAuth = serverAuth;
    }

    public String getServerSsl() {
        return serverSsl;
    }

    public void setServerSsl(String serverSsl) {
        this.serverSsl = serverSsl;
    }

    public String getServerCharset() {
        return serverCharset;
    }

    public void setServerCharset(String serverCharset) {
        this.serverCharset = serverCharset;
    }

    public String getServerHeader() {
        return serverHeader;
    }

    public void setServerHeader(String serverHeader) {
        this.serverHeader = serverHeader;
    }

    public String getServerFormat() {
        return serverFormat;
    }

    public void setServerFormat(String serverFormat) {
        this.serverFormat = serverFormat;
    }

    public Boolean getServerSameReceiveMail() {
        return serverSameReceiveMail;
    }

    public void setServerSameReceiveMail(Boolean serverSameReceiveMail) {
        this.serverSameReceiveMail = serverSameReceiveMail;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public Short getServerDeleted() {
        return serverDeleted;
    }

    public void setServerDeleted(Short serverDeleted) {
        this.serverDeleted = serverDeleted;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }


    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getServerMemo() {
        return serverMemo;
    }

    public void setServerMemo(String serverMemo) {
        this.serverMemo = serverMemo;
    }

    public String getServerSmtp() {
        return serverSmtp;
    }

    public void setServerSmtp(String serverSmtp) {
        this.serverSmtp = serverSmtp;
    }

    public Integer getServerSmtpPort() {
        return serverSmtpPort;
    }

    public void setServerSmtpPort(Integer serverSmtpPort) {
        this.serverSmtpPort = serverSmtpPort;
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
        if (!(object instanceof MailServer)) {
            return false;
        }
        MailServer other = (MailServer) object;
        if ((this.serverId == null && other.serverId != null) || (this.serverId != null && !this.serverId.equals(other.serverId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "jp.co.gnext.bean.MailServer[ serverId=" + serverId + " ]";
    }

}
