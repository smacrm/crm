package gnext.rest.twilio.utils;

/**
 *
 * @author hungpham
 * @since Dec 27, 2016
 */
import com.twilio.base.ResourceSet;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Queue;
import com.twilio.rest.api.v2010.account.queue.Member;
import com.twilio.type.PhoneNumber;
import gnext.rest.twilio.bean.WaitingCall;
import java.io.Serializable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class TwilioAuthenticatedActions implements Serializable{
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private TwilioRestClient twilioRestClient;
    
    public TwilioRestClient getTwilioRestClient(String accountSid, String accountToken){
        if(twilioRestClient == null){
            twilioRestClient = new TwilioRestClient.Builder(accountSid, accountToken).build();
        }
        
        return twilioRestClient;
    }
    
    public String callAgent(final String agentId, final String callbackUrl, final String recordCallbackUrl, String phoneNumber, String accountSid, String accountToken) {
        Call call = Call.creator(
            new PhoneNumber("client:" + agentId),
            new PhoneNumber(phoneNumber),
            URI.create(callbackUrl)
        ).setTimeout(20) //seconds
        .setRecord(Boolean.TRUE)
        .setRecordingStatusCallback(recordCallbackUrl)
        .create(getTwilioRestClient(accountSid, accountToken));
        String sid = call.getSid();
        return sid;
    }
    
    public List<WaitingCall> getQueue(String queueName, List<String> listContaints,String accountSid, String accountToken){
        TwilioRestClient restClient = getTwilioRestClient(accountSid, accountToken);
        List<WaitingCall> validQueueList = new ArrayList<>();
        
        ResourceSet<Queue> queues = Queue.reader().read(restClient);
        for (Queue queue : queues) {
            if(listContaints.contains(queue.getFriendlyName())){
                WaitingCall wc =  new WaitingCall();
                if(queue.getFriendlyName().equals(queueName)){
                    wc.setPrivateCall(true);
                }
                wc.setSize(queue.getCurrentSize());
                ResourceSet<Member> memberList = Member.reader(queue.getSid()).read(restClient);
                for(Member member: memberList){
                    Call call = Call.fetcher(member.getCallSid()).fetch(restClient);
                    String from = call.getFrom();
                    
                    wc.setName(from);
                    wc.setPhoneNumber(from);
                    wc.setCallsid(member.getCallSid());
                    
                    break;
                }
                validQueueList.add(wc);
            }
        }
        return validQueueList;
    }
}