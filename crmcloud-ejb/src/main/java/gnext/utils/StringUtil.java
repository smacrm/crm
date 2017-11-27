/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gnextadmin
 */
public class StringUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);
    public static final String PREFIX_LOGIN_ID = "GN%03d"; //format prefix to GN### pattern
    
    public static String DEFAULT_DB_PROPERTIES = "/mnt/cfg/cfg.properties";
    public static String DEFAULT_SCRIPT_SPLITSCHEMA_PATH = "/mnt/script/split_db.sh";
    public static String DEFAULT_SCRIPT_CLEAN_SLAVE_DB_PATH = "/mnt/script/clean_slave_database.sh";
        
    public static final String JPA_PROVIDER = "javax.persistence.provider";
    public static final String JPA_ECLIPSELINK_PROVIDER = "org.eclipse.persistence.jpa.PersistenceProvider";
    
    public static final String PERSISTENCE_UNIT_NAME = "myPersistenceUnit";
    
    // Khi sử dụng RESOURCE_LOCAL có nghĩa là bạn phải tự quản lí transactions.
    public static final String JPA_USE_RESOURCE_LOCAL = "RESOURCE_LOCAL";
    
    /**
     * Hàm trả về tên file lấy từ FTP loại bỏ chuỗi __CRMCLOUD_REPLACE_.
     * @param origin
     * @return 
     */
    public static String getDownloadFileName(String origin) {
        try {
            String regex = "(__CRMCLOUD_REPLACE_\\d*__)(.+)";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(origin);
            if(!matcher.find()) return origin;
            String suffix = matcher.group(2);
            return suffix;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return origin;
    }
    
    /**
     * trả về EMPTY nếu val là null hoặc có giá trị là 'null' hoặc 'NULL'.
     * @param val
     * @return 
     */
    public static String nullStringToEmpty(String val) {
        if(StringUtils.isBlank(val) || "null".equals(val) || "NULL".equals(val)) return StringUtils.EMPTY;
        return val;
    }

    public static List<String> getLampColors() {
        List<String> cols = new ArrayList<String>();
        cols.add("issue_id");
        cols.add("lamp_color");
        return cols;
    }
}
