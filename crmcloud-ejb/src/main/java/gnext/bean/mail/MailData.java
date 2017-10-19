package gnext.bean.mail;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import gnext.bean.MailAccount;
import gnext.bean.issue.Issue;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author daind
 */
@Entity
@Table(name = "crm_mail_data")
@XmlRootElement
public class MailData  extends BaseEntity {
    private static final long serialVersionUID = -793594377357678694L;
    public static final String[] SEARCH_FIELDS = new String[]{ "mail_data_subject", "mail_data_from", "mail_data_body"};
    public static String getTableName(Company c) { return "crm_mail_data"; }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "mail_data_id")
    private Integer mailDataId;
    
    @Column(name = "mail_data_account_name")
    private String mailDataAccountName;
    
    @Column(name = "mail_data_mail_server")
    private String mailDataMailServer;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "mail_data_unique_id")
    private String mailDataUniqueId;
    
    @Lob
    @Column(name = "mail_data_header")
    private String mailDataHeader;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "mail_data_subject")
    private String mailDataSubject;
    
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "mail_data_from")
    private String mailDataFrom;
    
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "mail_data_to")
    private String mailDataTo;
    
    @Lob
    @Column(name = "mail_data_cc")
    private String mailDataCc;
    
    @Lob
    @Column(name = "mail_data_bcc")
    private String mailDataBcc;
    
    @Basic(optional = false)
    @Column(name = "mail_data_datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date mailDataDatetime;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "mail_data_size")
    private int mailDataSize;
    
    @Column(name = "mail_data_priority")
    private Integer mailDataPriority;
    
    @Lob
    @Column(name = "mail_data_reply_to_address")
    private String mailDataReplyToAddress;
    
    @Lob
    @Column(name = "mail_data_reply_sender_address")
    private String mailDataReplySenderAddress;
    
    @Lob
    @Column(name = "mail_data_reply_return_path")
    private String mailDataReplyReturnPath;
    
    @Basic(optional = false)
    @NotNull
    @Lob
    @Column(name = "mail_data_body")
    private String mailDataBody;
    
    @Lob
    @Column(name = "mail_data_attach_display")
    private String mailDataAttachDisplay;
    
    @Column(name = "mail_data_attachreal_file")
    private String mailDataAttachrealFile;
    
    @Column(name = "mail_data_attach_file_type")
    private String mailDataAttachFileType;
    
    @JoinColumn(name = "mail_data_person_id", referencedColumnName = "mail_person_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    private MailPerson mailPerson; // người phụ trách issue liên quan tới mail này.
    
    @Column(name = "mail_data_folder_code")
    private String mailDataFolderCode;
    
    @Column(name = "mail_data_folder_name")
    private String mailDataFolderName;
    
    @Column(name = "mail_data_issue_relation_flag")
    private Integer mailDataIssueRelationFlag;
    
    @JoinColumn(name = "mail_data_issue_id", referencedColumnName = "issue_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    private Issue issue;
    
    @Column(name = "mail_data_delete_flag")
    private Short mailDataDeleteFlag;
    
    @Column(name = "mail_data_from_standard")
    private String mailDataFromStandard;
    
    @Column(name = "mail_data_is_history")
    private Short mailDataIsHistory;
    
    @Column(name = "mail_data_is_read")
    private Short mailDataIsRead;
    
    @Column(name = "mail_data_mail_explode_id")
    private Integer mailDataMailExplodeId;
    
    @Column(name = "mail_data_mail_sent")
    private Short mailDataMailSent;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "creator_id")
    private int creatorId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    
    @Column(name = "updated_id")
    private Integer updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;

    @JoinColumn(name = "mail_data_account_id", referencedColumnName = "account_id",
            nullable = false, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    private MailAccount mailDataAccount;
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    private Company company;
    
    public MailData() { }
    public MailData(Integer mailDataId) { this.mailDataId = mailDataId; }
    public MailData(Integer mailDataId, String mailDataUniqueId,
            String mailDataSubject, String mailDataFrom, String mailDataTo,
            Date mailDataDatetime, int mailDataSize, String mailDataBody,
            int creatorId, Date createdTime) {
        this.mailDataId = mailDataId;
        this.mailDataUniqueId = mailDataUniqueId;
        this.mailDataSubject = mailDataSubject;
        this.mailDataFrom = mailDataFrom;
        this.mailDataTo = mailDataTo;
        this.mailDataDatetime = mailDataDatetime;
        this.mailDataSize = mailDataSize;
        this.mailDataBody = mailDataBody;
        this.creatorId = creatorId;
        this.createdTime = createdTime;
    }

    public Integer getMailDataId() {
        return mailDataId;
    }

    public void setMailDataId(Integer mailDataId) {
        this.mailDataId = mailDataId;
    }

    public String getMailDataAccountName() {
        return mailDataAccountName;
    }

    public void setMailDataAccountName(String mailDataAccountName) {
        this.mailDataAccountName = mailDataAccountName;
    }

    public String getMailDataMailServer() {
        return mailDataMailServer;
    }

    public void setMailDataMailServer(String mailDataMailServer) {
        this.mailDataMailServer = mailDataMailServer;
    }

    public String getMailDataUniqueId() {
        return mailDataUniqueId;
    }

    public void setMailDataUniqueId(String mailDataUniqueId) {
        this.mailDataUniqueId = mailDataUniqueId;
    }

    public String getMailDataHeader() {
        return mailDataHeader;
    }

    public void setMailDataHeader(String mailDataHeader) {
        this.mailDataHeader = mailDataHeader;
    }

    public String getMailDataSubject() {
        return mailDataSubject;
    }

    public void setMailDataSubject(String mailDataSubject) {
        this.mailDataSubject = mailDataSubject;
    }

    public String getMailDataFrom() {
        return mailDataFrom;
    }

    public void setMailDataFrom(String mailDataFrom) {
        this.mailDataFrom = mailDataFrom;
    }

    public String getMailDataTo() {
        return mailDataTo;
    }

    public void setMailDataTo(String mailDataTo) {
        this.mailDataTo = mailDataTo;
    }

    public String getMailDataCc() {
        return mailDataCc;
    }

    public void setMailDataCc(String mailDataCc) {
        this.mailDataCc = mailDataCc;
    }

    public String getMailDataBcc() {
        return mailDataBcc;
    }

    public void setMailDataBcc(String mailDataBcc) {
        this.mailDataBcc = mailDataBcc;
    }

    public Date getMailDataDatetime() {
        return mailDataDatetime;
    }

    public void setMailDataDatetime(Date mailDataDatetime) {
        this.mailDataDatetime = mailDataDatetime;
    }

    public int getMailDataSize() {
        return mailDataSize;
    }

    public void setMailDataSize(int mailDataSize) {
        this.mailDataSize = mailDataSize;
    }

    public Integer getMailDataPriority() {
        return mailDataPriority;
    }

    public void setMailDataPriority(Integer mailDataPriority) {
        this.mailDataPriority = mailDataPriority;
    }

    public String getMailDataReplyToAddress() {
        return mailDataReplyToAddress;
    }

    public void setMailDataReplyToAddress(String mailDataReplyToAddress) {
        this.mailDataReplyToAddress = mailDataReplyToAddress;
    }

    public String getMailDataReplySenderAddress() {
        return mailDataReplySenderAddress;
    }

    public void setMailDataReplySenderAddress(String mailDataReplySenderAddress) {
        this.mailDataReplySenderAddress = mailDataReplySenderAddress;
    }

    public String getMailDataReplyReturnPath() {
        return mailDataReplyReturnPath;
    }

    public void setMailDataReplyReturnPath(String mailDataReplyReturnPath) {
        this.mailDataReplyReturnPath = mailDataReplyReturnPath;
    }

    public String getMailDataBody() {
        return mailDataBody;
    }

    public void setMailDataBody(String mailDataBody) {
        this.mailDataBody = mailDataBody;
    }

    public String getMailDataAttachDisplay() {
        return mailDataAttachDisplay;
    }

    public void setMailDataAttachDisplay(String mailDataAttachDisplay) {
        this.mailDataAttachDisplay = mailDataAttachDisplay;
    }

    public String getMailDataAttachrealFile() {
        return mailDataAttachrealFile;
    }

    public void setMailDataAttachrealFile(String mailDataAttachrealFile) {
        this.mailDataAttachrealFile = mailDataAttachrealFile;
    }

    public String getMailDataAttachFileType() {
        return mailDataAttachFileType;
    }

    public void setMailDataAttachFileType(String mailDataAttachFileType) {
        this.mailDataAttachFileType = mailDataAttachFileType;
    }

    public MailPerson getMailPerson() {
        return mailPerson;
    }

    public void setMailPerson(MailPerson mailPerson) {
        this.mailPerson = mailPerson;
    }
    
    public String getMailDataFolderCode() {
        return mailDataFolderCode;
    }

    public void setMailDataFolderCode(String mailDataFolderCode) {
        this.mailDataFolderCode = mailDataFolderCode;
    }

    public String getMailDataFolderName() {
        return mailDataFolderName;
    }

    public void setMailDataFolderName(String mailDataFolderName) {
        this.mailDataFolderName = mailDataFolderName;
    }

    public Integer getMailDataIssueRelationFlag() {
        return mailDataIssueRelationFlag;
    }

    public void setMailDataIssueRelationFlag(Integer mailDataIssueRelationFlag) {
        this.mailDataIssueRelationFlag = mailDataIssueRelationFlag;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public Short getMailDataDeleteFlag() {
        return mailDataDeleteFlag;
    }

    public void setMailDataDeleteFlag(Short mailDataDeleteFlag) {
        this.mailDataDeleteFlag = mailDataDeleteFlag;
    }

    public String getMailDataFromStandard() {
        return mailDataFromStandard;
    }

    public void setMailDataFromStandard(String mailDataFromStandard) {
        this.mailDataFromStandard = mailDataFromStandard;
    }

    public Integer getMailDataMailExplodeId() {
        return mailDataMailExplodeId;
    }

    public void setMailDataMailExplodeId(Integer mailDataMailExplodeId) {
        this.mailDataMailExplodeId = mailDataMailExplodeId;
    }

    public Short getMailDataIsHistory() {
        return mailDataIsHistory;
    }

    public void setMailDataIsHistory(Short mailDataIsHistory) {
        this.mailDataIsHistory = mailDataIsHistory;
    }

    public Short getMailDataIsRead() {
        return mailDataIsRead;
    }

    public void setMailDataIsRead(Short mailDataIsRead) {
        this.mailDataIsRead = mailDataIsRead;
    }

    public Short getMailDataMailSent() {
        return mailDataMailSent;
    }

    public void setMailDataMailSent(Short mailDataMailSent) {
        this.mailDataMailSent = mailDataMailSent;
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

    public MailAccount getMailDataAccount() {
        return mailDataAccount;
    }

    public void setMailDataAccount(MailAccount mailDataAccount) {
        this.mailDataAccount = mailDataAccount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (mailDataId != null ? mailDataId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MailData)) {
            return false;
        }
        MailData other = (MailData) object;
        if ((this.mailDataId == null && other.mailDataId != null) || (this.mailDataId != null && !this.mailDataId.equals(other.mailDataId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.CrmMailData[ mailDataId=" + mailDataId + " ]";
    }

}
