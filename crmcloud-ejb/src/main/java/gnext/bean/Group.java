package gnext.bean;

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
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_group")
@XmlRootElement
public class Group implements CloneSelfDataToDbChild {
    public static final String TARGET_ADMIN = "ADMIN";
    public static final String TARGET_CUSTOMER = "CUSTOMER";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "group_id", nullable = false)
    @Setter @Getter private Integer groupId;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "group_name")
    @Setter @Getter private String groupName;

    @JoinColumn(name = "group_parent_id", referencedColumnName = "group_id", nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Group parent;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "parent")
    @Setter @Getter private List<Group> childs = new ArrayList<>();

    @Size(max = 500)
    @Column(name = "group_tree_id")
    @Setter @Getter private String groupTreeId;

    @Column(name = "group_order")
    @Setter @Getter private Integer groupOrder;

    @Size(max = 500)
    @Column(name = "group_memo")
    @Setter @Getter private String groupMemo;

    @Column(name = "group_deleted")
    @Setter @Getter private Short groupDeleted;

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

    @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = true, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Getter private Company company;
    @Column(name = "company_id", nullable = false, insertable = true, updatable = true)
    @Setter @Getter private Integer companyId;
    public void setCompany(Company company) {
        this.company = company;
        if(company != null)
            this.companyId = company.getCompanyId();
        else 
            this.companyId = null;
    }
    
    @Transient
    @Setter @Getter private int treeLevel;
    
    @Transient
    @Setter @Getter private boolean disabled;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "group")
    @Setter @Getter private List<Member> members = new ArrayList<>();

    @Column(name = "manual_id")
    @Setter @Getter private Integer manualId; // sử dụng trong việc tách DB.
    
    @Column(name = "source")
    @Setter @Getter private String source;    // Sử dụng đánh dấu nguồn tạo ra group này.
    
    @Column(name = "target")
    @Setter @Getter private String target;    // Đánh dấu mục đích sử dụng GROUP này.
    
    public Group() { }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (groupId != null ? groupId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Group)) {
            return false;
        }
        Group other = (Group) object;
        if ((this.groupId == null && other.groupId != null) || (this.groupId != null && !this.groupId.equals(other.groupId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() { return String.valueOf(groupId); }
}
