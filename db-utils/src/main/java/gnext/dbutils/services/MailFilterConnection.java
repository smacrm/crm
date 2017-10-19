/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.services;

import gnext.dbutils.model.MailFilter;
import static gnext.dbutils.services.Connection.ANNOTATION_ROW_PROCESSOR;
import gnext.dbutils.util.SqlUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class MailFilterConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailFilterConnection.class);
    private final Connection connection;
    public MailFilterConnection(Connection connection) { this.connection = connection; }
    
    /**
     * Hàm tìm kiếm MailFilter theo compnay_id.
     * @param companyId
     * @return 
     */
    public List<MailFilter> findMailFilterByCompanyId(Integer companyId) {
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            ResultSetHandler<List<MailFilter>> handler = new BeanListHandler<>(MailFilter.class, ANNOTATION_ROW_PROCESSOR);
            Map<String, String> orderBy = new HashMap<>();
            orderBy.put("mail_filter_order", "desc");
            String sql = SqlUtil.genSelectSql(conn, MailFilter.class, Arrays.asList("company_id"), orderBy);
            return new QueryRunner().query(conn, sql, handler, companyId);
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage(), e);
            return new ArrayList<>();
        } finally { DbUtils.closeQuietly(conn); }
    }
}
