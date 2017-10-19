/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.test;

import gnext.mailapi.*;
import gnext.dbutils.util.FileUtil;
import gnext.mailapi.mail.SendEmail;
import gnext.mailapi.util.InterfaceUtil;
import gnext.mailapi.util.MailUtil;
import gnext.mailapi.util.ParameterUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;

/**
 * Class chỉ dùng cho việc test MailAPI.
 * Tham khảo khi muốn sử dụng thư viện mailclient.
 * @author daind
 */
public class DevMode {
    public static void main(String[] args) throws Exception {
//        findMailDataHasMailDataDateIsZero();
        
//        sendEmailWithAccountIdHasObject();
//        sendEmailWithInputStreamWithObject();
//        sendEmailNormalWithObject();
                
//        sendEmailWithAccountId();
//        sendEmailWithInputStream();
//        sendEmailNormal587();
//        sendEmailNormal465();
          
        testReadmailWithSeparareDb();
//        readMailWithConfiguration();
//        alertFromSupport();
        
//        easeyDecrypt("L21vZHVsZXMvYXV0aG9yaXR5L3BhZ2Vfc2Nhbi54aHRtbA==");
    }
    
    private static Map<String, String> getParam(String[] args) {
        return ParameterUtil.buildParameters(args);
    }
    
    private static void testReadmailWithSeparareDb() {
        String[] args = {
            "action:receive"
           ,"folder:inbox"
           ,"debug:false"
           ,"flag:all"
           ,"prior:5"                           // Tham số chỉ ra số ngày lùi lại từ ngày hiện tại.
           
           ,"cfg:/mnt/cfg/cfg.properties"       // file này chứa địa chỉ database và thông tin của mail gnextadmin.
           ,"companyid:37"                      // công ty cần đọc mail.
           ,"serverid:1"                        // thông tin của FTP server có trong database đã cấu hình ở trên.
        };
        MailClient.main(args);
    }
    
    /***
     * Hàm xử lí gửi email cùng accountid trong tham số.
     * Trường hợp sử dụng khi ta có 1 accountId(từ đây có thể lấy được mail account và mail server).
     * Hệ thống sẽ tự động lấy các thông số mailserver và mailaccount để gửi đồng thời lưu trữ dữ liệu lên FTP server.
     */
    private static void sendEmailWithAccountIdHasObject() throws Exception {
        String[] args = {
                "action:send"                        // Kiểu sử dụng của mail API.
                ,"savedb:true"                       // Co luu du lieu vao db hay khong.
                ,"accountid:1"                       // Id tồn tại của Account trong database.
                ,"cfg:/mnt/cfg/crm_mail_api.properties"       // File này chứa địa chỉ database và thông tin của mail gnextadmin. Lấy dữ liệu từ crm_conf.
                ,"serverid:1"                       // Thông tin của FTP server có trong database đã cấu hình ở trên.
        };
        
        Map<String, InputStream> map = new HashMap<>();
        try { map.put("ウィキペディアへようこそ.pdf", new FileInputStream(new File("/mnt/email/attachment/20170116165629451.pdf"))); } catch (Exception e) { }
        
        SendEmail se = new SendEmail();
        se.setType(InterfaceUtil.Type.SMTP);
        se.setRecipient(new String[]{"daind1@vnext.vn"});
//        se.setCc(new String[]{"dainguyendinh.85@gmail.com","tungdt@vnext.vn"});
//        se.setBcc(new String[]{"gnext383838@gmail.com"});
        se.setSubject("選り抜き記事");
        se.setMessage("タマーラ・カルサヴィナは20世紀前半に活躍したロシア人バレリーナである。マリインスキー");
        se.setAttchFiles(new String[]{"/mnt/cfg/cfg.properties"});
        se.setAttachments(map);
        
        MailClient._SendMail(se, getParam(args));
    }
    
    /***
     * Hàm xử lí gửi email cùng accountid trong tham số.
     * Trường hợp sử dụng khi ta có 1 accountId(từ đây có thể lấy được mail account và mail server).
     * Hệ thống sẽ tự động lấy các thông số mailserver và mailaccount để gửi đồng thời lưu trữ dữ liệu lên FTP server.
     */
    private static void sendEmailWithAccountId() throws Exception {
        String[] args = {
                "action:send"
                ,"to:daind1@vnext.vn"
//                ,"cc:dainguyendinh.85@gmail.com,tungdt@vnext.vn"
//                ,"bcc:gnext383838@gmail.com"
                ,"subject:選り抜き記事"
                ,"body:タマーラ・カルサヴィナは20世紀前半に活躍したロシア人バレリーナである。マリインスキー"
                ,"attchFiles:/mnt/cfg/cfg.properties"
                
                ,"accountid:1"                          // Id tồn tại của Account trong database.
                ,"cfg:/mnt/cfg/crm_mail_api.properties" // File này chứa địa chỉ database và thông tin của mail gnextadmin.
                ,"serverid:1"                           // Thông tin của FTP server có trong database đã cấu hình ở trên.
        };
        
        Map<String, InputStream> map = new HashMap<>();
        try { map.put("cfg.properties", new FileInputStream(new File("/mnt/cfg/cfg.properties"))); } catch (Exception e) { }
        
        MailClient._SendMail(getParam(args), map);
    }
    
    /***
     * Hàm xử lí gửi email cùng danh sách attachment là stream.
     * Người dùng muốn attachment theo dạng inputstram.
     */
    private static void sendEmailWithInputStreamWithObject() throws Exception {
        String[] args = { "action:send" };
        
        Map<String, InputStream> map = new HashMap<>();
//        try { map.put("ウィキペディアへようこそ.pdf", new FileInputStream(new File("/mnt/email/attachment/20170116165629451.pdf"))); } catch (Exception e) { }
        
        SendEmail se = new SendEmail();
        se.setDebug(false);
        se.setType(InterfaceUtil.Type.SMTP);
        se.setHost("smtp.gmail.com");
        se.setPort(465);
        se.setUserName("kienvnext@gmail.com");
        se.setPassword("Daithukien");
        se.setSsl(true);
        se.setAuth(true);
        se.setFrom("kienvnext@gmail.com");
        se.setRecipient(new String[]{"hungpham@gnext.co.jp"});
        
//        se.setCc(new String[]{"dainguyendinh.85@gmail.com"});
//        se.setBcc(new String[]{"gnext383838@gmail.com"});
        
        se.setSubject("選り抜き記事");
        se.setMessage("タマーラ・カルサヴィナは20世紀前半に活躍したロシア人バレリーナである。マリインスキー");
        se.setAttchFiles(new String[]{"/home/daind/workspace/vnext/ebooks/gnext/申出分類商品大分類別月件数_2017.xlsx"});
        se.setAttachments(map);
        
        MailClient._SendMail(se, getParam(args));
    }
    
    /***
     * Hàm xử lí gửi email cùng danh sách attachment là stream.
     * Người dùng muốn attachment theo dạng inputstram.
     */
    private static void sendEmailWithInputStream() throws Exception {
        String[] args = {
                "action:send"
                ,"host:smtp.gmail.com"
                ,"port:465"
                ,"user:kienvnext@gmail.com"
                ,"pass:Daithukien"
                ,"ssl:true"
                ,"auth:true"
                ,"from:kienvnext@gmail.com"
                ,"to:daind1@vnext.vn"
                ,"subject:選り抜き記事"
                ,"body:タマーラ・カルサヴィナは20世紀前半に活躍したロシア人バレリーナである。マリインスキー"
                ,"attchFiles:/mnt/email/attachment/部屋カード.pdf"
        };
        
        Map<String, InputStream> map = new HashMap<>();
        try { map.put("ウィキペディアへようこそ.pdf", new FileInputStream(new File("/mnt/email/attachment/20170116165629451.pdf"))); } catch (Exception e) { }
        
        MailClient._SendMail(getParam(args), map);
    }
    
    /***
     * Hàm xử lí gửi mail.
     * Được sử dụng như là 1 api cho các hệ thống khác.
     */
    private static void sendEmailNormalWithObject() throws Exception {
        String[] args = { "action:send" };
        
        SendEmail se = new SendEmail();
        se.setType(InterfaceUtil.Type.SMTP);
        se.setHost("smtp.gmail.com");
        se.setPort(465);
        se.setUserName("kienvnext@gmail.com");
        se.setPassword("Daithukien");
        se.setSsl(true);
        se.setAuth(true);
        se.setFrom("kienvnext@gmail.com");
        se.setRecipient(new String[]{"daind1@vnext.vn"});
        se.setCc(new String[]{"dainguyendinh.85@gmail.com","tungdt@vnext.vn"});
        se.setBcc(new String[]{"gnext383838@gmail.com"});
        se.setSubject("選り抜き記事");
        se.setMessage("タマーラ・カルサヴィナは20世紀前半に活躍したロシア人バレリーナである。マリインスキー");
        se.setAttchFiles(new String[]{"/mnt/email/attachment/部屋カード.pdf"});
        
        MailClient._SendMail(se, getParam(args));
    }
    
    /***
     * Hàm xử lí gửi mail.
     * Được sử dụng như là 1 api cho các hệ thống khác.
     */
//    private static void sendEmailNormal465() throws Exception {
//        String[] args = {
//                "action:send"
//                ,"host:smtp.gmail.com"
//                ,"port:465"
//                ,"user:kienvnext@gmail.com"
//                ,"pass:Daithukien"
//                ,"ssl:true"
//                ,"auth:true"
//                ,"tls:false"
//                ,"from:kienvnext@gmail.com"
//                ,"to:daind1@vnext.vn"
//                ,"subject:選り抜き記事"
//                ,"body:タマーラ・カルサヴィナは20世紀前半に活躍したロシア人バレリーナである。マリインスキー"
//                ,"attchFiles:/mnt/email/attachment/部屋カード.pdf"
//                
//                ,"savedb:false"
//                ,"cfg:/mnt/cfg/cfg.properties"
//                ,"accountid:2"
//                ,"companyid:1"
//        };
//        MailClient._SendMail(getParam(args), null);
//    }
    private static void sendEmailNormal465() throws Exception {
        String[] args = {
                "action:send"
                ,"host:smtp.gmail.com"
                ,"port:465"
                ,"user:kienvnext@gmail.com"
                ,"pass:Daithukien"
                ,"ssl:true"
                ,"auth:true"
                ,"tls:false"
                ,"from:kienvnext@gmail.com"
                ,"to:daind1@vnext.vn"
                ,"subject:選り抜き記事"
                ,"body:タマーラ・カルサヴィナは20世紀前半に活躍したロシア人バレリーナである。マリインスキー"
                ,"attchFiles:/mnt/email/attachment/部屋カード.pdf"
                
                ,"savedb:false"
                ,"cfg:/mnt/cfg/cfg.properties"
                ,"accountid:2"
                ,"companyid:1"
        };
        MailClient._SendMail(getParam(args), null);
    }
    
    private static void sendEmailNormal587() throws Exception {
        String[] args = {
                "action:send"
                ,"host:smtp.gmail.com"
                ,"port:587"
                ,"user:kienvnext@gmail.com"
                ,"pass:Daithukien"
                ,"ssl:false"
                ,"auth:true"
                ,"tls:true"
                ,"from:kienvnext@gmail.com"
                ,"to:daind1@vnext.vn"
                ,"subject:選り抜き記事"
                ,"body:タマーラ・カルサヴィナは20世紀前半に活躍したロシア人バレリーナである。マリインスキー"
                ,"attchFiles:/mnt/email/attachment/部屋カード.pdf"
                
                ,"savedb:true"
                ,"cfg:/mnt/cfg/cfg.properties"
                ,"accountid:2"
                ,"companyid:1"
                ,"creatorid:1"
        };
        MailClient._SendMail(getParam(args), null);
    }
    
    /***
     * Hàm xử lí đọc mail.
     * Nếu có đủ cấu hình kết nối tới db và ftp thì API sẽ tự động lưu.
     */
    private static void readMailWithConfiguration() {
        String[] args = {
            "action:receive"
           ,"folder:inbox"
           ,"debug:false"
           ,"flag:all"
           ,"prior:1"                           // Tham số chỉ ra số ngày lùi lại từ ngày hiện tại.
           
           ,"cfg:/mnt/cfg/cfg.properties"       // file này chứa địa chỉ database và thông tin của mail gnextadmin.
           ,"companyid:1"                       // công ty cần đọc mail.
           ,"serverid:4"                        // thông tin của FTP server có trong database đã cấu hình ở trên.
        };
        MailClient.main(args);
    }
    
    /***
     * Hàm xử lí gửi mail tới supporter.
     */
    private static void alertFromSupport() {
//        String[] args = {
//            "action:alert"
//            ,"subject:\"Gnext change your password\""
//            ,"body:\"Your new password: hPP27K\""
//            ,"to:daind1@vnext.vn"
//            ,"cfg:/mnt/cfg/cfg.properties" // file này chứa địa chỉ database và thông tin của mail gnextadmin.
//        };
//        MailClient.main(args);
        Properties properties = FileUtil.loadConf("/mnt/cfg/cfg.properties");
        SendEmail se = new SendEmail();
        se.setType(gnext.mailapi.util.InterfaceUtil.Type.SMTP);
        se.setHost(properties.getProperty("admin.host"));
        se.setPort(Integer.parseInt(String.valueOf(properties.get("admin.port"))));
        se.setUserName((String) properties.get("admin.user"));
        se.setPassword((String) properties.get("admin.pwd"));
        se.setSsl(true);
        se.setAuth(true);
        se.setFrom((String) properties.get("admin.from"));
        se.setRecipient(new String[] {"daind1@vnext.vn"});
        se.setSubject("Gnext change your password");
        se.setMessage("Your new password: hPP27K");
        se.setPriority("1");
        se.setContentType("text/html;charset=utf-8");
        MailUtil._Alert(se);
    }
    
    public static String easeyDecrypt(String secret) throws UnsupportedEncodingException {
        byte[] dectryptArray = secret.getBytes();
        byte[] decarray = Base64.decodeBase64(dectryptArray);
        String decstr = new String(decarray, "UTF-8");
        System.out.println("gnext.mailapi.test.DevMode.easeyDecrypt()" + decstr);
        return decstr;
    }
}
