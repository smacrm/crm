package gnext.bean.softphone;

import gnext.bean.Member;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Feb 9, 2017
 */
@Entity
@Table(name = "crm_twilio_config")
@NamedQueries({})
public class TwilioConfig implements Serializable {

    private static final long serialVersionUID = 5059939000713963972L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "twilio_id")
    @Getter @Setter
    private Integer twilioId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "phone_number")
    @Getter @Setter
    private String phoneNumber;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id")
    @Getter @Setter
    private Integer companyId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "application_sid")
    @Getter @Setter
    private String applicationSid;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "api_sid")
    @Getter @Setter
    private String apiSid;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "api_secret")
    @Getter @Setter
    private String apiSecret;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "account_sid")
    @Getter @Setter
    private String accountSid;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "account_auth_token")
    @Getter @Setter
    private String accountAuthToken;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2)
    @Column(name = "locale")
    @Getter @Setter
    private String locale = "ja";
    
    @Column(name = "allow_member_list")
    @Getter @Setter
    private String allowMemberList = "[]";
    
    @JoinColumn(name = "creator_id", referencedColumnName = "member_id", nullable = false, insertable = true, updatable = true)
    @ManyToOne()
    @Getter @Setter private Member creatorId;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date createdTime;
    
    
    @JoinColumn(name = "updated_id", referencedColumnName = "member_id", nullable = true, insertable = true, updatable = true)
    @ManyToOne()
    @Getter @Setter private Member updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date updatedTime;

    public TwilioConfig() {
    }

    public TwilioConfig(Integer twilioId) {
        this.twilioId = twilioId;
    }

    public TwilioConfig(Integer twilioId, String phoneNumber, String applicationSid, String apiSid, String apiSecret, String accountSid, String accountAuthToken, Member creatorId) {
        this.twilioId = twilioId;
        this.phoneNumber = phoneNumber;
        this.applicationSid = applicationSid;
        this.apiSid = apiSid;
        this.apiSecret = apiSecret;
        this.accountSid = accountSid;
        this.accountAuthToken = accountAuthToken;
        this.creatorId = creatorId;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (twilioId != null ? twilioId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TwilioConfig)) {
            return false;
        }
        TwilioConfig other = (TwilioConfig) object;
        if ((this.twilioId == null && other.twilioId != null) || (this.twilioId != null && !this.twilioId.equals(other.twilioId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.softphone.TwilioConfig[ twilioId=" + twilioId + " ]";
    }

}
