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
@Table(name = "crm_company")
public class Company implements Serializable {
    private static final long serialVersionUID = -623787263331052843L;

    @Column(name = "company_id", generated = true)
    private Integer company_id;
    
    @Column(name = "company_name")
    private String company_name;
    
    @Column(name = "company_post")
    private String company_post;
    
    @Column(name = "company_city")
    private Integer company_city;
    
    @Column(name = "company_address")
    private String company_address;
    
    @Column(name = "company_address_kana")
    private String company_address_kana;
    
    @Column(name = "company_logo")
    private String company_logo;
    
    @Column(name = "company_copy_right")
    private String company_copy_right;
    
    @Column(name = "company_layout")
    private String company_layout;
    
    @Column(name = "company_global_ip")
    private String company_global_ip;
    
    @Column(name = "company_global_group_flag")
    private Short company_global_group_flag;
    
    @Column(name = "company_union_key")
    private String company_union_key;
    
    @Column(name = "company_business_flag")
    private Short company_business_flag;
    
    @Column(name = "company_home_page")
    private String company_home_page;
    
    @Column(name = "company_global_locale")
    private Short company_global_locale;
    
    @Column(name = "company_seperate_data")
    private Short company_seperate_data;
    
    @Column(name = "company_deleted")
    private Short company_deleted;
    
    @Column(name = "creator_id")
    private Integer creator_id;
    
    @Column(name = "created_time")
    private Date created_time;
    
    @Column(name = "updated_id")
    private Integer updated_id;
    
    @Column(name = "updated_time")
    private Date updated_time;
    
    @Column(name = "company_memo")
    private String company_memo;
    
    @Column(name = "company_basic_login_id")
    private String company_basic_login_id;
    
    @Column(name = "company_basic_password")
    private String company_basic_password;

    public Integer getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Integer company_id) {
        this.company_id = company_id;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getCompany_post() {
        return company_post;
    }

    public void setCompany_post(String company_post) {
        this.company_post = company_post;
    }

    public Integer getCompany_city() {
        return company_city;
    }

    public void setCompany_city(Integer company_city) {
        this.company_city = company_city;
    }

    public String getCompany_address() {
        return company_address;
    }

    public void setCompany_address(String company_address) {
        this.company_address = company_address;
    }

    public String getCompany_address_kana() {
        return company_address_kana;
    }

    public void setCompany_address_kana(String company_address_kana) {
        this.company_address_kana = company_address_kana;
    }

    public String getCompany_logo() {
        return company_logo;
    }

    public void setCompany_logo(String company_logo) {
        this.company_logo = company_logo;
    }

    public String getCompany_copy_right() {
        return company_copy_right;
    }

    public void setCompany_copy_right(String company_copy_right) {
        this.company_copy_right = company_copy_right;
    }

    public String getCompany_layout() {
        return company_layout;
    }

    public void setCompany_layout(String company_layout) {
        this.company_layout = company_layout;
    }

    public String getCompany_global_ip() {
        return company_global_ip;
    }

    public void setCompany_global_ip(String company_global_ip) {
        this.company_global_ip = company_global_ip;
    }

    public Short getCompany_global_group_flag() {
        return company_global_group_flag;
    }

    public void setCompany_global_group_flag(Short company_global_group_flag) {
        this.company_global_group_flag = company_global_group_flag;
    }

    public String getCompany_union_key() {
        return company_union_key;
    }

    public void setCompany_union_key(String company_union_key) {
        this.company_union_key = company_union_key;
    }

    public Short getCompany_business_flag() {
        return company_business_flag;
    }

    public void setCompany_business_flag(Short company_business_flag) {
        this.company_business_flag = company_business_flag;
    }

    public String getCompany_home_page() {
        return company_home_page;
    }

    public void setCompany_home_page(String company_home_page) {
        this.company_home_page = company_home_page;
    }

    public Short getCompany_global_locale() {
        return company_global_locale;
    }

    public void setCompany_global_locale(Short company_global_locale) {
        this.company_global_locale = company_global_locale;
    }

    public Short getCompany_seperate_data() {
        return company_seperate_data;
    }

    public void setCompany_seperate_data(Short company_seperate_data) {
        this.company_seperate_data = company_seperate_data;
    }

    public Short getCompany_deleted() {
        return company_deleted;
    }

    public void setCompany_deleted(Short company_deleted) {
        this.company_deleted = company_deleted;
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

    public String getCompany_memo() {
        return company_memo;
    }

    public void setCompany_memo(String company_memo) {
        this.company_memo = company_memo;
    }

    public String getCompany_basic_login_id() {
        return company_basic_login_id;
    }

    public void setCompany_basic_login_id(String company_basic_login_id) {
        this.company_basic_login_id = company_basic_login_id;
    }

    public String getCompany_basic_password() {
        return company_basic_password;
    }

    public void setCompany_basic_password(String company_basic_password) {
        this.company_basic_password = company_basic_password;
    }

}
