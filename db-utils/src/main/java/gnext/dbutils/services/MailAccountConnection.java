/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.services;

import gnext.dbutils.model.MailAccount;
import static gnext.dbutils.services.Connection.ANNOTATION_ROW_PROCESSOR;
import gnext.dbutils.util.SqlUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class MailAccountConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailAccountConnection.class);
    private final Connection connection;
    public MailAccountConnection(Connection connection) { this.connection = connection; }
    
    /**
     * Hàm tìm kiếm tất cả MailAccount.
     * @return 
     */
    public List<MailAccount> findAllMailAccounts() {
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            ResultSetHandler<List<MailAccount>> handler = new BeanListHandler<>(MailAccount.class, ANNOTATION_ROW_PROCESSOR);
            return new QueryRunner().query(conn, SqlUtil.genSelectSql(conn, MailAccount.class, null), handler);
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage(), e);
            return new ArrayList<>();
        } finally { DbUtils.closeQuietly(conn); }
    }
    
    /**
     * Hàm tiếm kiếm MailAccount dựa theo account_receive_flag = 1 AND company_id=?.
     * @param companyid
     * @return 
     */
    public List<MailAccount> findMailReceiveAccounts(Integer companyid) {
        if(companyid == null) return findMailReceiveAccounts();
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            ResultSetHandler<List<MailAccount>> handler = new BeanListHandler<>(MailAccount.class, ANNOTATION_ROW_PROCESSOR);
            String sql = SqlUtil.genSelectSql(conn, MailAccount.class, Arrays.asList("account_receive_flag", "company_id"));
            return new QueryRunner().query(conn, sql, handler, 1, companyid);
        } catch(Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return new ArrayList<>();
        } finally { DbUtils.closeQuietly(conn); }
    }
    
    /**
     * Hàm tiếm kiếm MailAccount dựa theo account_receive_flag = 1.
     * @return 
     */
    public List<MailAccount> findMailReceiveAccounts() {
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            ResultSetHandler<List<MailAccount>> handler = new BeanListHandler<>(MailAccount.class, ANNOTATION_ROW_PROCESSOR);
            String sql = SqlUtil.genSelectSql(conn, MailAccount.class, Arrays.asList("account_receive_flag"));
            return new QueryRunner().query(conn, sql, handler, 1);
        } catch(Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return new ArrayList<>();
        } finally { DbUtils.closeQuietly(conn); }
    }
    
    /**
     * Hàm tìm kiếm MailAccount theo account_id.
     * @param accountId
     * @return 
     */
    public MailAccount findMailAccountById(Integer accountId) {
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            ResultSetHandler<MailAccount> handler = new BeanHandler<>(MailAccount.class, ANNOTATION_ROW_PROCESSOR);
            String sql = SqlUtil.genSelectSql(conn, MailAccount.class, Arrays.asList("account_id"));
            return new QueryRunner().query(conn, sql, handler, accountId);
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        } finally { DbUtils.closeQuietly(conn); }
    }
}
