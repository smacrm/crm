package gnext.caching;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author hungpham
 * @since Jul 14, 2017
 */
public interface Payload extends Serializable{
    
    public enum Alias{
      LABEL, // Danh sach label trong file properties 
      DYNAMIC_lABEL,  // Danh sach label cua dynamic form
      CONFIG, // Danh sach config trong crm_config
      MAINTE, // Danh sach du lieu mainte
    };
    
    public void init();
    public void close();
    
    public <T> boolean bulkSet(String alias, Map<String, T> values, Class<T> _class);
    public <T> boolean set(String alias, String key, T value, Class<T> _class);
    public <T> T get(String alias, String key, Class<T> _class);
    public boolean remove(String alias, String key);
    public boolean exists(String alias, String key);
}
