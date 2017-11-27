package gnext.caching.impl;

import gnext.caching.Payload;
import java.util.Map;

/**
 *
 * @author hungpham
 * @since Jul 14, 2017
 */
public abstract class AbstractPayload implements Payload {

    private static final long serialVersionUID = 7902702791953196900L;

    @Override
    public <T> boolean bulkSet(String alias, Map<String, T> values, Class<T> _class) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> boolean set(String alias, String key, T value, Class<T> _class) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T get(String alias, String key, Class<T> _class) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean remove(String alias, String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean exists(String alias, String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
