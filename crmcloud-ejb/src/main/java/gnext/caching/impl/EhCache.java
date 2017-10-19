package gnext.caching.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.ehcache.Cache;
import org.ehcache.Cache.Entry;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Jul 14, 2017
 */
public class EhCache extends AbstractPayload{
    private static final Logger LOGGER = LoggerFactory.getLogger(EhCache.class);
    
    private static final long serialVersionUID = 5257227859578940620L;
    
    private CacheManager cacheManager;
    
    final private int POOL_HEAP = 100;
    
    private boolean isInit = false;
    
    final private Map<String, Class> aliasValueClasses = new HashMap<>();
    
    @Override
    public void init() {
        if(!isInit){
            cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
            cacheManager.init();
            isInit = true;
        }
    }
    
    private <T> Cache<String, T> getPipe(String alias, Class<T> _class){
        Cache<String, T> cache = cacheManager.getCache(alias, String.class, _class);
        if(cache == null){
            cache = cacheManager.createCache(alias, 
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, _class, ResourcePoolsBuilder.heap(POOL_HEAP))
                    );
            aliasValueClasses.put(alias, _class);
        }
        return cache;
    }
    
    @Override
    public <T> boolean set(String alias, String key, T value, Class<T> _class) {
        Cache<String, T> cache = getPipe(alias, _class);
        cache.put(key, value);
        
        return cache.containsKey(key);
    }

    @Override
    public <T> boolean bulkSet(String alias, Map<String, T> values, Class<T> _class) {
        final Boolean[] ok = new Boolean[1];
        ok[0] = true;
        values.forEach((key, value) -> {
            ok[0] &= set(alias, key, value, _class);
        });
        return ok[0];
    }

    @Override
    public <T> T get(String alias, String key, Class<T> _class) {
        Cache<String, T> cache = getPipe(alias, _class);
//        if(cache == null) {
//            System.out.println("Khong the lay duoc instance cua Cache.");
//        }
        // duyet tat ca cac key trong cache.
//        explorer(cache);
        
//        System.out.println("Lay key tu trong cache: " + key);
        
        T t = cache.get(key);
//        if(t == null) {
//            System.out.println("Khong the lay key");
//        }
        
        return t;
    }

    @Override
    public boolean exists(String alias, String key) {
        Class _class = aliasValueClasses.containsKey(alias) ? aliasValueClasses.get(alias) : Object.class;
        Cache<String, Object> cache = getPipe(alias, _class);
        return cache.containsKey(key);
    }

    @Override
    public boolean remove(String alias, String key) {
        Class _class = aliasValueClasses.containsKey(alias) ? aliasValueClasses.get(alias) : Object.class;
        Cache<String, Object> cache = getPipe(alias, _class);
        cache.remove(key);
        return cache.containsKey(key);
    }

    @Override
    public void close() {
        if(cacheManager == null) return;
        try {
            cacheManager.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public <T> void explorer(Cache<String, T> cache) {
        Iterator<Entry<String, T>> iter = cache.iterator();
        while(iter.hasNext()) {
            Entry<String, T> entry = iter.next();
            System.out.println("gnext.caching.impl.EhCache.get()" + entry.getKey() + "  -  " + entry.getValue());
        }
    }
}
