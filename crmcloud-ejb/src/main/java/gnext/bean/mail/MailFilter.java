/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.mail;

import gnext.bean.Company;
import java.io.Serializable;
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author daind
 */
@Entity
@Table(name = "crm_mail_filter")
@XmlRootElement
public class MailFilter implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "mail_filter_id")
    private Integer mailFilterId;
    @Size(max = 256)
    @Column(name = "mail_filter_title")
    private String mailFilterTitle;
    @Column(name = "mail_filter_search_type")
    private Short mailFilterSearchType;
    @Lob
    @Size(max = 65535)
    @Column(name = "mail_filter_conditions")
    private String mailFilterConditions;
    @Column(name = "mail_filter_order")
    private Integer mailFilterOrder;
    @Column(name = "mail_filter_move_folder_flag")
    private Short mailFilterMoveFolderFlag;
    @Size(max = 64)
    @Column(name = "mail_filter_move_folder_code")
    private String mailFilterMoveFolderCode;
    @Size(max = 128)
    @Column(name = "mail_filter_move_folder_name")
    private String mailFilterMoveFolderName;
    @Column(name = "mail_filter_make_issue_flag")
    private Boolean mailFilterMakeIssueFlag;
    @Column(name = "mail_filter_select_member_flag")
    private Boolean mailFilterSelectMemberFlag;
    @Column(name = "mail_filter_use_setting_person_flag")
    private Boolean mailFilterUseSettingPersonFlag;
    @Size(max = 64)
    @Column(name = "mail_filter_select_member_code")
    private String mailFilterSelectMemberCode;
    @Size(max = 128)
    @Column(name = "mail_filter_select_member_name")
    private String mailFilterSelectMemberName;
    @Column(name = "mail_filter_use_setting_explode_flag")
    private Short mailFilterUseSettingExplodeFlag;
    @Column(name = "mail_filter_mail_explode_id")
    private Short mailFilterMailExplodeId;
    @Column(name = "mail_filter_deleted")
    private Short mailFilterDeleted;
    @Column(name = "creator_id")
    private Integer creatorId;
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    @Column(name = "updated_id")
    private Integer updatedId;
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    private Company company;

    public MailFilter() {
    }

    public MailFilter(Integer mailFilterId) {
        this.mailFilterId = mailFilterId;
    }

    public Integer getMailFilterId() {
        return mailFilterId;
    }

    public void setMailFilterId(Integer mailFilterId) {
        this.mailFilterId = mailFilterId;
    }

    public String getMailFilterTitle() {
        return mailFilterTitle;
    }

    public void setMailFilterTitle(String mailFilterTitle) {
        this.mailFilterTitle = mailFilterTitle;
    }

    public Short getMailFilterSearchType() {
        return mailFilterSearchType;
    }

    public void setMailFilterSearchType(Short mailFilterSearchType) {
        this.mailFilterSearchType = mailFilterSearchType;
    }

    public String getMailFilterConditions() {
        return mailFilterConditions;
    }

    public void setMailFilterConditions(String mailFilterConditions) {
        this.mailFilterConditions = mailFilterConditions;
    }

    public Integer getMailFilterOrder() {
        return mailFilterOrder;
    }

    public void setMailFilterOrder(Integer mailFilterOrder) {
        this.mailFilterOrder = mailFilterOrder;
    }

    public Short getMailFilterMoveFolderFlag() {
        return mailFilterMoveFolderFlag;
    }

    public void setMailFilterMoveFolderFlag(Short mailFilterMoveFolderFlag) {
        this.mailFilterMoveFolderFlag = mailFilterMoveFolderFlag;
    }

    public String getMailFilterMoveFolderCode() {
        return mailFilterMoveFolderCode;
    }

    public void setMailFilterMoveFolderCode(String mailFilterMoveFolderCode) {
        this.mailFilterMoveFolderCode = mailFilterMoveFolderCode;
    }

    public String getMailFilterMoveFolderName() {
        return mailFilterMoveFolderName;
    }

    public void setMailFilterMoveFolderName(String mailFilterMoveFolderName) {
        this.mailFilterMoveFolderName = mailFilterMoveFolderName;
    }

    public Boolean getMailFilterMakeIssueFlag() {
        return mailFilterMakeIssueFlag;
    }

    public void setMailFilterMakeIssueFlag(Boolean mailFilterMakeIssueFlag) {
        this.mailFilterMakeIssueFlag = mailFilterMakeIssueFlag;
    }

    public Boolean getMailFilterSelectMemberFlag() {
        return mailFilterSelectMemberFlag;
    }

    public void setMailFilterSelectMemberFlag(Boolean mailFilterSelectMemberFlag) {
        this.mailFilterSelectMemberFlag = mailFilterSelectMemberFlag;
    }

    public Boolean getMailFilterUseSettingPersonFlag() {
        return mailFilterUseSettingPersonFlag;
    }

    public void setMailFilterUseSettingPersonFlag(Boolean mailFilterUseSettingPersonFlag) {
        this.mailFilterUseSettingPersonFlag = mailFilterUseSettingPersonFlag;
    }

    public String getMailFilterSelectMemberCode() {
        return mailFilterSelectMemberCode;
    }

    public void setMailFilterSelectMemberCode(String mailFilterSelectMemberCode) {
        this.mailFilterSelectMemberCode = mailFilterSelectMemberCode;
    }

    public String getMailFilterSelectMemberName() {
        return mailFilterSelectMemberName;
    }

    public void setMailFilterSelectMemberName(String mailFilterSelectMemberName) {
        this.mailFilterSelectMemberName = mailFilterSelectMemberName;
    }

    public Short getMailFilterUseSettingExplodeFlag() {
        return mailFilterUseSettingExplodeFlag;
    }

    public void setMailFilterUseSettingExplodeFlag(Short mailFilterUseSettingExplodeFlag) {
        this.mailFilterUseSettingExplodeFlag = mailFilterUseSettingExplodeFlag;
    }

    public Short getMailFilterMailExplodeId() {
        return mailFilterMailExplodeId;
    }

    public void setMailFilterMailExplodeId(Short mailFilterMailExplodeId) {
        this.mailFilterMailExplodeId = mailFilterMailExplodeId;
    }

    public Short getMailFilterDeleted() {
        return mailFilterDeleted;
    }

    public void setMailFilterDeleted(Short mailFilterDeleted) {
        this.mailFilterDeleted = mailFilterDeleted;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (mailFilterId != null ? mailFilterId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MailFilter)) {
            return false;
        }
        MailFilter other = (MailFilter) object;
        if ((this.mailFilterId == null && other.mailFilterId != null) || (this.mailFilterId != null && !this.mailFilterId.equals(other.mailFilterId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.mail.CrmMailFilter[ mailFilterId=" + mailFilterId + " ]";
    }

}
