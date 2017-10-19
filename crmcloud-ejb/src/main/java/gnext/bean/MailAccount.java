/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import gnext.bean.mail.MailServer;
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
@Table(name = "crm_mail_account")
@XmlRootElement
public class MailAccount implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "account_id")
    private Integer accountId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "account_name")
    private String accountName;
    
    @Column(name = "account_send_flag")
    private Short accountSendFlag; // gui mail tu man hinh issue.
    
    @Column(name = "account_receive_flag")
    private Short accountReceiveFlag; // duoc phep nhan mail(mail-client kiem tra neu la true thi nhan mail tu account nay).
    
    @Column(name = "acount_support")
    private Short accountSupport; // gui trong noi bo cong ty.
    
    @Column(name = "acount_request")
    private Short accountRequest; // gui cho khach hang
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 70)
    @Column(name = "account_mail_address")
    private String accountMailAddress;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 70)
    @Column(name = "account_user_name")
    private String accountUserName;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "account_password")
    private String accountPassword;
    
    @Column(name = "account_delete_received_days")
    private Integer accountDeleteReceivedDays;
    
    @Column(name = "account_order")
    private Integer accountOrder;
    
    @Size(max = 500)
    @Column(name = "acount_memo")
    private String accountMemo;
    
    @Column(name = "account_is_deleted")
    private Short accountIsDeleted;
    
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

    @JoinColumn(name = "server_id", referencedColumnName = "server_id",
            nullable = false, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    private MailServer mailServer;

    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    private Company company;

    public MailAccount() {
    }

    public MailAccount(Integer accountId) {
        this.accountId = accountId;
    }

    public MailAccount(MailServer mailServer, Company company) {
        this.mailServer = mailServer;
        this.company = company;
    }

    public MailAccount(Integer accountId, String accountName,
            String accountMailAddress,
            String accountUserName, String accountPassword) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.accountMailAddress = accountMailAddress;
        this.accountUserName = accountUserName;
        this.accountPassword = accountPassword;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Short getAccountSendFlag() {
        return accountSendFlag;
    }

    public void setAccountSendFlag(Short accountSendFlag) {
        this.accountSendFlag = accountSendFlag;
    }

    public Short getAccountReceiveFlag() {
        return accountReceiveFlag;
    }

    public void setAccountReceiveFlag(Short accountReceiveFlag) {
        this.accountReceiveFlag = accountReceiveFlag;
    }

    public String getAccountMailAddress() {
        return accountMailAddress;
    }

    public void setAccountMailAddress(String accountMailAddress) {
        this.accountMailAddress = accountMailAddress;
    }

    public String getAccountUserName() {
        return accountUserName;
    }

    public void setAccountUserName(String accountUserName) {
        this.accountUserName = accountUserName;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    public Integer getAccountDeleteReceivedDays() {
        return accountDeleteReceivedDays;
    }

    public void setAccountDeleteReceivedDays(Integer accountDeleteReceivedDays) {
        this.accountDeleteReceivedDays = accountDeleteReceivedDays;
    }

    public Integer getAccountOrder() {
        return accountOrder;
    }

    public void setAccountOrder(Integer accountOrder) {
        this.accountOrder = accountOrder;
    }

    public String getAccountMemo() {
        return accountMemo;
    }

    public void setAccountMemo(String accountMemo) {
        this.accountMemo = accountMemo;
    }

    public Short getAccountIsDeleted() {
        return accountIsDeleted;
    }

    public void setAccountIsDeleted(Short accountIsDeleted) {
        this.accountIsDeleted = accountIsDeleted;
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

    public Short getAccountSupport() {
        return accountSupport;
    }

    public void setAccountSupport(Short accountSupport) {
        this.accountSupport = accountSupport;
    }

    public Short getAccountRequest() {
        return accountRequest;
    }

    public void setAccountRequest(Short accountRequest) {
        this.accountRequest = accountRequest;
    }

    public MailServer getMailServer() {
        return mailServer;
    }

    public void setMailServer(MailServer mailServer) {
        this.mailServer = mailServer;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (accountId != null ? accountId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MailAccount)) {
            return false;
        }
        MailAccount other = (MailAccount) object;
        if ((this.accountId == null && other.accountId != null) || (this.accountId != null && !this.accountId.equals(other.accountId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.MailAccount[ accountId=" + accountId + " ]";
    }

}
