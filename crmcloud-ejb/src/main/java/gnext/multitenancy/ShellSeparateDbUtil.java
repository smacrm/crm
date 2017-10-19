/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.multitenancy;

import gnext.bean.Company;
import gnext.utils.Console;
import gnext.utils.PropertiesUtil;
import gnext.utils.StringUtil;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public final class ShellSeparateDbUtil {
    private static final String SEPARATE_DB_PATTERN = "%s %s %s %s %s %s %s %s %s %s %s"; 
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellSeparateDbUtil.class);
    
    public static void deleteSlaveDb(final Integer companyId, final DataSource ds) {
        if(companyId == null) return;
        ByteArrayOutputStream outputStream = null;
        try {
            PropertiesUtil master_config = PropertiesUtil.getInstance();
            Properties master_config_properties = master_config.loadConf(StringUtil.DEFAULT_DB_PROPERTIES);
            if(master_config_properties == null) throw new IllegalArgumentException("Please provider the system configuration.");
            if(!master_config.checkSystemConfig(master_config_properties)) throw new IllegalArgumentException("Please verify the content of system configuration.");

            String master_host = master_config_properties.getProperty("host");
            String master_port = master_config_properties.getProperty("port");
            String master_usr = master_config_properties.getProperty("usr");
            String master_pwd = master_config_properties.getProperty("pwd");
            String schema = master_config_properties.getProperty("schema");

            String command = "sh " + String.format(SEPARATE_DB_PATTERN, StringUtil.DEFAULT_SCRIPT_CLEAN_SLAVE_DB_PATH
                    // Thong tin ket noi toi DB Master.
                    ,master_host
                    ,master_port
                    ,master_usr
                    ,master_pwd
                    ,schema
                    ,companyId

                    // Thong tin ket noi toi DB Slave.
                    ,ds.getHost()
                    ,ds.getPort()
                    ,ds.getUsr()
                    ,ds.getPwd()
            );

            outputStream = new ByteArrayOutputStream();
            Console.exec(command, outputStream);
            String standardOutputString = new String(outputStream.toByteArray());
            System.out.println("gnext.multitenancy.ShellSeparateDbUtil.splitSchemaWithNewCompany()" + standardOutputString);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try { outputStream.close(); outputStream = null; } catch (Exception e) { }
        }
    }
    
    /**
     * Tham khảo sourcecode trong file {@link split_db.sh}.
     * @param aNewCompany
     * @param ds
     * @throws Exception 
     */
    public static void splitSchemaWithNewCompany(final Company aNewCompany, final DataSource ds) throws Exception {
        if(aNewCompany.getCompanyId() == null) return;
        ByteArrayOutputStream outputStream = null;
        try {
            PropertiesUtil master_config = PropertiesUtil.getInstance();
            Properties master_config_properties = master_config.loadConf(StringUtil.DEFAULT_DB_PROPERTIES);
            if(master_config_properties == null) throw new IllegalArgumentException("Please provider the system configuration.");
            if(!master_config.checkSystemConfig(master_config_properties)) throw new IllegalArgumentException("Please verify the content of system configuration.");

            Integer slave_company_id = aNewCompany.getCompanyId();
            String master_host = master_config_properties.getProperty("host");
            String master_port = master_config_properties.getProperty("port");
            String master_usr = master_config_properties.getProperty("usr");
            String master_pwd = master_config_properties.getProperty("pwd");
            String schema = master_config_properties.getProperty("schema");

            String command = "sh " + String.format(SEPARATE_DB_PATTERN, StringUtil.DEFAULT_SCRIPT_SPLITSCHEMA_PATH
                    // Thong tin ket noi toi DB Master.
                    ,master_host
                    ,master_port
                    ,master_usr
                    ,master_pwd
                    ,schema
                    ,slave_company_id

                    // Thong tin ket noi toi DB Slave.
                    ,ds.getHost()
                    ,ds.getPort()
                    ,ds.getUsr()
                    ,ds.getPwd()
            );

            outputStream = new ByteArrayOutputStream();
            Console.exec(command, outputStream);
            String standardOutputString = new String(outputStream.toByteArray());
            System.out.println("gnext.multitenancy.ShellSeparateDbUtil.splitSchemaWithNewCompany()" + standardOutputString);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            try { outputStream.close(); outputStream = null; } catch (Exception e) { }
        }
    }
}
