package gnext.bean.issue;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import gnext.bean.Member;
import gnext.bean.Prefecture;
import gnext.bean.mente.MenteItem;
import gnext.bean.mente.MenteOptionDataValue;
import gnext.interceptors.annotation.QuickSearchDb;
import gnext.interceptors.annotation.QuickSearchField;
import gnext.interceptors.annotation.enums.Module;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
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
@Table(name = "crm_customer")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Customer.findAll", query = "SELECT c FROM Customer c")
    , @NamedQuery(name = "Customer.findByCustId", query = "SELECT c FROM Customer c WHERE c.custId = :custId")
})
@QuickSearchDb(module = Module.CUSTOMER, fieldTargetId = "custId", disable = false)
public class Customer extends BaseEntity {
    public static final Short CUSTOMER_FLAG_MAIL = 1;
    public static final Short CUSTOMER_FLAG_TEL = 2;
    public static final Short CUSTOMER_FLAG_MOBILE = 3;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Getter @Setter
    @Column(name = "cust_id")
    @QuickSearchField(name = "cust_id")
    private Integer custId;

    @Getter @Setter
    @JoinColumn(name = "cust_special_id", referencedColumnName = "item_id")
    @ManyToOne(optional = false)
    private MenteItem custSpecialId;

    @Getter @Setter
    @JoinColumn(name = "cust_cooperation_id", referencedColumnName = "item_id")
    @ManyToOne(optional = false)
    private MenteItem custCooperationId;

    @Size(max = 70)
    @Getter @Setter
    @Column(name = "cust_first_hira")
    @QuickSearchField(name = "cust_first_hira")
    private String custFirstHira;

    @Size(max = 70)
    @Getter @Setter
    @Column(name = "cust_last_hira")
    @QuickSearchField(name = "cust_last_hira")
    private String custLastHira;

    @Size(max = 70)
    @Getter @Setter
    @Column(name = "cust_first_kana")
    @QuickSearchField(name = "cust_first_kana")
    private String custFirstKana;

    @Size(max = 70)
    @Getter @Setter
    @Column(name = "cust_last_kana")
    @QuickSearchField(name = "cust_last_kana")
    private String custLastKana;

    @Getter @Setter
    @JoinColumn(name = "cust_sex_id", referencedColumnName = "item_id")
    @ManyToOne(optional = false)
    private MenteItem custSexId;

    @Getter @Setter
    @JoinColumn(name = "cust_age_id", referencedColumnName = "item_id")
    @ManyToOne(optional = false)
    private MenteItem custAgeId;

    @Size(max = 8)
    @Getter @Setter
    @Column(name = "cust_post")
    @QuickSearchField(name = "cust_post")
    private String custPost;

    @JoinColumn(name = "cust_city", referencedColumnName = "prefecture_id")
    @ManyToOne(optional = false)
    private Prefecture custCity;

    @Size(max = 150)
    @Getter @Setter
    @Column(name = "cust_address")
    @QuickSearchField(name = "cust_address")
    private String custAddress;

    @Size(max = 200)
    @Getter @Setter
    @Column(name = "cust_address_kana")
    @QuickSearchField(name = "cust_address_kana")
    private String custAddressKana;

    @Size(max = 2000)
    @Getter @Setter
    @Column(name = "cust_memo")
    @QuickSearchField(name = "cust_memo")
    private String custMemo;
    
    @Size(max = 20)
    @Getter @Setter
    @Column (name = "cust_code")
    @QuickSearchField(name = "cust_code")
    private String custCode;

    @Getter @Setter
    @Column(name = "cust_deleted")
    private Boolean custDeleted;

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

    @Getter @Setter
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedTime;

    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;

    @Getter @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CustTargetInfo> custTargetInfoList = new ArrayList<>();
    
    @ManyToMany(mappedBy = "customerList")
    @Setter @Getter
    private List<Issue> issueList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CustDataSpecial> custDataSpecials = new ArrayList<>();
    
    @Getter @Setter
    @Column(name = "cust_status")
    @QuickSearchField(name = "cust_status")
    private Integer custStatus;
    
    public String getViewAddress() {
        if(!StringUtils.isEmpty(custAddress)) return custAddress;
        for(CustDataSpecial custDataSpecial : custDataSpecials) {
            if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_ADDRESS) {
                if(!StringUtils.isEmpty(custDataSpecial.getCustData3())) return custDataSpecial.getCustData3();
            }
        }
        return StringUtils.EMPTY;
    }
    
    public String getViewPost() {
        if(!StringUtils.isEmpty(custPost)) return custPost;
        for(CustDataSpecial custDataSpecial : custDataSpecials) {
            if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_ADDRESS) {
                if(!StringUtils.isEmpty(custDataSpecial.getCustData1())) return custDataSpecial.getCustData1();
            }
        }
        return StringUtils.EMPTY;
    }
    
    public String getViewCustFirstHira() {
        if(!StringUtils.isEmpty(custFirstHira)) return custFirstHira;
        for(CustDataSpecial custDataSpecial : custDataSpecials) {
            if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_NAME) {
                if(!StringUtils.isEmpty(custDataSpecial.getCustData1())) return custDataSpecial.getCustData1();
            }
        }
        return StringUtils.EMPTY;
    }
    
    public String getViewCustFirstKana() {
        if(!StringUtils.isEmpty(custFirstHira)) return custFirstHira;
        for(CustDataSpecial custDataSpecial : custDataSpecials) {
            if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_NAME) {
                if(!StringUtils.isEmpty(custDataSpecial.getCustData3())) return custDataSpecial.getCustData3();
            }
        }
        return StringUtils.EMPTY;
    }

    public Prefecture getCustCity() {
        return custCity;
    }

    public void setCustCity(Prefecture custCity) {
        this.custCity = custCity;
    }
    
    public void initSpecicalData() {
        // firstname(kana, kanji) - lastname(kana, kanji) - custSpecialId
        initSpecialDataByType(CustDataSpecial.CUST_SPECIAL_TYPE_NAME);
        
        // address(kana, kanji) - post - city
        initSpecialDataByType(CustDataSpecial.CUST_SPECIAL_TYPE_ADDRESS);
        
        // history
        initSpecialDataByType(CustDataSpecial.CUST_SPECIAL_TYPE_HISTORY);
    }
    
    private void initSpecialDataByType(int type) {
        boolean exists = false;
        
        if(custDataSpecials != null) {
            for(CustDataSpecial custDataSpecial : custDataSpecials) {
                if(custDataSpecial.getCustType() == type) {
                    exists = true;
                    break;
                }
            }
        }
        
        if(!exists) addNewCustDataSpecial(type);
    }
    
    public void addNewCustDataSpecial(int type) {
        CustDataSpecial newCustDataSpecial = new CustDataSpecial();
        newCustDataSpecial.setCustDataSpecialId(0-(custDataSpecials.size()+1));
        newCustDataSpecial.setCustType(type);
        newCustDataSpecial.setCustomer(this);
        custDataSpecials.add(newCustDataSpecial);
    }
    
    public void removeCustDataSpecial(int type, int custDataSpecialId) {
        for (Iterator<CustDataSpecial> iterator = custDataSpecials.iterator(); iterator.hasNext();) {
            CustDataSpecial custDataSpecial = iterator.next();
            if(custDataSpecial.getCustDataSpecialId() == custDataSpecialId && custDataSpecial.getCustType() == type) {
                iterator.remove();
                custDataSpecial.setCustomer(null);
            }
        }
        initSpecialDataByType(type);
    }
    
    public void smoothCustDataSpecInvalid() {
        for (Iterator<CustDataSpecial> iterator = custDataSpecials.iterator(); iterator.hasNext();) {
            CustDataSpecial custDataSpecial = iterator.next();
            
            if(custDataSpecial.getCustDataSpecialId() != null && custDataSpecial.getCustDataSpecialId() <= 0)
                    custDataSpecial.setCustDataSpecialId(null);
            
            if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_ADDRESS) {
                if(StringUtils.isEmpty(custDataSpecial.getCustData3()) /*address*/
                        && StringUtils.isEmpty(custDataSpecial.getCustData4()) /*address_kana*/
                        && StringUtils.isEmpty(custDataSpecial.getCustData1()) /*cust_post*/
                        && StringUtils.isEmpty(custDataSpecial.getCustData2()) /*cust_city*/
                        ) {
                    iterator.remove();
                    custDataSpecial.setCustomer(null);
                }
                if(StringUtils.isEmpty(custAddress) && !StringUtils.isEmpty(custDataSpecial.getCustData3())) {
                    custAddress = custDataSpecial.getCustData3();
                }
                if(StringUtils.isEmpty(custAddressKana) && !StringUtils.isEmpty(custDataSpecial.getCustData4())) {
                    custAddressKana = custDataSpecial.getCustData4();
                }
                if(StringUtils.isEmpty(custPost) && !StringUtils.isEmpty(custDataSpecial.getCustData1())) {
                    custPost = custDataSpecial.getCustData1();
                }
            }
            
            if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_HISTORY) {
                if(StringUtils.isEmpty(custDataSpecial.getCustData1()) /*history_memo*/
                        && StringUtils.isEmpty(custDataSpecial.getCustData2()) /*history_date*/
                        ) {
                    iterator.remove();
                    custDataSpecial.setCustomer(null);
                }
            }
            
            if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_NAME) {
                if(StringUtils.isEmpty(custDataSpecial.getCustData1()) /*first_name_kana*/
                        && StringUtils.isEmpty(custDataSpecial.getCustData3()) /*first_name_kanji*/
                        ) {
                    iterator.remove();
                    custDataSpecial.setCustomer(null);
                }
                if(StringUtils.isEmpty(custFirstHira) && !StringUtils.isEmpty(custDataSpecial.getCustData1())) {
                    custFirstHira = custDataSpecial.getCustData1();
                }
                if(StringUtils.isEmpty(custLastHira) && !StringUtils.isEmpty(custDataSpecial.getCustData2())) {
                    custLastHira = custDataSpecial.getCustData2();
                }
                
                if(StringUtils.isEmpty(custFirstKana) && !StringUtils.isEmpty(custDataSpecial.getCustData3())) {
                    custFirstKana = custDataSpecial.getCustData3();
                }
                if(StringUtils.isEmpty(custLastKana) && !StringUtils.isEmpty(custDataSpecial.getCustData4())) {
                    custLastKana = custDataSpecial.getCustData4();
                }
            }
        } // kết thúc làm min dữ liệu rỗng.
    }

    public List<CustDataSpecial> getCustDataSpecials() {
        return custDataSpecials;
    }

    public void setCustDataSpecials(List<CustDataSpecial> custDataSpecials) {
        this.custDataSpecials = custDataSpecials;
    }
    
    public Customer() {
    }

    public Customer(Member member, MenteItem custSpecialId) {
        if(member != null) {
            Company com = member.getGroup().getCompany();
            Integer mId = member.getMemberId();
            Date date = Calendar.getInstance().getTime();
            this.setCompany(com);
            this.setCustDeleted(Boolean.FALSE);
            this.setCreatorId(mId);
            this.setCreatedTime(date);
            this.setUpdatedId(mId);
            this.setUpdatedTime(date);
            if(custSpecialId != null) this.setCustSpecialId(custSpecialId);
        }
    }

    public Customer(Integer custId) {
        this.custId = custId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (custId != null ? custId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Customer)) {
            return false;
        }
        Customer other = (Customer) object;
        return !((this.custId == null && other.custId != null) || (this.custId != null && !this.custId.equals(other.custId)));
    }

    @Override
    public String toString() {
        return "gnext.issue.CrmCustomer[ custId=" + custId + " ]";
    }

    @Transient
    @Getter @Setter
    private String tabIdx = UUID.randomUUID().toString(); // random uuid
    
    public List<CustTargetInfo> getTel() {
        List<CustTargetInfo> list = new ArrayList<>();
        getCustTargetInfoList().forEach((target) -> {
            if(target.getCustFlagType().equals(CUSTOMER_FLAG_TEL)) {
                list.add(target);
            }
        });
        if(list.isEmpty()){
            CustTargetInfo target = new CustTargetInfo();
            target.setCustFlagType(CUSTOMER_FLAG_TEL);
            target.setCompany(this.getCompany());
            target.setCustomer(this);
            list.add(target);
            getCustTargetInfoList().add(target);
        }
        return list;
    }

    public List<CustTargetInfo> getMobile() {
        List<CustTargetInfo> list = new ArrayList<>();
        getCustTargetInfoList().forEach((target) -> {
            if(target.getCustFlagType().equals(CUSTOMER_FLAG_MOBILE)) {
                list.add(target);
            }
        });
        if(list.isEmpty()){
            CustTargetInfo target = new CustTargetInfo();
            target.setCustFlagType(CUSTOMER_FLAG_MOBILE);
            target.setCompany(this.getCompany());
            target.setCustomer(this);
            list.add(target);
            getCustTargetInfoList().add(target);
        }
        return list;
    }

    public List<CustTargetInfo> getMail() {
        List<CustTargetInfo> list = new ArrayList<>();
        getCustTargetInfoList().forEach((target) -> {
            if(target.getCustFlagType().equals(CUSTOMER_FLAG_MAIL)) {
                list.add(target);
            }
        });
        if(list.isEmpty()){
            CustTargetInfo target = new CustTargetInfo();
            target.setCustFlagType(CUSTOMER_FLAG_MAIL);
            target.setCompany(this.getCompany());
            target.setCustomer(this);
            list.add(target);
            getCustTargetInfoList().add(target);
        }
        return list;
    }
    
    public boolean isValid(){
        boolean hasCustomerInfo = !(this.getCustId() == null &&
                this.getCustSpecialId() == null &&
                this.getCustCooperationId() == null &&
                this.getCustFirstHira() == null &&
                this.getCustLastHira() == null &&
                this.getCustFirstKana() == null &&
                this.getCustLastKana() == null &&
                this.getCustSexId() == null &&
                this.getCustAgeId() == null &&
                this.getCustPost() == null &&
                this.getCustCity() == null &&
                this.getCustAddress() == null &&
                this.getCustAddressKana() == null &&
                this.getCustMemo() == null);
        boolean hasCusTargetInfo = false;
        for(CustTargetInfo target : getCustTargetInfoList()){
            hasCusTargetInfo |= target.isValid();
        }
        return hasCustomerInfo || hasCusTargetInfo;
    }
    
    public void removeInvalidData(){
        for (Iterator<CustTargetInfo> iterator = getCustTargetInfoList().iterator(); iterator.hasNext();) {
            CustTargetInfo o = iterator.next();
            if(!o.isValid()){
                iterator.remove();
            }
        }
    }
    
    //Check customer is same mobile | tel | email
    public boolean isSameCustTarget(Customer other){
        for(CustTargetInfo c : getCustTargetInfoList()){
            if(c.getCustFlagType().equals(Customer.CUSTOMER_FLAG_MAIL) ||
                c.getCustFlagType().equals(Customer.CUSTOMER_FLAG_MOBILE) ||
                c.getCustFlagType().equals(Customer.CUSTOMER_FLAG_TEL)){
                
                String data = c.getCustTargetData();
                
                for(CustTargetInfo c1 : other.getCustTargetInfoList()){
                    if(c1.getCustFlagType().equals(Customer.CUSTOMER_FLAG_MAIL) ||
                        c1.getCustFlagType().equals(Customer.CUSTOMER_FLAG_MOBILE) ||
                        c1.getCustFlagType().equals(Customer.CUSTOMER_FLAG_TEL)){
                        
                        String data1 = c1.getCustTargetData();
                        
                        if(!StringUtils.isEmpty(data) && !StringUtils.isEmpty(data1) && data.equals(data1)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public String getCustFullHira(){
        return StringUtils.defaultString(this.getCustFirstHira(), "") + StringUtils.defaultString(this.getCustLastHira(), "");
    }
    
    public String getCustFullKana(){
        return StringUtils.defaultString(this.getCustFirstKana(), "") + StringUtils.defaultString(this.getCustLastKana(), "");
    }
    
    public Short isCustomerDeleted(){
        return custDeleted ? (short) 1 : (short) 0;
    }

    private String getBinaryByObject(Object o){
        if(o == null) return StringUtils.EMPTY;
        StringBuilder binary = new StringBuilder();
        
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field f : fields) {
            if(f.getAnnotation(QuickSearchField.class) == null) continue;
            
            try {
                f.setAccessible(true);
                Object val = f.get(o);
                if(val == null) continue;

                if(o instanceof CustDataSpecial) {
                    CustDataSpecial custDataSpecial = (CustDataSpecial) o;
                    if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_ADDRESS) {
                        if("custData2".equals(f.getName())) {
                            val = custDataSpecial.getCustCityNameData2();
                        } else if("custCityNameData2".equals(f.getName())) {
                            continue;
                        }
                    }
                }
                
                String str = null;
                if (val instanceof Date) {
                    str = new SimpleDateFormat("yyyy/MM/dd").format(val);
                } else {
                    str = val.toString();
                }

                if (!StringUtils.isEmpty(str)) {
                    binary.append(str).append(" ");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return binary.append(" ").toString();
    }
    
    public String getBinarySearchContent(){
        StringBuilder binary = new StringBuilder();
        
        // các kiểu primitive trên đối tượng.
        binary.append(getBinaryByObject(this));
        
        // tỉnh-thành phố của customer.
        binary.append(getBinaryByObject(getCustCity()));
        
        // danh sách thông tin của khách hàng đặc biệt.
        for(CustDataSpecial custDataSpecial : custDataSpecials) {
            binary.append(getBinaryByObject(custDataSpecial));
        }
        
        // thông tin liên quan tới các Mente.
        if(custSpecialId != null) {
            List<MenteOptionDataValue> items = custSpecialId.getLangs();
            if(items != null) {
                for(MenteOptionDataValue item : items) {
                    binary.append(getBinaryByObject(item));
                }
            }
        }
        
        if(custCooperationId != null) {
            List<MenteOptionDataValue> items = custCooperationId.getLangs();
            if(items != null) {
                for(MenteOptionDataValue item : items) {
                    binary.append(getBinaryByObject(item));
                }
            }
        }
        
        if(custSexId != null) {
            List<MenteOptionDataValue> items = custSexId.getLangs();
            if(items != null) {
                for(MenteOptionDataValue item : items) {
                    binary.append(getBinaryByObject(item));
                }
            }
        }
        
        if(custAgeId != null) {
            List<MenteOptionDataValue> items = custAgeId.getLangs();
            if(items != null) {
                for(MenteOptionDataValue item : items) {
                    binary.append(getBinaryByObject(item));
                }
            }
        }
        
        return binary.toString();
    }
}
