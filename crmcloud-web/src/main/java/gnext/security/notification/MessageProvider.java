package gnext.security.notification;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;

/**
 *
 * @author hungpham
 * @since Feb 16, 2017
 */
public class MessageProvider {
    public static JsonObject confirmSession(){
        JsonProvider provider = JsonProvider.provider();
        return provider.createObjectBuilder()
                .add("action", "confirm")
                .build();
    }
    
    public static JsonObject closeSession(){
        JsonProvider provider = JsonProvider.provider();
        return provider.createObjectBuilder()
                .add("action", "close")
                .build();
    }
    
    public static JsonObject sessionExists(){
        JsonProvider provider = JsonProvider.provider();
        return provider.createObjectBuilder()
                .add("action", "confirm")
                .add("data", "SESSION_EXISTS")
                .build();
    }
    
    public static JsonObject sessionNotExists(){
        JsonProvider provider = JsonProvider.provider();
        return provider.createObjectBuilder()
                .add("action", "confirm")
                .add("data", "SESSION_NOT_EXISTS")
                .build();
    }
    
    public static JsonObject getJsCommand(String command, String... params){
        JsonProvider provider = JsonProvider.provider();
        return provider.createObjectBuilder()
                .add("action", "exec")
                .add("data", params.length > 0 ? String.format(command, params) : command)
                .build();
    }
}
