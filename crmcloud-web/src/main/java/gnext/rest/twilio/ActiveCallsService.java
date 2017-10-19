package gnext.rest.twilio;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hungpham
 * @since Dec 27, 2016
 */
public class ActiveCallsService {

    public static Map<String, String> localPersistence = new HashMap<>();

    public static void saveNewConference(String agentId, String conferenceId) {
        localPersistence.put(agentId, conferenceId);
    }

    public static String getConferenceFromAgentID(String agentId) {
        return localPersistence.get(agentId);
    }
}
