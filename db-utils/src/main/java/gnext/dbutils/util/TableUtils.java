/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.util;

import gnext.dbutils.processor.Table;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.LinkedList;

/**
 *
 * @author daind
 */
public final class TableUtils {
    private TableUtils() {}
    
    /**
     * trả về tên bảng từ class.
     * @param clazz
     * @return 
     */
    public static String getTableName(final Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) { 
            Table table = clazz.getAnnotation(Table.class);
            return table.name();
        }
        return null;
    }
    
    /**
     * trả về danh sách cột trong bảng.
     * @param conn
     * @param tableName
     * @return 
     */
    public static LinkedList<String> getColNames(final Connection conn, String tableName) {
        try {
            String sql = "SELECT * FROM " + tableName;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            return getColNames(rs);
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * trả về danh sách cột trong bảng.
     * @param rs
     * @return
     * @throws Exception 
     */
    public static LinkedList<String> getColNames(ResultSet rs) throws Exception {
        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();
        LinkedList<String> columnNames = new LinkedList<>();
        for (int i = 1; i <= count; i++) {
            columnNames.add(rsmd.getColumnLabel(i));
        }
        return columnNames;
    }
}

