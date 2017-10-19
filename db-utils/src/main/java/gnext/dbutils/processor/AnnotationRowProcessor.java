/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.processor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.RowProcessor;

/**
 *
 * @author daind
 */
public class AnnotationRowProcessor implements RowProcessor {
    private static final AnnotationBeanProcessor PROCESSOR = new AnnotationBeanProcessor();
    
    @Override
    public Object[] toArray(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        Object[] result = new Object[cols];
        for (int i = 0; i < cols; i++) result[i] = rs.getObject(i + 1);
        return result;
    }
    
    @Override
    public Map<String, Object> toMap(ResultSet rs) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            String columnName = rsmd.getColumnLabel(i);
            if (null == columnName || 0 == columnName.length()) columnName = rsmd.getColumnName(i);
            result.put(columnName, rs.getObject(i));
        }
        return result;
    }
    
    @Override
    public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
        try {
            return PROCESSOR.toBean(rs, type);
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
    
    @Override
    public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
        try {
            return PROCESSOR.toBeanList(rs, type);
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}
