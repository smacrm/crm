package gnext.bean;

import gnext.bean.role.Role;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import lombok.Setter;
import gnext.interceptors.annotation.QuickSearchField;
import gnext.interceptors.items.MetaDataContent;
import gnext.bean.issue.IssueLamp;

/**
 *
 * @author hungpham
 */
@Entity
//@Cacheable(true)
@Table(name = "crm_company")
@XmlRootElement
public class Company implements QuickSearchEntity, CloneSelfDataToDbChild {
    public static final int MASTER_COMPANY_ID = 1;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "company_id", nullable = false)
    @QuickSearchField(name = "company_id", view = true, title = "ID")
    @Setter @Getter private Integer companyId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 80)
    @Column(name = "company_name", nullable = false, length = 80)
    @QuickSearchField(name = "company_name", view = true, title = "label.company.name")
    @Setter @Getter private String companyName;
    
    @Size(max = 8)
    @Column(name = "company_post", length = 8)
    @Setter @Getter private String companyPost;
    
    @Column(name = "company_city")
    @Setter @Getter private Integer companyCity;
    
    @Size(max = 150)
    @Column(name = "company_address", length = 150)
    @QuickSearchField(name = "company_address")
    @Setter @Getter private String companyAddress;
    
    @Size(max = 200)
    @Column(name = "company_address_kana", length = 200)
    @QuickSearchField(name = "company_address_kana")
    @Setter @Getter private String companyAddressKana;
    
    @Size(max = 150)
    @Column(name = "company_logo", length = 150)
    @Setter @Getter private String companyLogo;
    
    @Size(max = 150)
    @Column(name = "company_copy_right", length = 150)
    @Setter @Getter private String companyCopyRight;
    
    @Size(max = 20)
    @Column(name = "company_layout", length = 20)
    @Setter @Getter private String companyLayout;
    
    @Size(max = 120)
    @Column(name = "company_global_ip", length = 120)
    @QuickSearchField(name = "company_global_ip")
    @Setter @Getter private String companyGlobalIp;
    
    @Size(max = 100)
    @Column(name = "company_union_key", length = 100)
    @QuickSearchField(name = "company_union_key")
    @Setter @Getter private String companyUnionKey;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_business_flag", nullable = false)
    @Setter @Getter private Short companyBusinessFlag;
    
    @Size(max = 150)
    @Column(name = "company_home_page")
    @Setter @Getter private String companyHomePage;
    
    @Column(name = "company_global_locale")
    @Setter @Getter private Short companyGlobalLocale; // có cho phép người dùng sử dụng nhiều ngôn ngữ hay không?
    
    @Column(name = "company_deleted")
    @Setter @Getter private Short companyDeleted;
    
    @JoinColumn(name = "creator_id", referencedColumnName = "member_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Member creator;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter private Date createdTime;
    
    @JoinColumn(name = "updated_id", referencedColumnName = "member_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Member updator;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter private Date updatedTime;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "company", fetch = FetchType.EAGER)
    @XmlTransient
    @Setter @Getter private List<Group> groupList;
    
    @Size(max = 500)
    @Column(name = "company_memo")
    @Setter @Getter private String companyMemo;

    @Size(max = 30)
    @NotNull
    @Column(name = "company_basic_login_id")
    @Setter @Getter private String companyBasicLoginId;
    
    @Size(max = 70)
    @NotNull
    @Column(name = "company_basic_password")
    @Setter @Getter private String companyBasicPassword;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "company", fetch = FetchType.EAGER)
    @XmlTransient
    @Setter @Getter private List<Role> roleCollection = new ArrayList<>();
    
    @Getter @Setter
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "company")
    private List<IssueLamp> issueLampList = new ArrayList<>();
    
    @Column(name = "company_two_factor")
    @Setter @Getter private Short companyTwoFactor;
    
    @Transient private boolean usingTwoFactor;
    
    /** for quick search module */
    @Setter @Getter @Transient private String phoneFaxMailHomepage;
    
    @Column(name = "manual_id")
    @Setter @Getter private Integer manualId; // sử dụng trong việc tách DB.
    
    @Setter @Getter @Transient private Integer databaseServerId;
    
    public Company() { }
    public Company(Integer companyId) { this.companyId = companyId; }
    public Company(Integer companyId, String companyName, short companyBusinessFlag) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyBusinessFlag = companyBusinessFlag;
    }
    
    public void setUsingTwoFactor(boolean usingTwoFactor) {
        this.companyTwoFactor = usingTwoFactor ? (short)1 : (short)0;
    }

    public boolean isUsingTwoFactor() {
        if(companyTwoFactor==null)return false;
        return companyTwoFactor == 1;
    }
    
    public boolean isDeleted(){
        return companyDeleted != null && companyDeleted == 1;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (companyId != null ? companyId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Company)) {
            return false;
        }
        Company other = (Company) object;
        if(this.companyId == null && other.getCompanyId() == null) return false;
        
        if ((this.companyId == null && other.companyId != null) || (this.companyId != null && !this.companyId.equals(other.companyId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.Company[ companyId=" + companyId + " ]";
    }

    @Override
    public List<MetaDataContent> getMetadata() {
        List<MetaDataContent> mdcs = new ArrayList<>();
        
        if(phoneFaxMailHomepage != null && !phoneFaxMailHomepage.isEmpty())
            mdcs.add(new MetaDataContent("phone_fax_mail_homepage", phoneFaxMailHomepage));
        
        return mdcs;
    }
}
