/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.services;

import gnext.dbutils.model.Config;
import static gnext.dbutils.services.Connection.ANNOTATION_ROW_PROCESSOR;
import gnext.dbutils.util.SqlUtil;
import java.util.Arrays;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public class ConfigurationConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationConnection.class);
    private final Connection connection;
    public ConfigurationConnection(Connection connection) { this.connection = connection; }
    
    /**
     * Hàm tìm kiếm cấu hình theo key.
     * @param key
     * @return 
     */
    public String findConfigurationByKey(String key) {
        try {
            Config config = findConfiguration(key);
            return config.getConfig_value();
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        }
    }
    
    /**
     * Hàm tìm kiếm cấu hình theo key.
     * @param key
     * @return 
     */
    public Config findConfiguration(String key) {
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            ResultSetHandler<Config> handler = new BeanHandler<>(Config.class, ANNOTATION_ROW_PROCESSOR);
            String sql = SqlUtil.genSelectSql(conn, Config.class, Arrays.asList("config_key"));
            Config config = new QueryRunner().query(conn, sql, handler, key);
            return config;
        } catch(Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        } finally { DbUtils.closeQuietly(conn); }
    }
}
