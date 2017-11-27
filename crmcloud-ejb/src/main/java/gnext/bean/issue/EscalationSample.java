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
import javax.persistence.Lob;
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

/**
 *
 * @author gnextadmin
 */
@Entity
@Table(name = "crm_escalation_sample")
@XmlRootElement
public class EscalationSample implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "sample_id")
    private Integer sampleId;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "sample_type_id")
    private int sampleTypeId;

    @Getter @Setter
    @Column(name = "sample_target_id")
    private Integer sampleTargetId;

    @Size(max = 100)
    @Getter @Setter
    @Column(name = "sample_subject")
    private String sampleSubject;

    @Size(max = 300)
    @Getter @Setter
    @Column(name = "sample_header")
    private String sampleHeader;

    @Basic(optional = false)
//    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Getter @Setter
    @Column(name = "sample_body")
    private String sampleBody;

    @Size(max = 500)
    @Getter @Setter
    @Column(name = "sample_fotter")
    private String sampleFotter;

    @Size(max = 3)
    @Getter @Setter
    @Column(name = "sample_lang")
    private String sampleLang;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "creator_id")
    private int creatorId;

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

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "company_id")
    private Integer companyId;

    public EscalationSample() {
    }

    public EscalationSample(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public EscalationSample(Integer sampleId, int sampleTypeId, String sampleBody, int companyId, int creatorId) {
        this.sampleId = sampleId;
        this.sampleTypeId = sampleTypeId;
        this.sampleBody = sampleBody;
        this.companyId = companyId;
        this.creatorId = creatorId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (sampleId != null ? sampleId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof EscalationSample)) {
            return false;
        }
        EscalationSample other = (EscalationSample) object;
        return !((this.sampleId == null && other.sampleId != null) || (this.sampleId != null && !this.sampleId.equals(other.sampleId)));
    }

    @Override
    public String toString() {
        return "gnext.issue.CrmEscalationSample[ sampleId=" + sampleId + " ]";
    }
}
