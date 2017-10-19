package gnext.bean.mente;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import gnext.bean.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
@Table(name = "crm_mente_item")
@XmlRootElement
public class MenteItem  extends BaseEntity {

    private static final long serialVersionUID = -4462144050958419078L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    @Setter @Getter
    private Integer itemId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 70)
    @Column(name = "item_name")
    @Setter @Getter
    private String itemName;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "item_level")
    @Setter @Getter
    private Integer itemLevel;
    
    @Transient
    @Setter @Getter
    private String itemData;
    
    @Column(name = "item_edit_add_flag")
    @Setter @Getter
    private Boolean itemEditAddFlag = Boolean.FALSE;
    
    @Column(name = "item_search_flag")
    @Setter @Getter
    private Boolean itemSearchFlag = Boolean.FALSE;
    
    @Column(name = "item_show_flag")
    @Setter @Getter
    private Boolean itemShowFlag = Boolean.FALSE;

    @Column(name = "item_risk_sensor")
    @Setter @Getter
    private Boolean itemRiskSensor = Boolean.FALSE;
    
    @Column(name = "item_order")
    @Setter @Getter
    private Integer itemOrder = 1;
    
    @Column(name = "item_deleted")
    @Setter @Getter
    private Boolean itemDeleted = Boolean.FALSE;
    
    @Column(name = "item_status_step")
    @Setter @Getter
    private Integer issueStatusStep = 0;
    
    @Column(name = "item_status_required")
    @Setter @Getter
    private Boolean issueStatusDataRequired = Boolean.FALSE;
    
    @Column(name = "item_status_end_date_required")
    @Setter @Getter
    private Boolean issueStatusEndDateRequired = Boolean.FALSE;
    
    @Transient
    @Setter @Getter
    private Boolean issueStatusAutoMail = Boolean.FALSE;
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;
    
    @Column(name = "creator_id")
    @Setter @Getter
    private Integer creatorId;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date createdTime;
    
    @Column(name = "updated_id")
    @Setter @Getter
    private Integer updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date updatedTime;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "menteItem", orphanRemoval = true)
    @OrderBy("createdTime ASC")
    @Setter @Getter
    private List<MenteOptionDataValue> langs = new ArrayList<>();
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "itemParent")
    @OrderBy("itemLevel ASC, createdTime ASC")
    @Setter @Getter
    private List<MenteItem> itemChilds = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="item_parent_id", referencedColumnName = "item_id")
    @Setter @Getter
    private MenteItem itemParent;

    public MenteItem() {
        
    }
 
     public MenteItem(Member member, String itemName, int level, MenteItem itemParent) {
        if(member != null) {
            Company com = member.getGroup().getCompany();
            Integer mId = member.getMemberId();
            Date date = Calendar.getInstance().getTime();
            this.setItemName(itemName);
            this.setItemLevel(level == 0 ? 1 : level);
            this.setCreatorId(mId);
            this.setCreatedTime(date);
            this.setUpdatedId(mId);
            this.setUpdatedTime(date);
            this.setItemDeleted(false);
            this.setCompany(com);
            this.setItemParent(itemParent);
        }
    }
    
    public MenteItem(Integer itemId){
        this.itemId = itemId;
    }
    
    @Transient
    @Setter
    private String itemViewData;
    public String getItemViewData(String locale){
        if(StringUtils.isBlank(locale) || this.langs == null) return StringUtils.EMPTY;
        for(MenteOptionDataValue item : this.langs){
            if(item == null || !locale.equals(item.getMenteOptionDataValuePK().getItemLanguage())) continue;
            this.setItemData(item.getItemData());
            return item.getItemData();
        }
        return StringUtils.EMPTY;
    }

    @Transient
    @Setter
    private String itemViewTreeName;
    public String getItemViewTreeName(String locale) {
        if(StringUtils.isBlank(locale) || this.langs == null) return StringUtils.EMPTY;
        if(itemLevel == null || itemLevel == 1) return getItemViewData(locale);
        MenteItem m2 = this.itemParent;
        for(MenteOptionDataValue item2 : m2.langs){
            if(item2 == null || !locale.equals(item2.getMenteOptionDataValuePK().getItemLanguage())) continue;
            if(itemLevel == 2) return item2.getItemData() + " -> " + getItemViewData(locale);
            MenteItem m3 = m2.itemParent;
            for(MenteOptionDataValue item3 : m3.langs){
                if(item3 == null || !locale.equals(item3.getMenteOptionDataValuePK().getItemLanguage())) continue;
                return item3.getItemData() + " -> " + item2.getItemData() + " -> " + getItemViewData(locale);
            }
        }
        return getItemViewData(locale);
    }
    
    public boolean isRequired(){
        return Arrays.asList("issue_public_id", "issue_status_id", "issue_proposal_id","cust_cooperation_id","cust_support_method_id","cust_support_class_id","mail_request_id","cust_special_id").contains(this.itemName);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.getItemId() != null ? this.getItemId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MenteItem)) {
            return false;
        }
        MenteItem other = (MenteItem) object;
        return Objects.equals(this.getItemId(), other.getItemId());
    }

    @Override
    public String toString() {
        return Objects.toString(this.getItemId());
    }
    
}
