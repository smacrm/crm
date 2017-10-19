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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
@Entity
@Table(name = "crm_mail_person")
public class MailPerson implements Serializable {
    private static final long serialVersionUID = -459102279166984678L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "mail_person_id")
    private Integer mailPersonId;
    
    @JoinColumn(name = "mail_person_in_charge_id", referencedColumnName = "member_id",
            nullable = false, insertable = true, updatable = true)
    @OneToOne(optional = false)
    @Setter @Getter private Member mailPersonInCharge;
    
    @Column(name = "mail_person_order")
    private Integer mailPersonOrder;
    
    @Column(name = "mail_person_target_folder")
    @Getter @Setter private Integer mailPersonTargetFolder;
    
    @Column(name = "mail_person_rechange_folder")
    @Getter @Setter private Short mailPersonIsRechangeFolder; //フォルダに既に張ってる場合は 1：移動する、0：移動しない
    
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
    
    @Column(name = "mail_person_deleted")
    private Short mailPersonIsDeleted;
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    private Company company;
    
    public MailPerson() { }
    public MailPerson(Integer mailPersonId) { this.mailPersonId = mailPersonId; }

    public Integer getMailPersonId() {
        return mailPersonId;
    }

    public void setMailPersonId(Integer mailPersonId) {
        this.mailPersonId = mailPersonId;
    }

    public Integer getMailPersonOrder() {
        return mailPersonOrder;
    }

    public void setMailPersonOrder(Integer mailPersonOrder) {
        this.mailPersonOrder = mailPersonOrder;
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

    public Short getMailPersonIsDeleted() {
        return mailPersonIsDeleted;
    }

    public void setMailPersonIsDeleted(Short mailPersonIsDeleted) {
        this.mailPersonIsDeleted = mailPersonIsDeleted;
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
        hash += (mailPersonId != null ? mailPersonId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MailPerson)) {
            return false;
        }
        MailPerson other = (MailPerson) object;
        if ((this.mailPersonId == null && other.mailPersonId != null)
                || (this.mailPersonId != null && !this.mailPersonId.equals(other.mailPersonId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.mail.MailPerson[ mailPersonId=" + mailPersonId + " ]";
    }
    
}
