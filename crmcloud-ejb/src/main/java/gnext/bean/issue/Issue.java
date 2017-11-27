package gnext.bean.issue;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import gnext.bean.mail.MailData;
import gnext.bean.Member;
import gnext.bean.mente.MenteItem;
import gnext.interceptors.annotation.QuickSearchDb;
import gnext.interceptors.annotation.QuickSearchField;
import gnext.interceptors.annotation.enums.Module;
import gnext.utils.InterfaceUtil;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.QueryHint;
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
@Table(name = "crm_issue")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Issue.findAll", query = "SELECT c FROM Issue c")
    , @NamedQuery(
        name = "Issue.findByIssueId", 
        query = "SELECT c FROM Issue c WHERE c.issueId = :issueId AND c.issueDeleted = :issueDeleted ",
            hints = {
            @QueryHint(name="eclipselink.query-results-cache", value="true"),
            @QueryHint(name="eclipselink.query-results-cache.size", value="200")
        })
    , @NamedQuery(
        name = "Issue.findByIssueIdList", 
        query = "SELECT c FROM Issue c WHERE c.issueId IN :issueIdList AND c.issueDeleted = :issueDeleted ",
            hints = {
            @QueryHint(name="eclipselink.query-results-cache", value="true"),
            @QueryHint(name="eclipselink.query-results-cache.size", value="200")
        })
    , @NamedQuery(
            name = "Issue.findByIssueViewCode", 
            query = "SELECT c FROM Issue c WHERE c.company.companyId = :companyId AND c.issueViewCode = :issueViewCode AND c.issueDeleted = :issueDeleted ",
            hints = {
            @QueryHint(name="eclipselink.query-results-cache", value="true"),
            @QueryHint(name="eclipselink.query-results-cache.size", value="200")
        })
    , @NamedQuery(
            name = "Issue.findByIssueViewCodeLike", 
            query = "SELECT c FROM Issue c WHERE c.company.companyId = :companyId AND c.issueViewCode like :code AND c.issueDeleted = :issueDeleted ",
            hints = {
            @QueryHint(name="eclipselink.query-results-cache", value="true"),
            @QueryHint(name="eclipselink.query-results-cache.size", value="200")
        })
})
@QuickSearchDb(module = Module.ISSUE, fieldTargetId = "issueId", disable = false)
public class Issue extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "issue_id")
    @QuickSearchField(name = "issue_id")
    private Integer issueId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "issue_code")
    @Getter @Setter private int issueCode;
    

    @Getter @Setter
    @Size(max = 20)
    @Column(name = "issue_view_code")
    @QuickSearchField(name = "issue_view_code")
    private String issueViewCode;

    @Getter @Setter
    @JoinColumn(name = "issue_receive_id", referencedColumnName = "item_id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @QuickSearchField(name = "issue_receive_id")
    private MenteItem issueReceiveId; //Phuong thuc nhan issue, email, fax, phone, ...

    @Getter @Setter
    @JoinColumn(name = "issue_authorizer_id", referencedColumnName = "member_id")
    @ManyToOne(optional = false)
    private Member issueAuthorizerId;

    @JoinColumn(name = "issue_receive_person_id", referencedColumnName = "member_id")
    @ManyToOne(optional = false)
    @Setter @Getter 
    private Member issueReceivePerson;

    @Getter @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issue_receive_date")
    @QuickSearchField(name = "issue_receive_date")
    private Date issueReceiveDate;

    @Getter @Setter
    @JoinColumn(name = "issue_status_id", referencedColumnName = "item_id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @QuickSearchField(name = "issue_status_id")
    private MenteItem issueStatusId;

    @Getter @Setter
    @JoinColumn(name = "issue_public_id", referencedColumnName = "item_id")
    @ManyToOne(optional = false, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @QuickSearchField(name = "issue_public_id")
    private MenteItem issuePublicId;

    @Getter @Setter
    @Column(name = "issue_business_status")
    private Boolean issueBusinessStatus;

    @Getter @Setter
    @Column(name = "issue_closed_date")
    @Temporal(TemporalType.TIMESTAMP)
    @QuickSearchField(name = "issue_closed_date")
    private Date issueClosedDate;

//    @Size(max = 500)
//    @Getter @Setter
//    @Column(name = "issue_product_memo")
//    @QuickSearchField(name = "issue_product_memo")
//    private String issueProductMemo;
//
//    @Size(max = 2000)
//    @Getter @Setter
//    @Column(name = "issue_content_ask")
//    @QuickSearchField(name = "issue_content_ask")
//    private String issueContentAsk;

    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;

    @Size(max = 50)
    @Getter @Setter
    @Column(name = "issue_row_id")
    @QuickSearchField(name = "issue_row_id")
    private String issueRowId;
    
    @Column(name = "issue_deleted")
    @Getter @Setter private Short issueDeleted;

    @Getter @Setter
    @JoinColumn(name = "creator_id", referencedColumnName = "member_id")
    @ManyToOne(optional = false)
    private Member creatorId;

    @Getter @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time")
    @QuickSearchField(name = "issue_created_time")
    private Date createdTime;

    @Getter @Setter
    @JoinColumn(name = "updated_id", referencedColumnName = "member_id")
    @ManyToOne(optional = false)
    private Member updatedId;

    @Getter @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_time")
    @QuickSearchField(name = "issue_updated_time")
    private Date updatedTime;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "crm_issue_cust_rel",
        joinColumns = {
            @JoinColumn(name = "issue_id", nullable = false)
        },
        inverseJoinColumns = {
            @JoinColumn(name = "cust_id", nullable = false)
        }
    )
    @OrderBy("createdTime ASC")
    @Setter
    private List<Customer> customerList = new ArrayList<>();
    
//    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinTable(name = "crm_mente_item_value",
//        joinColumns = {
//            @JoinColumn(name = "issue_id", referencedColumnName = "issue_id", nullable = false)
//        },
//        inverseJoinColumns = {
//            @JoinColumn(name = "mente_issue_field_value", referencedColumnName = "item_id", nullable = false),
//            @JoinColumn(name = "mente_issue_item_name", referencedColumnName = "item_name", nullable = false),
//            @JoinColumn(name = "mente_issue_field_level", referencedColumnName = "item_level", nullable = false),
//            @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)
//        }
//    )
//    @OrderBy("createdTime ASC")
//    @Setter @Getter
//    private List<MenteItem> menteItem = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "issueId", orphanRemoval = true)
    @OrderBy("tabOrder ASC")
    @Setter
    private List<IssueInfo> issueInfoList = new ArrayList<>();
    
//    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinTable(name = "crm_issue_product",
//        joinColumns = {
//            @JoinColumn(name = "issue_id", nullable = false)
//        },
//        inverseJoinColumns = {
//            @JoinColumn(name = "product_id", nullable = false)
//        }
//    )
//    @OrderBy("productsCreatedDatetime ASC")
//    @Setter @Getter
//    private List<Products> productList = new ArrayList<>();
//
//    @Transient
//    private boolean isShortMenteItem = false;
    
    @Transient
    @Getter @Setter
    private String riskSensorOneMonth;
    
    @Transient
    @Getter @Setter
    private List<Integer> riskSensorOneMonthList = new ArrayList<>();
    
    @Getter @Setter
    @Transient
    private MailData mailData;

    @Transient
    @Getter @Setter
    private String riskSensorThreeMonth;
    
    @Transient
    @Getter @Setter
    private List<Integer> riskSensorThreeMonthList = new ArrayList<>();

    @Transient
    @Getter @Setter
    private String issueActiveTab;

    @Transient
    @Getter @Setter
    private String issueCustActiveTab;

    @Transient
    @Setter @Getter
    private List<Integer> delCustomerList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "issueId")
    @OrderBy("createdTime ASC")
    @Getter @Setter
    private List<IssueRelated> relatedCases = new ArrayList<>();

    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "todoIssueId")
    @OrderBy("createdTime ASC")
    @Getter @Setter
    private List<IssueTodo> issueTodoList = new ArrayList<>();
    
    @Getter @Setter
    @OneToMany(mappedBy = "escalationIssueId")
    private List<Escalation> escalationList = new ArrayList<>();
    
    @Getter @Setter
    @OneToMany(mappedBy = "issueId")
    @OrderBy("updatedTime DESC")
    private List<IssueStatusHistory> statusHistory = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "issue", orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("createdTime ASC")
    @Getter @Setter
    private List<IssueAttachment> issueAttachmentList = new ArrayList<>();
    
    //Luu tru data mapping theo dang itemId -> itemValue cho customize nam trong issue
    @Transient
    @Getter @Setter
    private Map<Integer, String> customizeDataMapping = new HashMap<>();
    
    @Transient
    @Getter @Setter
    private String twilioCallSid; //Luu tru thong tin tam thoi cua callId doi voi issue duoc tao tu cuoc goi den
    
    @Transient
    @Getter @Setter
    private Integer duplicateId; //Luu thong tin flag, object co phai la duplicate hay khong, neu co thi duplicate tu ID nao
    
    @Transient
    @Getter @Setter
    private String source = "manual"; //Luu thong tin xac nhan Issue duoc tao tu man hinh (manual), hay la tu phan import (auto)
    
    @Transient
    @Getter @Setter
    private List<Issue> historyCustomers = new ArrayList<>(); // Luu thong tin lich su cua khach hang trong issue
    
    @Transient
    @Getter @Setter 
    private List<Customer> historySpecials = new ArrayList<>(); // Luu thong tin danh sach khach hang dac biet lien quan

    @Transient
    @Getter @Setter 
    private List<Escalation> escalations = new ArrayList<>(); // Luu thong tin comment cua issue
    
    @Transient
    @Getter @Setter 
    private String lampColor;

    @Transient
    @Getter @Setter 
    private String lampTextColor;

    public Issue() {
        this.customerList = new ArrayList<>();
    }

    public Issue(Member member) {
        if(member != null) {
            Company com = member.getGroup().getCompany();
            Integer mId = member.getMemberId();
            Date date = Calendar.getInstance().getTime();
            this.setIssueCustActiveTab("#tabViewIssue:tabViewIssueBase");
            this.setIssueCustActiveTab("#tabViewCustomer:cust_1");
            this.setIssueReceiveDate(Calendar.getInstance().getTime());
            this.setCompany(com);
            this.setIssueReceivePerson(member);
            this.setCreatorId(member);
            this.setCreatedTime(date);
            this.setUpdatedId(member);
            this.setUpdatedTime(date);

            this.issueInfoList = new ArrayList<>();
            IssueInfo info = new IssueInfo();
            info.setCompany(com);
            info.setTabOrder((short)0);
            info.setInfoDeleted(InterfaceUtil.EXISTS);
            info.setCreatorId(mId);
            info.setCreatedTime(date);
            info.setUpdatedId(mId);
            info.setUpdatedTime(date);
            this.issueInfoList.add(info);

            this.customerList = new ArrayList<>();
            Customer cust = new Customer();
            cust.setCompany(com);
            cust.setCustDeleted(InterfaceUtil.EXISTS);
            cust.setCreatorId(mId);
            cust.setCreatedTime(date);
            cust.setUpdatedId(mId);
            cust.setUpdatedTime(date);
            this.customerList.add(cust);
        }
    }

    public Issue(Integer issueId) {
        this.customerList = new ArrayList<>();
        this.issueInfoList = new ArrayList<>();
        this.issueId = issueId;
    }
    
    public String getIssueReceiveViewDate(String pattern) {
        if(StringUtils.isBlank(pattern) || this.issueReceiveDate == null) return StringUtils.EMPTY;
        try {
            return (new SimpleDateFormat(pattern)).format(this.issueReceiveDate);
        } catch (Exception ex) {
            return StringUtils.EMPTY;
        }
    }

    public List<Customer> getCustomerList() {
        if(customerList.isEmpty()){
            Customer cust = new Customer();
            cust.setCompany(getCompany());
            cust.setCustDeleted(InterfaceUtil.EXISTS);
            this.customerList.add(cust);
        }
        return customerList;
    }

    public List<IssueInfo> getIssueInfoList() {
        if(issueInfoList.isEmpty()){
            IssueInfo info = new IssueInfo();
            info.setCompany(getCompany());
            info.setInfoDeleted(InterfaceUtil.EXISTS);
            this.issueInfoList.add(info);
        }
        return issueInfoList;
    }
    
    public IssueStatusHistory getChangeFromStatus(Integer statusId){
        IssueStatusHistory d = null;
        for(IssueStatusHistory e : statusHistory){
            if(e.getFromStatusId().equals(statusId)){
                if(d == null) d = e;
                else if(d.getUpdatedTime().before(e.getUpdatedTime())){
                    d = e;
                }
            }
        }
        return d;
    }
    
    public IssueStatusHistory getChangeToStatus(Integer statusId){
        IssueStatusHistory d = null;
        for(IssueStatusHistory e : statusHistory){
            if(e.getToStatusId().equals(statusId)){
                if(d == null) d = e;
                else if(d.getUpdatedTime().before(e.getUpdatedTime())){
                    d = e;
                }
            }
        }
        return d;
    }
    
    
    @Transient
    public String getIssueLinkedMenteName(String key, String locale) {
        final List<String> linkedMenteNameList = new ArrayList<>();
        issueInfoList.forEach((item) -> {
            linkedMenteNameList.add(item.getIssueLinkedMenteName(key, locale));
        });
        return StringUtils.join(linkedMenteNameList, "<br/>");
    }
    
    @Transient
    public String getIssueProductName(String locale) {
        final List<String> productNameList = new ArrayList<>();
        issueInfoList.forEach((item) -> {
            productNameList.add(item.getIssueProductName(locale));
        });
        return StringUtils.join(productNameList, "<br/>");
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (issueId != null ? issueId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Issue)) {
            return false;
        }
        Issue other = (Issue) object;
        return !((this.issueId == null && other.issueId != null) || (this.issueId != null && !this.issueId.equals(other.issueId)));
    }

    @Override
    public String toString() {
        return "gnext.bean.CrmIssue[ issueId=" + issueId + " ]";
    }

//    private void sortMenteItem(){
//        if(!isShortMenteItem){
//            forceSortMenteItem();
//            isShortMenteItem = true;
//        }
//    }
//    
//    @Transient
//    public void forceSortMenteItem(){
//        menteItem.sort((i1, i2) -> i1.getItemLevel().compareTo(i2.getItemLevel()));
//    }
//    
//    @Transient
//    public List<MenteItem> getIssueLinkedMenteList(String key){
//        sortMenteItem();
//        List<MenteItem> list = new ArrayList<>();
//        menteItem.forEach((item) -> {
//            if(key.equals(item.getItemName())){
//                list.add(item);
//            }
//        });
//        return list;
//    }
//    
//    @Transient
//    public List<String> getIssueLinkedMenteIdList(String key){
//        sortMenteItem();
//        List<String> list = new ArrayList<>();
//        menteItem.forEach((item) -> {
//            if(key.equals(item.getItemName())){
//                list.add(item.getItemId().toString());
//            }
//        });
//        return list;
//    }
//    
//    @Transient
//    public List<String> getIssueLinkedMenteNameList(String key, String locale) {
//        sortMenteItem();
//        List<String> label = new ArrayList<>();
//        menteItem.forEach((item) -> {
//            if(key.equals(item.getItemName())){
//                label.add(item.getItemViewData(locale));
//            }
//        });
//        return label;
//    }
//    
//    @Transient
//    public String getIssueLinkedMenteName(String key, String locale) {
//        return getIssueLinkedMenteName(key, locale, " > ");
//    }
//    
//    @Transient
//    public String getIssueLinkedMenteName(String key, String locale, String seperator) {
//        return StringUtils.join(getIssueLinkedMenteNameList(key, locale), seperator);
//    }
//    
//    @Transient
//    public String getIssueProductName(String locale) {
//        sortMenteItem();
//        List<String> label = getIssueLinkedMenteNameList("issue_product_id", locale);
//        if(!productList.isEmpty()){
//            label.add(productList.get(0).getProductsName());
//        }
//        return StringUtils.join(label, " > ");
//    }
//    
//    @Transient
//    public List<MenteItem> getKeywords() {
//        return getIssueLinkedMenteList("issue_keyword_id");
//    }
//
//    @Transient
//    public void setKeywords(List<MenteItem> keywords) {
//        for (Iterator<MenteItem> iterator = menteItem.iterator(); iterator.hasNext();) {
//            MenteItem o = iterator.next();
//            if ("issue_keyword_id".equals(o.getItemName())) {
//                iterator.remove();
//            }
//        }
//        menteItem.addAll(keywords);
//        forceSortMenteItem();
//    }
//
//    @Transient
//    @Setter
//    @QuickSearchField(name = "issue_proposal_level_id")
//    private Integer issueProposalLevelId;
//    public Integer getIssueProposalLevelId() {
//        if(this.getMenteItem()== null || this.getMenteItem().isEmpty()) return null;
//        for(MenteItem item:this.getMenteItem()) {
//            if(!COLS.PROPOSAL.equals(item.getItemName())
//                    || item.getItemLevel() != 1) continue;
//            return item.getItemId();
//        }
//        return null;
//    }
//    
//    @Transient
//    public String getProductsName(){
//        if(!productList.isEmpty()) return productList.get(0).getProductsName();
//        return StringUtils.EMPTY;
//    }

    private String getBinaryByObject(Object o){
        if(o == null) return StringUtils.EMPTY;
        
        StringBuilder binary = new StringBuilder();
        
        Field[] fields = o.getClass().getDeclaredFields();
        for(Field f : fields) {
            if(f.getAnnotation(QuickSearchField.class) != null) {
                try {
                    f.setAccessible(true); 
                    Object val = f.get(o);
                    if(val != null){
                        String str = StringUtils.EMPTY;
                        if(val instanceof Date){
                            str = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(val);
                        }else if(val instanceof MenteItem){
                            MenteItem mi = (MenteItem) val;
                            mi.getLangs().forEach((t) -> {
                                String strTmp = getBinaryByObject(t);
                                if( !StringUtils.isEmpty(strTmp) ){
                                    binary.append(strTmp).append(" ");
                                }
                            });
                        }else{
                            str = val.toString();
                        }
                        if( !StringUtils.isEmpty(str) ){
                            binary.append(str).append(" ");
                        }
                    }
                   
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        return binary.append(" ").toString();
    }
    
    public String getBinarySearchContent(){
        StringBuilder binary = new StringBuilder();
        
        binary.append(getBinaryByObject(this));
        
        //issueReceivePerson
        binary.append(getBinaryByObject(this.getIssueReceivePerson()));
        
        //issueAuthorizerPerson
        binary.append(getBinaryByObject(this.getIssueAuthorizerId()));
        
        //issueCreatorPerson
        binary.append(getBinaryByObject(this.getCreatorId()));
        
        //issueUpdatedPerson
        binary.append(getBinaryByObject(this.getUpdatedId()));
        
        //customer
        for(Customer cust: this.getCustomerList()){
            binary.append(getBinaryByObject(cust));
            
            //customer city
            binary.append(getBinaryByObject(cust.getCustCity()));
            
            //cust target info
            for(CustTargetInfo target : cust.getCustTargetInfoList()){
                binary.append(getBinaryByObject(target));
            }
        }
        //menteitem
        for(IssueInfo info : this.getIssueInfoList()){
            for(MenteItem item: info.getMenteItem()){
                item.getLangs().forEach((t) -> {
                    binary.append(getBinaryByObject(t)).append(" ");
                });
            }
            if( !StringUtils.isEmpty(info.getIssueContentAsk())){
                binary.append(info.getIssueContentAsk());
            }
            if( !StringUtils.isEmpty(info.getIssueProductMemo())){
                binary.append(info.getIssueProductMemo());
            }
        }
        
        return binary.toString();
    }
}
