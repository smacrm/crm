/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.mail;

import gnext.bean.Company;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author daind
 */
@Entity
@Table(name = "crm_mail_folder")
@XmlRootElement
public class MailFolder implements Serializable {
    
    public static final String DATA_MAIL_FOLDER_INBOX = "1";
    public static final String DATA_MAIL_FOLDER_SENT = "2";
    public static final String DATA_MAIL_FOLDER_DRAFT = "3";
    public static final String DATA_MAIL_FOLDER_JUNK = "4";
    public static final String DATA_MAIL_FOLDER_TRASH = "5";

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "mail_folder_id")
    private Integer mailFolderId;
    @Size(max = 100)
    @Column(name = "mail_folder_name")
    private String mailFolderName;
    @Column(name = "mail_folder_order")
    private Integer mailFolderOrder;
    @Column(name = "mail_folder_is_deleted")
    private Short mailFolderIsDeleted;
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

    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    private Company company;

    public MailFolder() {
    }

    public MailFolder(Integer mailFolderId) {
        this.mailFolderId = mailFolderId;
    }

    public MailFolder(Integer mailFolderId, String mailFolderName) {
        this.mailFolderId = mailFolderId;
        this.mailFolderName = mailFolderName;
    }

    public Integer getMailFolderId() {
        return mailFolderId;
    }

    public void setMailFolderId(Integer mailFolderId) {
        this.mailFolderId = mailFolderId;
    }

    public String getMailFolderName() {
        return mailFolderName;
    }

    public void setMailFolderName(String mailFolderName) {
        this.mailFolderName = mailFolderName;
    }

    public Integer getMailFolderOrder() {
        return mailFolderOrder;
    }

    public void setMailFolderOrder(Integer mailFolderOrder) {
        this.mailFolderOrder = mailFolderOrder;
    }

    public Short getMailFolderIsDeleted() {
        return mailFolderIsDeleted;
    }

    public void setMailFolderIsDeleted(Short mailFolderIsDeleted) {
        this.mailFolderIsDeleted = mailFolderIsDeleted;
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (mailFolderId != null ? mailFolderId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MailFolder)) {
            return false;
        }
        MailFolder other = (MailFolder) object;
        if ((this.mailFolderId == null && other.mailFolderId != null) || (this.mailFolderId != null && !this.mailFolderId.equals(other.mailFolderId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.mail.MailFolder[ mailFolderId=" + mailFolderId + " ]";
    }

}
