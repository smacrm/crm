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
@Table(name = "crm_mail_account")
public class MailAccount implements Serializable {
    private static final long serialVersionUID = 5639275697623667868L;

    @Column(name = "account_id", generated = true)
    private Integer account_id;
    
    @Column(name = "account_name")
    private String account_name;
    
    @Column(name = "account_send_flag")
    private Short account_send_flag;
    
    @Column(name = "account_receive_flag")
    private Short account_receive_flag;
    
    @Column(name = "acount_support")
    private Short acount_support;
    
    @Column(name = "acount_request")
    private Short acount_request;
    
    @Column(name = "account_mail_address")
    private String account_mail_address;
    
    @Column(name = "account_user_name")
    private String account_user_name;
    
    @Column(name = "account_password")
    private String account_password;
    
    @Column(name = "account_delete_received_days")
    private Integer account_delete_received_days;
    
    @Column(name = "account_order")
    private Integer account_order;
    
    @Column(name = "acount_memo")
    private String acount_memo;
    
    @Column(name = "account_is_deleted")
    private Short account_is_deleted;
    
    @Column(name = "creator_id")
    private Integer creator_id;
    
    @Column(name = "created_time")
    private Date created_time;
    
    @Column(name = "updated_id")
    private Integer updated_id;
    
    @Column(name = "updated_time")
    private Date updated_time;
    
    @Column(name = "server_id")
    private Integer server_id;
    
    @Column(name = "company_id")
    private Integer company_id;

    public Integer getAccount_id() {
        return account_id;
    }

    public void setAccount_id(Integer account_id) {
        this.account_id = account_id;
    }

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public Short getAccount_send_flag() {
        return account_send_flag;
    }

    public void setAccount_send_flag(Short account_send_flag) {
        this.account_send_flag = account_send_flag;
    }

    public Short getAccount_receive_flag() {
        return account_receive_flag;
    }

    public void setAccount_receive_flag(Short account_receive_flag) {
        this.account_receive_flag = account_receive_flag;
    }

    public Short getAcount_support() {
        return acount_support;
    }

    public void setAcount_support(Short acount_support) {
        this.acount_support = acount_support;
    }

    public Short getAcount_request() {
        return acount_request;
    }

    public void setAcount_request(Short acount_request) {
        this.acount_request = acount_request;
    }

    public String getAccount_mail_address() {
        return account_mail_address;
    }

    public void setAccount_mail_address(String account_mail_address) {
        this.account_mail_address = account_mail_address;
    }

    public String getAccount_user_name() {
        return account_user_name;
    }

    public void setAccount_user_name(String account_user_name) {
        this.account_user_name = account_user_name;
    }

    public String getAccount_password() {
        return account_password;
    }

    public void setAccount_password(String account_password) {
        this.account_password = account_password;
    }

    public Integer getAccount_delete_received_days() {
        return account_delete_received_days;
    }

    public void setAccount_delete_received_days(Integer account_delete_received_days) {
        this.account_delete_received_days = account_delete_received_days;
    }

    public Integer getAccount_order() {
        return account_order;
    }

    public void setAccount_order(Integer account_order) {
        this.account_order = account_order;
    }

    public String getAcount_memo() {
        return acount_memo;
    }

    public void setAcount_memo(String acount_memo) {
        this.acount_memo = acount_memo;
    }

    public Short getAccount_is_deleted() {
        return account_is_deleted;
    }

    public void setAccount_is_deleted(Short account_is_deleted) {
        this.account_is_deleted = account_is_deleted;
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

    public Integer getServer_id() {
        return server_id;
    }

    public void setServer_id(Integer server_id) {
        this.server_id = server_id;
    }

}
