package gnext.rest.twilio;

import gnext.bean.softphone.TwilioConfig;
import gnext.service.softphone.TwilioConfigService;
import gnext.rest.twilio.utils.TwilioAuthenticatedActions;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.ws.rs.core.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Dec 28, 2016
 */
public class BaseTwilioService implements Serializable{

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private static final long serialVersionUID = -3978008125809512834L;
    
    @EJB private TwilioConfigService twilioConfigService;
    
//    // TwiML APP
//    // https://www.twilio.com/console/voice/dev-tools/twiml-apps
//    protected String APP_SID;
//    
//    // API Keys
//    // https://www.twilio.com/console/voice/dev-tools/api-keys
//    protected String API_SID;
//    protected String API_SECRET;
//
//    // Phone Number
//    // https://www.twilio.com/console/phone-numbers/incoming
//    protected String PHONENUMBER;
//
//    // API Credentials
//    // https://www.twilio.com/console/account/settings
//    protected String LIVE_ACCOUNT_SID;
//    protected String LIVE_AUTH_TOKEN;
    
    private TwilioConfig config;

    TwilioAuthenticatedActions twilioAuthenticatedActions;
    
    protected void initConfigParams(String phoneNumber){
        TwilioConfig cfg = twilioConfigService.getByPhonenumber(phoneNumber);
        if(cfg.getTwilioId() != null){
            config = cfg;
        }else{
            throw new ExceptionInInitializerError("Twilio Config not found for phonenumber " + phoneNumber);
        }
    }
    
    public void initConfigParams(Integer companyId, Integer memberId){
        TwilioConfig cfg = twilioConfigService.getByUserId(companyId, memberId);
        if(cfg.getTwilioId() != null){
            config = cfg;
        }else{
            throw new ExceptionInInitializerError("Twilio Config not found for user " + memberId);
        }
    }
    
    public TwilioConfig getConfig() {
        if(config == null){
            throw new ExceptionInInitializerError("Twilio Config not found!");
        }
        return config;
    }
    
    protected void debug(Form form){
        form.asMap().forEach((key, value) -> {
            logger.info(String.format("\t%s = %s", key, value));
        });
    }
    
    protected void debug(String block,Form form){
        logger.info("------- "+block+" -------");
        debug(form);
    }
}
