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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_company_user_attachment")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CompanyUserAttachment.findAll", query = "SELECT c FROM CompanyUserAttachment c"),
    @NamedQuery(name = "CompanyUserAttachment.findByAttachmentId", query = "SELECT c FROM CompanyUserAttachment c WHERE c.attachmentId = :attachmentId"),    
    @NamedQuery(name = "CompanyUserAttachment.findByAttachmentTargetId", query = "SELECT c FROM CompanyUserAttachment c WHERE c.attachmentTargetId = :attachmentTargetId"),
})
public class CompanyUserAttachment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "attachment_id", nullable = false)
    private Integer attachmentId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "attachment_name", nullable = false, length = 150)
    private String attachmentName;
    @Size(max = 100)
    @Column(name = "attachment_hash_name", length = 100)
    private String attachmentHashName;
    @Size(max = 10)
    @Column(name = "attachment_extension", length = 10)
    private String attachmentExtension;
    @Size(max = 100)
    @Column(name = "attachment_mime_type", length = 100)
    private String attachmentMimeType;
    @Size(max = 254)
    @Column(name = "attachment_path", length = 254)
    private String attachmentPath;
    @Size(max = 16)
    @Column(name = "attachment_file_size", length = 16)
    private String attachmentFileSize;
    @Column(name = "attachment_target")
    private Short attachmentTarget;
    @Column(name = "attachment_target_id")
    private Integer attachmentTargetId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "attachment_deleted", nullable = false)
    private short attachmentDeleted;
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

    public CompanyUserAttachment() {
    }

    public CompanyUserAttachment(Integer attachmentId) {
        this.attachmentId = attachmentId;
    }

    public CompanyUserAttachment(Integer attachmentId, String attachmentName, short attachmentDeleted) {
        this.attachmentId = attachmentId;
        this.attachmentName = attachmentName;
        this.attachmentDeleted = attachmentDeleted;
    }

    public Integer getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Integer attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentHashName() {
        return attachmentHashName;
    }

    public void setAttachmentHashName(String attachmentHashName) {
        this.attachmentHashName = attachmentHashName;
    }

    public String getAttachmentExtension() {
        return attachmentExtension;
    }

    public void setAttachmentExtension(String attachmentExtension) {
        this.attachmentExtension = attachmentExtension;
    }

    public String getAttachmentMimeType() {
        return attachmentMimeType;
    }

    public void setAttachmentMimeType(String attachmentMimeType) {
        this.attachmentMimeType = attachmentMimeType;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getAttachmentFileSize() {
        return attachmentFileSize;
    }

    public void setAttachmentFileSize(String attachmentFileSize) {
        this.attachmentFileSize = attachmentFileSize;
    }

    public Short getAttachmentTarget() {
        return attachmentTarget;
    }

    public void setAttachmentTarget(Short attachmentTarget) {
        this.attachmentTarget = attachmentTarget;
    }

    public Integer getAttachmentTargetId() {
        return attachmentTargetId;
    }

    public void setAttachmentTargetId(Integer attachmentTargetId) {
        this.attachmentTargetId = attachmentTargetId;
    }

    public short getAttachmentDeleted() {
        return attachmentDeleted;
    }

    public void setAttachmentDeleted(short attachmentDeleted) {
        this.attachmentDeleted = attachmentDeleted;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (attachmentId != null ? attachmentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CompanyUserAttachment)) {
            return false;
        }
        CompanyUserAttachment other = (CompanyUserAttachment) object;
        if ((this.attachmentId == null && other.attachmentId != null) || (this.attachmentId != null && !this.attachmentId.equals(other.attachmentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.CompanyUserAttachment[ attachmentId=" + attachmentId + " ]";
    }
    
}
