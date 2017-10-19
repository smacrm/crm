package gnext.security.notification;

import java.io.StringReader;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Feb 16, 2017
 */
@ApplicationScoped
@ServerEndpoint("/nc") //notification center
public class DeviceSocketServer {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceSocketServer.class);
    
    @Inject private DeviceSessionHandler sessionHandler;

    @OnOpen
    public void open(Session session) {
        //System.out.println("OnOpen");
        sessionHandler.addSession(session);
    }

    @OnClose
    public void close(Session session) {
        //System.out.println("OnClose");
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {
        //logger.error(error.getMessage(), error);
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            JsonObject jsonMessage = reader.readObject();
            String action = jsonMessage.getString("action");
            switch(action){
                case "debug":
                    System.out.println("Session size: " + sessionHandler.getSessions().size() + ":" + sessionHandler.getSessions().size());
                    break;
                case "test":
                    Integer companyId = jsonMessage.getInt("id");
                    sessionHandler.sendToOneWindowActiveSession(companyId, 
                    MessageProvider.getJsCommand("CRMCloudTwilio.openNewTab('%s', '%s', '%s')", "aaaa", "aaaa", "xxxx"));
                    break;
                case "logout":
                    sessionHandler.sendToSession(session, MessageProvider.closeSession());
                    break;
            }
        }
    }
    
    
}
