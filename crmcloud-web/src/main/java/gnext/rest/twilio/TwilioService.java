package gnext.rest.twilio;

import com.google.gson.Gson;
import com.twilio.twiml.Say;
import gnext.bean.Member;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.bean.softphone.Twilio;
import gnext.bean.softphone.TwilioHistory;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.security.notification.DeviceSessionHandler;
import gnext.security.notification.MessageProvider;
import gnext.service.MemberService;
import gnext.service.config.ConfigService;
import gnext.rest.twilio.bean.WaitingCall;
import gnext.rest.twilio.utils.TwilioAuthenticatedActions;
import gnext.rest.twilio.utils.TwimlBuilder;
import gnext.service.attachment.AttachmentService;
import gnext.service.attachment.ServerService;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 *
 * @author hungpham
 * @since Dec 22, 2016
 */
@ApplicationScoped
@Path("/twilio")
@Produces(MediaType.APPLICATION_XML)
public class TwilioService extends BaseTwilioService{

    private static final long serialVersionUID = -7764124490010593599L;
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @EJB private MemberService memberservice;
    @EJB private gnext.service.softphone.TwilioService twilio;
    @EJB private ConfigService configService;
    @EJB private ServerService serverService;
    @EJB private AttachmentService attachmentService;
    
    private String baseUrl;
    private Map<String, String> forwardToList;
    
    final private String CALL_CENTER_QUEUE_NAME = "CALLCENTER";
    
    @Inject private DeviceSessionHandler sessionHandler;
    
    @PostConstruct
    public void TwilioService(){
        logger.info("Initial TwilioAuthenticatedActions");
        this.twilioAuthenticatedActions = new TwilioAuthenticatedActions();
        this.baseUrl = configService.get("WEBSITE_URL").replaceAll("/$", "");
        forwardToList = new HashMap<>();
    }
    
    private Member getMember(String username){
        try{
            return memberservice.findByUsername(username.replaceAll("client:", ""));
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Start Call Center (Index action)
     * 
     * @param form
     * @param from
     * @param to
     * @param source
     * @return
     * @throws com.twilio.twiml.TwiMLException 
     */
    @POST
    public Response callCenter(Form form, 
            @FormParam("From") String from,
            @FormParam("To") String to,
            @DefaultValue("") @FormParam("Source") String source) throws com.twilio.twiml.TwiMLException{
        debug("callCenter", form);
        if( source.equals("CRMCLOUD") ){ //from inside to specified phonenumber
            if (StringUtils.isEmpty(to)) {
                return Response.serverError().build();
            }
            Member member = getMember(from);
            Integer memberId = member.getMemberId();
            Integer companyId = member.getGroup().getCompany().getCompanyId();
            initConfigParams(companyId, memberId);
            
            TwimlBuilder twimlBuilder = new TwimlBuilder(this.baseUrl);
            
            if (Pattern.compile("^[\\d\\(\\)\\- \\+]+$").matcher(to).matches()) { // call to phonenumber
                return Response.ok(twimlBuilder.generateOutboundCall(to, getConfig().getPhoneNumber()).toEscapedXML()).build();
            } else { // call to other agent
                //String say = "Welcome to our call center! Please wait until someone is ready to assist you.";
                //sessionHandler.sendToSession(companyId, to, 
                //        MessageProvider.getJsCommand("CRMCloudTwilio.notifyPrivateIncommingCall('%s', '%s')", from, from));
                return Response.ok(twimlBuilder.generateEnQueue(to, to).toEscapedXML()).build();
            }
        }else{// from outside to callcenter
            initConfigParams(to);
            Integer companyId = getConfig().getCompanyId();
            return Response.ok(new TwimlBuilder(this.baseUrl).generateEnQueue(this.CALL_CENTER_QUEUE_NAME + "-" + companyId).toEscapedXML()).build();
        }
    }
    
    /**
     * Add to queue event
     * @param form
     * @param agent
     * @param from
     * @param to
     * @param callSid
     * @param queueSize
     * @return
     * @throws com.twilio.twiml.TwiMLException 
     */
    @POST
    @Path("/wait/{Agent:.*}")
    public Response wait(Form form, 
            @DefaultValue("") @PathParam("Agent") String agent,
            @FormParam("From") String from, 
            @FormParam("To") String to, 
            @FormParam("CallSid") String callSid, 
            @FormParam("CurrentQueueSize") int queueSize) throws com.twilio.twiml.TwiMLException{
        debug("wait", form);
        //wait
        initConfigParams(to);
        Integer companyId = getConfig().getCompanyId();
        
        String say = null;
        String locale = getConfig().getLocale();
        Say.Language lang = Say.Language.JA_JP;
        switch(locale){
            case "en":
                lang = Say.Language.EN_US;
                break;
            case "vi":
                lang = Say.Language.EN_US;
                break;
            default:
        }
        
        if(StringUtils.isEmpty(agent)){
            say = JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_SOFTPHONE, new Locale(locale), "say.welcome", queueSize);
            //open with sepcified member
            sessionHandler.sendToOneWindowActiveSession(companyId, 
                    MessageProvider.getJsCommand("CRMCloudTwilio.openNewTab('%s', '%s', '%s', %s)", from, from, callSid, getConfig().getAllowMemberList()));
        }else{
            from = from.replaceAll("client:", "");
            sessionHandler.sendToSession(companyId, agent, 
                    MessageProvider.getJsCommand("CRMCloudTwilio.notifyPrivateIncommingCall('%s', '%s', '%s')", from, from, callSid));
        }
        
        twilio.startConference(Twilio.INCOMMING_CALL, from, to, callSid, companyId);
        String redirectUrl = String.format("%s/rest/twilio/wait/%s", this.baseUrl, org.apache.commons.lang3.StringUtils.isEmpty(agent) ? "" : agent);
        int musicLoopTimes = 1;
        
        return Response.ok(new TwimlBuilder(this.baseUrl).generateWait(musicLoopTimes, say, lang, redirectUrl).toEscapedXML()).build();
    }
    
    /**
     * join call on the queue
     * @param form
     * @param queueName
     * @param from
     * @return
     * @throws com.twilio.twiml.TwiMLException 
     */
    @POST
    @Path("/join/{QueueName:.*}")
    public Response join(Form form, 
            @DefaultValue("") @PathParam("QueueName") String queueName,
            @FormParam("From") String from) throws com.twilio.twiml.TwiMLException{
        debug("join", form);
        //connected
        initConfigParams(from);
        int companyId = getConfig().getCompanyId();
        
        TwimlBuilder twimlBuilder = new TwimlBuilder(this.baseUrl);
        
        if(StringUtils.isEmpty(queueName)){
            sessionHandler.sendToAllConnectedSessions(companyId, 
                    MessageProvider.getJsCommand("CRMCloudTwilio.notifyStopIncommingCall()"));
            queueName = this.CALL_CENTER_QUEUE_NAME + "-" + companyId;
        }
        return Response.ok(twimlBuilder.generateDialQueue(queueName).toEscapedXML()).build();
    }
    
    /**
     * forward the call
     * @param form
     * @param to
     * @param callPhoneNumber
     * @param callSid
     * @return
     * @throws com.twilio.twiml.TwiMLException 
     */
    @POST
    @Path("/forward")
    public Response forward(Form form, 
            @FormParam("To") String to,
            @FormParam("CallPhoneNumber") String callPhoneNumber,
            @FormParam("CallSid") String callSid) throws com.twilio.twiml.TwiMLException{
        debug("forward", form);
        //forward
        forwardToList.put(callSid, to);
        
        return Response.ok().build();
    }
    
    /**
     * answer the call
     * @param form
     * @param to
     * @param companyId
     * @param memberId
     * @param privateCall
     * @param callSid
     * @return
     * @throws com.twilio.twiml.TwiMLException 
     */
    @POST
    @Path("/answer")
    public Response answer(Form form,
            @FormParam("To") String to, 
            @FormParam("companyId") Integer companyId, 
            @FormParam("memberId") Integer memberId, 
            @FormParam("Private") boolean privateCall,
            @FormParam("CallSid") String callSid) throws com.twilio.twiml.TwiMLException, Exception {
        debug("answer", form);
        //answer
        //init twilio config
        initConfigParams(companyId, memberId);
        
        String callback = String.format("%s/rest/twilio/join/%s", this.baseUrl, privateCall ? to : "");
        String recordCallback = String.format("%s/rest/twilio/record/%d/%s/%s", this.baseUrl, companyId, to, callSid);
        twilioAuthenticatedActions.callAgent(to, callback, recordCallback, getConfig().getPhoneNumber(), getConfig().getAccountSid(), getConfig().getAccountAuthToken());
        twilio.updateConference(callSid, to, Twilio.STATUS_IN_PROGRESS, companyId);
        return Response.ok().build();
    }
    
    /**
     * record the call
     * @param form
     * @param recordingUrl
     * @param recordingStatus
     * @param recordingDuration
     * @param companyId
     * @param callSid
     * @param agentId
     * @return
     * @throws com.twilio.twiml.TwiMLException 
     */
    @POST
    @Path("/record/{CompanyId}/{AgentId}/{CallSid}")
    public Response record(Form form,
            @FormParam("RecordingUrl") String recordingUrl,
            @FormParam("RecordingStatus") String recordingStatus,
            @FormParam("RecordingDuration") Integer recordingDuration,
            @PathParam("CompanyId") Integer companyId,
            @PathParam("AgentId") String agentId,
            @PathParam("CallSid") String callSid
            ) throws com.twilio.twiml.TwiMLException{
        debug("record", form);
        twilio.updateRecording(companyId, callSid, agentId, recordingUrl, recordingStatus, recordingDuration);
        //https://api.twilio.com/2010-04-01/Accounts/AC13bedd79718dbc136bb9f4fc8532b2bd/Recordings/REec05fc2e2b3884b389934b2e941eb409
        //https://api.twilio.com/2010-04-01/Accounts/AC13bedd79718dbc136bb9f4fc8532b2bd/Recordings/RE85b610b29dc63ca4650f00070b08cfb3
        
        return Response.ok().build();
    }
    
    /**
     * leave call event
     * @param form
     * @param callSid
     * @param conferenceSid
     * @param from
     * @param to
     * @return
     * @throws com.twilio.twiml.TwiMLException 
     */
    @POST
    @Path("/end")
    public Response end(Form form, 
            @FormParam("CallSid") String callSid,
            @FormParam("ConferenceSid") String conferenceSid,
            @FormParam("From") String from,
            @FormParam("To") String to) throws com.twilio.twiml.TwiMLException, Exception {
        //forward or end call

        initConfigParams(to);
        Integer companyId = getConfig().getCompanyId();
        
        TwimlBuilder twimlBuilder = new TwimlBuilder(this.baseUrl);
        String forwardTo = forwardToList.get(callSid);
        if(StringUtils.isEmpty(forwardTo)){
            //end call
            debug("end", form);
            sessionHandler.sendToAllConnectedSessions(companyId, 
                    MessageProvider.getJsCommand("CRMCloudTwilio.notifyStopIncommingCall(true)"));
            twilio.finishConference(callSid, companyId);
            return Response.ok(twimlBuilder.generateLeave().toEscapedXML()).build();
        }else{
            debug("end with forward", form);
            forwardToList.remove(callSid);
            twilio.forwardConference(callSid, forwardTo, companyId);
            return Response.ok(twimlBuilder.generateEnQueue(forwardTo, forwardTo).toEscapedXML()).build();
        }
    }
    
    @GET
    @Path("/record/{CallSid}")
    @Produces("audio/mpeg")
    public Response record(@PathParam("CallSid") String callSid) throws IOException{
        byte[] data = null;
        if(NumberUtils.isNumber(callSid)){
            Integer twilioHistoryId = Integer.parseInt(callSid);
            TwilioHistory history = twilio.getHistoryById(twilioHistoryId);
            Integer recordId = history.getRecordingId();
            if(recordId == null || recordId == 0){
                return Response.ok().build();
            }else{
                List<Server> servers = serverService.getAvailable(history.getCompany().getCompanyId(), TransferType.FTP.getType(), ServerFlag.SOUND.getId());
                if(servers == null || servers.isEmpty()) return Response.ok().build();
                data = getCombineRecordings(loadRecordRemoteFile(servers.get(0), recordId));
            }
        }else{
            List<TwilioHistory> historyList = twilio.getHistoryByCallId(callSid);
            
            if(historyList.isEmpty()) return Response.ok().build();
            List<Server> servers = serverService.getAvailable(historyList.get(0).getCompany().getCompanyId(), TransferType.FTP.getType(), ServerFlag.SOUND.getId());
            if(servers == null || servers.isEmpty()) return Response.ok().build();
            
            List<InputStream> isList = new ArrayList<>();
            historyList.forEach((item) -> {
                if(item.getRecordingId() != null && item.getRecordingId() > 0){
                        isList.add(loadRecordRemoteFile(servers.get(0), item.getRecordingId()));
                }
            });
            data = getCombineRecordings(
                isList.toArray(new InputStream[isList.size()])
            );
        }
        
        
        return data != null ? Response.ok(new ByteArrayInputStream(data)).build() : Response.ok().build();
    }
    
    private InputStream loadRecordRemoteFile(Server server, Integer attachmentId) {
        try {
            Attachment attachment = attachmentService.find(attachmentId);
            
            String host = server.getServerHost();
            int port = server.getServerPort();
            String username = server.getServerUsername();
            String password = server.getDecryptServerPassword();
            boolean security = StatusUtil.getBoolean(server.getServerSsl());
            String protocol = server.getServerProtocol();
            String servertype = server.getServerType();
            
            TransferType tt = TransferType.getTransferType(servertype);
            Parameter param_ftp = Parameter.getInstance(tt).manualconfig(true).storeDb(false);;
            param_ftp.host(host).port(port).username(username).password(password).security(security).protocol(protocol);
            
            String remotePath2File = attachment.getAttachmentPath() + File.separator + attachment.getAttachmentName();
            InputStream input = FileTransferFactory.getTransfer(param_ftp).download(remotePath2File);
            return input;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
    
    private byte[] getCombineRecordings(InputStream... in) throws IOException {
        byte[] concatBytes = new byte[0];
        for (InputStream is : in){
            concatBytes = ArrayUtils.addAll(concatBytes, IOUtils.toByteArray(is));
        }
        return concatBytes;
    }
    
    @GET
    @Path("/check/{QueueName}")
    public Response check(@PathParam("QueueName") String queueName) throws IOException{
        Member member = getMember(queueName);
        Integer companyId = member.getGroup().getCompany().getCompanyId();
        Integer memberId = member.getMemberId();
        initConfigParams(companyId, memberId);
        List<WaitingCall> queueList = this.twilioAuthenticatedActions.getQueue(queueName, Arrays.asList(queueName, this.CALL_CENTER_QUEUE_NAME + "-" + companyId), getConfig().getAccountSid(), getConfig().getAccountAuthToken());
     
        WaitingCall responseCall = null;
        for(WaitingCall wc : queueList){
            responseCall = wc;
            if(wc.getPrivateCall() && wc.getSize() > 0) break;
        }
        return Response.ok(new Gson().toJson(responseCall)).build();
    }
    
    @GET
    @Path("test")
    public Response test(){
        List<Twilio> list = twilio.findByCompanyId(1);
        
        return Response.ok(list.size()).build();
    }
}