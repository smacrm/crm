package gnext.bean.automail;

import gnext.bean.mente.MenteItem;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Aug 24, 2017
 */
@Entity
@Table(name = "crm_auto_mail")
@NamedQueries({})
public class AutoMail implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "auto_config_id")
    @Getter @Setter
    private Integer autoConfigId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "option_id")
    @Getter @Setter
    private int optionId;
    
    @Basic(optional = false)
    @Column(name = "auto_send")
    @Getter @Setter
    private Boolean autoSend;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "day")
    @Getter @Setter
    private int day;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "hour")
    @Getter @Setter
    private int hour;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "mode")
    @Getter @Setter
    private Integer mode;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id")
    @Getter @Setter
    private int companyId;
    
    @Column(name = "creator_id")
    @Getter @Setter
    private Integer creatorId;
    
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date createdDate = new Date();
    
    @Column(name = "updated_id")
    @Getter @Setter
    private Integer updatedId;
    
    @Column(name = "updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date updatedDate = new Date();
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "autoMail", orphanRemoval = true)
    @Getter @Setter
    private List<AutoMailMember> autoMailMemberList;
    
    @JoinTable(name = "crm_auto_mail_mente", joinColumns = {
        @JoinColumn(name = "auto_id", referencedColumnName = "auto_config_id")}, inverseJoinColumns = {
        @JoinColumn(name = "item_id", referencedColumnName = "item_id")})
    @ManyToMany
    @Getter @Setter
    private List<MenteItem> menteItemList;
    
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    @ManyToOne(optional = false)
    @Getter @Setter
    private MenteItem itemId;
    
    public AutoMail() {
    }

    public AutoMail(Integer autoConfigId) {
        this.autoConfigId = autoConfigId;
    }

    public AutoMail(Integer autoConfigId, int day, int hour, Integer mode, int companyId) {
        this.autoConfigId = autoConfigId;
        this.day = day;
        this.hour = hour;
        this.mode = mode;
        this.companyId = companyId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (autoConfigId != null ? autoConfigId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoMail)) {
            return false;
        }
        AutoMail other = (AutoMail) object;
        if ((this.autoConfigId == null && other.autoConfigId != null) || (this.autoConfigId != null && !this.autoConfigId.equals(other.autoConfigId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.automail.AutoMail[ autoConfigId=" + autoConfigId + " ]";
    }

}
