/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.issue;

import gnext.bean.Member;
import gnext.bean.attachment.Attachment;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author gnextadmin
 */
@Entity
@Table(name = "crm_escalation")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Escalation.findByEscalationId", query = " SELECT c FROM Escalation c WHERE c.escalationId = :escalationId ")
    ,@NamedQuery(name = "Escalation.findEscalationListByIssueId"
            , query = " SELECT c FROM Escalation c "
                    + " WHERE c.escalationIssueId.issueId = :escalationIssueId "
                    + " AND c.escalationIsDeleted = :escalationIsDeleted "
                    + " AND c.escalationIsSaved <> :escalationIsSaved "
                    + " ORDER BY c.createdTime DESC,c.escalationId DESC ")
    ,@NamedQuery(name = "Escalation.findEscalationByIssueId"
            , query = " SELECT c FROM Escalation c "
                    + " WHERE c.escalationIssueId.issueId = :escalationIssueId "
                    + " AND c.escalationMemberId.memberId = :escalationMemberId "
                    + " AND c.escalationIsSaved = :escalationIsSaved "
                    + " AND c.escalationSendType = :escalationSendType "
                    + " AND c.escalationIsDeleted = :escalationIsDeleted ")
})
public class Escalation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "escalation_id")
    private Integer escalationId;

    @JoinColumn(name = "escalation_member_id", referencedColumnName = "member_id", nullable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private Member escalationMemberId;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "escalation_send_type")
    private short escalationSendType;

    @Transient
    @Setter
    private String escalationSendTypeName;
    public String getEscalationSendTypeName() {
        return String.format("%s%d", "label.escalation_", escalationSendType);
    }

    @Transient
    @Setter
    private String escalationBackground;
    public String getEscalationBackground() {
        return String.format("%s%d", "label.escalationBackground_", escalationSendType);
    }

    @Basic(optional = false)
    @NotNull
    @Size(max = 150)
    @Getter @Setter
    @Column(name = "escalation_from_email")
    private String escalationFromEmail;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Getter @Setter
    @Column(name = "escalation_title")
    private String escalationTitle;
    @Size(max = 300)

    @Getter @Setter
    @Column(name = "escalation_header")
    private String escalationHeader;

    @Lob
    @Size(max = 65535)
    @Getter @Setter
    @Column(name = "escalation_body")
    private String escalationBody;

    @Lob
    @Size(max = 65535)
    @Getter @Setter
    @Column(name = "escalation_body_reply")
    private String escalationBodyReply;

    @Getter @Setter
    @Size(max = 500)
    @Column(name = "escalation_footer")
    private String escalationFooter;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "escalation_send_flag")
    private short escalationSendFlag;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "escalation_send_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date escalationSendDate;

    @Transient
    @Setter
    private String escalationSendDateName;
    public String getEscalationSendDateName() {
        return String.format("%s%d", "label.escalationSendDateName_", escalationSendType);
    }

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "escalation_request_type")
    private short escalationRequestType;

    @Getter @Setter
    @Column(name = "escalation_request_id")
    private Integer escalationRequestId;

    @Getter @Setter
    @Column(name = "escalation_request_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date escalationRequestDate;

    @Getter @Setter
    @Column(name = "escalation_customer_send_flag")
    private Short escalationCustomerSendFlag;

    @Size(max = 100)
    @Getter @Setter
    @Column(name = "escalation_attach_id")
    private String escalationAttachId;

    @Getter @Setter
    @Column(name = "escalation_is_saved")
    private Short escalationIsSaved;

    @Getter @Setter
    @Column(name = "escalation_is_deleted")
    private Short escalationIsDeleted;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "company_id")
    private int companyId;

    @Size(max = 500)
    @Getter @Setter
    @Column(name = "escalation_to_id")
    private String escalationToId;

    @Size(max = 1000)
    @Getter @Setter
    @Column(name = "escalation_to")
    private String escalationTo;

    @Size(max = 1000)
    @Getter @Setter
    @Column(name = "escalation_cc")
    private String escalationCc;

    @Size(max = 1000)
    @Getter @Setter
    @Column(name = "escalation_bcc")
    private String escalationBcc;

    @JoinColumn(name = "creator_id", referencedColumnName = "member_id", nullable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private Member creatorId;

    @Getter @Setter
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Getter @Setter
    @Column(name = "updated_id")
    private Integer updatedId;

    @Getter @Setter
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;

//    @Getter @Setter
//    @Column(name = "escalation_issue_id")
//    private Integer escalationIssueId;

    @Getter @Setter
    @JoinColumn(name = "escalation_issue_id", referencedColumnName = "issue_id", nullable = false)
    @ManyToOne
    private Issue escalationIssueId;

    public Escalation() {
    }

    public Escalation(Integer escalationId) {
        this.escalationId = escalationId;
    }
    
    public Escalation(Integer escalationId, short escalationSendType) {
        this.escalationId = escalationId;
        this.escalationSendType = escalationSendType;
    }

    public Escalation(Integer escalationId
            , short escalationSendType
            , String escalationFromEmail
            , String escalationTitle
            , short escalationSendFlag
            , Date escalationSendDate
            , short escalationRequestType
            , int companyId) {
        this.escalationId = escalationId;
        this.escalationSendType = escalationSendType;
        this.escalationFromEmail = escalationFromEmail;
        this.escalationTitle = escalationTitle;
        this.escalationSendFlag = escalationSendFlag;
        this.escalationSendDate = escalationSendDate;
        this.escalationRequestType = escalationRequestType;
        this.companyId = companyId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (escalationId != null ? escalationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Escalation)) {
            return false;
        }
        Escalation other = (Escalation) object;
        return !((this.escalationId == null && other.escalationId != null) || (this.escalationId != null && !this.escalationId.equals(other.escalationId)));
    }

    @Override
    public String toString() {
        return "gnext.issue.CrmEscalation[ escalationId=" + escalationId + " ]";
    }

    @Transient
    @Getter @Setter
    private List<Attachment> attachs = new ArrayList<>();

    @Transient
    @Getter @Setter
    private String label1;

    @Transient
    @Getter @Setter
    private String label2;
}
