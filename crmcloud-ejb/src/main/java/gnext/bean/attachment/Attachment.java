/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.attachment;

import gnext.bean.Company;
import gnext.utils.StringUtil;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
@Entity
@Cacheable(true)
@Table(name = "crm_attachment")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Attachment.findAttachmentByEscalationId"
            , query = " SELECT DISTINCT c FROM Attachment c "
                    + " WHERE c.attachmentDeleted = :attachmentDeleted "
                    + " AND c.attachmentTargetType = :attachmentTargetType "
                    + " AND c.attachmentTargetId = :attachmentTargetId ")
})
public class Attachment implements Serializable {
    private static final long serialVersionUID = 3016717476086875089L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Setter @Getter
    @Column(name = "attachment_id")
    private Integer attachmentId;
    
    @Basic(optional = false)
    @NotNull
    @Setter @Getter
    @Column(name = "attachment_name")
    private String attachmentName;

    @Transient
    @Setter
    private String attachmentViewName;
    public String getAttachmentViewName() {
        if(this.attachmentName == null) return null;
        return StringUtil.getDownloadFileName(this.attachmentName);
    }

    @Setter @Getter
    @Column(name = "attachment_hash_name")
    private String attachmentHashName;
    
    @Setter @Getter
    @Column(name = "attachment_extension")
    private String attachmentExtension;
    
    @Setter @Getter
    @Column(name = "attachment_mime_type")
    private String attachmentMimeType;
    
    @Setter @Getter
    @Column(name = "attachment_path")
    private String attachmentPath;
    
    @Setter @Getter
    @Column(name = "attachment_file_size")
    private String attachmentFileSize;
    
    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "attachment_deleted")
    private Short attachmentDeleted;
    
    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "attachment_target_type")
    private Integer attachmentTargetType;
    
    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "attachment_target_id")
    private Integer attachmentTargetId;
    
    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "creator_id")
    private int creatorId;
    
    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    
    @Getter @Setter
    @Column(name = "updated_id")
    private Integer updatedId;
    
    @Getter @Setter
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;
    
    @JoinColumn(name = "company_id",
            referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private Company company;
    
    @JoinColumn(name = "server_id",
            referencedColumnName = "server_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private Server server;
    
    public Attachment() { }
    public Attachment(Integer attachmentId) { this.attachmentId = attachmentId; }
    public Attachment(Integer attachmentId, String attachmentName, short attachmentDeleted, int attachmentTargetType, int attachmentTargetId, int creatorId, Date createdTime) {
        this.attachmentId = attachmentId;
        this.attachmentName = attachmentName;
        this.attachmentDeleted = attachmentDeleted;
        this.attachmentTargetType = attachmentTargetType;
        this.attachmentTargetId = attachmentTargetId;
        this.creatorId = creatorId;
        this.createdTime = createdTime;
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
        if (!(object instanceof Attachment)) {
            return false;
        }
        Attachment other = (Attachment) object;
        if ((this.attachmentId == null && other.attachmentId != null) || (this.attachmentId != null && !this.attachmentId.equals(other.attachmentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.attachment.Attachment[ attachmentId=" + attachmentId + " ]";
    }
    
}
