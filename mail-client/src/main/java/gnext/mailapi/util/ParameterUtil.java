/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util;

import gnext.dbutils.model.MailAccount;
import gnext.dbutils.model.MailServer;
import gnext.dbutils.services.Connection;
import gnext.dbutils.services.MailAccountConnection;
import gnext.dbutils.services.MailServerConnection;
import gnext.mailapi.mail.DeletedMail;
import gnext.mailapi.mail.ReadEmail;
import gnext.mailapi.mail.SendEmail;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 * @author daind
 */
public class ParameterUtil {
    
    public static Map<String, String> buildParameters(String[] args) {
        Map<String, String> parameters = new HashMap<>();
        if(args == null || args.length <=0) return parameters;
        
        for (String arg : args) {
            String[] pair = arg.split(":");
            String key = pair[0];
            String value = arg.substring(arg.indexOf(key) + key.length() + 1);
            parameters.put(key, value);
        }
        return parameters;
    }
    
    public static void printMap(Map<String, String> reversedMap) {
        for (Map.Entry entry : reversedMap.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
    }
    
    public static String getAction(Map<String, String> parameters) {
        if(parameters == null) return "NOACTION";
        if(!parameters.containsKey("action")) return "NOACTION";
        
        return parameters.get("action");
    } // end function::
    
    public static Integer getTimeout(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("timeout")) return null;
        
        String timeout = parameters.get("timeout");
        if(NumberUtils.isDigits(timeout)) return Integer.parseInt(timeout);
        
        return null;
    } // end function::
    
    public static String getAndSetPrior(Map<String, String> parameters, ReadEmail re) {
        if(parameters == null) return null;
        if(!parameters.containsKey("prior")) return null;
        
        String prior = parameters.get("prior");
        re.setPrior(prior);
        
        return prior;
    } // end function::
    
    public static String[] geAndSettBcc(Map<String, String> parameters, SendEmail se) {
        if(parameters == null) return null;
        if(!parameters.containsKey("bcc")) return null;
        if(StringUtils.isEmpty(parameters.get("bcc"))) return null;
        
        String[] bcc = parameters.get("bcc").split(",");
        se.setBcc(bcc);
        
        return bcc;
    } // end function::
    
    public static String[] getAndSetAttchFiles(Map<String, String> parameters, SendEmail se) {
        if(parameters == null) return null;
        if(!parameters.containsKey("attchFiles")) return null;
        if(StringUtils.isEmpty(parameters.get("attchFiles"))) return null;
        
        String[] attchFiles = parameters.get("attchFiles").split(",");
        se.setAttchFiles(attchFiles);
        
        return attchFiles;
    } // end function::
    
    public static String[] geAndSettCc(Map<String, String> parameters, SendEmail se) {
        if(parameters == null) return null;
        if(!parameters.containsKey("cc")) return null;
        if(StringUtils.isEmpty(parameters.get("cc"))) return null;
        
        String[] cc = parameters.get("cc").split(",");
        se.setCc(cc);
        
        return cc;
    } // end function::
    
    public static String getAndSetMessageid(Map<String, String> parameters, DeletedMail dm) {
        if(parameters == null) return null;
        if(!parameters.containsKey("messageid")) return null;
        
        String messageid = parameters.get("messageid");
        dm.setMessageId(messageid);
        
        return messageid;
    } // end function::
    
    public static String[] getCc(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("cc")) return null;
        return parameters.get("cc").split(",");
    } // end function::
    
    public static String getBody(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("body")) return null;
        return parameters.get("body");
    } // end function::
    
    public static String getSubject(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("subject")) return null;
        return parameters.get("subject");
    } // end function::
    
    public static String[] getTo(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("to")) return null;
        return parameters.get("to").split(",");
    } // end function::
    
    public static String getFrom(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("from")) return null;
        return parameters.get("from");
    } // end function::
    
    public static String getCfg(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("cfg")) return null;
        return parameters.get("cfg");
    } // end function::
    
    public static Integer getAccountId(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("accountid")) return null;
        
        return Integer.parseInt(parameters.get("accountid"));
    } // end function::
    
    public static Integer getCreatorId(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("creatorid")) return null;
        
        return Integer.parseInt(parameters.get("creatorid"));
    } // end function::
    
    public static Integer getCompanyId(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("companyid")) return null;
        
        return Integer.parseInt(parameters.get("companyid"));
    } // end function::
    
    public static Integer getServerId(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("serverid")) return null;
        
        return Integer.parseInt(parameters.get("serverid"));
    } // end function::
    
    public static boolean getSavedb(Map<String, String> parameters) {
        if(parameters == null) return false;
        if(!parameters.containsKey("savedb")) return false;
        
        return Boolean.valueOf(parameters.get("savedb"));
    } // end function::
    
    public static String getType(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("type")) return null;
        
        return parameters.get("type");
    } // end function::
    
    public static String getHost(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("host")) return null;
        
        return parameters.get("host");
    } // end function::
    
    public static String getUser(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("user")) return null;
        
        return parameters.get("user");
    } // end function::
    
    public static String getPass(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("pass")) return null;
        
        return parameters.get("pass");
    } // end function::
    
    public static Boolean getSsl(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("ssl")) return null;
        
        return Boolean.valueOf(parameters.get("ssl"));
    } // end function::
    
    public static Integer getSslSmtpPort(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("sslSmtpPort")) return null;
        
        return Integer.parseInt(parameters.get("sslSmtpPort"));
    } // end function::
    
    public static Integer getSslPort(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("sslPort")) return null;
        
        return Integer.parseInt(parameters.get("sslPort"));
    } // end function::
    
    public static Boolean getAuth(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("auth")) return null;
        
        return Boolean.valueOf(parameters.get("auth"));
    } // end function::
    
    public static Boolean getTls(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("tls")) return null;
        
        return Boolean.valueOf(parameters.get("tls"));
    } // end function::
    
    public static String getFolder(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("folder")) return null;
        
        return parameters.get("folder");
    } // end function::
    
    public static String getFlag(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("flag")) return null;
        
        return parameters.get("flag");
    } // end function::
    
    public static Boolean getDebug(Map<String, String> parameters) {
        if(parameters == null) return Boolean.TRUE;
        if(!parameters.containsKey("debug")) return Boolean.TRUE;
        
        return Boolean.valueOf(parameters.get("debug"));
    } // end function::
    
    public static Integer getPort(Map<String, String> parameters) {
        if(parameters == null) return null;
        if(!parameters.containsKey("port")) return null;
        
        return Integer.parseInt(parameters.get("port"));
    } // end function::
    
    /**
     * Hàm kiểm tra tham số đầu vào khi thực hiện đọc MAIL.
     * @param parameters
     * @return
     * @throws Exception 
     */
    public static boolean checkReceiveParameter(Map<String, String> parameters)
            throws Exception {
        if(!parameters.containsKey("cfg")) throw new IllegalArgumentException("The 'cfg' parameter must not empty.");
        if(!parameters.containsKey("companyid")) throw new IllegalArgumentException("The 'companyid' parameter must not empty.");
        if(!parameters.containsKey("serverid")) throw new IllegalArgumentException("The 'serverid' parameter must not empty.");
        return true;
    } // end function::
    
    
    /***
     * Kiểm tra tham số đầu vào.
     * Trường hợp nếu có cấu hình kết nối tới DB và accountid hệ thống sẽ tự động
     * tìm kiếm Mailserver trong db bao gồm thông tin như host, port, username, password...
     * @param se
     * @param parameters 
     * @return  
     * @throws java.lang.Exception 
     */
    public static boolean checkSendParameter(SendEmail se, Map<String, String> parameters)
            throws Exception {
        // nếu chứa tham số accountid, cfg, companyid thì lấy thông tin kết nối mail từ cơ sở dữ liệu.
        if(parameters.containsKey("accountid") && parameters.containsKey("cfg") && parameters.containsKey("companyid")) {
            Connection connection = new Connection(getCfg(parameters), getCompanyId(parameters));
            MailAccountConnection mac = new MailAccountConnection(connection);
            Integer accountId = getAccountId(parameters);
            MailAccount mailAccount = mac.findMailAccountById(accountId);
            externalData(mailAccount, se, connection, parameters);
        }
        
        // nếu yêu cầu lưu vào cơ sở dữ liệu cần validation các tham số cần thiết.
        // creatorid là lấy từ mailaccount.
        if(getSavedb(parameters)) {
            if(!parameters.containsKey("cfg")) throw new Exception("The cfg property must not empty.");
            if(!parameters.containsKey("companyid")) throw new Exception("The companyid property must not empty.");
            if(!parameters.containsKey("accountid")) throw new Exception("The accountid property must not empty.");
            if(!parameters.containsKey("creatorid")) throw new Exception("The creatorid property must not empty.");
            return true;
        }
        
        return false;
    } // end function::
    
    /**
     * Hàm cập nhật thông tin MAIL_ACCOUNT từ DB.
     * @param mailAccount
     * @param se
     * @param connection
     * @param parameters 
     */
    private static void externalData(MailAccount mailAccount, SendEmail se, Connection connection, Map<String, String> parameters) {
        if(mailAccount == null) return;
        
        MailServerConnection msc = new MailServerConnection(connection);
        MailServer mailServer = msc.findMailServerById(mailAccount.getServer_id());
        if(mailServer == null) return;
        
        if(StringUtils.isEmpty(se.getFrom())) se.setFrom(mailAccount.getAccount_mail_address());
        if(StringUtils.isEmpty(se.getType())) se.setType(InterfaceUtil.Type.SMTP);
        if(StringUtils.isEmpty(se.getHost())) se.setHost(mailServer.getServer_smtp());
        if(se.getPort() <= 0) se.setPort(mailServer.getServer_smtp_port());
        
        if(se.getSsl() == null) se.setSsl(Boolean.valueOf(mailServer.getServer_ssl()));
        if(se.getAuth()== null) se.setAuth(Boolean.valueOf(mailServer.getServer_auth()));
        
        if(StringUtils.isEmpty(se.getUserName())) se.setUserName(mailAccount.getAccount_user_name());
        if(StringUtils.isEmpty(se.getPassword())) se.setPassword(mailAccount.getAccount_password());
        
        parameters.put("accountid", String.valueOf(mailAccount.getAccount_id()));
        parameters.put("companyid", String.valueOf(mailAccount.getCompany_id()));
        parameters.put("creatorid", String.valueOf(mailAccount.getCreator_id()));
    }
}
