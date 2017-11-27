package gnext.bean.issue;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import gnext.bean.mente.MenteItem;
import gnext.bean.mente.Products;
import gnext.interceptors.annotation.QuickSearchField;
import gnext.utils.InterfaceUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hungpham
 * @since Sep 22, 2017
 */
@Entity
@Table(name = "crm_issue_info")
@NamedQueries({
    @NamedQuery(name = "IssueInfo.findAll", query = "SELECT i FROM IssueInfo i")})
public class IssueInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "info_id")
    @Getter @Setter
    private Integer infoId;
    
    @Transient
    @Getter @Setter
    private String tabIdx = UUID.randomUUID().toString(); // random uuid
    
    @Getter @Setter
    @JoinColumn(name = "issue_id", referencedColumnName = "issue_id")
    @ManyToOne(optional = false)
    private Issue issueId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "tab_order")
    @Getter @Setter
    private short tabOrder;
    
    @Size(max = 500)
    @Column(name = "issue_product_memo")
    @Getter @Setter
    private String issueProductMemo;
    
    @Size(max = 2000)
    @Column(name = "issue_content_ask")
    @Getter @Setter
    private String issueContentAsk;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "info_deleted")
    @Getter @Setter
    private Boolean infoDeleted;
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "creator_id")
    @Getter @Setter
    private int creatorId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date createdTime;
    
    @Column(name = "updated_id")
    @Getter @Setter
    private Integer updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date updatedTime;
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "crm_mente_item_value",
        joinColumns = {
            @JoinColumn(name = "info_id", referencedColumnName = "info_id", nullable = false)
        },
        inverseJoinColumns = {
            @JoinColumn(name = "mente_issue_field_value", referencedColumnName = "item_id", nullable = false),
            @JoinColumn(name = "mente_issue_item_name", referencedColumnName = "item_name", nullable = false),
            @JoinColumn(name = "mente_issue_field_level", referencedColumnName = "item_level", nullable = false),
            @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)
        }
    )
    @OrderBy("createdTime ASC")
    @Setter @Getter
    private List<MenteItem> menteItem = new ArrayList<>();
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "crm_issue_product",
        joinColumns = {
            @JoinColumn(name = "info_id", nullable = false)
        },
        inverseJoinColumns = {
            @JoinColumn(name = "product_id", nullable = false)
        }
    )
    @OrderBy("productsCreatedDatetime ASC")
    @Setter @Getter
    private List<Products> productList = new ArrayList<>();

    @Transient
    private boolean isShortMenteItem = false;
    
    private void sortMenteItem(){
        if(!isShortMenteItem){
            forceSortMenteItem();
            isShortMenteItem = true;
        }
    }
    
    @Transient
    public void forceSortMenteItem(){
        menteItem.sort((i1, i2) -> i1.getItemLevel().compareTo(i2.getItemLevel()));
    }
    
    @Transient
    public List<MenteItem> getIssueLinkedMenteList(String key){
        sortMenteItem();
        List<MenteItem> list = new ArrayList<>();
        menteItem.forEach((item) -> {
            if(key.equals(item.getItemName())){
                list.add(item);
            }
        });
        return list;
    }
    
    @Transient
    public List<String> getIssueLinkedMenteIdList(String key){
        sortMenteItem();
        List<String> list = new ArrayList<>();
        menteItem.forEach((item) -> {
            if(key.equals(item.getItemName())){
                list.add(item.getItemId().toString());
            }
        });
        return list;
    }
    
    @Transient
    public List<String> getIssueLinkedMenteNameList(String key, String locale) {
        sortMenteItem();
        List<String> label = new ArrayList<>();
        menteItem.forEach((item) -> {
            if(key.equals(item.getItemName())){
                label.add(item.getItemViewData(locale));
            }
        });
        return label;
    }
    
    @Transient
    public String getIssueLinkedMenteName(String key, String locale) {
        return getIssueLinkedMenteName(key, locale, " > ");
    }
    
    @Transient
    public String getIssueLinkedMenteName(String key, String locale, String seperator) {
        return StringUtils.join(getIssueLinkedMenteNameList(key, locale), seperator);
    }
    
    @Transient
    public String getIssueProductName(String locale) {
        sortMenteItem();
        List<String> label = getIssueLinkedMenteNameList("issue_product_id", locale);
        if(!productList.isEmpty()){
            label.add(productList.get(0).getProductsName());
        }
        return StringUtils.join(label, " > ");
    }
    
    @Transient
    public List<MenteItem> getKeywords() {
        return getIssueLinkedMenteList("issue_keyword_id");
    }

    @Transient
    public void setKeywords(List<MenteItem> keywords) {
        for (Iterator<MenteItem> iterator = menteItem.iterator(); iterator.hasNext();) {
            MenteItem o = iterator.next();
            if ("issue_keyword_id".equals(o.getItemName())) {
                iterator.remove();
            }
        }
        menteItem.addAll(keywords);
        forceSortMenteItem();
    }

    @Transient
    @Setter
    @QuickSearchField(name = "issue_proposal_level_id")
    private Integer issueProposalLevelId;
    public Integer getIssueProposalLevelId() {
        if(this.getMenteItem()== null || this.getMenteItem().isEmpty()) return null;
        for(MenteItem item:this.getMenteItem()) {
            if(!InterfaceUtil.COLS.PROPOSAL.equals(item.getItemName())
                    || item.getItemLevel() != 1) continue;
            return item.getItemId();
        }
        return null;
    }
    
    @Transient
    public String getProductsName(){
        if(!productList.isEmpty()) return productList.get(0).getProductsName();
        return StringUtils.EMPTY;
    }

    public IssueInfo() {
    }

    public IssueInfo(Integer issueInfoId) {
        this.infoId = issueInfoId;
    }
    
    /**
     * Kiem tra tab co du lieu hay khong truoc khi luu
     * @return 
     */
    public boolean isValid() {
        return !this.getMenteItem().isEmpty() 
                || !this.getProductList().isEmpty() 
                || !this.getKeywords().isEmpty() 
                || !StringUtils.isEmpty(this.getIssueProductMemo()) 
                || !StringUtils.isEmpty(this.getIssueContentAsk());
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (infoId != null ? infoId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IssueInfo)) {
            return false;
        }
        IssueInfo other = (IssueInfo) object;
        if ((this.infoId == null && other.infoId != null) || (this.infoId != null && !this.infoId.equals(other.infoId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.issue.IssueInfo[ infoId=" + infoId + " ]";
    }

}
