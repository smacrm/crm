/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util;

import gnext.mailapi.datastructure.TreeFolder;
import gnext.dbutils.model.MailData;
import gnext.dbutils.util.Console;
import gnext.dbutils.util.FileUtil;
import gnext.dbutils.util.StringUtil;
import gnext.mailapi.EmailBuilder;
import gnext.mailapi.mail.DateRange;
import gnext.mailapi.mail.ReadEmail;
import gnext.mailapi.mail.SendEmail;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author daind
 */
public class MailUtil {

    /**
     * Scan all the messages base on folder parameter.
     *
     * @param re
     * @param root
     * @throws java.lang.Exception @see Email
     * @return The messages
     */
    public static LinkedList<Message> getMessages(ReadEmail re, TreeFolder root) throws Exception {
        if (root == null) return new LinkedList<>();
        TreeFolder.export(root, 1);
        
        // xây dựng điều kiện tìm kiếm mails.
        List<FlagTerm> ft = gnext.mailapi.util.FlagTerm.getFlagTerm(re); // mặc định tìm kiếm theo loại items.
        
        // bộ đệ qui lấy tòan bộ messages.
        LinkedList<Message> messages = new LinkedList<>();
        List<TreeFolder> treeFolders = root.getChildren();
        for (TreeFolder treeFolder : treeFolders)
            addMessages(re, treeFolder, messages, ft);
        
        // trả về danh sách messages lấy được.
        return messages;
    }
    
    /***
     * Read mail with thread(period).
     * 
     * @param e
     * @param tf
     * @param m
     * @param ft
     * @param term
     * @throws Exception 
     */
    private static void addMessages(ReadEmail re, TreeFolder tf, LinkedList<Message> m, List<FlagTerm> ft)
            throws Exception {
        // lấy messages từ folder cha.
        Message[] ma = searchMessages(re, tf.getFolder(), ft);
        if(ma != null) m.addAll(Arrays.asList(ma));
        
        //  duyệt tất cả các folders con và lấy tiếp messages.
        List<TreeFolder> tfcs = tf.getChildren();
        for (TreeFolder tfc : tfcs)
            addMessages(re, tfc, m, ft);
    }
    
    /**
     * tìm kiếm messages trong the folder cùng trạng thái ft và điều kiện tìm kiếm term.
     * @param e
     * @param folder
     * @param ft
     * @param term
     * @return
     * @throws Exception 
     */
    private static Message[] searchMessages(ReadEmail re, Folder folder, List<FlagTerm> ft) throws Exception {
        if(folder == null) return null;
        
        // kiểm tra nếu folder đang trỏ tới không đúng với folder chỉ định đọc thỉ bỏ qua.
        String source_folder = folder.getFullName().toLowerCase().trim();
        String target_folder = re.getFolder().toLowerCase().trim();
        if (!source_folder.contains(target_folder)) return null;
        
        // để đọc được messages cần mở folder đó.
        FolderUtil.openFolder(folder);
        
        // Searching for a period or in general by time is done by the server only if you use IMAP to connect.
        DateRange range = new DateRange(re.getPrior());
        if(re.isIMapProtocol()) {
            return filter(folder, ft, range.buildReceivedDateTerm());
        } else if(re.isPop3Protocol()) {
            // If you instead of IMAP use POP3,
            // me choice is to filter (on the client) on the entire list of messages that you fetched from the server
            Message[] messages = filter(folder, ft, null);
            messages = MessageUtil.getByRangeBaseOnSentDate(messages, range);

            // Hoặc có thể sử dụng lọc theo sent-date.
            // TODO: Cần cải thiện tốc độ lọc theo kiểu POP3.
//            Message[] messages = filter(folder, ft, range.buildSentDateTermTerm());

            return messages;
        }
        
        return null;
    }
    
    /**
     * Hàm xác định theo điều kiện tìm kiếm theo flag-term và search-term.
     * Gmail handles POP deletion specially.
     * You can configure what Gmail should do when a message is deleted through POP in Gmail Settings,
     * on the Forwarding and POP / IMAP tab.
     * @param folder
     * @param ft
     * @param st
     * @return
     * @throws Exception 
     */
    private static Message[] filter(Folder folder, List<FlagTerm> ft, SearchTerm st) throws Exception {
//        FetchProfile fetchProfile = new FetchProfile();
//        fetchProfile.add(FetchProfile.Item.ENVELOPE);

        if((ft == null || ft.isEmpty()) && st == null) return folder.getMessages();
        if((ft != null && !ft.isEmpty()) && st == null) return folder.search(new OrTerm(ft.toArray(new FlagTerm[ft.size()])));
        
        if((ft == null || ft.isEmpty()) && st != null) return folder.search(st);
        if((ft != null && !ft.isEmpty()) && st != null) return folder.search(new AndTerm(new OrTerm(ft.toArray(new FlagTerm[ft.size()])), st));
        
        return null;
    }
    
    /**
     * API for sending email.
     *
     * @param se
     * @return  @see SendEmail
     * @param parameters the parameters input by users.
     */
    public static MailData sendEmail(SendEmail se, Map<String, String> parameters)  {
        try {
            Properties conf = se.getConfiguration();
            boolean auth = se.getAuth() != null ? se.getAuth() : false;
            Session session = SessionUtil.getSession(conf, auth, se.getUserName(), se.getPassword(), se.getDebug());

            MimeMessage msg = new MimeMessage(session);
            msg.setDescription(se.getDescription());
            msg.setFrom(new InternetAddress(se.getFrom(), se.getName()));
            msg.setSentDate(Calendar.getInstance().getTime());
            
            if (se.getRecipient() != null && se.getRecipient().length > 0) msg.setRecipients(Message.RecipientType.TO, StringUtil.getAddress(se.getRecipient()));
            if (se.getCc() != null && se.getCc().length > 0) msg.setRecipients(Message.RecipientType.CC, StringUtil.getAddress(se.getCc()));
            if (se.getBcc() != null && se.getBcc().length > 0) msg.setRecipients(Message.RecipientType.BCC, StringUtil.getAddress(se.getBcc()));
            
            msg.setSubject(se.getSubject());
            msg.setHeader("X-Priority", se.getPriority());
            
            // Attachment files to email.
            _Attachment(se, msg);
            
            // Sending mail.
            Transport.send(msg);
            
            // create mail data.
            MailData md = new MailData();
            md.setMail_data_unique_id(msg.getMessageID());
            md.setMail_data_header(MessageUtil.getHeader(msg));
            md.setMail_data_subject(msg.getSubject());
            md.setMail_data_from(MessageUtil.getFrom(msg));
            md.setMail_data_to(MessageUtil.getRecipient(msg, Message.RecipientType.TO));
            md.setMail_data_cc(MessageUtil.getRecipient(msg, Message.RecipientType.CC));
            md.setMail_data_bcc(MessageUtil.getRecipient(msg, Message.RecipientType.BCC));
            md.setMail_data_datetime(msg.getSentDate());
            md.setMail_data_size(msg.getSize());
            md.setMail_data_reply_return_path(MessageUtil.getHeaderValue(msg, "Return-Path"));
            md.setMail_data_body(se.getMessage());
            md.setMail_data_folder_code(FolderUtil.getFolderCode(InterfaceUtil.Folder.SENT));
            md.setMail_data_folder_name(InterfaceUtil.Folder.SENT);
            md.setMail_data_delete_flag(0);
            md.setCreated_time(Calendar.getInstance().getTime());
            return md;
        } catch (Exception e) {
            Console.error(e);
            if (parameters != null) {
                try { 
                    _AlertToSupport(parameters, e);
                } catch (Exception ex_alert_support) {
                    // Có phải thật sự không cần làm j ở đây ????
                }
            }
        }
        
        return null;
    }

    /**
     * Hàm xử lí gửi thông báo lỗi tới supporter.
     * @param parameters
     * @param e 
     */
    private static void _AlertToSupport(Map<String, String> parameters, Exception e) {
        LocaleUtil locale = new LocaleUtil();
        
        Properties prop = FileUtil.loadConf(ParameterUtil.getCfg(parameters));
        if(prop == null) return;

        EmailBuilder<SendEmail> eb = new EmailBuilder<>(new SendEmail(), InterfaceUtil.Type.SMTP);
        eb.host("smtp.gmail.com").port(Integer.parseInt(String.valueOf(prop.get("admin.port"))))
                    .username((String) prop.get("admin.user")).password((String) prop.get("admin.pwd"))
                    .ssl(false).auth(true);
        SendEmail se = eb.builder();
        se.setFrom((String) prop.get("admin.from"));
        se.setRecipient(new String[]{(String) prop.get("support.to")});
        se.setSubject(locale.get("mail.alert.support.subject"));
        se.setMessage(e.getMessage());
        se.setPriority(InterfaceUtil.Priority.HEIGHEST);
        se.setContentType(InterfaceUtil.ContentType.TEXT_PLAIN_UTF_8);
        se.setDescription(locale.get("mail.alert.support.description"));
        Console.log("Begin send an email alert to support.");
        sendEmail(se, null);
    }
    
    /**
     * Hàm gửi mail alert.
     * @param parameters 
     * @return  
     */
    public static MailData _Alert(Map<String, String> parameters) {
        Properties prop = FileUtil.loadConf(ParameterUtil.getCfg(parameters));
        if(prop == null) return null;
        
        EmailBuilder<SendEmail> eb = new EmailBuilder<>(new SendEmail(), InterfaceUtil.Type.SMTP);
        eb.host("smtp.gmail.com").port(Integer.parseInt(String.valueOf(prop.get("admin.port"))))
                .username((String) prop.get("admin.user")).password((String) prop.get("admin.pwd"))
                .ssl(true).auth(true);
        SendEmail se = eb.builder();
        se.setFrom((String) prop.get("admin.from"));
        se.setRecipient(ParameterUtil.getTo(parameters));
        se.setSubject(ParameterUtil.getSubject(parameters));
        se.setMessage(ParameterUtil.getBody(parameters));
        se.setPriority(InterfaceUtil.Priority.HEIGHEST);
        se.setContentType(InterfaceUtil.ContentType.TEXT_PLAIN_UTF_8);
        return _Alert(se);
    }
    
    /**
     * Thông báo từ gnextadmin tới người dùng.
     * @param se 
     * @return  
     */
    public static MailData _Alert(SendEmail se) {
        Console.log("Begin send an email alert.");
        return sendEmail(se, null);
    }

    /**
     * Hàm xử lí attachment file tới mail.
     * 
     * @param se
     * @param msg
     * @throws Exception 
     */
    private static void _Attachment(SendEmail se, Message msg) throws Exception {
        boolean hasAttachment = (se.getAttchFiles() != null && se.getAttchFiles().length > 0) || (se.getAttachments() != null && !se.getAttachments().isEmpty());
        if (hasAttachment) {
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(se.getMessage(), se.getContentType());
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            attachmentFile(se, multipart);
            attachmentStream(se, multipart);
            msg.setContent(multipart, se.getContentType());
        } else {
            msg.setContent(se.getMessage(), se.getContentType());
        }
    }
    
    private static void attachmentFile(SendEmail se, Multipart multipart) throws Exception {
        if(se == null) return;
        if(se.getAttchFiles() == null || se.getAttchFiles().length <=0) return;
        for (String filePath : se.getAttchFiles()) {
            MimeBodyPart attachPart = new MimeBodyPart();
            attachPart.attachFile(filePath);
            
            String filename = FilenameUtils.getName(filePath);
            attachPart.setFileName(filename);
//            attachPart.setFileName(MimeUtility.encodeText(filename, "UTF-8", null));
            
            multipart.addBodyPart(attachPart);
        }
    }
    
    private static void attachmentStream(SendEmail se, Multipart multipart) throws Exception {
        if(se == null) return;
        if(se.getAttachments() == null || se.getAttachments().isEmpty()) return;
        for (Map.Entry<String, InputStream> entry : se.getAttachments().entrySet()) {
            InputStream cloneIn = null;
            try {
                cloneIn = FileUtil.cloneStream(entry.getValue());
                File tempFile = FileUtil.stream2file(cloneIn, entry.getKey());
                
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(tempFile); se.getTempFiles().add(tempFile);
                
                String fileName = entry.getKey();
                attachPart.setFileName(fileName);
//                attachPart.setFileName(MimeUtility.encodeText(fileName, "UTF-8", null));

                multipart.addBodyPart(attachPart);
            } finally {
                try { entry.getValue().reset(); } catch (Exception e) { } // chuyển con trỏ về đầu stream.
                try { cloneIn.close(); } catch (Exception e) { }
            }
        }
    }
    
    /**
     * Hàm xử lí lấy địa chỉ mail từ 1 chuỗi.
     * @param s
     * @return 
     */
    public static Set<String> getEmailAddress(final String s) {
        Set<String> r = new HashSet<>();
        Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(s);
        while (m.find()) {
            r.add(m.group());
        }
        return r;
    }
    
    public static void printTwoMessage(LinkedList<Message> messages) throws Exception {
        for(int i=0; i< messages.size(); i++) {
            System.out.println("Sent date: " + messages.get(i).getSentDate());
            System.out.println("Received date: " + messages.get(i).getReceivedDate());
            System.out.println("From: " + Arrays.toString(messages.get(i).getFrom()));
            System.out.println("Subject: " + messages.get(i).getSubject());
            if(i == 2) break;
        }
    }
}
