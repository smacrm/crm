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
@Table(name = "crm_mail_filter")
public class MailFilter implements Serializable {
    private static final long serialVersionUID = -8882668188286697132L;

    @Column(name = "mail_filter_id", generated = true)
    private Integer mail_filter_id;
    
    @Column(name = "mail_filter_title")
    private String mail_filter_title;
    
    @Column(name = "mail_filter_search_type")
    private Short mail_filter_search_type;
    
    @Column(name = "mail_filter_conditions")
    private String mail_filter_conditions;
    
    @Column(name = "mail_filter_order")
    private Integer mail_filter_order;
    
    // folder
    @Column(name = "mail_filter_move_folder_flag")
    private Short mail_filter_move_folder_flag;
    
    @Column(name = "mail_filter_move_folder_code")
    private String mail_filter_move_folder_code;
    
    @Column(name = "mail_filter_move_folder_name")
    private String mail_filter_move_folder_name;
    
    @Column(name = "mail_filter_make_issue_flag")
    private Boolean mail_filter_make_issue_flag;
    
    @Column(name = "mail_filter_select_member_flag")
    private Boolean mail_filter_select_member_flag;
    
    @Column(name = "mail_filter_use_setting_person_flag")
    private Boolean mail_filter_use_setting_person_flag;
    
    @Column(name = "mail_filter_select_member_code")
    private String mail_filter_select_member_code;
    
    @Column(name = "mail_filter_select_member_name")
    private String mail_filter_select_member_name;
    
    // explode
    @Column(name = "mail_filter_use_setting_explode_flag")
    private Short mail_filter_use_setting_explode_flag;
    
    @Column(name = "mail_filter_mail_explode_id")
    private Short mail_filter_mail_explode_id;
    
    @Column(name = "mail_filter_deleted")
    private Short mail_filter_deleted;
    
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

    public Integer getMail_filter_id() {
        return mail_filter_id;
    }

    public void setMail_filter_id(Integer mail_filter_id) {
        this.mail_filter_id = mail_filter_id;
    }

    public String getMail_filter_title() {
        return mail_filter_title;
    }

    public void setMail_filter_title(String mail_filter_title) {
        this.mail_filter_title = mail_filter_title;
    }

    public Short getMail_filter_search_type() {
        return mail_filter_search_type;
    }

    public void setMail_filter_search_type(Short mail_filter_search_type) {
        this.mail_filter_search_type = mail_filter_search_type;
    }

    public String getMail_filter_conditions() {
        return mail_filter_conditions;
    }

    public void setMail_filter_conditions(String mail_filter_conditions) {
        this.mail_filter_conditions = mail_filter_conditions;
    }

    public Integer getMail_filter_order() {
        return mail_filter_order;
    }

    public void setMail_filter_order(Integer mail_filter_order) {
        this.mail_filter_order = mail_filter_order;
    }

    public Short getMail_filter_move_folder_flag() {
        return mail_filter_move_folder_flag;
    }

    public void setMail_filter_move_folder_flag(Short mail_filter_move_folder_flag) {
        this.mail_filter_move_folder_flag = mail_filter_move_folder_flag;
    }

    public String getMail_filter_move_folder_code() {
        return mail_filter_move_folder_code;
    }

    public void setMail_filter_move_folder_code(String mail_filter_move_folder_code) {
        this.mail_filter_move_folder_code = mail_filter_move_folder_code;
    }

    public String getMail_filter_move_folder_name() {
        return mail_filter_move_folder_name;
    }

    public void setMail_filter_move_folder_name(String mail_filter_move_folder_name) {
        this.mail_filter_move_folder_name = mail_filter_move_folder_name;
    }

    public Boolean getMail_filter_make_issue_flag() {
        return mail_filter_make_issue_flag;
    }

    public void setMail_filter_make_issue_flag(Boolean mail_filter_make_issue_flag) {
        this.mail_filter_make_issue_flag = mail_filter_make_issue_flag;
    }

    public Boolean getMail_filter_select_member_flag() {
        return mail_filter_select_member_flag;
    }

    public void setMail_filter_select_member_flag(Boolean mail_filter_select_member_flag) {
        this.mail_filter_select_member_flag = mail_filter_select_member_flag;
    }

    public Boolean getMail_filter_use_setting_person_flag() {
        return mail_filter_use_setting_person_flag;
    }

    public void setMail_filter_use_setting_person_flag(Boolean mail_filter_use_setting_person_flag) {
        this.mail_filter_use_setting_person_flag = mail_filter_use_setting_person_flag;
    }

    public String getMail_filter_select_member_code() {
        return mail_filter_select_member_code;
    }

    public void setMail_filter_select_member_code(String mail_filter_select_member_code) {
        this.mail_filter_select_member_code = mail_filter_select_member_code;
    }

    public String getMail_filter_select_member_name() {
        return mail_filter_select_member_name;
    }

    public void setMail_filter_select_member_name(String mail_filter_select_member_name) {
        this.mail_filter_select_member_name = mail_filter_select_member_name;
    }

    public Short getMail_filter_use_setting_explode_flag() {
        return mail_filter_use_setting_explode_flag;
    }

    public void setMail_filter_use_setting_explode_flag(Short mail_filter_use_setting_explode_flag) {
        this.mail_filter_use_setting_explode_flag = mail_filter_use_setting_explode_flag;
    }

    public Short getMail_filter_mail_explode_id() {
        return mail_filter_mail_explode_id;
    }

    public void setMail_filter_mail_explode_id(Short mail_filter_mail_explode_id) {
        this.mail_filter_mail_explode_id = mail_filter_mail_explode_id;
    }

    public Short getMail_filter_deleted() {
        return mail_filter_deleted;
    }

    public void setMail_filter_deleted(Short mail_filter_deleted) {
        this.mail_filter_deleted = mail_filter_deleted;
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
