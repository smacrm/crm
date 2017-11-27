package gnext.bean.softphone;

import gnext.bean.Company;
import gnext.bean.Member;
import gnext.bean.issue.Issue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.persistence.Basic;
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
import javax.persistence.OrderBy;
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
 * @since Jan 17, 2017
 */
@Entity
@Table(name = "crm_twilio")
@NamedQueries({
    @NamedQuery(name = "Twilio.getActiveConference", query = "SELECT o FROM Twilio o WHERE o.conferenceId = :conferenceId AND o.deleted = FALSE  AND o.status != 'completed' ORDER BY o.twilioId DESC"),
})
public class Twilio implements Serializable {

    private static final long serialVersionUID = 1L;
    
    final public static String STATUS_QUEUED = "queued";
    final public static String STATUS_RINGING = "ringing";
    final public static String STATUS_IN_PROGRESS = "in-progress";
    final public static String STATUS_COMPLETED = "completed";
    final public static String STATUS_BUSY = "busy";
    final public static String STATUS_FAILED = "failed";
    final public static String STATUS_NO_ANSWER = "no-answer";
    final public static String STATUS_CANCELED = "canceled";
    final public static String STATUS_FORWARD = "forwarded";
    
    final public static Integer INCOMMING_CALL = 1;
    final public static Integer DIALING_CALL = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "twilio_id")
    @Getter @Setter
    private Integer twilioId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "twilio_type")
    @Getter @Setter
    private Integer type; // 1:着信  2:発進
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "twilio_from")
    @Getter @Setter
    private String from;
    
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "twilio_to")
    @Getter @Setter
    private String to;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "twilio_conference_id")
    @Getter @Setter
    private String conferenceId;
    
    @Column(name = "twilio_agent_id")
    @Setter
    private String agentId;
    
    @JoinColumn(name = "twilio_agent_id", referencedColumnName = "member_login_id", nullable = true, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Member agent;
    
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
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "twilio_received_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date receivedTime;
    
    @Column(name = "twilio_call_start_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date callStartTime;
    
    @Column(name = "twilio_call_finish_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date callFinishTime;
    
    @JoinColumn(name = "twilio_issue_id", referencedColumnName = "issue_id", nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = true)
    @Getter @Setter
    private Issue issue;
    
    @Column(name = "twilio_is_deleted")
    @Getter @Setter
    private Short deleted;
    
    @JoinColumn(name = "twilio_company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;
    
    @JoinColumn(name = "twilio_conference_id", referencedColumnName = "twilio_conference_id",
            nullable = false, insertable = true, updatable = false)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "twilio")
    @OrderBy("callUpdateTime ASC")
    @Getter @Setter
    private List<TwilioHistory> historyList = new ArrayList<>();

    public Twilio() {
    }

    public Twilio(Integer twilioId) {
        this.twilioId = twilioId;
    }
    
    public boolean isDeleted(){
        return deleted == 1;
    }

    public String getAgentId() {
        if(!StringUtils.isEmpty(agentId)){
            return agentId.replaceAll("GN[0-9]{3}", "");
        }
        return agentId;
    }
    
    public String getFullAgentId(){
        return agentId;
    }
    
    public String getCalledTime(){
        Integer seconds = 0;
        for(TwilioHistory item : getHistoryList()){
            seconds += item.getRecordingDuration();
        }
        
        int day = (int)TimeUnit.SECONDS.toDays(seconds);        
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);
        
        return (hours < 10 ? "0"+hours : hours ) + ":" + (minute < 10 ? "0"+minute : minute ) + ":" + (second < 10 ? "0"+second : second );
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
        if (!(object instanceof Twilio)) {
            return false;
        }
        Twilio other = (Twilio) object;
        if ((this.twilioId == null && other.twilioId != null) || (this.twilioId != null && !this.twilioId.equals(other.twilioId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.softphone.Twilio[ twilioId=" + twilioId + " ]";
    }

}
