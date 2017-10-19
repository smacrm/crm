/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.services;

import gnext.dbutils.model.MailData;
import static gnext.dbutils.services.Connection.ANNOTATION_ROW_PROCESSOR;
import gnext.dbutils.util.SqlUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
public class MailDataConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailDataConnection.class);
    private final Connection connection;
    public MailDataConnection(Connection connection) { this.connection = connection; }
    
    /***
     * Hàm tìm kiếm MailData theo field mail_data_unique_id.
     * @param mailData
     * @return 
     */
    public List<MailData> findByUniqueId(MailData mailData) {
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            ResultSetHandler<List<MailData>> handler = new BeanListHandler<>(MailData.class, ANNOTATION_ROW_PROCESSOR);
            String sql = SqlUtil.genSelectSql(conn, MailData.class, Arrays.asList("mail_data_unique_id"));
            return new QueryRunner().query(conn, sql, handler, mailData.getMail_data_unique_id());
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage(), e);
            return new ArrayList<>();
        } finally { DbUtils.closeQuietly(conn); }
    }
    
    /**
     * Hàm xử lí lưu MailData cùng java.sql.Connection làm tham số.
     * Tăng tính mềm dẻo cho hàm, người dùng có thể sử dụng transaction.
     * @param mailData
     * @param conn
     * @return
     * @throws Exception 
     */
    public MailData create(final MailData mailData, final java.sql.Connection conn) throws Exception {
        Long pk = SqlUtil.insert(conn, MailData.class, mailData);
        mailData.setMail_data_id(pk.intValue());
        return mailData;
    }
    
    /**
     * Hàm xóa MailData theo mail_data_unique_id.
     * @param company_id
     * @param mail_data_unique_id
     * @throws Exception 
     */
    public void deleteMailData(int company_id, String mail_data_unique_id) throws Exception {
        java.sql.Connection conn = null;
        try {
            conn = this.connection.getConnection();
            LinkedHashMap<String, String> params = new LinkedHashMap<>();
            params.put("company_id", "="); params.put("mail_data_unique_id", "LIKE");
            String sql = SqlUtil.genDeleteSql(conn, MailData.class, params);
            
            int re = new QueryRunner().update(conn, sql, new Object[] {company_id, mail_data_unique_id});
            if(re <= 0) throw new Exception("can delete data from sql " + sql);
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally { DbUtils.closeQuietly(conn); }
    }
}
