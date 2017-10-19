/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import gnext.bean.issue.Issue;
import gnext.utils.AddToElasticSearch;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
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
@Table(name = "crm_mente_item_value")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MenteItemValue.findAll", query = "SELECT c FROM MenteItemValue c")
    , @NamedQuery(name = "MenteItemValue.findByMenteId", query = "SELECT c FROM MenteItemValue c WHERE c.menteId = :menteId")
    , @NamedQuery(name = "MenteItemValue.findByMenteIssueItemName", query = "SELECT c FROM MenteItemValue c WHERE c.menteIssueItemName = :menteIssueItemName")
})
public class MenteItemValue implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "mente_id")
    private Integer menteId;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 70)
    @Getter @Setter
    @Column(name = "mente_issue_item_name")
    @AddToElasticSearch(name = "mente_issue_item_name")
    private String menteIssueItemName;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "mente_issue_field_level")
    @AddToElasticSearch(name = "mente_issue_field_level")
    private Integer menteIssueFieldLevel;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "mente_issue_field_value")
    @AddToElasticSearch(name = "mente_issue_field_value")
    private Integer menteIssueFieldValue;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "company_id")
    private Integer companyId;

    @Getter @Setter
    @JoinColumn(name = "issue_id", referencedColumnName = "issue_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Issue issueId;

    public MenteItemValue() {
    }

    public MenteItemValue(Integer menteId) {
        this.menteId = menteId;
    }

    public MenteItemValue(Integer menteId, String menteIssueItemName, int menteIssueFieldLevel, int menteIssueFieldValue, int companyId) {
        this.menteId = menteId;
        this.menteIssueItemName = menteIssueItemName;
        this.menteIssueFieldLevel = menteIssueFieldLevel;
        this.menteIssueFieldValue = menteIssueFieldValue;
        this.companyId = companyId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (menteId != null ? menteId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MenteItemValue)) {
            return false;
        }
        MenteItemValue other = (MenteItemValue) object;
        return !((this.menteId == null && other.menteId != null) || (this.menteId != null && !this.menteId.equals(other.menteId)));
    }

    @Override
    public String toString() {
        return "gnext.bean.CrmMenteItemValue[ menteId=" + menteId + " ]";
    }

    @Transient
    @Getter @Setter
    @AddToElasticSearch(name = "mente_issue_field_value_name")
    private String menteIssueFieldValueName;
}
