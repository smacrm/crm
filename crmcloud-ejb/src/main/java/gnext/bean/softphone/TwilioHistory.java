package gnext.bean.softphone;

import gnext.bean.Company;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hungpham
 * @since Mar 2, 2017
 */
@Entity
@Table(name = "crm_twilio_history")
public class TwilioHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "twilio_history_id")
    @Getter @Setter
    private Integer twilioHistoryId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "twilio_conference_id")
    @Getter @Setter
    private String conferenceId;
    
    @Column(name = "twilio_agent_id")
    @Setter
    private String agentId;
    
    /**
     *  The following are the possible values for the 'CallStatus' parameter.
     * 
     *   queued           The call is ready and waiting in line before going out.
     *   ringing	The call is currently ringing.
     *   in-progress	The call was answered and is currently in progress.
     *   completed	The call was answered and has ended normally.
     *   busy           The caller received a busy signal.
     *   failed         The call could not be completed as dialed, most likely because the phone number was non-existent.
     *   no-answer	The call ended without being answered.
     *   canceled	The call was canceled via the REST API while queued or ringing.
     */
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 15)
    @Column(name = "twilio_status")
    @Getter @Setter
    private String status;
    
    @Column(name = "twilio_call_update_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date callUpdateTime;
    
    @JoinColumn(name = "twilio_company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;
    
    @Column(name = "twilio_recording_id")
    @Getter @Setter
    private Integer recordingId;
    
    @Column(name = "twilio_recording_url")
    @Getter @Setter
    private String recordingUrl;
    
    @Column(name = "twilio_recording_status")
    @Getter @Setter
    private String recordingStatus;
    
    @Column(name = "twilio_recording_duration")
    @Getter @Setter
    private Integer recordingDuration = 0;
    
    @JoinColumn(name = "twilio_conference_id", referencedColumnName = "twilio_conference_id",
            nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Twilio twilio;

    public TwilioHistory() {
    }
    
    public String getAgentId() {
        if(!StringUtils.isEmpty(agentId)){
            return agentId.replaceAll("GN[0-9]{3}", "");
        }
        return agentId;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (twilioHistoryId != null ? twilioHistoryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TwilioHistory)) {
            return false;
        }
        TwilioHistory other = (TwilioHistory) object;
        if ((this.twilioHistoryId == null && other.twilioHistoryId != null) || (this.twilioHistoryId != null && !this.twilioHistoryId.equals(other.twilioHistoryId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.softphone.TwilioHistory[ twilioHistoryId=" + twilioHistoryId + " ]";
    }

}
