/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.multitenancy;

import gnext.service.DatabaseServerService;
import gnext.utils.PropertiesUtil;
import gnext.utils.StringUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;

/**
 *
 * @author daind
 */
@ApplicationScoped
public class ProxyMasterEntityManager implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(ProxyMasterEntityManager.class);
    public static Map<String, Object> services = new HashMap<>();
    
    @EJB private DatabaseServerService databaseServerService;
    private EntityManagerFactory entityManagerFactory = null;
    
    @PostConstruct public void init() {
        this.entityManagerFactory = createEntityManagerFactory();
        services.put("DatabaseServerService", databaseServerService);
    }
    @PreDestroy public void destroy() {
        try {
            services.clear();

            if(this.entityManagerFactory != null) {
                try {
                    this.entityManagerFactory.close();
                    this.entityManagerFactory = null; // release memory.
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

            if(!ProxySlaveEntityManager.slaveEntityManagerCache.isEmpty()) {
                synchronized(this) {
                    Iterator<Map.Entry<Integer, EntityManagerFactory>> it = ProxySlaveEntityManager.slaveEntityManagerCache.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Integer, EntityManagerFactory> pair = it.next();
                        EntityManagerFactory emf = pair.getValue();
                        try {
                            emf.close();
                            emf = null; // release memory.
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                    ProxySlaveEntityManager.slaveEntityManagerCache.clear();
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    public EntityManager getEntityManager() {
        if(entityManagerFactory == null) return null;
        return entityManagerFactory.createEntityManager();
        
//        ClassLoader classLoader = this.getClass().getClassLoader();
//        return (EntityManager) Proxy.newProxyInstance(classLoader, new Class<?>[]{EntityManager.class},
//                (proxy, method, args) -> {
//                    return method.invoke(target, args);
//                });
    }
    
    /**
     * Hàm trả về một EntityManager dựa vào công ty đã logined.
     * @return 
     */
    private EntityManagerFactory createEntityManagerFactory() {
        LOGGER.info("[MASTER] Begin create master entity-manager.");
        
        PropertiesUtil master_config = PropertiesUtil.getInstance();
        Properties master_config_properties = master_config.loadConf(StringUtil.DEFAULT_DB_PROPERTIES);
        
        if(master_config_properties == null) throw new IllegalArgumentException("Please provider the system configuration.");
        if(!master_config.checkSystemConfig(master_config_properties)) throw new IllegalArgumentException("Please verify the content of system configuration.");
        
        String url = master_config_properties.getProperty("url");
        String driver = master_config_properties.getProperty("driver");
        String usr = master_config_properties.getProperty("usr");
        String pwd = master_config_properties.getProperty("pwd");
        
        final Map<String, String> properties = new TreeMap<>();
        
        properties.put(StringUtil.JPA_PROVIDER, StringUtil.JPA_ECLIPSELINK_PROVIDER);
        properties.put(PersistenceUnitProperties.TRANSACTION_TYPE, StringUtil.JPA_USE_RESOURCE_LOCAL);
        
        properties.put(PersistenceUnitProperties.JDBC_DRIVER, driver);
        properties.put(PersistenceUnitProperties.JDBC_URL, url);
        properties.put(PersistenceUnitProperties.JDBC_USER, usr);
        properties.put(PersistenceUnitProperties.JDBC_PASSWORD, pwd);
        
        // EclipseLink interprets zero values as zero. This permits primary keys to use a value of zero.
        properties.put(PersistenceUnitProperties.ID_VALIDATION, "NULL");
        
        // <!--  Optimization: statement caching -->
//        properties.put(PersistenceUnitProperties.CACHE_STATEMENTS, "true");
        // <!--  Optimization: turn logging off -->
//        properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
        // <!-- Optimization: close EntityManager on commit, to avoid cost of resume -->
//        properties.put(PersistenceUnitProperties.PERSISTENCE_CONTEXT_CLOSE_ON_COMMIT, "true");
        // <!-- Optimization: avoid auto flush cost on query execution -->
//        properties.put(PersistenceUnitProperties.PERSISTENCE_CONTEXT_FLUSH_MODE, "commit");
        // <!-- Optimization: avoid cost of persist on commit -->
//        properties.put(PersistenceUnitProperties.PERSISTENCE_CONTEXT_PERSIST_ON_COMMIT, "false");
        
        // <!-- Optimization: Settings the connection pool -->
        properties.put(PersistenceUnitProperties.JDBC_CONNECTIONS_INITIAL, "1");
        properties.put(PersistenceUnitProperties.JDBC_CONNECTIONS_MIN, "64");
        properties.put(PersistenceUnitProperties.JDBC_CONNECTIONS_MAX, "64");
        
        return Persistence.createEntityManagerFactory(StringUtil.PERSISTENCE_UNIT_NAME, properties);
    }
}
