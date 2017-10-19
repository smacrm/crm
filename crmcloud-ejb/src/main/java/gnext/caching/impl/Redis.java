package gnext.caching.impl;

import gnext.bean.config.Config;
import gnext.multitenancy.MasterConnection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 
 * @author hungpham
 */
public class Redis extends AbstractPayload{
    
    private static final long serialVersionUID = 361468224638399767L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Redis.class);
    
    @Inject @MasterConnection  private EntityManager em_master;
    protected EntityManager getEntityManager() { em_master.clear(); return em_master; }
    
    private Jedis redis;
    private boolean allow = true;

    @Override
    public void init(){
        List<Config> redisConfigs = getEntityManager().createQuery("SELECT o FROM Config o WHERE o.configGroup = :group", Config.class)
                    .setParameter("group", "REDIS_CACHE").getResultList();
        String host = "127.0.0.1";
        int port = 6379;
        
        for(Config cfg : redisConfigs){
            switch(cfg.getConfigKey()){
                case "REDIS_HOST":
                    host = cfg.getConfigValue().trim();
                    break;
                case "REDIS_PORT":
                    if(StringUtils.isNumeric(cfg.getConfigValue().trim())){
                    port = Integer.parseInt(cfg.getConfigValue().trim());
                    }
                case "REDIS_ENABLE":
                    allow = cfg.getConfigValue().equalsIgnoreCase("true");
                    break;
            }
        }
        if(allow){
            redis = new Jedis(host, port);
        }
    }
    
    @Override
    public <T> boolean bulkSet(String alias, Map<String, T> values, Class<T> _class){
        if ( allow && "PONG".equals(redis.ping()) ) {
            Pipeline pipeline = redis.pipelined();
            values.forEach((key, value) -> {
                pipeline.set(getKey(alias, key), value.toString());
            });
            pipeline.sync();
            return true;
        }
        return false;
    }
    
    @Override
    public <T> boolean set(String alias, String key, T value, Class<T> _class){
        if( allow && "PONG".equals(redis.ping()) ){
            redis.set(getKey(alias, key), value.toString());
            return redis.exists(getKey(alias, key));
        }
        return false;
    }
    
    @Override
    public <T> T get(String alias, String key, Class<T> _class){
        if( allow && "PONG".equals(redis.ping()) ){
            return (T) redis.get(getKey(alias, key));
        }
        return null;
    }

    @Override
    public boolean remove(String alias, String key) {
        if( allow && "PONG".equals(redis.ping()) ){
            redis.del(getKey(alias, key));
            return redis.exists(getKey(alias, key));
        }
        return false;
    }
    
    @Override
    public boolean exists(String alias, String key){
        if( allow && "PONG".equals(redis.ping()) ){
            return redis.exists(getKey(alias, key));
        }
        return false;
    }
    
    private String getKey(String alias, String key){
        return String.format("%s:%s", alias, key);
    }

    @Override
    public void close() {
        if( allow && "PONG".equals(redis.ping()) ){
            redis.close();
        }
    }
}
