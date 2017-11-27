package gnext.service.softphone;

import gnext.bean.softphone.Twilio;
import gnext.bean.softphone.TwilioHistory;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;


/**
 *
 * @author hungpham
 */
@Local
public interface TwilioService extends EntityService<Twilio>{
    
    
    /**
     * Start conference
     * 
     * @param type
     * @param from
     * @param to
     * @param conferenceId
     * @param companyId
     */
    public void startConference(Integer type, String from, String to, String conferenceId, int companyId);
    
    /**
     * Update conference
     * 
     * @param conferenceId
     * @param agent 
     * @param status 
     * @param companyId 
     */
    public void updateConference(String conferenceId, String agent, String status, int companyId) throws Exception;
    
    /**
     * Stop current call of the agent
     * 
     * @param conferenceId
     * @param companyId 
     */
    public void finishConference(String conferenceId, int companyId) throws Exception;
    
    /**
     * Forward call between two agents
     * 
     * @param conferenceId
     * @param agent 
     * @param companyId 
     */
    public void forwardConference(String conferenceId, String agent, int companyId) throws Exception;

    /**
     * Update record URL for the call
     * 
     * @param companyId
     * @param conferenceId
     * @param agentId
     * @param recordingUrl
     * @param recordingStatus 
     * @param recordingDuration 
     */
    public void updateRecording(Integer companyId, String conferenceId, String agentId, String recordingUrl, String recordingStatus, Integer recordingDuration);

    public List<Twilio> findByCompanyId(Integer companyId);

    public List<Twilio> search(Integer companyId, String query);
    
    public TwilioHistory getHistoryById(Integer historyId);

    public List<TwilioHistory> getHistoryByCallId(String callSid);

    public Twilio getByCallId(String twilioCallSid);
}
