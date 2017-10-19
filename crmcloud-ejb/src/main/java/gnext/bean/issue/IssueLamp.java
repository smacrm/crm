package gnext.bean.issue;

import com.google.gson.Gson;
import gnext.bean.Company;
import gnext.utils.LabelValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author gnextadmin
 */
@Entity
// @Cacheable(true)
@Table(name = "crm_issue_lamp")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "IssueLamp.findByCompanyId",
        query = " SELECT c FROM IssueLamp c WHERE c.company.companyId = :companyId"
        ,hints = {
            @QueryHint(name="eclipselink.query-results-cache", value="true"),
            @QueryHint(name="eclipselink.query-results-cache.size", value="100")
        }
    ),
    @NamedQuery(
        name = "IssueLamp.findSameLampColor",
        query = " SELECT c FROM IssueLamp c WHERE c.company.companyId = :companyId AND c.lampId != :lampId AND c.lampColor = :lampColor "
    )
})
public class IssueLamp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "lamp_id")
    private Integer lampId;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "lamp_proposal_id")
    private Integer lampProposalId;

    @Basic(optional = false)
    @NotNull
    @Setter
    @Size(min = 1, max = 20)
    @Column(name = "lamp_color")
    private String lampColor;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "lamp_text_color")
    private String lampTextColor;

    @Basic(optional = false)
    @NotNull
    @Getter @Setter
    @Column(name = "lamp_order")
    private int lampOrder;

    @Getter @Setter
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "crmIssueLamp")
    private List<IssueLampGlobal> issueLampsGlobal = new ArrayList<>();

    @Getter @Setter
    @JoinColumn(name = "company_id", referencedColumnName = "company_id")
    @ManyToOne(optional = false)
    private Company company;

    @Getter @Setter
    @Transient
    private String itemName;

    @Setter
    @Transient
    private String itemViewLampName;
    public String getItemViewLampName(String locale) {
        if(StringUtils.isBlank(locale) || this.issueLampsGlobal == null) return this.itemName;
        for(IssueLampGlobal global:this.issueLampsGlobal) {
            if(global == null || !locale.equals(global.getCrmIssueLampGlobalPK().getItemLang())) continue;
            Map<String, Object> obj = null;
            try {
                obj = new Gson().fromJson(global.getItemName(), Map.class);
            }catch(Exception e){
                System.err.println(e.getMessage());
            }
            if(obj == null) continue;
            return String.valueOf(obj.get("name"));
        }
        return null;
    }

//    @Getter @Setter
//    @Transient
//    private String itemViewName;
//    public String getItemViewName(String locale) {
//        if(StringUtils.isBlank(locale) || this.issueLampsGlobal == null) return this.itemName;
//        for(IssueLampGlobal global:this.issueLampsGlobal) {
//            if(global == null || !locale.equals(global.getCrmIssueLampGlobalPK().getItemLang())) continue;
//            this.setItemName(global.getItemName());
//            return global.getItemName();
//        }
//        return this.itemName;
//    }
    
    @Getter @Setter
    @Transient
    private String locale;
    
    @Getter @Setter
    @Transient
    private String lampProposalName;

    public String getLampColor() {
        int length = lampColor.length();
        return lampColor.substring(lampColor.indexOf("#")+1, length);
    }

    @Getter @Setter
    @Transient
    private List<LabelValue> lampsItemList = new ArrayList<>();

    public IssueLamp() {
    }

    public IssueLamp(Integer lampId) {
        this.lampId = lampId;
    }

    public IssueLamp(Integer lampId, Integer lampProposalId, String lampColor) {
        this.lampId = lampId;
        this.lampProposalId = lampProposalId;
        this.lampColor = lampColor;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (lampId != null ? lampId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IssueLamp)) {
            return false;
        }
        IssueLamp other = (IssueLamp) object;
        return !((this.lampId == null && other.lampId != null) || (this.lampId != null && !this.lampId.equals(other.lampId)));
    }

    @Override
    public String toString() {
        return "gnext.issue.IssueLamp[ lampId=" + lampId + " ]";
    }
}
