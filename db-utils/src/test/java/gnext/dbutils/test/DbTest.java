/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.test;

import gnext.dbutils.model.Config;
import gnext.dbutils.model.MailData;
import gnext.dbutils.model.Server;
import gnext.dbutils.services.Connection;
import gnext.dbutils.services.MailAccountConnection;
import gnext.dbutils.services.MailDataConnection;
import gnext.dbutils.services.ServerConnection;
import gnext.dbutils.util.SqlUtil;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author daind
 */
public class DbTest {
    private final static int MASTER_COMPANY_ID = 1;
    
    public static void main(String[] args) throws Exception {
//        testInsertConfig();
//        testfindAllMailAccounts();
//        testfindByUniqueId();
//        testdeleteMailData();
        testfindServerGnext();
    }
    
    private static void testInsertConfig() throws Exception {
        Connection connection = new Connection("/mnt/cfg/cfg.properties", MASTER_COMPANY_ID);
        java.sql.Connection conn = connection.getConnection();
        try {
            Config config = new Config();
            config.setConfig_deleted((short) 0);
            config.setConfig_group("TST");
            config.setConfig_key("001" + System.currentTimeMillis());
            config.setConfig_note("001 note.");
            config.setConfig_type((short) 1);
            config.setConfig_value("001 value");
            config.setCreated_time(Calendar.getInstance().getTime());
            config.setCreator_id(1);
            config.setUpdated_id(1);
            config.setUpdated_time(Calendar.getInstance().getTime());
            long pk = SqlUtil.insert(conn, Config.class, config);
            System.out.println("gnext.dbutils.test.DbTest.testInsertConfig()" + pk);
        } finally {
            try { conn.close(); } catch (Exception e) { }
        }
    }
    
    private static void testfindAllMailAccounts() throws Exception {
        Connection connection = new Connection("/mnt/cfg/cfg.properties", MASTER_COMPANY_ID);
        MailAccountConnection mac = new MailAccountConnection(connection);
        System.out.println("gnext.dbutils.test.DbTest.testfindAllMailAccounts()" + mac.findAllMailAccounts().size());
    }
    
    private static void testfindByUniqueId() {
        Connection connection = new Connection("/mnt/cfg/cfg.properties", MASTER_COMPANY_ID);
        MailDataConnection mdc = new MailDataConnection(connection);
        MailData mailData = new MailData();
        mailData.setMail_data_unique_id("<003e6ab4-a92c-265d-a133-797348a9eace@vnext.vn>");
        System.out.println("gnext.dbutils.test.DbTest.testfindByUniqueId()" + mdc.findByUniqueId(mailData).size());
    }
    
    private static void testdeleteMailData() throws Exception {
        MailDataConnection mdc = new MailDataConnection(new Connection("/mnt/cfg/cfg.properties", MASTER_COMPANY_ID));
        mdc.deleteMailData(1, "<003e6ab4-a92c-265d-a133-797348a9eace@vnext.vn>");
    }
    
    private static void testfindServerGnext() throws Exception {
        Connection connection = new Connection("/mnt/cfg/cfg.properties", MASTER_COMPANY_ID);
        ServerConnection serverConnection = new ServerConnection(connection);
        List<Server> servers = serverConnection.findServerGnext();
        System.out.println("gnext.dbutils.test.DbTest.testfindServerGnext()" + servers.size());
    }
}
