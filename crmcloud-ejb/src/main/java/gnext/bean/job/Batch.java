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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
@Entity
@Cacheable(true)
@Table(name = "crm_batch")
@XmlRootElement
public class Batch implements Serializable {
    private static final long serialVersionUID = 7907776584114231181L;
    
    public static int RUNNING       = 1;
    public static int PAUSE         = 2;
    public static int NOTSTARTED    = 3;
    public static int FAILED        = 5;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "batch_id")
    private Integer batchId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "batch_name")
    private String batchName;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "batch_group")
    private String batchGroup;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "batch_command")
    private String batchCommand;
    @JoinColumn(name = "batch_command", referencedColumnName = "command_id",
            nullable = true, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Command command;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "batch_cron")
    private String batchCron;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "batch_cron_json")
    private String batchCronJson;
    
    @Basic(optional = false)
    @Column(name = "batch_flag")
    private Short batchFlag;
    @Transient @Setter @Getter private boolean booleanBatchFlag;
    
    @Basic(optional = false)
    @Column(name = "batch_status")
    private Integer batchStatus;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "batch_deleted")
    private Short batchDeleted;
    
    @Basic(optional = false)
    @Column(name = "batch_memo")
    private String batchMemo;
    
    @JoinColumn(name = "creator_id", referencedColumnName = "member_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Member creator;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    
    @JoinColumn(name = "updated_id", referencedColumnName = "member_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Member updator;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter private Company company;
    
    @Basic(optional = false)
    @Column(name = "start_date")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter private Date startDate;
    
    @Basic(optional = false)
    @Column(name = "pause_date")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter private Date pauseDate;
    
    public Batch() { }
    public Batch(Integer batchId) { this.batchId = batchId; }

    public String getBatchMemo() {
        return batchMemo;
    }

    public void setBatchMemo(String batchMemo) {
        this.batchMemo = batchMemo;
    }

    public Integer getBatchStatus() {
        return batchStatus;
    }

    public void setBatchStatus(Integer batchStatus) {
        this.batchStatus = batchStatus;
    }

    public Short getBatchFlag() {
        return batchFlag;
    }

    public void setBatchFlag(Short batchFlag) {
        this.batchFlag = batchFlag;
    }

    public Integer getBatchId() {
        return batchId;
    }

    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getBatchGroup() {
        return batchGroup;
    }

    public void setBatchGroup(String batchGroup) {
        this.batchGroup = batchGroup;
    }

    public String getBatchCommand() {
        return batchCommand;
    }

    public void setBatchCommand(String batchCommand) {
        this.batchCommand = batchCommand;
    }

    public String getBatchCron() {
        return batchCron;
    }

    public void setBatchCron(String batchCron) {
        this.batchCron = batchCron;
    }

    public String getBatchCronJson() {
        return batchCronJson;
    }

    public void setBatchCronJson(String batchCronJson) {
        this.batchCronJson = batchCronJson;
    }

    public Short getBatchDeleted() {
        return batchDeleted;
    }

    public void setBatchDeleted(Short batchDeleted) {
        this.batchDeleted = batchDeleted;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (batchId != null ? batchId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Batch)) {
            return false;
        }
        Batch other = (Batch) object;
        if ((this.batchId == null && other.batchId != null) || (this.batchId != null && !this.batchId.equals(other.batchId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.job.Batch[ batchId=" + batchId + " ]";
    }
    
}
