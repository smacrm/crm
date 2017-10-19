/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.util;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public class ConnectionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionUtil.class);
    
    public static boolean testConnection() {
        return true;
    }
    
    /**
     * Bắt đầu 1 transaction.
     * @param conn 
     */
    public static void startTransaction(java.sql.Connection conn) {
        if(conn == null) return;
        try {
            if(conn.getAutoCommit()) conn.setAutoCommit(false);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }
    
    /**
     * Đẩy dữ liệu lên DB.
     * @param conn 
     */
    public static void commitTransaction(java.sql.Connection conn) {
        if(conn == null) return;
        try {
            DbUtils.commitAndCloseQuietly(conn);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        } finally {
            revert(conn);
        }
    }
    
    /**
     * Reset lại dữ liệu về ban đầu.
     * @param conn 
     */
    public static void rollbackTransaction(java.sql.Connection conn) {
        if(conn == null) return;
        try {
            DbUtils.rollbackAndCloseQuietly(conn);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        } finally {
            revert(conn);
        }
    }
    
    private static void revert(java.sql.Connection conn) {
        if(conn == null) return;
        try {
            if(!conn.isClosed() && !conn.getAutoCommit()) conn.setAutoCommit(true);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }
}
