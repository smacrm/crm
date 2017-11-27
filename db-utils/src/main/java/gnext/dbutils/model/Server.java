/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.model;

import gnext.dbutils.processor.Column;
import gnext.dbutils.processor.Table;
import gnext.dbutils.util.EncryptDecrypt;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author daind
 */
@Table(name = "crm_server")
public class Server implements Serializable {
    private static final long serialVersionUID = -3869872200297051575L;

    @Column(name = "server_id", generated = true)
    private Integer server_id;
    
    @Column(name = "server_name")
    private String server_name;
    
    @Column(name = "server_type")
    private String server_type;
    
    @Column(name = "server_flag")
    private Integer server_flag;
    
    @Column(name = "server_folder")
    private String server_folder;
    
    @Column(name = "server_host")
    private String server_host;
    
    @Column(name = "server_port")
    private Integer server_port;
    
    @Column(name = "server_username")
    private String server_username;
    
    @Column(name = "server_password")
    private String server_password;
    
    @Column(name = "server_ssl")
    private Short server_ssl;
    
    @Column(name = "server_protocol")
    private String server_protocol;
    
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
    
    @Column(name = "server_gnext")
    private Short server_gnext;

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

    public String getServer_type() {
        return server_type;
    }

    public void setServer_type(String server_type) {
        this.server_type = server_type;
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

    public String getServer_username() {
        return server_username;
    }

    public void setServer_username(String server_username) {
        this.server_username = server_username;
    }

    public String getDecryptServerPassword() {
        return EncryptDecrypt.decrypt(server_password);
    }
    
    public String getServer_password() {
        return server_password;
    }

    public void setServer_password(String server_password) {
        this.server_password = server_password;
    }

    public Short getServer_ssl() {
        return server_ssl;
    }

    public void setServer_ssl(Short server_ssl) {
        this.server_ssl = server_ssl;
    }

    public String getServer_protocol() {
        return server_protocol;
    }

    public void setServer_protocol(String server_protocol) {
        this.server_protocol = server_protocol;
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

    public Integer getServer_flag() {
        return server_flag;
    }

    public void setServer_flag(Integer server_flag) {
        this.server_flag = server_flag;
    }

    public String getServer_folder() {
        return server_folder;
    }

    public void setServer_folder(String server_folder) {
        this.server_folder = server_folder;
    }

    public Short getServer_gnext() {
        return server_gnext;
    }

    public void setServer_gnext(Short server_gnext) {
        this.server_gnext = server_gnext;
    }
    
}
