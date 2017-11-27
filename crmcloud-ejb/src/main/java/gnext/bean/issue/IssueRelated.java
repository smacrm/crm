/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.issue;

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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.persistence.annotations.AdditionalCriteria;

/**
 *
 * @author gnextadmin
 */
@Entity
@Table(name = "crm_issue_related")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "IssueRelated.findAll", query = "SELECT c FROM IssueRelated c")
    , @NamedQuery(name = "IssueRelated.findRelatedByIssueId", query = " SELECT c FROM IssueRelated c WHERE c.issueId.issueId = :issueId ")
    , @NamedQuery(name = "IssueRelated.findByRelatedIdIssueId", query = " SELECT c FROM IssueRelated c WHERE c.issueId.issueId = :issueId AND c.issueRelatedId.issueId = :issueRelatedId ")
    , @NamedQuery(name = "IssueRelated.deleteAllByIssueId", query = " DELETE FROM IssueRelated c WHERE c.issueId.issueId = :issueId ")
})
@AdditionalCriteria("this.issueRelatedId.issueDeleted = 0")
public class IssueRelated implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "related_id")
    private Integer relatedId;

    @NotNull
    @Getter @Setter
    @JoinColumn(name = "issue_id", referencedColumnName = "issue_id")
    @ManyToOne(optional = false)
    private Issue issueId;

    @Lob
    @Size(max = 65535)
    @Getter @Setter
    @Column(name = "issue_related_comment")
    private String issueRelatedComment;

    @Getter @Setter
    @Column(name = "creator_id")
    private Integer creatorId;

    @Getter @Setter
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Getter @Setter
    @Column(name = "updated_id")
    private Integer updatedId;
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)

    @Getter @Setter
    private Date updatedTime;

    @Getter @Setter
    @JoinColumn(name = "issue_related_id", referencedColumnName = "issue_id")
    @ManyToOne(optional = false)
    private Issue issueRelatedId;

    public IssueRelated() {
    }

    public IssueRelated(Integer relatedId) {
        this.relatedId = relatedId;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (relatedId != null ? relatedId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IssueRelated)) {
            return false;
        }
        IssueRelated other = (IssueRelated) object;
        return !((this.relatedId == null && other.relatedId != null) || (this.relatedId != null && !this.relatedId.equals(other.relatedId)));
    }

    @Override
    public String toString() {
        return "gnext.issue.CrmIssueRelated[ relatedId=" + relatedId + " ]";
    }
}
