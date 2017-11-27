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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
@Entity
@Table(name = "crm_mail_explode")
@XmlRootElement
public class MailExplode implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "mail_explode_id")
    private Integer mailExplodeId;
    
    @Column(name = "mail_explode_title")
    private String mailExplodeTitle;
    
    @Column(name = "mail_explode_order")
    private Integer mailExplodeOrder;
    
    @Lob
    @Column(name = "mail_explode_conditions")
    private String mailExplodeConditions;
    
    @Column(name = "mail_explode_creator_id")
    @Setter @Getter private Integer creatorId;
    
    @Column(name = "mail_explode_created_datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date mailExplodeCreatedDatetime;
    
    @Column(name = "mail_explode_updater_id")
    @Setter @Getter private Integer updatedId;
    
    @Column(name = "mail_explode_updated_datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date mailExplodeUpdatedDatetime;
    
    @Column(name = "mail_explode_deleted")
    private Short mailExplodeDeleted;
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    private Company company;

    public MailExplode() {
    }

    public MailExplode(Integer mailExplodeId) {
        this.mailExplodeId = mailExplodeId;
    }

    public Integer getMailExplodeId() {
        return mailExplodeId;
    }

    public void setMailExplodeId(Integer mailExplodeId) {
        this.mailExplodeId = mailExplodeId;
    }

    public String getMailExplodeTitle() {
        return mailExplodeTitle;
    }

    public void setMailExplodeTitle(String mailExplodeTitle) {
        this.mailExplodeTitle = mailExplodeTitle;
    }

    public Integer getMailExplodeOrder() {
        return mailExplodeOrder;
    }

    public void setMailExplodeOrder(Integer mailExplodeOrder) {
        this.mailExplodeOrder = mailExplodeOrder;
    }

    public String getMailExplodeConditions() {
        return mailExplodeConditions;
    }

    public void setMailExplodeConditions(String mailExplodeConditions) {
        this.mailExplodeConditions = mailExplodeConditions;
    }

    public Date getMailExplodeCreatedDatetime() {
        return mailExplodeCreatedDatetime;
    }

    public void setMailExplodeCreatedDatetime(Date mailExplodeCreatedDatetime) {
        this.mailExplodeCreatedDatetime = mailExplodeCreatedDatetime;
    }

    public Date getMailExplodeUpdatedDatetime() {
        return mailExplodeUpdatedDatetime;
    }

    public void setMailExplodeUpdatedDatetime(Date mailExplodeUpdatedDatetime) {
        this.mailExplodeUpdatedDatetime = mailExplodeUpdatedDatetime;
    }

    public Short getMailExplodeDeleted() {
        return mailExplodeDeleted;
    }

    public void setMailExplodeDeleted(Short mailExplodeDeleted) {
        this.mailExplodeDeleted = mailExplodeDeleted;
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
        hash += (mailExplodeId != null ? mailExplodeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MailExplode)) {
            return false;
        }
        MailExplode other = (MailExplode) object;
        if ((this.mailExplodeId == null && other.mailExplodeId != null) || (this.mailExplodeId != null && !this.mailExplodeId.equals(other.mailExplodeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.mail.MailExplode[ mailExplodeId=" + mailExplodeId + " ]";
    }

}
