/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.util;

import com.mysql.jdbc.StringUtils;
import gnext.dbutils.processor.Column;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

/**
 *
 * @author daind
 */
public class SqlUtil {
    public static final String SQL_INSERT_KEY = "SQL_INSERT_KEY";
    public static final String COLUMN_INSERT_KEY = "COLUMN_INSERT_KEY";
    
    public static final String TABLE_KEY = "TABLE_KEY";
    public static final String COLUMN_KEY = "COLUMN_KEY";
    
    /**
     * Hàm xử lí thêm mới bản ghi từ class-annotation và instance từ class đó.
     * @param <T>
     * @param conn
     * @param clazz
     * @param t
     * @return
     * @throws Exception 
     */
    public static <T> long insert(final Connection conn, final Class<T> clazz, T t) throws Exception {
        Map<String, Object> re = genInsertSql(conn, clazz);
        if(re == null || re.isEmpty()) throw new Exception("Can not create sql from " + clazz.toString());
        String sql = (String) re.get(SQL_INSERT_KEY);
        LinkedList<String> cols = (LinkedList<String>) re.get(COLUMN_INSERT_KEY);
        
        List<Field> fields = new ArrayList<>(); FieldUtils.recusiveFields(fields, clazz);
        LinkedList<Object> params = new LinkedList<>();
        for(String col : cols) {
            for(Field field : fields) {
                if(!field.isAnnotationPresent(Column.class)) continue;
                Column column = field.getAnnotation(Column.class);
                if(column.generated()) continue;
                if(column.name().equalsIgnoreCase(col)) {
                    field.setAccessible(true);
                    params.add(field.get(t));
                }
            }
        }
        return new QueryRunner().insert(conn, sql, new ScalarHandler<Long>(), params.toArray());
    }
    
    /**
     * Hàm xử lí tạo câu lệnh SQL xóa một bản ghi từ class-annotation và điều kiện tương ứng với class đó.
     * @param conn
     * @param clazz
     * @param fieldWhere
     * @return 
     */
    public static String genDeleteSql(final Connection conn, final Class<?> clazz, LinkedHashMap<String, String> fieldWhere) {
        if(fieldWhere == null || fieldWhere.isEmpty()) return null;
        
        Map<String, Object> re = parse(conn, clazz);
        if(re == null) return null;
        
        String tableNameSpec = (String) re.get(TABLE_KEY);
        LinkedList<String> colNamesSpec = (LinkedList<String>) re.get(COLUMN_KEY);
        
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(tableNameSpec);
        sql.append(" WHERE 1=1 ");
        
        for (Map.Entry<String, String> entry : fieldWhere.entrySet()) {
            String fieldName = entry.getKey();
            String operator = entry.getValue();
            
            String actualCol = getActualCol(conn, clazz, fieldName);
            if(actualCol == null || actualCol.isEmpty()) continue;
            
            sql.append(" AND ").append(actualCol).append(" ").append(operator).append(" ").append(" ? ");
        }
        
        return sql.toString();
    }
    
    /**
     * Hàm xử lí tạo câu lệnh SQL chọn một bản ghi từ class-annotation và điều kiện tương ứng với class đó.
     * @param conn
     * @param clazz
     * @param fieldWhere
     * @return 
     */
    public static String genSelectSql(final Connection conn, final Class<?> clazz, List<String> fieldWhere) {
        Map<String, Object> re = parse(conn, clazz);
        if(re == null) return null;
        
        String tableNameSpec = (String) re.get(TABLE_KEY);
        LinkedList<String> colNamesSpec = (LinkedList<String>) re.get(COLUMN_KEY);
        
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableNameSpec);
        if(fieldWhere == null) return sql.toString();
        
        sql.append(" WHERE 1=1 ");
        for(String fieldName : fieldWhere) {
            String actualCol = getActualCol(conn, clazz, fieldName);
            if(actualCol == null || actualCol.isEmpty()) continue;
            sql.append(" AND ").append(actualCol).append(" = ?");
        }
        
        return sql.toString();
    }
    
    /**
     * Hàm xử lí tạo câu lệnh SQL chọn một bản ghi từ class-annotation và điều kiện tương ứng với class đó.
     * @param conn
     * @param clazz
     * @param fieldWhere
     * @param orderBy
     * @return 
     */
    public static String genSelectSql(final Connection conn, final Class<?> clazz, List<String> fieldWhere, Map<String, String> orderBy) {
        if(orderBy == null || orderBy.isEmpty())
            return genSelectSql(conn, clazz, fieldWhere);
        
        String space = " ";
        String sql = genSelectSql(conn, clazz, fieldWhere);
        
        StringBuilder sqlOderBy = new StringBuilder();
        for (Map.Entry<String, String> itemOrderBy : orderBy.entrySet()) {
            String fieldName = itemOrderBy.getKey();
            String orderType = itemOrderBy.getValue();
            
            String actualCol = getActualCol(conn, clazz, fieldName);
            if(actualCol == null || actualCol.isEmpty()) continue;
            sqlOderBy.append(space).append(actualCol).append(space).append(orderType).append(space).append(",").append(space);
        }
        String szSqlOrderBy = StringUtil.killLastCharacter(sqlOderBy, ",");
        return sql + " order by " + szSqlOrderBy;
    }
    
    /**
     * Từ filed trong class-annotation lấy được tên col trong bảng.
     * @param conn
     * @param clazz
     * @param fieldName
     * @return 
     */
    private static String getActualCol(final Connection conn, final Class<?> clazz, String fieldName) {
        Map<String, Object> re = parse(conn, clazz);
        if(re == null) return null;
        
        LinkedList<String> cols = (LinkedList<String>) re.get(COLUMN_KEY);
        if(cols == null || cols.isEmpty()) return null;
        
        List<Field> fields = new ArrayList<>(); FieldUtils.recusiveFields(fields, clazz);
        for(Field field : fields) {
            if(!field.isAnnotationPresent(Column.class)) continue;
            Column column = field.getAnnotation(Column.class);
            if(!field.getName().equalsIgnoreCase(fieldName)) continue;
            String[] _cols = column.name().split(",");
            
            for(String _col : _cols) {
                if(cols.contains(_col)) return _col;
            }
        }
        
        return null;
    }
    
    /**
     * Tạo câu lệnh thêm mới bản ghi từ class-annotation.
     * @param conn
     * @param clazz
     * @return 
     */
    private static Map<String, Object> genInsertSql(final Connection conn, final Class<?> clazz) {
        Map<String, Object> re = parse(conn, clazz);
        if(re == null) return null;
        
        String tableNameSpec = (String) re.get(TABLE_KEY);
        LinkedList<String> colNamesSpec = (LinkedList<String>) re.get(COLUMN_KEY);
        
        FieldUtils.removeAutoGene(colNamesSpec, clazz);
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableNameSpec).append("(");
        for(int i=0; i< colNamesSpec.size(); i++) {
            sql.append(colNamesSpec.get(i));
            if(i < colNamesSpec.size() - 1) sql.append(",");
        }
        sql.append(") VALUES (");
        for(int i=0; i< colNamesSpec.size(); i++) {
            sql.append("?");
            if(i < colNamesSpec.size() - 1) sql.append(",");
        }
        sql.append(")");
        
        Map<String, Object> _re = new HashMap<>();
        _re.put(SQL_INSERT_KEY, sql.toString());
        _re.put(COLUMN_INSERT_KEY, colNamesSpec);
        return _re;
    }
    
    /**
     * Hàm xử lí chung trả về một Map chứa tên bảng và danh sách cột trong bảng đó từ class-annotation.
     * @param conn
     * @param clazz
     * @return 
     */
    private static Map<String, Object> parse(final Connection conn, final Class<?> clazz) {
        String tableName = TableUtils.getTableName(clazz);
        if(StringUtils.isNullOrEmpty(tableName)) return null;
        
        String tableNameSpec = null;
        LinkedList<String> colNamesSpec = null;
        
        String[] tableNames = tableName.split(",");
        for(String tn : tableNames) {
            if(StringUtils.isEmptyOrWhitespaceOnly(tn)) continue;
            
            colNamesSpec = TableUtils.getColNames(conn, tn);
            if(colNamesSpec == null || colNamesSpec.isEmpty()) continue;
            
            tableNameSpec = tn;
            break;
        }
        
        if(tableNameSpec == null) return null;
        
        Map<String, Object> re = new HashMap<>();
        re.put(TABLE_KEY, tableNameSpec);
        re.put(COLUMN_KEY, colNamesSpec);
        return re;
    }
}
