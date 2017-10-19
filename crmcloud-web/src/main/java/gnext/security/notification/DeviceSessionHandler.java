package gnext.security.notification;

import gnext.model.authority.UserModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import java.util.Set;
import javax.json.JsonObject;
import javax.websocket.Session;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 *
 * @author hungpham
 * @since Feb 16, 2017
 */
@ApplicationScoped
public class DeviceSessionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeviceSessionHandler.class);
    @Getter private final Map<Integer, Set<Session>> sessions = new HashMap<>(); //Truong hop multiple server -> su rung redis de giai quyet van de session replication

    public boolean isExists(Integer companyId, Session session){
        return sessions.containsKey(companyId) ? sessions.get(companyId).contains(session): false;
    }
    
    public Session isExists(Integer companyId, String username){
        if(sessions.containsKey(companyId)){
            for (Session session : sessions.get(companyId)) {
                if(getUserModel(session).getUsername().equals(username)){
                    return session;
                }
            }
        }
        return null;
    }
    
    public void addSession(Session session) {
        UserModel u = getUserModel(session);
        Integer companyId = u.getCompanyId();
        if(companyId == null) companyId = 0;
        if(!sessions.containsKey(companyId)) sessions.put(companyId, new HashSet<>());
        sessions.get(companyId).add(session);
    }

    public void removeSession(Session session) {
        UserModel u = getUserModel(session);
        Integer companyId = u.getCompanyId();
        if(companyId == null) companyId = 0;
        if(sessions.containsKey(companyId)) sessions.get(companyId).remove(session);
    }
    
    public UserModel getUserModel(Session session){
        try{
            UsernamePasswordAuthenticationToken principal = (UsernamePasswordAuthenticationToken)session.getUserPrincipal();
            return (UserModel) principal.getPrincipal();
        }catch(Exception e){
            return new UserModel();
        }
    }
    
    public void forceCloseIfExistsSession(Integer companyId, String username){
        if(sessions.containsKey(companyId)){
            List<Session> forceClosedSessions = new ArrayList<>();
            for (Session session : sessions.get(companyId)) {
                if(username.equals(getUserModel(session).getUsername())){
                    sendToSession(session, MessageProvider.closeSession());
                    forceClosedSessions.add(session);
                }
            }
            if(!forceClosedSessions.isEmpty()) sessions.get(companyId).removeAll(forceClosedSessions);
        }
    }
    
    public void sendToOneWindowActiveSession(Integer companyId, JsonObject message) {
        if(sessions.containsKey(companyId)) {
            List<String> sentList = new ArrayList<>();
            for (Session session : sessions.get(companyId)) {
                String username = getUserModel(session).getUsername();
                if(!sentList.contains(username)){
                    try{
                        sendToSession(session, message);
                    }catch(NullPointerException e){
                        continue;
                    }
                    sentList.add(username);
                }
            }
        }
    }

    public void sendToAllConnectedSessions(Integer companyId, JsonObject message) {
        if(sessions.containsKey(companyId)) {
            for (Session session : sessions.get(companyId)) {
                sendToSession(session, message);
            }
        }
    }

    public void sendToSession(Session session, JsonObject message) {
        try {
            session.getBasicRemote().sendText(message.toString());
        } catch (IOException e) {
            removeSession(session);
            logger.error(e.getMessage(), e);
        }
    }
    
    public void sendToSession(Integer companyId, String username, JsonObject message) {
        try {
            if(sessions.containsKey(companyId)){
                for (Session session : sessions.get(companyId)) {
                    if(getUserModel(session).getUsername().equals(username)){
                        session.getBasicRemote().sendText(message.toString());
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}