/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.job;

import gnext.bean.Company;
import gnext.bean.Member;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author daind
 */
@Entity
@Cacheable(true)
@Table(name = "crm_command")
public class Command implements Serializable {
    private static final long serialVersionUID = 8293397628378207734L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "command_id")
    private Integer commandId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "command_type")
    private Integer commandType;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "command_name")
    private String commandName;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2000)
    @Column(name = "command_value")
    private String commandValue;
    
    @Basic(optional = false)
    @Size(min = 1, max = 2000)
    @Column(name = "command_json")
    private String commandJson;
    
    @Size(max = 500)
    @Column(name = "command_memo")
    private String commandMemo;
    
    @Column(name = "command_deleted")
    private Short commandDeleted;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id")
    private int companyId;
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Company company;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "creator_id")
    private int creatorId;
    @JoinColumn(name = "creator_id", referencedColumnName = "member_id",
            nullable = true, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Member creator;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    
    @Column(name = "updated_id")
    private Integer updatedId;
    @JoinColumn(name = "updated_id", referencedColumnName = "member_id",
            nullable = true, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Member updator;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;

    public Command() { } 
    public Command(Integer commandId) { this.commandId = commandId; }
    public Command(Integer commandId, int commandType, String commandName,
            String commandValue, int companyId, int creatorId) {
        this.commandId = commandId;
        this.commandType = commandType;
        this.commandName = commandName;
        this.commandValue = commandValue;
        this.companyId = companyId;
        this.creatorId = creatorId;
    }

    public Integer getCommandId() {
        return commandId;
    }

    public void setCommandId(Integer commandId) {
        this.commandId = commandId;
    }

    public Integer getCommandType() {
        return commandType;
    }

    public void setCommandType(Integer commandType) {
        this.commandType = commandType;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandValue() {
        return commandValue;
    }

    public void setCommandValue(String commandValue) {
        this.commandValue = commandValue;
    }

    public String getCommandMemo() {
        return commandMemo;
    }

    public void setCommandMemo(String commandMemo) {
        this.commandMemo = commandMemo;
    }

    public Short getCommandDeleted() {
        return commandDeleted;
    }

    public void setCommandDeleted(Short commandDeleted) {
        this.commandDeleted = commandDeleted;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getUpdatedId() {
        return updatedId;
    }

    public void setUpdatedId(Integer updatedId) {
        this.updatedId = updatedId;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (commandId != null ? commandId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Command)) {
            return false;
        }
        Command other = (Command) object;
        if ((this.commandId == null && other.commandId != null)
                || (this.commandId != null && !this.commandId.equals(other.commandId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.job.Command[ commandId=" + commandId + " ]";
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Member getCreator() {
        return creator;
    }

    public void setCreator(Member creator) {
        this.creator = creator;
    }

    public Member getUpdator() {
        return updator;
    }

    public void setUpdator(Member updator) {
        this.updator = updator;
    }

    public String getCommandJson() {
        return commandJson;
    }

    public void setCommandJson(String commandJson) {
        this.commandJson = commandJson;
    }
    
}
