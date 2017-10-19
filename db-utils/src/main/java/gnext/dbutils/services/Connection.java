/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.services;

import gnext.dbutils.model.Company;
import gnext.dbutils.model.DatabaseServer;
import gnext.dbutils.processor.AnnotationRowProcessor;
import gnext.dbutils.util.Console;
import gnext.dbutils.util.FileUtil;
import gnext.dbutils.util.SqlUtil;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import lombok.Getter;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public final class Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);
    public static final AnnotationRowProcessor ANNOTATION_ROW_PROCESSOR = new AnnotationRowProcessor();
    
    private String url;
    private String driver;
    private String usr;
    private String pwd;
    private String host;
    private String port;
    private String hostname;
    private String schema;
    
    @Getter private String path;

    public Connection(String path, Integer companyId) {
        if(companyId == null) throw new IllegalArgumentException("The parameter companyId must be not null.");
        
        Properties prop = FileUtil.loadConf(path);
        if(prop == null) return;
        
        this.path = path;
        this.url = prop.getProperty("url");
        this.driver = prop.getProperty("driver");
        this.usr = prop.getProperty("usr");
        this.pwd = prop.getProperty("pwd");
        this.schema = prop.getProperty("schema");
        
        /** trường hợp là super-admin thì không cần lookup từ database */
        if(companyId != 1) {
            DatabaseServer ds = findDatabaseServerByCompanyId(companyId);
            if(ds == null) throw new IllegalArgumentException("Can not find the server host of comanyid="+companyId);
            this.driver = ds.getDatabase_server_driver();
            this.host = ds.getDatabase_server_host();
            this.port = ds.getDatabase_server_port();
            this.usr = ds.getDatabase_server_username();
            this.pwd = ds.getDatabase_server_password();
            this.hostname = ds.getDatabase_server_name();
            this.url = String.format("jdbc:mysql://%s:%s/%s?characterEncoding=utf-8&amp;charset=utf8mb4", host, port, (companyId ==1 ? this.schema : getSchemaName(companyId)));
        }
    }
    
    private String getSchemaName(Integer companyId) {
        String newTableName = String.format(this.schema + "_%03d", companyId);
        return newTableName;
    }
    
    public java.sql.Connection getConnection() throws Exception {
        try {
            DbUtils.loadDriver(driver);
            return DriverManager.getConnection(url, usr, pwd);
        } catch (SQLException e) {
            Console.error("An error occurred. Maybe user/password is invalid");
        }  catch (Exception e) {
            Console.error(e.getMessage());
        }
        throw new Exception("A error occur when try connection to MAIL database.");
    }
    
    /**
     * Hàm tìm kiếm Company theo company_id.
     * @param companyId
     * @return 
     */
    public Company findCompanyById(Integer companyId) {
        java.sql.Connection conn = null;
        try {
            conn = getConnection();
            ResultSetHandler<Company> handler = new BeanHandler<>(Company.class, ANNOTATION_ROW_PROCESSOR);
            String sql = SqlUtil.genSelectSql(conn, Company.class, Arrays.asList("company_id"));
            return new QueryRunner().query(conn, sql, handler, companyId);
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        } finally { DbUtils.closeQuietly(conn); }
    }
    
    public DatabaseServer findDatabaseServerByCompanyId(Integer companyId) {
        java.sql.Connection conn = null;
        try {
            conn = getConnection();
            ResultSetHandler<List<DatabaseServer>> handler = new BeanListHandler<>(DatabaseServer.class, ANNOTATION_ROW_PROCESSOR);
            String sql = "select * from crm_database_server where database_server_id in (select database_server_id from crm_database_server_company_rel where company_id=?)";
            List<DatabaseServer> databaseServers = new QueryRunner().query(conn, sql, handler, companyId);
            
            if(databaseServers != null && !databaseServers.isEmpty())
                return databaseServers.get(0);
            
            return null;
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        } finally { DbUtils.closeQuietly(conn); }
    }
}
