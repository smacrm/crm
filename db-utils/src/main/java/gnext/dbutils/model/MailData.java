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
@Table(name = "crm_mail_data")
public class MailData implements Serializable {
    private static final long serialVersionUID = 2069557919610978198L;

    @Column(name = "mail_data_id", generated = true)
    private Integer mail_data_id;
    
    @Column(name = "mail_data_account_id")
    private Integer mail_data_account_id;
    
    @Column(name = "mail_data_account_name")
    private String mail_data_account_name;
    
    @Column(name = "mail_data_mail_server")
    private String mail_data_mail_server;
    
    @Column(name = "mail_data_unique_id")
    private String mail_data_unique_id;
    
    @Column(name = "mail_data_header")
    private String mail_data_header;
    
    @Column(name = "mail_data_subject")
    private String mail_data_subject;
    
    @Column(name = "mail_data_from")
    private String mail_data_from;
    
    @Column(name = "mail_data_to")
    private String mail_data_to;
    
    @Column(name = "mail_data_cc")
    private String mail_data_cc;
    
    @Column(name = "mail_data_bcc")
    private String mail_data_bcc;
    
    @Column(name = "mail_data_datetime")
    private Date mail_data_datetime; // The sent datetime.
    
    @Column(name = "mail_data_size")
    private Integer mail_data_size;
    
    @Column(name = "mail_data_priority")
    private Integer mail_data_priority;
    
    @Column(name = "mail_data_reply_to_address")
    private String mail_data_reply_to_address;
    
    @Column(name = "mail_data_reply_sender_address")
    private String mail_data_reply_sender_address;
    
    @Column(name = "mail_data_reply_return_path")
    private String mail_data_reply_return_path;
    
    @Column(name = "mail_data_body")
    private String mail_data_body;
    
    @Column(name = "mail_data_attach_display")
    private String mail_data_attach_display;
    
    @Column(name = "mail_data_attachreal_file")
    private String mail_data_attachreal_file;
    
    @Column(name = "mail_data_attach_file_type")
    private String mail_data_attach_file_type;
    
    @Column(name = "mail_data_person_id")
    private String mail_data_person_id; // mã người phụ trách xử lí issue liên quan tới mail.
    
    @Column(name = "mail_data_folder_code")
    private String mail_data_folder_code;
    
    @Column(name = "mail_data_folder_name")
    private String mail_data_folder_name;
    
    @Column(name = "mail_data_issue_relation_flag")
    private Integer mail_data_issue_relation_flag;
    
    @Column(name = "mail_data_issue_id")
    private String mail_data_issue_id;
    
    @Column(name = "mail_data_delete_flag")
    private Integer mail_data_delete_flag;
    
    @Column(name = "mail_data_from_standard")
    private String mail_data_from_standard;
    
    @Column(name = "mail_data_is_history")
    private Boolean mail_data_is_history;
    
    @Column(name = "mail_data_is_read")
    private Boolean mail_data_is_read;
    
    @Column(name = "mail_data_mail_explode_id")
    private Integer mail_data_mail_explode_id;
    
    @Column(name = "mail_data_mail_sent")
    private Boolean mail_data_mail_sent;
    
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
    
    public Integer getMail_data_id() {
        return mail_data_id;
    }

    public void setMail_data_id(Integer mail_data_id) {
        this.mail_data_id = mail_data_id;
    }

    public String getMail_data_account_name() {
        return mail_data_account_name;
    }

    public void setMail_data_account_name(String mail_data_account_name) {
        this.mail_data_account_name = mail_data_account_name;
    }

    public String getMail_data_mail_server() {
        return mail_data_mail_server;
    }

    public void setMail_data_mail_server(String mail_data_mail_server) {
        this.mail_data_mail_server = mail_data_mail_server;
    }

    public String getMail_data_unique_id() {
        return mail_data_unique_id;
    }

    public void setMail_data_unique_id(String mail_data_unique_id) {
        this.mail_data_unique_id = mail_data_unique_id;
    }

    public String getMail_data_header() {
        return mail_data_header;
    }

    public void setMail_data_header(String mail_data_header) {
        this.mail_data_header = mail_data_header;
    }

    public String getMail_data_subject() {
        return mail_data_subject;
    }

    public void setMail_data_subject(String mail_data_subject) {
        this.mail_data_subject = mail_data_subject;
    }

    public String getMail_data_from() {
        return mail_data_from;
    }
    
    public String getMail_data_from_address(){
        String[] mailAddress = mail_data_from.split("[\\<\\>]");
        if(mailAddress.length != 0) return mailAddress[1];
        return "";
    }
    
    public String getMail_data_from_name(){
        String[] mailAddress = mail_data_from.split("[\\<\\>]");
        if(mailAddress.length != 0) return mailAddress[0];
        return "";
    }

    public void setMail_data_from(String mail_data_from) {
        this.mail_data_from = mail_data_from;
    }

    public String getMail_data_to() {
        return mail_data_to;
    }

    public void setMail_data_to(String mail_data_to) {
        this.mail_data_to = mail_data_to;
    }

    public String getMail_data_cc() {
        return mail_data_cc;
    }

    public void setMail_data_cc(String mail_data_cc) {
        this.mail_data_cc = mail_data_cc;
    }

    public String getMail_data_bcc() {
        return mail_data_bcc;
    }

    public void setMail_data_bcc(String mail_data_bcc) {
        this.mail_data_bcc = mail_data_bcc;
    }

    public Date getMail_data_datetime() {
        return mail_data_datetime;
    }

    public void setMail_data_datetime(Date mail_data_datetime) {
        this.mail_data_datetime = mail_data_datetime;
    }

    public Integer getMail_data_size() {
        return mail_data_size;
    }

    public void setMail_data_size(Integer mail_data_size) {
        this.mail_data_size = mail_data_size;
    }

    public Integer getMail_data_priority() {
        return mail_data_priority;
    }

    public void setMail_data_priority(Integer mail_data_priority) {
        this.mail_data_priority = mail_data_priority;
    }

    public String getMail_data_reply_to_address() {
        return mail_data_reply_to_address;
    }

    public void setMail_data_reply_to_address(String mail_data_reply_to_address) {
        this.mail_data_reply_to_address = mail_data_reply_to_address;
    }

    public String getMail_data_reply_sender_address() {
        return mail_data_reply_sender_address;
    }

    public void setMail_data_reply_sender_address(String mail_data_reply_sender_address) {
        this.mail_data_reply_sender_address = mail_data_reply_sender_address;
    }

    public String getMail_data_reply_return_path() {
        return mail_data_reply_return_path;
    }

    public void setMail_data_reply_return_path(String mail_data_reply_return_path) {
        this.mail_data_reply_return_path = mail_data_reply_return_path;
    }

    public String getMail_data_body() {
        return mail_data_body;
    }

    public void setMail_data_body(String mail_data_body) {
        this.mail_data_body = mail_data_body;
    }

    public String getMail_data_attach_display() {
        return mail_data_attach_display;
    }

    public void setMail_data_attach_display(String mail_data_attach_display) {
        this.mail_data_attach_display = mail_data_attach_display;
    }

    public String getMail_data_attachreal_file() {
        return mail_data_attachreal_file;
    }

    public void setMail_data_attachreal_file(String mail_data_attachreal_file) {
        this.mail_data_attachreal_file = mail_data_attachreal_file;
    }

    public String getMail_data_attach_file_type() {
        return mail_data_attach_file_type;
    }

    public void setMail_data_attach_file_type(String mail_data_attach_file_type) {
        this.mail_data_attach_file_type = mail_data_attach_file_type;
    }

    public String getMail_data_person_id() {
        return mail_data_person_id;
    }

    public void setMail_data_person_id(String mail_data_person_id) {
        this.mail_data_person_id = mail_data_person_id;
    }

//    public String getMail_data_person_code() {
//        return mail_data_person_code;
//    }
//
//    public void setMail_data_person_code(String mail_data_person_code) {
//        this.mail_data_person_code = mail_data_person_code;
//    }
//
//    public String getMail_data_person_name() {
//        return mail_data_person_name;
//    }
//
//    public void setMail_data_person_name(String mail_data_person_name) {
//        this.mail_data_person_name = mail_data_person_name;
//    }

    public String getMail_data_folder_code() {
        return mail_data_folder_code;
    }

    public void setMail_data_folder_code(String mail_data_folder_code) {
        this.mail_data_folder_code = mail_data_folder_code;
    }

    public String getMail_data_folder_name() {
        return mail_data_folder_name;
    }

    public void setMail_data_folder_name(String mail_data_folder_name) {
        this.mail_data_folder_name = mail_data_folder_name;
    }

    public Integer getMail_data_issue_relation_flag() {
        return mail_data_issue_relation_flag;
    }

    public void setMail_data_issue_relation_flag(Integer mail_data_issue_relation_flag) {
        this.mail_data_issue_relation_flag = mail_data_issue_relation_flag;
    }

    public String getMail_data_issue_id() {
        return mail_data_issue_id;
    }

    public void setMail_data_issue_id(String mail_data_issue_id) {
        this.mail_data_issue_id = mail_data_issue_id;
    }

    public Integer getMail_data_delete_flag() {
        return mail_data_delete_flag;
    }

    public void setMail_data_delete_flag(Integer mail_data_delete_flag) {
        this.mail_data_delete_flag = mail_data_delete_flag;
    }

    public String getMail_data_from_standard() {
        return mail_data_from_standard;
    }
    
    public void setMail_data_from_standard(String mail_data_from_standard) {
        this.mail_data_from_standard = mail_data_from_standard;
    }

    public Boolean getMail_data_is_history() {
        return mail_data_is_history;
    }

    public void setMail_data_is_history(Boolean mail_data_is_history) {
        this.mail_data_is_history = mail_data_is_history;
    }

    public Boolean getMail_data_is_read() {
        return mail_data_is_read;
    }

    public void setMail_data_is_read(Boolean mail_data_is_read) {
        this.mail_data_is_read = mail_data_is_read;
    }

    public Integer getMail_data_mail_explode_id() {
        return mail_data_mail_explode_id;
    }

    public void setMail_data_mail_explode_id(Integer mail_data_mail_explode_id) {
        this.mail_data_mail_explode_id = mail_data_mail_explode_id;
    }

    public Boolean getMail_data_mail_sent() {
        return mail_data_mail_sent;
    }

    public void setMail_data_mail_sent(Boolean mail_data_mail_sent) {
        this.mail_data_mail_sent = mail_data_mail_sent;
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

    public Integer getMail_data_account_id() {
        return mail_data_account_id;
    }

    public void setMail_data_account_id(Integer mail_data_account_id) {
        this.mail_data_account_id = mail_data_account_id;
    }

    public Integer getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Integer company_id) {
        this.company_id = company_id;
    }

}
