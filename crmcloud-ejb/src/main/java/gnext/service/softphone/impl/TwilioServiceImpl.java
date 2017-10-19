package gnext.service.softphone.impl;

import gnext.bean.Company;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.bean.softphone.Twilio;
import gnext.bean.softphone.TwilioHistory;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.filetransfer.BaseFileTransfer;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.HttpParameter;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.multitenancy.TenantHolder;
import gnext.service.attachment.ServerService;
import gnext.service.impl.AbstractService;
import gnext.service.softphone.TwilioService;
import gnext.utils.JPAUtils;
import gnext.utils.MapObjectUtil;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityTransaction;


/**
 *
 * @author hungpham
 */
@Stateless
public class TwilioServiceImpl extends AbstractService<Twilio> implements TwilioService{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioServiceImpl.class);
    private static final long serialVersionUID = 8643352056115852814L;
    
    @EJB private ServerService serverService;
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public TwilioServiceImpl() { super(Twilio.class); }

    private Twilio getConference(String conferenceId, EntityManager em_slave) {
        try{
            Query q = em_slave.createNamedQuery("Twilio.getActiveConference");
            q.setParameter("conferenceId", conferenceId);
            q.setMaxResults(1);
            Twilio o = (Twilio) q.getSingleResult();
            return o;
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
    
    private void pushHistory(Twilio o, String agentId, EntityManager em_slave){
        TwilioHistory h = new TwilioHistory();
        h.setConferenceId(o.getConferenceId());
        h.setAgentId(agentId);
        h.setStatus(o.getStatus());
        h.setCompany(o.getCompany());
        h.setCallUpdateTime(new Date());
        em_slave.persist(h);
    }
    
    @Override
    public void startConference(Integer type, String from, String to, String conferenceId, int companyId) {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            Twilio o = new Twilio();
            o.setConferenceId(conferenceId);
            o.setFrom(from);
            o.setType(companyId);
            o.setTo(to);
            o.setStatus(Twilio.STATUS_QUEUED);
            o.setDeleted((short)0);
            o.setReceivedTime(new Date());
            o.setCompany(new Company(companyId));

            JPAUtils.create(o, em_slave, false);

            pushHistory(o, null, em_slave);
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    @Override
    public void finishConference(String conferenceId, int companyId) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            Twilio o = getConference(conferenceId, em_slave);
            if(o != null){
                o.setStatus(Twilio.STATUS_COMPLETED);
                o.setCallFinishTime(new Date());
                o.setCompany(new Company(companyId));

                JPAUtils.edit(o, em_slave, false);
                pushHistory(o, null, em_slave);
            }else{
                LOGGER.error("NO conference exists for id: " + conferenceId);
            }
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public void forwardConference(String conferenceId, String agent, int companyId) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            Twilio o = getConference(conferenceId, em_slave);
            if(o != null){
                o.setStatus(Twilio.STATUS_FORWARD);
                o.setCompany(new Company(companyId));
                JPAUtils.edit(o, em_slave, false);
                pushHistory(o, agent, em_slave);
            }else{
                LOGGER.error("NO conference exists for agent: " + agent);
            }
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public void updateConference(String conferenceId, String agent, String status, int companyId) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            Twilio o = getConference(conferenceId, em_slave);
            if(o != null){
                o.setStatus(status);
                if(status.equals(Twilio.STATUS_IN_PROGRESS)) {
                    o.setCallStartTime(new Date());
                }
                if(!StringUtils.isEmpty(agent)){
                    if(StringUtils.isEmpty(o.getAgentId()))
                        o.setAgentId(agent);
                    else
                        o.setAgentId(String.format("%s,%s", o.getAgentId(), agent));
                }
                o.setCompany(new Company(companyId));

                JPAUtils.edit(o, em_slave, false);
                pushHistory(o, agent, em_slave);
            }else{
                LOGGER.error("NO conference exists for id: " + conferenceId);
            }
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    @Override
    public void updateRecording(Integer companyId, String conferenceId, String agentId, String recordingUrl, String recordingStatus, Integer recordingDuration){
        String sql;
        Integer historyId;
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            sql = "SELECT twilio_history_id FROM crm_twilio_history WHERE twilio_conference_id = '%s' AND twilio_agent_id='%s' ORDER BY twilio_history_id DESC LIMIT 1;";
            sql = String.format(sql, conferenceId, agentId);
            historyId = (Integer)em_slave.createNativeQuery(sql).getSingleResult();

            Integer attachmentId = uploadRecordingFileToFtpServer(historyId, conferenceId, recordingUrl, companyId, em_slave);
            
            sql = "UPDATE crm_twilio_history SET twilio_recording_id='%d', twilio_recording_url='%s', twilio_recording_status='%s', twilio_recording_duration = '%d' "
                    + "WHERE twilio_history_id = '%d';";
            sql = String.format(sql, attachmentId, recordingUrl, recordingStatus, recordingDuration, historyId);
            em_slave.createNativeQuery(sql).executeUpdate();
            
            commitAndCloseTransaction(tx_slave);
        } catch(Exception nre){
            LOGGER.error(nre.getMessage(), nre);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    private Integer uploadRecordingFileToFtpServer(Integer historyId, String callSid, String recordingUrl, Integer companyId, EntityManager em_slave) throws Exception {
        List<Server> servers = serverService.getAvailable(companyId, TransferType.FTP.getType(), ServerFlag.SOUND.getId());
        if(servers == null || servers.isEmpty()) return null;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy"+File.separator+"MM"+File.separator+"dd"+File.separator);
        
        Server server = servers.get(0);
        String path = server.getServerFolder();
        path = path + File.separator + companyId + File.separator + AttachmentTargetType.SOUND.getName() + File.separator + sdf.format(new Date());
        String host = server.getServerHost();
        int port = server.getServerPort();
        String username = server.getServerUsername();
        String password = server.getDecryptServerPassword();
        boolean security = getBoolean(server.getServerSsl());
        String protocol = server.getServerProtocol();
        String servertype = server.getServerType();
        
        TransferType tt = TransferType.getTransferType(servertype);
        HttpParameter param_http = HttpParameter.getInstance(TransferType.HTTP);
        param_http.url(recordingUrl);
        
        Parameter param_ftp = Parameter.getInstance(tt).manualconfig(true).storeDb(false);
        param_ftp.host(host).port(port).username(username).password(password).security(security).protocol(protocol)
                .uploadfilename(callSid + ".mp3").uploadpath(path).createfolderifnotexists();
        
        BaseFileTransfer ftpTransfer = FileTransferFactory.getTransfer(param_ftp);
        param_http.callback(Arrays.asList(ftpTransfer));
        
        FileTransferFactory.getTransfer(param_http).upload(null);
        
        gnext.dbutils.model.Attachment attachment = param_ftp.getAttachment();
        if(attachment == null) throw new Exception("Upload recording error.");
        Attachment ea = MapObjectUtil.convert(attachment);
        ea.setAttachmentTargetType(AttachmentTargetType.SOUND.getId());
        ea.setAttachmentTargetId(historyId);
        ea.setServer(server);
        ea.setCompany(new Company(companyId));
        ea.setCreatorId(1);
        
        JPAUtils.create(ea, em_slave, false);
        return ea.getAttachmentId();
    }
    
    /**
     * Hàm xử lí chuyển đổi Short sang Boolean.
     * @param flag
     * @return 
     */
    private boolean getBoolean(Short flag) {
        if (flag != null && flag == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Twilio> findByCompanyId(Integer companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Query q = em_slave.createQuery(
                    "SELECT o FROM Twilio o WHERE o.company.companyId = :companyId ORDER BY o.twilioId DESC"
                    , Twilio.class)
                    .setParameter("companyId", companyId);
            if(q != null) return q.getResultList();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
    
    @Override
    public List<Twilio> search(Integer companyId, String query) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Query q = null;
            StringBuilder nq = new StringBuilder("SELECT o FROM Twilio o WHERE o.company.companyId = :companyId");

            if (!StringUtils.isEmpty(query)) {
                nq.append(" AND ").append(query);
            }
            nq.append(" ORDER BY o.twilioId DESC");
            q = em_slave.createQuery(nq.toString()).setParameter("companyId", companyId);

            return q.getResultList();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public TwilioHistory getHistoryById(Integer historyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Query q = em_slave.createQuery("SELECT o FROM TwilioHistory o WHERE o.twilioHistoryId = :historyId").setParameter("historyId", historyId);
            return (TwilioHistory) q.getSingleResult();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<TwilioHistory> getHistoryByCallId(String callSid) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            return em_slave.createQuery("SELECT o FROM TwilioHistory o WHERE o.conferenceId = :conferenceId")
                .setParameter("conferenceId", callSid)
                .getResultList();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public Twilio getByCallId(String twilioCallSid) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Query q = em_slave.createQuery("SELECT o FROM Twilio o WHERE o.conferenceId = :callSid");
            q.setParameter("callSid", twilioCallSid);
            return (Twilio)q.getSingleResult();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
}