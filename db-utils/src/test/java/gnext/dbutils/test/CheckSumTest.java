/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.test;

import gnext.dbutils.model.Config;
import gnext.dbutils.model.MailAccount;
import gnext.dbutils.services.ConfigurationConnection;
import gnext.dbutils.services.Connection;
import gnext.dbutils.services.MailAccountConnection;
import java.util.List;

/**
 *
 * @author daind
 */
public class CheckSumTest {
    private final static int MASTER_COMPANY_ID = 1;
    
    public static void main(String[] args) throws Exception {
//        testFindConfigurationByKey();
        testFindAllMailAccounts();
    }
    
    private static void testFindConfigurationByKey() {
        Connection connection = new Connection("/mnt/cfg/cfg.properties", MASTER_COMPANY_ID);
        ConfigurationConnection cc = new ConfigurationConnection(connection);
        Config config = cc.findConfiguration("REDIS_HOST");
        System.out.println("gnext.dbutils.test.CheckSumTest.testFindConfigurationByKey()" + config);
    }
    
    private static void testFindAllMailAccounts() {
        Connection connection = new Connection("/mnt/cfg/cfg.properties", MASTER_COMPANY_ID);
        MailAccountConnection mac = new MailAccountConnection(connection);
        List<MailAccount> accounts = mac.findAllMailAccounts();
        System.out.println("gnext.dbutils.test.CheckSumTest.testFindAllMailAccounts()" + accounts);
    }
}
