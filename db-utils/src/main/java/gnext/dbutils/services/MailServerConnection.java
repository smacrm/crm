/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.services;

import gnext.dbutils.model.MailServer;
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
public class MailServerConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailServerConnection.class);
    private final Connection connection;
    public MailServerConnection(Connection connection) { this.connection = connection; }
    
    /**
     * Hàm tìm kiếm MailServer theo server_id.
     * @param serverId
     * @return 
     */
    public MailServer findMailServerById(Integer serverId) {
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            ResultSetHandler<MailServer> handler = new BeanHandler<>(MailServer.class, ANNOTATION_ROW_PROCESSOR);
            String sql = SqlUtil.genSelectSql(conn, MailServer.class, Arrays.asList("server_id"));
            return new QueryRunner().query(conn, sql, handler, serverId);
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        } finally { DbUtils.closeQuietly(conn); }
    }
}
