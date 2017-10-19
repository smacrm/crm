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
@Table(name = "crm_mail_server")
public class MailServer implements Serializable {
    private static final long serialVersionUID = 603979065588466699L;

    @Column(name = "server_id", generated = true)
    private Integer server_id;
    
    @Column(name = "server_name")
    private String server_name;
    
    @Column(name = "server_host")
    private String server_host;
    
    @Column(name = "server_port")
    private Integer server_port;
    
    @Column(name = "server_smtp")
    private String server_smtp;
    
    @Column(name = "server_smtp_port")
    private Integer server_smtp_port;
    
    @Column(name = "server_auth")
    private String server_auth;
    
    @Column(name = "server_ssl")
    private String server_ssl;
    
    @Column(name = "server_charset")
    private String server_charset;
    
    @Column(name = "server_header")
    private String server_header;
    
    @Column(name = "server_format")
    private String server_format;
    
    @Column(name = "server_same_receive_mail")
    private Boolean server_same_receive_mail;
    
    @Column(name = "server_type")
    private String server_type;
    
    @Column(name = "server_memo")
    private String server_memo;
    
    @Column(name = "server_deleted")
    private Short server_deleted;
    
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

    public Integer getServer_id() {
        return server_id;
    }

    public void setServer_id(Integer server_id) {
        this.server_id = server_id;
    }

    public String getServer_name() {
        return server_name;
    }

    public void setServer_name(String server_name) {
        this.server_name = server_name;
    }

    public String getServer_host() {
        return server_host;
    }

    public void setServer_host(String server_host) {
        this.server_host = server_host;
    }

    public Integer getServer_port() {
        return server_port;
    }

    public void setServer_port(Integer server_port) {
        this.server_port = server_port;
    }

    public String getServer_smtp() {
        return server_smtp;
    }

    public void setServer_smtp(String server_smtp) {
        this.server_smtp = server_smtp;
    }

    public Integer getServer_smtp_port() {
        return server_smtp_port;
    }

    public void setServer_smtp_port(Integer server_smtp_port) {
        this.server_smtp_port = server_smtp_port;
    }

    public String getServer_auth() {
        return server_auth;
    }

    public void setServer_auth(String server_auth) {
        this.server_auth = server_auth;
    }

    public String getServer_ssl() {
        return server_ssl;
    }

    public void setServer_ssl(String server_ssl) {
        this.server_ssl = server_ssl;
    }

    public String getServer_charset() {
        return server_charset;
    }

    public void setServer_charset(String server_charset) {
        this.server_charset = server_charset;
    }

    public String getServer_header() {
        return server_header;
    }

    public void setServer_header(String server_header) {
        this.server_header = server_header;
    }

    public String getServer_format() {
        return server_format;
    }

    public void setServer_format(String server_format) {
        this.server_format = server_format;
    }

    public Boolean getServer_same_receive_mail() {
        return server_same_receive_mail;
    }

    public void setServer_same_receive_mail(Boolean server_same_receive_mail) {
        this.server_same_receive_mail = server_same_receive_mail;
    }

    public String getServer_type() {
        return server_type;
    }

    public void setServer_type(String server_type) {
        this.server_type = server_type;
    }

    public String getServer_memo() {
        return server_memo;
    }

    public void setServer_memo(String server_memo) {
        this.server_memo = server_memo;
    }

    public Short getServer_deleted() {
        return server_deleted;
    }

    public void setServer_deleted(Short server_deleted) {
        this.server_deleted = server_deleted;
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
