package gnext.rest.twilio.utils;

/**
 *
 * @author hungpham
 * @since Dec 27, 2016
 */
import com.twilio.twiml.Dial;
import com.twilio.twiml.Enqueue;
import com.twilio.twiml.Leave;
import com.twilio.twiml.Play;
import com.twilio.twiml.Queue;
import com.twilio.twiml.Say;
import com.twilio.twiml.Number;
import com.twilio.twiml.Redirect;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import org.apache.commons.lang3.StringUtils;

public class TwimlBuilder {
    private final VoiceResponse.Builder builder;
    private final String baseUrl;
    private final String WAIT_MUSIC = "http://com.twilio.music.guitars.s3.amazonaws.com/Pitx_-_Long_Winter.mp3";
    
    public TwimlBuilder(String baseUrl) {
        this.baseUrl = baseUrl;
        this.builder = new VoiceResponse.Builder();
    }

    public TwimlBuilder generateWait(int musicLoop, String sayBefore, Say.Language language, String redirectUrl) throws TwiMLException {
        Play play = new Play.Builder(WAIT_MUSIC)
                .loop(musicLoop)
                .build();
        generateSayWait(sayBefore, language);
        builder.play(play);
        builder.redirect(new Redirect.Builder()
                .url(redirectUrl)
                .build());
        return this;
    }
    
    public TwimlBuilder generateSayWait(String sayBefore, Say.Language language) throws TwiMLException {
        if( !StringUtils.isEmpty(sayBefore) ){
            Say say = new Say.Builder(sayBefore)
                    .language(language)
                    .build();
            builder.say(say);
        }
        return this;
    }
    
    public TwimlBuilder generateLeave() throws TwiMLException {
        builder.leave(new Leave());
        return this;
    }

    public TwimlBuilder generateDialQueue(String queueName) throws TwiMLException {
        Dial dial = new Dial.Builder()
                .queue(new Queue.Builder(queueName).build())
//                .record(Dial.Record.RECORD_FROM_ANSWER)
//                .recordingStatusCallback(String.format("%s/rest/twilio/record", this.baseUrl))
                .build();
        builder.dial(dial);
        return this;
    }
    
    public TwimlBuilder generateEnQueue(String queueName) throws TwiMLException {
        return generateEnQueue(queueName, null);
    }
    
    public TwimlBuilder generateEnQueue(String queueName, String agent) throws TwiMLException {
        generateLeave();
        builder.enqueue(new Enqueue.Builder()
                        .queueName(queueName)
                        .waitUrl(String.format("%s/rest/twilio/wait/%s", this.baseUrl, StringUtils.isEmpty(agent) ? "" : agent))
                        .action(String.format("%s/rest/twilio/end", this.baseUrl))
                        .build());
        return this;
    }

    public TwimlBuilder generateOutboundCall(String to, String callerId) {
        Dial dial = new Dial.Builder()
                .number(new Number.Builder(to)
                        .build())
                .callerId(callerId)
//                .record(Dial.Record.RECORD_FROM_ANSWER)
//                .recordingStatusCallback(String.format("%s/rest/twilio/record", this.baseUrl))
                .build();
        builder.dial(dial);
        return this;
    }
    
    public String toEscapedXML() {
        try {
            return this.builder.build().toXml();
        } catch (TwiMLException e) {
            throw new RuntimeException(e);
        }
    }
}