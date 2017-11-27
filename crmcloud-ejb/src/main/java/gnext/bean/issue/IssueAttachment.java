package gnext.bean.issue;

import gnext.bean.Member;
import gnext.bean.attachment.Attachment;
import gnext.bean.mente.MenteItem;
import gnext.bean.softphone.Twilio;
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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Apr 28, 2017
 */
@Entity
@Table(name = "crm_issue_attachment")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "IssueAttachment.findAll", query = "SELECT i FROM IssueAttachment i")})
public class IssueAttachment implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "attachment_id")
    @Setter @Getter private Integer attachmentId;
    
    @JoinColumn(name = "attachment_file_id", referencedColumnName = "attachment_id")
    @ManyToOne(optional = false)
    @Setter @Getter private Attachment attachment;
    
    @JoinColumn(name = "attachment_twilio_id", referencedColumnName = "twilio_id")
    @ManyToOne(optional = false)
    @Setter @Getter private Twilio twilio;
    
    @JoinColumn(name = "attachment_category_id", referencedColumnName = "item_id", nullable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private MenteItem category;
            
    @Column(name = "attachment_share_flag")
    @Setter @Getter private Short attachmentShareFlag;
    
    @Column(name = "attachment_expire_flag")
    @Setter @Getter private Short attachmentExpireFlag;
    
    @Column(name = "attachment_deleted")
    @Setter @Getter private Short attachmentDeleted;
    
    @JoinColumn(name = "creator_id", referencedColumnName = "member_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Member creator;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter private Date createdTime;
    
    @Column(name = "updated_id")
    @Setter @Getter private Integer updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter private Date updatedTime;
    
    @JoinColumn(name = "issue_id", referencedColumnName = "issue_id")
    @ManyToOne(optional = false)
    @Setter @Getter private Issue issue;
    
    @Transient
    @Setter @Getter private Object uploadedFile;
    
    public IssueAttachment() {
    }

    public IssueAttachment(Integer attachmentId) {
        this.attachmentId = attachmentId;
    }

    public Boolean getShareFlag() {
        return this.attachmentShareFlag != null && this.attachmentShareFlag == 1;
    }

    public void setShareFlag(Boolean shareFlag) {
        this.attachmentShareFlag = (short)(shareFlag ? 1: 0);
    }

    public Boolean getExpireFlag() {
        return this.attachmentExpireFlag != null && this.attachmentExpireFlag == 1;
    }

    public void setExpireFlag(Boolean expireFlag) {
        this.attachmentExpireFlag = (short)(expireFlag ? 1: 0);
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (attachmentId != null ? attachmentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IssueAttachment)) {
            return false;
        }
        IssueAttachment other = (IssueAttachment) object;
        if ((this.attachmentId == null && other.attachmentId != null) || (this.attachmentId != null && !this.attachmentId.equals(other.attachmentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.issue.IssueAttachment[ attachmentId=" + attachmentId + " ]";
    }

}
