/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer.test;

import gnext.dbutils.model.Server;
import gnext.filetransfer.*;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.services.Connection;
import gnext.dbutils.services.ServerConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ex: ftp://ftpuser@192.168.1.165:21/
 * @author daind
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        try {
//            deleteFolderManualFtp();
//            deleteFileManualFtp();
            
//            uploadHttpAuthenticate();
            
//            uploadManualFtp();
//            uploadManualFtps();
//            uploadManualFtpsWithStore();
        
//            uploadHttp();
//            uploadConfigurationFtpsWithoughtStore();
//            uploadConfigurationFtpsWithStore();

//            download();
//            downloadFtp();

            testConnection();

//            getEmailAddress("Elia から Google <elia-noreply@google.com>,Google <no-reply@accounts.google.com>");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    private static void deleteFolderManualFtp() throws Exception {
        DeleteParameter delParam = DeleteParameter.getInstance(TransferType.FTP);
        delParam.type(TransferType.FTP).host("127.0.0.1").username("ftpuser").password("Kien123456.").security(false);
        delParam.deletePath("/1/mail/925/abc").folder(true);
        FileTransferFactory.getTransfer(delParam).delete();
    }
    
    private static void deleteFileManualFtp() throws Exception {
        DeleteParameter delParam = DeleteParameter.getInstance(TransferType.FTP);
        delParam.type(TransferType.FTP).host("127.0.0.1").username("ftpuser").password("Kien123456.").security(false);
        delParam.deletePath("1/mail/925/abc/abc.txt").folder(false);
        FileTransferFactory.getTransfer(delParam).delete();
    }
    
    /***
     * Lưu trữ dữ liệu lên FTP server, thông tin kết nối tới server do người dùng cấu hình.
     * @throws Exception 
     */
    private static void uploadManualFtp() throws Exception {
        Parameter param = Parameter.getInstance(TransferType.FTP).manualconfig(true);
        param.type(TransferType.FTP).host("127.0.0.1").username("ftpuser").password("Kien123456.").security(false)
                .uploadfilename(System.currentTimeMillis() + ".properties").uploadpath("/home/ftpuser/upload/").createfolderifnotexists()
                .storeDb(false);
        FileTransferFactory.getTransfer(param).upload(new FileInputStream(new File("/mnt/email/cfg/cfg.properties")));
    }
    
    /***
     * Lưu trữ dữ liệu lên FTP server.
     * @throws Exception 
     */
    private static void uploadManualFtps() throws Exception {
        Parameter param = Parameter.getInstance(TransferType.FTP).manualconfig(true);
        param.host("192.168.1.165").username("crmcloud").password("Kien123456.").usesecurity().protocol("SSL")
                .uploadfilename(System.currentTimeMillis() + ".png").uploadpath("/home/crmcloud/upload/mail/").createfolderifnotexists()
                .storeDb(false);
        FileTransferFactory.getTransfer(param).upload(new FileInputStream(new File("/home/daind/workspace/vnext/document/images/vnext.png")));
        System.out.println("gnext.filetransfer.Main.uploadManualFtps()" + param.getAttachment().getAttachment_name());
    }
    
    /***
     * Lưu trữ dữ liệu lên FTP server, Lưu thông tin file vào database.
     * @throws Exception 
     */
    private static void uploadManualFtpsWithStore() throws Exception {
        Parameter param = Parameter.getInstance(TransferType.FTP).manualconfig(true);
        param.host("192.168.1.165").username("crmcloud").password("Kien123456.").usesecurity().protocol("SSL")
                .uploadfilename(System.currentTimeMillis() + ".png").createfolderifnotexists()
                .storeDb(true).conf("/mnt/email/cfg/cfg.properties").serverid(33)
                .attachmentTargetType(AttachmentTargetType.COMPANY).attachmentTargetId(1);
        InputStream targetStream = FileUtils.openInputStream(new File("/home/daind/workspace/vnext/document/images/vnext.png"));
        FileTransferFactory.getTransfer(param).upload(targetStream);
        System.out.println("gnext.filetransfer.Main.uploadManualFtpsWithStore()" + param.getAttachment().getAttachment_id());
    }
    
    /**
     * Lưu trữ dữ liệu lên ftp server.
     * Thông tin dữ liệu không được lưu vào trong database.
     * @throws Exception 
     */
    private static void uploadHttpAuthenticate() throws Exception {
        String url = "http://localhost:8080/login.xhtml";
        HttpParameter param_http = HttpParameter.getInstance().basicauthenticate("admin1", "admin1");
        param_http.url(url);
        
        Parameter param_ftp = Parameter.getInstance(TransferType.FTP).manualconfig(true)
                .host("192.168.1.165").port(21).username("ftpuser").password("Kien123456.").security(false)
                .uploadfilename(System.currentTimeMillis() + ".xhtml").uploadpath("/home/ftpuser/upload").createfolderifnotexists()
                .storeDb(false);
        BaseFileTransfer ftpTransfer = FileTransferFactory.getTransfer(param_ftp);
        param_http.callback(Arrays.asList(ftpTransfer));
        
        FileTransferFactory.getTransfer(param_http).upload(null);
    }
    
    /**
     * Lưu trữ dữ liệu lên ftp server.
     * Thông tin dữ liệu không được lưu vào trong database.
     * @throws Exception 
     */
    private static void uploadConfigurationFtpsWithoughtStore() throws Exception {
        String url = "https://api.twilio.com/2010-04-01/Accounts/AC8deba3947d47047efcaf58ced9199426/Recordings/REc64ceb5542087e39923dbc7ba75dbc2d";
        HttpParameter param_http = HttpParameter.getInstance();
        param_http.url(url);
        
        Parameter param_ftp = Parameter.getInstance(TransferType.FTP).manualconfig(false).serverid(2).conf("/mnt/email/cfg/cfg.properties")
                .uploadfilename(System.currentTimeMillis() + ".mp3").createfolderifnotexists()
                .storeDb(false);
        BaseFileTransfer ftpTransfer = FileTransferFactory.getTransfer(param_ftp);
        param_http.callback(Arrays.asList(ftpTransfer));
        
        FileTransferFactory.getTransfer(param_http).upload(null);
    }
    
    /***
     * Lưu trữ dữ liệu lên ftp server tu HTTP.
     * Đồng thời lưu thông tin dữ liệu vào database.
     * @throws Exception 
     */
    private static void uploadHttp() throws Exception {
        String url = "https://api.twilio.com/2010-04-01/Accounts/AC13bedd79718dbc136bb9f4fc8532b2bd/Recordings/REec05fc2e2b3884b389934b2e941eb409";
        HttpParameter param_http = HttpParameter.getInstance(TransferType.HTTP);
        param_http.url(url);
        
        Parameter param_ftp = Parameter.getInstance(TransferType.FTP).manualconfig(true).host("192.168.1.165").username("ftpuser").password("Kien123456.").security(false)
                .uploadfilename(System.currentTimeMillis() + ".mp3").uploadpath("/home/ftpuser").createfolderifnotexists()
                .storeDb(false);
        
        BaseFileTransfer ftpTransfer = FileTransferFactory.getTransfer(param_ftp);
        param_http.callback(Arrays.asList(ftpTransfer));
        
        FileTransferFactory.getTransfer(param_http).upload(null);
    }
    
    /***
     * Lưu trữ dữ liệu lên ftp server.
     * Đồng thời lưu thông tin dữ liệu vào database.
     * @throws Exception 
     */
    private static void uploadConfigurationFtpsWithStore() throws Exception {
        String url = "https://api.twilio.com/2010-04-01/Accounts/AC8deba3947d47047efcaf58ced9199426/Recordings/REc64ceb5542087e39923dbc7ba75dbc2d";
        HttpParameter param_http = HttpParameter.getInstance(TransferType.HTTP);
        param_http.url(url);
        
        Parameter param_ftp = Parameter.getInstance(TransferType.FTP).manualconfig(false).serverid(2).conf("/mnt/email/cfg/cfg.properties")
                .uploadfilename(System.currentTimeMillis() + ".mp3").createfolderifnotexists()
                .storeDb(true).attachmentTargetType(AttachmentTargetType.COMPANY).attachmentTargetId(1);
        
        BaseFileTransfer ftpTransfer = FileTransferFactory.getTransfer(param_ftp);
        param_http.callback(Arrays.asList(ftpTransfer));
        
        FileTransferFactory.getTransfer(param_http).upload(null);
    }
    
    /***
     * Tải dữ liệu từ FTP server.
     * @throws Exception 
     */
    private static void downloadFtps() throws Exception {
        String path = "/home/crmcloud/upload/mail/tow.png";
        Parameter param_ftp = Parameter.getInstance(TransferType.FTP).manualconfig(false).storeDb(false).serverid(33).conf("/mnt/email/cfg/cfg.properties");
        InputStream input = FileTransferFactory.getTransfer(param_ftp).download(path);
        System.out.println("gnext.filetransfer.Main.download()" + input);
    }
    
    /***
     * Tải dữ liệu từ FTP server.
     * @throws Exception 
     */
    private static void downloadFtp() throws Exception {
        String path = "/home/ftpuser/upload/_CRMCLOUD_REPLACE_ウィキペディアへようこそ.docx";
        Parameter param = Parameter.getInstance(TransferType.FTP).manualconfig(true).storeDb(false);
        param.type(TransferType.FTP).host("192.168.1.165").username("ftpuser").password("Kien123456.").security(false);
        InputStream input = FileTransferFactory.getTransfer(param).download(path);
        System.out.println("gnext.filetransfer.Main.download()" + gnext.dbutils.util.FileUtil.copyFromInputStream(input).length);
    }
    
    private static void testConnection() throws Exception {
//        Parameter param = Parameter.getInstance(TransferType.FTP).manualconfig(true);
//        param.type(TransferType.FTP).host("127.0.0.1").username("ftpuser").password("Kien123456.").security(false);
//        FileTransferFactory.getTransfer(param).test();
        
        Connection connection = new Connection("/mnt/cfg/cfg.properties", 1);
        ServerConnection serverConnection = new ServerConnection(connection);
        List<Server> servers = serverConnection.findServerById(1);
        System.out.println("gnext.filetransfer.test.Main.testConnection()" + servers.size());
    }
    
    /**
     * Hàm xử lí lấy địa chỉ mail từ 1 chuỗi.
     * @param s
     * @return 
     */
    private static Set<String> getEmailAddress(final String s) {
        Set<String> r = new HashSet<>();
        Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(s);
        while (m.find()) {
            System.out.println("gnext.util.EmailUtil.getEmailAddress()" + m.group());
            r.add(m.group());
        }
        return r;
    }
}
