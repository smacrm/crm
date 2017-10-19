/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.multitenancy;

import gnext.bean.Company;
import gnext.bean.DatabaseServer;
import gnext.service.DatabaseServerService;
import gnext.utils.PropertiesUtil;
import gnext.utils.StringUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;

/**
 * @author daind
 */
public class ProxySlaveEntityManager {
    private static final Logger LOGGER = Logger.getLogger(ProxySlaveEntityManager.class);
    
    private final int companyId;
    public static final Map<Integer, EntityManagerFactory> slaveEntityManagerCache = Collections.synchronizedMap(new HashMap<>());
    
    public ProxySlaveEntityManager(TenantHolder tenantHolder) {
        if(tenantHolder == null) {
            this.companyId = 0;
        } else {
            this.companyId = tenantHolder.getCompanyId();
        }
    }
    public ProxySlaveEntityManager(int companyId) {
        this.companyId = companyId;
    }
    
    public EntityManager getEntityManager() {
        try {
            return createEntityManagerFactory().createEntityManager();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
        
//        ClassLoader classLoader = this.getClass().getClassLoader();
//        return (EntityManager) Proxy.newProxyInstance(classLoader, new Class<?>[]{EntityManager.class},
//                (proxy, method, args) -> {
//                    return method.invoke(target, args);
//                });
    }
    
    private DatabaseServerService getDatabaseServerService() {
        return (DatabaseServerService) ProxyMasterEntityManager.services.get("DatabaseServerService");
    }
    
    private DataSource getDataSource(int companyId) {
        DataSource dataSource = null;
        
        if(companyId != Company.MASTER_COMPANY_ID) {
            DatabaseServerService databaseServerService = getDatabaseServerService();
            if(databaseServerService == null)  throw new IllegalArgumentException("Can not read ejb bean.");
            DatabaseServer dataServer = databaseServerService.findOneDatabaseServer(companyId);
            if(dataServer == null) throw new IllegalArgumentException("Can not retrieve the configuration of slave db.");

            String driver = dataServer.getDatabaseServerDriver();
            String usr = dataServer.getDatabaseServerUsername();
            String pwd = dataServer.getDatabaseServerPassword();
            String host = dataServer.getDatabaseServerHost();
            String port = dataServer.getDatabaseServerPort();
            
            dataSource = new DataSource(driver, usr, pwd, host, port);
        } else {
            PropertiesUtil master_config = PropertiesUtil.getInstance();
            Properties master_config_properties = master_config.loadConf(StringUtil.DEFAULT_DB_PROPERTIES);

            if(master_config_properties == null) throw new IllegalArgumentException("Please provider the system configuration.");
            if(!master_config.checkSystemConfig(master_config_properties)) throw new IllegalArgumentException("Please verify the content of system configuration.");

            String driver = master_config_properties.getProperty("driver");
            String usr = master_config_properties.getProperty("usr");
            String pwd = master_config_properties.getProperty("pwd");
            String host = master_config_properties.getProperty("host");
            String port = master_config_properties.getProperty("port");
        
            dataSource = new DataSource(driver, usr, pwd, host, port);
        }
        
        return dataSource;
    }
    
    /**
     * The default connection pool size is 32 connections.
     * If you have > 32 active transactions, then the next request will wait until a connection is released.
     * Are you calling close() on your EntityManager and commit() or rollback() on your transaction?
     * You can also configure the connection pool wait time to trigger an exception if the pool size is exceeded,
     * 
     * Hàm trả về một EntityManager dựa vào công ty đã logined.
     * @return 
     */
    private EntityManagerFactory createEntityManagerFactory() {
        LOGGER.info("[SLAVE] Begin create entity-manager base on company " + this.companyId + " logined.");
        if(slaveEntityManagerCache.containsKey(companyId)) return slaveEntityManagerCache.get(companyId);
        
        DataSource ds = getDataSource(companyId);
        String driver = ds.getDriver();
        String usr = ds.getUsr();
        String pwd = ds.getPwd();
        String host = ds.getHost();
        String port = ds.getPort();
        
        final Map<String, String> properties = new TreeMap<>();
        
        properties.put(StringUtil.JPA_PROVIDER, StringUtil.JPA_ECLIPSELINK_PROVIDER);
        properties.put(PersistenceUnitProperties.TRANSACTION_TYPE, StringUtil.JPA_USE_RESOURCE_LOCAL);
        
        properties.put(PersistenceUnitProperties.JDBC_DRIVER, driver);
        properties.put(PersistenceUnitProperties.JDBC_URL, String.format("jdbc:mysql://%s:%s/%s?characterEncoding=utf-8&amp;charset=utf8mb4", host, port, getSchemaName()));
        properties.put(PersistenceUnitProperties.JDBC_USER,     usr);
        properties.put(PersistenceUnitProperties.JDBC_PASSWORD, pwd);
        
        // EclipseLink interprets zero values as zero. This permits primary keys to use a value of zero.
        properties.put(PersistenceUnitProperties.ID_VALIDATION, "NULL");
        
        // <!--  Optimization: statement caching -->
//        properties.put(PersistenceUnitProperties.CACHE_STATEMENTS, "false");
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
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(StringUtil.PERSISTENCE_UNIT_NAME, properties);
        slaveEntityManagerCache.put(companyId, emf);
        return emf;
    }
    
    private String getSchemaName() {
        PropertiesUtil master_config = PropertiesUtil.getInstance();
        Properties master_config_properties = master_config.loadConf(StringUtil.DEFAULT_DB_PROPERTIES);
        
        if(master_config_properties == null) throw new IllegalArgumentException("Please provider the system configuration.");
        if(!master_config.checkSystemConfig(master_config_properties)) throw new IllegalArgumentException("Please verify the content of system configuration.");
        
        String schema = master_config_properties.getProperty("schema");
        if(this.companyId <= 0 || this.companyId == 1) return schema; // trả về masterdb.
        String newTableName = String.format(schema + "_%03d", this.companyId);
        return newTableName;
    }
}
