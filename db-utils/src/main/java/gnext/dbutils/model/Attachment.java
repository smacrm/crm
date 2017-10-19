/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.model;

import gnext.dbutils.processor.Column;
import gnext.dbutils.processor.Table;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import javax.mail.internet.MimeBodyPart;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
@Table(name = "crm_attachment")
public class Attachment implements Serializable {
    private static final long serialVersionUID = 2680723562198986414L;
    
    @Column(name = "attachment_id", generated = true)
    private Integer attachment_id;
    
    @Column(name = "attachment_name")
    private String attachment_name;
    
    @Column(name = "attachment_hash_name")
    private String attachment_hash_name;
    
    @Column(name = "attachment_extension")
    private String attachment_extension;
    
    @Column(name = "attachment_mime_type")
    private String attachment_mime_type;
    
    @Column(name = "attachment_path")
    private String attachment_path;
    
    @Column(name = "attachment_file_size")
    private String attachment_file_size;
    
    @Column(name = "attachment_deleted")
    private Short attachment_deleted;
    
    @Column(name = "attachment_target_type")
    private Integer attachment_target_type;
    
    @Column(name = "attachment_target_id")
    private Integer attachment_target_id;
    
    @Column(name = "creator_id")
    private int creator_id;
    
    @Column(name = "created_time")
    private Date created_time;
    
    @Column(name = "updated_id")
    private Integer updated_id;
    
    @Column(name = "updated_time")
    private Date updated_time;
    
    @Column(name = "company_id")
    private Integer company_id;
    
    @Column(name = "server_id")
    private Integer server_id;

    @Getter @Setter private MimeBodyPart part; // đọc mail: lưu các part đính kèm.
    @Getter @Setter InputStream inputStream;    // gửi mail: lưu các inputstream.
    
    public Integer getAttachment_id() {
        return attachment_id;
    }

    public void setAttachment_id(Integer attachment_id) {
        this.attachment_id = attachment_id;
    }

    public String getAttachment_name() {
        return attachment_name;
    }

    public void setAttachment_name(String attachment_name) {
        this.attachment_name = attachment_name;
    }

    public String getAttachment_hash_name() {
        return attachment_hash_name;
    }

    public void setAttachment_hash_name(String attachment_hash_name) {
        this.attachment_hash_name = attachment_hash_name;
    }

    public String getAttachment_extension() {
        return attachment_extension;
    }

    public void setAttachment_extension(String attachment_extension) {
        this.attachment_extension = attachment_extension;
    }

    public String getAttachment_mime_type() {
        return attachment_mime_type;
    }

    public void setAttachment_mime_type(String attachment_mime_type) {
        this.attachment_mime_type = attachment_mime_type;
    }

    public String getAttachment_path() {
        return attachment_path;
    }

    public void setAttachment_path(String attachment_path) {
        this.attachment_path = attachment_path;
    }

    public String getAttachment_file_size() {
        return attachment_file_size;
    }

    public void setAttachment_file_size(String attachment_file_size) {
        this.attachment_file_size = attachment_file_size;
    }

    public Short getAttachment_deleted() {
        return attachment_deleted;
    }

    public void setAttachment_deleted(Short attachment_deleted) {
        this.attachment_deleted = attachment_deleted;
    }

    public Integer getAttachment_target_type() {
        return attachment_target_type;
    }

    public void setAttachment_target_type(Integer attachment_target_type) {
        this.attachment_target_type = attachment_target_type;
    }

    public Integer getAttachment_target_id() {
        return attachment_target_id;
    }

    public void setAttachment_target_id(Integer attachment_target_id) {
        this.attachment_target_id = attachment_target_id;
    }

    public int getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(int creator_id) {
        this.creator_id = creator_id;
    }

    public Date getCreated_time() {
        return created_time;
    }

    public void setCreated_time(Date created_time) {
        this.created_time = created_time;
    }

    public Integer getUpdated_id() {
        return updated_id;
    }

    public void setUpdated_id(Integer updated_id) {
        this.updated_id = updated_id;
    }

    public Date getUpdated_time() {
        return updated_time;
    }

    public void setUpdated_time(Date updated_time) {
        this.updated_time = updated_time;
    }

    public Integer getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Integer company_id) {
        this.company_id = company_id;
    }

    public Integer getServer_id() {
        return server_id;
    }

    public void setServer_id(Integer server_id) {
        this.server_id = server_id;
    }
    
}
