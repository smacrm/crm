/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.mail;

import gnext.dbutils.util.StringUtil;
import gnext.mailapi.util.InterfaceUtil;
import java.io.Serializable;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Một vài giải thích cho cấu hình MAIL.
 * 
 * 1. mail.pop3.connectiontimeout is how long you are willing to wait to make an initial connection to the POP3 mail server.
 * E.g. If you set this to 30 secs, then if you try to connect to a POP3 server and get no response inside 30 seconds, you'll give up and return an error.
 * 
 * 2. mail.pop3.timeout is after you have connected to the mail server, how long you are willing to wait to get data back as you are reading mail messages.
 * So again if you set it 20 seconds, this means after you ask to read an email if you don't get a response inside of 20 seconds, you'll give up an report an error.
 * 
 * 3. Thông số Port các dịch vụ này được chia làm 2 loại.Có sử dụng SSL và không sử dụng SSL. Tùy vào từng loại mà sẽ có cách cấu hình khác nhau.
 * 
 * @author daind
 */
public class Email implements Serializable {

    private static final long serialVersionUID = 2063697185478810783L;
    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    @Getter @Setter private String type;
    @Getter @Setter private Integer port;
    @Getter @Setter private String host;
    @Getter @Setter private String userName;
    @Getter @Setter private String password;
    @Getter @Setter private String folder;
    @Getter @Setter private String flag;
    
    @Getter @Setter private Boolean ssl;
    @Getter @Setter private Integer sslPort = 0;
    @Getter @Setter private Integer sslSmtpPort = 0;
    
    @Getter @Setter private Boolean auth;
    @Getter @Setter private Boolean tls;
    
    @Getter @Setter private Integer timeout = 0;
    @Getter @Setter private Boolean debug = true;
    
    @Getter @Setter private String description = "";
    @Getter @Setter private String contentType = InterfaceUtil.ContentType.TEXT_HTML_UTF_8;
    
    private static final String[] STORE_IMAPS_OVER_SSL = new String[]{ "imap.gmail.com", "imap.mail.yahoo.com", "imap-mail.outlook.com" };
    public String getStoreProtocol() {
        for (String s : STORE_IMAPS_OVER_SSL) if (host.equals(s)) return type.toLowerCase() + "s";
        return type.toLowerCase();
    }

    /**
     * @return
     * @throws Exception 
     */
    public Properties getConfiguration() throws Exception {
        if(StringUtil.isEmpty(host)) throw new Exception("Cannot find valid hostname for mail session.");
        if(StringUtils.isEmpty(type)) throw new Exception("Type of email is invalid, please provider it.");
        
        final Properties pros = new Properties();
        if (debug) pros.put("mail.debug", "true");
        if(isIMapProtocol() || isPop3Protocol()) pros.put("mail.store.protocol", type + "s");
        if (isIMapProtocol()) {
            buildImapConf(pros);
        } else if (isPop3Protocol()) {
            buildPop3Conf(pros);
        } else if (isSmtpProtocol()) {
            buildSmtpConf(pros);
        }
        return pros;
    }
    
    /**
     * IMAP uses port 443, but SSL/TLS encrypted IMAP uses port 993.
     * @param pros 
     */
    private void buildImapConf(final Properties pros) {
        pros.put("mail.imap.host", host);
        pros.put("mail.imap.port", port);
        pros.put("mail.imap.user", userName);
        pros.put("mail.imap.ssl.enable", ssl != null ? ssl : false);
        if (ssl) {
            pros.put("mail.imap.port", sslPort == null || sslPort <= 0  ? port : sslPort);
            pros.put("mail.imap.socketFactory.port", sslPort == null || sslPort <= 0  ? port : sslPort);
            pros.put("mail.imap.socketFactory.class", SSL_FACTORY);
            pros.put("mail.imap.socketFactory.fallback", "false");
        }
        
        /**
         * STARTTLS is a way to take an existing insecure connection and upgrade it to a secure connection using SSL/TLS.
         * Note that despite having TLS in the name, STARTTLS doesn't mean you have to use TLS, you can use SSL.
         * At some point, it was decided that having 2 ports for every protocol was wasteful,
         * and instead you should have 1 port that starts off as plaintext,
         * but the client can upgrade the connection to an SSL/TLS encrypted one. This is what STARTTLS was created to do.
         */
        pros.put("mail.imap.starttls.enable", tls != null ? tls : false);
        
        if (timeout != null && timeout > 0) pros.put("mail.imap.connectiontimeout", timeout.toString());
        if (timeout != null && timeout > 0) pros.put("mail.imap.timeout", timeout.toString());
    }
    
    /**
     * POP uses port 110, but SSL/TLS encrypted POP uses port 995.
     * @param pros 
     */
    private void buildPop3Conf(final Properties pros) {
        pros.put("mail.pop3.host", host);
        pros.put("mail.pop3.port", port);
        pros.put("mail.pop3.user", userName);
        pros.put("mail.pop3.ssl.enable", ssl != null ? ssl : false);
        if (ssl) {
            pros.put("mail.pop3.port", sslPort == null || sslPort <= 0 ? port : sslPort);
            pros.put("mail.pop3.socketFactory.port", sslPort == null || sslPort <= 0 ? port : sslPort);
            pros.put("mail.pop3.socketFactory.class", SSL_FACTORY);
            pros.put("mail.pop3.socketFactory.fallback", "false");
        }
        
        /**
         * STARTTLS is a way to take an existing insecure connection and upgrade it to a secure connection using SSL/TLS.
         * Note that despite having TLS in the name, STARTTLS doesn't mean you have to use TLS, you can use SSL.
         * At some point, it was decided that having 2 ports for every protocol was wasteful,
         * and instead you should have 1 port that starts off as plaintext,
         * but the client can upgrade the connection to an SSL/TLS encrypted one. This is what STARTTLS was created to do.
         */
        pros.put("mail.pop3.starttls.enable", tls != null ? tls : false);
        
        if (timeout != null && timeout > 0) pros.put("mail.pop3.connectiontimeout", timeout.toString());
        if (timeout != null && timeout > 0) pros.put("mail.pop3.timeout", timeout.toString());
    }
    
    /**
     * SMTP uses port 25, but SSL/TLS encrypted SMTP uses port 465.
     * @param pros 
     */
    private void buildSmtpConf(final Properties pros) {
        pros.put("mail.transport.protocol", "smtp");
        pros.put("mail.smtp.host", host);
        pros.put("mail.smtp.port", port);
        pros.put("mail.smtp.ssl.enable", ssl != null ? ssl : false);
        if (ssl) {
            pros.put("mail.smtp.port", sslSmtpPort == null || sslSmtpPort <= 0 ? port : sslSmtpPort);
            pros.put("mail.smtp.socketFactory.port", sslSmtpPort == null || sslSmtpPort <= 0  ? port : sslSmtpPort);
            pros.put("mail.smtp.socketFactory.class", SSL_FACTORY);
            pros.put("mail.smtp.socketFactory.fallback", "false");
        }
        pros.put("mail.smtp.auth", auth != null ? auth : false);
        
        /**
         * STARTTLS is a way to take an existing insecure connection and upgrade it to a secure connection using SSL/TLS.
         * Note that despite having TLS in the name, STARTTLS doesn't mean you have to use TLS, you can use SSL.
         * At some point, it was decided that having 2 ports for every protocol was wasteful,
         * and instead you should have 1 port that starts off as plaintext,
         * but the client can upgrade the connection to an SSL/TLS encrypted one. This is what STARTTLS was created to do.
         */
        pros.put("mail.smtp.starttls.enable", tls != null ? tls : false);
        
//         pros.put("mail.smtp.starttls.required", tls != null ? tls.booleanValue() : false);
        if (timeout != null && timeout > 0) pros.put("mail.smtp.connectiontimeout", timeout.toString());
        if (timeout != null && timeout > 0) pros.put("mail.smtp.timeout", timeout.toString());
    }
    
    public boolean isIMapProtocol() { return type.equals(InterfaceUtil.Type.IMAP); }
    public boolean isPop3Protocol() { return type.equals(InterfaceUtil.Type.POP3); }
    public boolean isSmtpProtocol() { return type.equals(InterfaceUtil.Type.SMTP); }
}
