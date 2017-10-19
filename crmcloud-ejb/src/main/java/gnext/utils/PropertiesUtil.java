/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.utils;

import gnext.dbutils.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public class PropertiesUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    private static PropertiesUtil propertiesUtil;
    
    private PropertiesUtil() {}
    
    public static PropertiesUtil getInstance(){
        if(propertiesUtil == null){
            propertiesUtil = new PropertiesUtil();
        }
        return propertiesUtil;
    }
    
    /**
     * Lấy thông tin từ File properties với path là đường dẫn tới File.
     * @param path
     * @return 
     */
    public Properties loadConf(String path) {
        return loadConf(new File(path));
    }
    
    /**
     * Lấy thông tin từ File properties với path là đường dẫn tới File.
     * @param resource
     * @return 
     */
    public Properties loadConf(File resource) {
        InputStream is = null;
        try {
            is = Files.newInputStream(Paths.get(resource.getAbsolutePath()));
            Properties prop = new Properties();
            prop.load(is);
            return prop;
        } catch (IOException io) {
            LOGGER.error(io.getLocalizedMessage(), io);
            gnext.dbutils.util.Console.error(io);
        } finally {
            if(is != null) try { is.close(); } catch (IOException e) {}
        }
        
        return null;
    }
    
    /**
     * Kiểm tra file cấu hính hệ thống.
     * @param properties
     * @return 
     */
    public boolean checkSystemConfig(final Properties properties) {
        if(!properties.containsKey("host")
                || !properties.containsKey("port")
                || !properties.containsKey("usr")
                || !properties.containsKey("pwd")
                || !properties.containsKey("url")
                || !properties.containsKey("driver")
                || !properties.containsKey("schema")) {
            return false;
        }
        return true;
    }
}
