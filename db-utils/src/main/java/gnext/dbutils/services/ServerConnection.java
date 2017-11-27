/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.services;

import gnext.dbutils.model.Server;
import static gnext.dbutils.services.Connection.ANNOTATION_ROW_PROCESSOR;
import gnext.dbutils.util.SqlUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public class ServerConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnection.class);
    private final Connection connection;
    public ServerConnection(Connection connection) { this.connection = connection; }
    
    /**
     * Hàm tìm kiếm Ftp Server theo serverid.
     * @param serverId
     * @return 
     */
    public List<Server> findServerById(Integer serverId) {
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            ResultSetHandler<List<Server>> handler = new BeanListHandler<>(Server.class, ANNOTATION_ROW_PROCESSOR);
            String sql = SqlUtil.genSelectSql(conn, Server.class, Arrays.asList("server_id"));
            return new QueryRunner().query(conn, sql, handler, serverId);
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage(), e);
            return new ArrayList<>();
        } finally { DbUtils.closeQuietly(conn); }
    }
    
    /**
     * Hàm trả về danh sách server của GNEXT.
     * @return 
     */
    public List<Server> findServerGnext() {
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            ResultSetHandler<List<Server>> handler = new BeanListHandler<>(Server.class, ANNOTATION_ROW_PROCESSOR);
            String sql = SqlUtil.genSelectSql(conn, Server.class, Arrays.asList("server_gnext"));
            return new QueryRunner().query(conn, sql, handler, 1);
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage(), e);
            return new ArrayList<>();
        } finally { DbUtils.closeQuietly(conn); }
    }
}
