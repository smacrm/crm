/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.log;

import gnext.util.DateUtil;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public abstract class FolderRollingFileAppender {
    private final Logger MAINLOGGER = LoggerFactory.getLogger(FolderRollingFileAppender.class);
    private static final String UTF_8_ENCODING = "utf-8";
    private static final String MAX_FILE_SIZE = "5MB";
    
    protected org.apache.log4j.Logger FOLDERLOGGER = null;
    
    private static final PatternLayout PATTERN_LAYOUT = new PatternLayout("%d{yyyy-MM-dd/HH:mm:ss.SSS/zzz} %-5p %c{1}:%L - %m%n");
    
    // Chuyển giá trị sang 1 nếu sử dụng FTP server lưu LOG.
    private static final int MAX_BACKUP_INDEX = 10;
    
    protected void logInfo(String message) {
        if(FOLDERLOGGER == null) return;
        FOLDERLOGGER.info(message);
    }
    
    protected void logError(String message, Throwable e) {
        if(FOLDERLOGGER == null) return;
        FOLDERLOGGER.error(message, e);
    }
    
    /**
     * Những dữ liệu log sẽ đựoc lưu theo tùy chọn người dùng.
     * @param dir
     * @param filename
     * @param clazz
     * @throws Exception 
     */
    protected void setupFolderLog(String dir, String filename, Class<?> clazz) throws Exception {
        if(StringUtils.isEmpty(dir) && StringUtils.isEmpty(filename) && clazz == null) return;
        
        String fullpath = dir + File.separator + filename;
        MAINLOGGER.info("Beging initial log into " + fullpath);
        
//        CrmRollingFileAppender appender = getRollingFileAppender(fullpath);
//        if(appender == null) appender = getDefaultAppender(fullpath);

        CrmRollingFileAppender appender = getDefaultAppender(fullpath);
        if(FOLDERLOGGER == null) {
            org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
            FOLDERLOGGER = org.apache.log4j.Logger.getLogger(clazz);
            FOLDERLOGGER.removeAllAppenders();
            FOLDERLOGGER.addAppender(appender);
        } else {
            FOLDERLOGGER.removeAllAppenders();
            FOLDERLOGGER.addAppender(appender);
        }
    }
    
    /**
     * Tạm thời không sử dụng FTP Server để lưu trữ Logging.
     * @return 
     */
//    private Server getOneServer() {
//        Map<String,Object> parameters = buildParameters();
//        if (parameters.containsKey(SERVICE_PARAMETER)) return null;
//        
//        Map<String, Object> services = (Map<String, Object>) parameters.get(SERVICE_PARAMETER);
//        ServerService mServerService = (ServerService) services.get("serverService");
//        if (mServerService == null) return null;
//        
//        UserModel userModel = getUserModel();
//        if (userModel == null) return null;
//        
//        List<Server> servers = mServerService.search(userModel.getCompanyId(), TransferType.FTP.getType(), ServerFlag.JOB_LOG.getId());
//        if(servers == null || servers.isEmpty()) return null;
//        
//        return servers.get(0);
//        return null;
//    }
    
    /**
     * Thông tin người dùng logined.
     * @return 
     */
//    private UserModel getUserModel() {
//        Map<String,Object> parameters = buildParameters();
//        if(parameters == null) return null;
//        if (!parameters.containsKey(USERMODEL_PARAMETER)) return null;
//        return (UserModel) parameters.get(USERMODEL_PARAMETER);
//    }
    
//    private CrmRollingFileAppender getRollingFileAppender(String fullpath) throws Exception {
//        Map<String,Object> parameters = buildParameters();
//        if(parameters == null) return null;
//        
//        Batch p_batch = (Batch) parameters.get(BATCH_PARAMETER);
//        if(p_batch == null) return null;
//        
//        Server server = getOneServer();
//        if(server == null) return null;
//        
//        UserModel userModel = getUserModel();
//        if (userModel == null) return null;
//        
//        String remotePath = server.getServerFolder()    + File.separator 
//                            + userModel.getCompanyId()  + File.separator
//                            + p_batch.getBatchId()      + File.separator
//                            + getFolderFolowDate();
//        
//        String fn = p_batch.getBatchId()+"."+getFolderFolowDate()+"."+System.currentTimeMillis()+".log";
//        CrmRollingFileAppender rfa = new CrmRollingFileAppender(PATTERN_LAYOUT, fullpath, true, remotePath, fn, server);
//        rfa.setImmediateFlush(true);
//        rfa.setMaxFileSize(MAX_FILE_SIZE);
//        rfa.setMaxBackupIndex(MAX_BACKUP_INDEX);
//        rfa.setThreshold(Level.DEBUG);
//        rfa.setEncoding(UTF_8_ENCODING);
//        rfa.activateOptions();// ← activate the options
//        return rfa;
//    }
    
    private CrmRollingFileAppender getDefaultAppender(String fullpath) throws Exception {
        CrmRollingFileAppender rfa = new CrmRollingFileAppender(PATTERN_LAYOUT, fullpath, true);
        rfa.setImmediateFlush(true);
        rfa.setMaxFileSize(MAX_FILE_SIZE);
        rfa.setMaxBackupIndex(MAX_BACKUP_INDEX);
        rfa.setThreshold(Level.DEBUG);
        rfa.setEncoding(UTF_8_ENCODING);
        rfa.activateOptions();// ← activate the options
        return rfa;
    }
    
    protected Map<String, Object> buildParameters() {return null;}
    
    protected String getFolderFolowDate() {
        return new SimpleDateFormat(DateUtil.PATTERN_CSV_EXPORT_DATE).format(DateUtil.now());
    }
}
