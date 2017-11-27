/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.model;

import gnext.dbutils.processor.Column;
import gnext.dbutils.processor.Table;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author daind
 */
@Table(name = "crm_mail_folder")
public class MailFolder implements Serializable {
    public static final String DATA_MAIL_FOLDER_INBOX   = "1";
    public static final String DATA_MAIL_FOLDER_SENT    = "2";
    public static final String DATA_MAIL_FOLDER_DRAFT   = "3";
    public static final String DATA_MAIL_FOLDER_JUNK    = "4";
    public static final String DATA_MAIL_FOLDER_TRASH   = "5";
    private static final long serialVersionUID = 4022184351977915060L;
    
    @Column(name = "mail_folder_id", generated = true)
    private Integer mail_folder_id;
    
    @Column(name = "mail_folder_name")
    private String mail_folder_name;
    
    @Column(name = "mail_folder_order")
    private Integer mail_folder_order;
    
    @Column(name = "mail_folder_is_deleted")
    private Short mail_folder_is_deleted;
    
    @Column(name = "creator_id")
    private Integer creator_id;
    
    @Column(name = "created_time")
    private Date created_time;
    
    @Column(name = "updated_id")
    private Integer updated_id;
    
    @Column(name = "updated_time")
    private Date updated_time;
    
    @Column(name = "company_id")
    private Integer company_id;

    public Integer getMail_folder_id() {
        return mail_folder_id;
    }

    public void setMail_folder_id(Integer mail_folder_id) {
        this.mail_folder_id = mail_folder_id;
    }

    public String getMail_folder_name() {
        return mail_folder_name;
    }

    public void setMail_folder_name(String mail_folder_name) {
        this.mail_folder_name = mail_folder_name;
    }

    public Integer getMail_folder_order() {
        return mail_folder_order;
    }

    public void setMail_folder_order(Integer mail_folder_order) {
        this.mail_folder_order = mail_folder_order;
    }

    public Short getMail_folder_is_deleted() {
        return mail_folder_is_deleted;
    }

    public void setMail_folder_is_deleted(Short mail_folder_is_deleted) {
        this.mail_folder_is_deleted = mail_folder_is_deleted;
    }

    public Integer getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Integer creator_id) {
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
    
    
}
