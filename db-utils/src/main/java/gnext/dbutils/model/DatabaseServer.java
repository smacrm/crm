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
@Table(name = "crm_database_server")
public class DatabaseServer implements Serializable {
    @Column(name = "database_server_id", generated = true)
    private Integer database_server_id;
    
    @Column(name = "database_server_name")
    private String database_server_name;
    
    @Column(name = "database_server_host")
    private String database_server_host;
    
    @Column(name = "creator_id")
    private Integer creator_id;
    
    @Column(name = "created_time")
    private Date created_time;
    
    @Column(name = "updated_id")
    private Integer updated_id;
    
    @Column(name = "updated_time")
    private Date updated_time;
    
    @Column(name = "database_server_port")
    private String database_server_port;
    
    @Column(name = "database_server_driver")
    private String database_server_driver;
    
    @Column(name = "database_server_username")
    private String database_server_username;
    
    @Column(name = "database_server_password")
    private String database_server_password;

    public Integer getDatabase_server_id() {
        return database_server_id;
    }

    public void setDatabase_server_id(Integer database_server_id) {
        this.database_server_id = database_server_id;
    }

    public String getDatabase_server_name() {
        return database_server_name;
    }

    public void setDatabase_server_name(String database_server_name) {
        this.database_server_name = database_server_name;
    }

    public String getDatabase_server_host() {
        return database_server_host;
    }

    public void setDatabase_server_host(String database_server_host) {
        this.database_server_host = database_server_host;
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

    public String getDatabase_server_port() {
        return database_server_port;
    }

    public void setDatabase_server_port(String database_server_port) {
        this.database_server_port = database_server_port;
    }

    public String getDatabase_server_driver() {
        return database_server_driver;
    }

    public void setDatabase_server_driver(String database_server_driver) {
        this.database_server_driver = database_server_driver;
    }

    public String getDatabase_server_username() {
        return database_server_username;
    }

    public void setDatabase_server_username(String database_server_username) {
        this.database_server_username = database_server_username;
    }

    public String getDatabase_server_password() {
        return database_server_password;
    }

    public void setDatabase_server_password(String database_server_password) {
        this.database_server_password = database_server_password;
    }
    
    
}
