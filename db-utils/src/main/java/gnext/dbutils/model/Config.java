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
@Table(name = "crm_config")
public class Config implements Serializable {
    private static final long serialVersionUID = 4620442112766293477L;

    @Column(name = "config_id", generated = true)
    private Integer config_id;
    
    @Column(name = "config_key")
    private String config_key;
    
    @Column(name = "config_value")
    private String config_value;
    
    @Column(name = "config_group")
    private String config_group;
    
    @Column(name = "config_type")
    private Short config_type = 0;
    
    @Column(name = "config_note")
    private String config_note;
    
    @Column(name = "config_deleted")
    private Short config_deleted;
    
    @Column(name = "creator_id")
    private Integer creator_id;
    
    @Column(name = "created_time")
    private Date created_time;
    
    @Column(name = "updated_id")
    private Integer updated_id;
    
    @Column(name = "updated_time")
    private Date updated_time;

    public Integer getConfig_id() {
        return config_id;
    }

    public void setConfig_id(Integer config_id) {
        this.config_id = config_id;
    }

    public String getConfig_key() {
        return config_key;
    }

    public void setConfig_key(String config_key) {
        this.config_key = config_key;
    }

    public String getConfig_value() {
        return config_value;
    }

    public void setConfig_value(String config_value) {
        this.config_value = config_value;
    }

    public String getConfig_group() {
        return config_group;
    }

    public void setConfig_group(String config_group) {
        this.config_group = config_group;
    }

    public Short getConfig_type() {
        return config_type;
    }

    public void setConfig_type(Short config_type) {
        this.config_type = config_type;
    }

    public String getConfig_note() {
        return config_note;
    }

    public void setConfig_note(String config_note) {
        this.config_note = config_note;
    }

    public Short getConfig_deleted() {
        return config_deleted;
    }

    public void setConfig_deleted(Short config_deleted) {
        this.config_deleted = config_deleted;
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

}
