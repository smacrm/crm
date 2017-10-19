/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi;

import com.google.gson.Gson;
import gnext.mailapi.datastructure.TreeFolder;
import gnext.dbutils.model.Attachment;
import gnext.dbutils.model.MailAccount;
import gnext.dbutils.model.MailData;
import gnext.dbutils.model.MailFilter;
import gnext.dbutils.model.MailServer;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.services.AttachmentConnection;
import gnext.dbutils.services.Connection;
import gnext.dbutils.services.MailAccountConnection;
import gnext.dbutils.services.MailDataConnection;
import gnext.dbutils.services.MailFilterConnection;
import gnext.dbutils.services.MailServerConnection;
import gnext.dbutils.util.ConnectionUtil;
import gnext.dbutils.util.Console;
import gnext.dbutils.util.FileUtil;
import gnext.dbutils.util.Stopwatchs;
import gnext.dbutils.util.StringUtil;
import gnext.mailapi.mail.DeletedMail;
import gnext.mailapi.mail.Email;
import gnext.mailapi.mail.MailBody;
import gnext.mailapi.mail.ReadEmail;
import gnext.mailapi.mail.SendEmail;
import gnext.mailapi.mail.upload.LocalUpload;
import gnext.mailapi.mail.upload.RemoteUpload;
import gnext.mailapi.mail.upload.UploadBase;
import gnext.mailapi.util.AttachmentUtil;
import gnext.mailapi.util.FolderUtil;
import gnext.mailapi.util.InterfaceUtil;
import static gnext.mailapi.util.InterfaceUtil.ContentType.TEXT_HTML;
import static gnext.mailapi.util.InterfaceUtil.ContentType.TEXT_PLAIN;
import gnext.mailapi.util.MailUtil;
import gnext.mailapi.util.MessageUtil;
import gnext.mailapi.util.ParameterUtil;
import gnext.mailapi.util.items.SearchFilter;
import gnext.mailapi.util.items.SearchGroup;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author daind
 */
public final class MailClient {
    // ExecutorService đại diện cho cơ chế thực thi bất đồng bộ có khả năng thực thi các nhiệm vụ trong background (nền).
    // Việc này tạo ra 1 thread pool với 10 thread dành cho việc thực thi các nhiệm vụ.
//    private final static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    
    public static void main(String[] args) {
        Map<String, String> parameters = ParameterUtil.buildParameters(args);
        if (parameters.isEmpty()) {
            Console.error("You must provider the parameters!");
            return;
        }
        Stopwatchs stopwatchs = new Stopwatchs("[MAILAPI]");
        switch (ParameterUtil.getAction(parameters)) {
            case "receive":
                _InitReadMail(parameters);
                break;
            case "send":
                _InitSendMail(parameters);
                break;
            case "delete":
                _DeleteMail(parameters);
                break;
            case "alert":
                MailUtil._Alert(parameters);
                break;
            case "test":
                _TestMailServer(parameters);
                break;
            default:
                Console.error("The action parameter is not correct.");
        }
        Console.log(MessageFormat.format("{0} ({1} seconds)", stopwatchs.getName(), stopwatchs.elapsedTime()));
    }
    
    private static void _TestMailServer(Map<String, String> parameters) {
        TreeFolder root = null;
        Store store = null;
        try {
            EmailBuilder<Email> builder = new EmailBuilder(new Email()).parameters(parameters);
            Email email = builder.builder();
            email.setDebug(ParameterUtil.getDebug(parameters));
            
            // lấy toàn bộ FOLDERS từ MAIL-SERVER.
            Map<String, Object> ret = FolderUtil.getTreeFolder(email);
            root = (TreeFolder) ret.get("TF");
            store = (Store) ret.get("S");
            
            if(root != null) Console.success();
        } catch (Exception e) {
            Console.error(e);
        } finally {
            try { FolderUtil.closeFolder(root); } catch (Exception e) { } // close folders
            try { if(store != null) store.close(); } catch (Exception e) { } // close store
        }
    }
    
    private static void _InitSendMail(Map<String, String> parameters) {
        try {
            _SendMail(parameters, null);
        } catch (Exception e) {
            Console.error(e.getLocalizedMessage());
        }
    }

    /***
     * Hàm xử lí gửi mail cùng Attachment.
     * Đựoc dùng cho API hoặc CallMethod trong các project khác.
     * @param parameters
     * @param attachments
     * @return 
     * @throws java.lang.Exception 
     */
    public static MailData _SendMail(Map<String, String> parameters, Map<String, InputStream> attachments) 
            throws Exception {
        EmailBuilder<SendEmail> builder = new EmailBuilder(new SendEmail(), InterfaceUtil.Type.SMTP);
        SendEmail se = builder.parameters(parameters).builder();
        se.setDebug(ParameterUtil.getDebug(parameters));
        
        se.setFrom(ParameterUtil.getFrom(parameters));
        se.setRecipient(ParameterUtil.getTo(parameters));
        
        ParameterUtil.geAndSettCc(parameters, se);
        ParameterUtil.geAndSettBcc(parameters, se);
        
        se.setSubject(ParameterUtil.getSubject(parameters));
        se.setMessage(ParameterUtil.getBody(parameters));
        
        ParameterUtil.getAndSetAttchFiles(parameters, se);
        if(attachments != null && !attachments.isEmpty()) se.setAttachments(attachments);
        
        return _SendMail(se, parameters);
    }
    
    /***
     * Hàm xử lí gửi mail cùng Attachment.
     * Đựoc dùng cho API hoặc CallMethod trong các project khác.
     * @param se
     * @param parameters
     * @return 
     * @throws java.lang.Exception 
     */
    public static MailData _SendMail(SendEmail se, Map<String, String> parameters)
            throws Exception {
        boolean savedb = ParameterUtil.checkSendParameter(se, parameters);
        MailData mailData = MailUtil.sendEmail(se, parameters);
        FileUtil.deleteFiles(se.getTempFiles());
        if(!savedb) return mailData;
        
        // slave connection.
        Connection connection = new Connection(ParameterUtil.getCfg(parameters), ParameterUtil.getCompanyId(parameters));
        
        MailDataConnection mdc = new MailDataConnection(connection);
        AttachmentConnection ac = new AttachmentConnection(connection);
        
        java.sql.Connection conn = null;
        try {
            List<Attachment> tmp_Attachments = new ArrayList<>();
            if (se.getAttchFiles() != null && se.getAttchFiles().length > 0) {
                for(String file : se.getAttchFiles()) {
                    Attachment mailAttachment = AttachmentUtil.create(FilenameUtils.getName(file) , null);
                    mailAttachment.setInputStream(new FileInputStream(new File(file)));
                    tmp_Attachments.add(mailAttachment);
                }
            }
            if(se.getAttachments() != null && !se.getAttachments().isEmpty()) {
                for (Map.Entry<String, InputStream> entry : se.getAttachments().entrySet()) {
                    Attachment mailAttachment = AttachmentUtil.create(entry.getKey() , null);
                    mailAttachment.setInputStream(entry.getValue());
                    tmp_Attachments.add(mailAttachment);
                }
            }
            for(Attachment attachment : tmp_Attachments) {
                attachment.setCompany_id(ParameterUtil.getCompanyId(parameters));
                attachment.setCreator_id(ParameterUtil.getCreatorId(parameters));
            }
            mailData.setCompany_id(ParameterUtil.getCompanyId(parameters));
            mailData.setCreator_id(ParameterUtil.getCreatorId(parameters));
            mailData.setMail_data_account_id(ParameterUtil.getAccountId(parameters));

            conn = connection.getConnection();
            ConnectionUtil.startTransaction(conn);
            mdc.create(mailData, conn);
            uploadFileToServer(parameters, connection.getPath(), mailData, tmp_Attachments, ParameterUtil.getCompanyId(parameters));
            for(Attachment a : tmp_Attachments) {
                a.setAttachment_target_type(AttachmentTargetType.MAIL.getId());
                a.setAttachment_target_id(mailData.getMail_data_id());
                ac.create(a, conn);
            }
            ConnectionUtil.commitTransaction(conn);
        } catch (Exception e) {
            ConnectionUtil.rollbackTransaction(conn);
            throw e;
        } finally {
            DbUtils.closeQuietly(conn);
            conn = null;// Giải phóng bộ nhớ.
        }
        
        return mailData;
    }
    
    /***
     * Hàm xóa mail.
     * @param parameters 
     */
    private static void _DeleteMail(Map<String, String> parameters) {
        TreeFolder root = null;
        Store store = null;
        try {
            EmailBuilder<DeletedMail> builder = new EmailBuilder(new DeletedMail());
            DeletedMail email = builder.parameters(parameters).builder();
            
            ParameterUtil.getAndSetMessageid(parameters, email);
            email.setDebug(ParameterUtil.getDebug(parameters)); 

            // lấy toàn bộ FOLDERS từ MAIL-SERVER.
            Map<String, Object> ret = FolderUtil.getTreeFolder(email);
            root = (TreeFolder) ret.get("TF");
            store = (Store) ret.get("S");
            
            List<Message> messages = MailUtil.getMessages(email, root);
            for (Message message : messages) {
                if (MessageUtil.getMailDataUniqueId(message).contains(email.getMessageId())) {
                    MessageUtil.deleteMessage(message);
                    
                    Connection conn = new Connection(ParameterUtil.getCfg(parameters), ParameterUtil.getCompanyId(parameters));
                    int cid = ParameterUtil.getCompanyId(parameters);
                    new MailDataConnection(conn).deleteMailData(cid, email.getMessageId());
                }
            }
        } catch (Exception e) {
            Console.error(e);
        } finally {
            try { FolderUtil.closeFolder(root); } catch (Exception e) { } // close folders
            try { if(store != null) store.close(); } catch (Exception e) { } // close store
        }
    }

    private static void _InitReadMail(Map<String, String> parameters) {
        try {
            ParameterUtil.checkReceiveParameter(parameters);
            Connection slave_connection = new Connection(ParameterUtil.getCfg(parameters), ParameterUtil.getCompanyId(parameters));
            
            MailAccountConnection mac = new MailAccountConnection(slave_connection);
            MailServerConnection msc = new MailServerConnection(slave_connection);
            
            int cid = ParameterUtil.getCompanyId(parameters);
            List<MailAccount> accounts = mac.findMailReceiveAccounts(cid);
            if(accounts == null || accounts.isEmpty()) {
                Console.log("No accounts for reading mails.");
                return;
            } else {
                Console.log("Have " + accounts.size() + " Accounts is preparing...");
            }
            for (MailAccount account : accounts) {
                if (account.getAccount_is_deleted() != null && account.getAccount_is_deleted() == 1) {
                    Console.log(account.getAccount_name() + " is mark is deleted. ignore it!");
                    continue;
                }
                
                MailServer mailServer = msc.findMailServerById(account.getServer_id());
                if(mailServer == null) continue;
                
                try {
                    _ReadEmail(parameters, mailServer, account, slave_connection);
                } catch (Exception e) {
                    Console.error(e);
                }
                
//                EXECUTOR_SERVICE.execute(() -> {
//                    try { _ReadEmail(parameters, mailServer, account, connection); } catch (Exception e) { Console.error(e); }
//                });
            }
//            EXECUTOR_SERVICE.shutdown();
        } catch (Exception e) {
            Console.error(e.getLocalizedMessage());
        }
    }

    /**
     * Hàm xử lí đọc nội dung Mail cùng Attachment lưu trên FTP server.
     * @param parameters
     * @param mailServer
     * @param mailAccount
     * @param connection
     * @throws Exception 
     */
    private static void _ReadEmail(Map<String, String> parameters, MailServer mailServer, MailAccount mailAccount, Connection connection)
            throws Exception {
        TreeFolder root = null;
        Store store = null;
        try {
            MailDataConnection mdc = new MailDataConnection(connection);
            AttachmentConnection ac = new AttachmentConnection(connection);

            EmailBuilder<ReadEmail> builder = new EmailBuilder(new ReadEmail()).parameters(parameters);
            builder = builder.type(mailServer.getServer_type()).host(mailServer.getServer_host()).port(mailServer.getServer_port())
                    .username(mailAccount.getAccount_user_name()).password(mailAccount.getAccount_password())
                    .ssl(Boolean.valueOf(mailServer.getServer_ssl())).auth(Boolean.valueOf(mailServer.getServer_auth()));

            ReadEmail email = builder.builder();
            ParameterUtil.getAndSetPrior(parameters, email);
            email.setDebug(ParameterUtil.getDebug(parameters));

            // lấy toàn bộ FOLDERS từ MAIL-SERVER.
            Map<String, Object> ret = FolderUtil.getTreeFolder(email);
            root = (TreeFolder) ret.get("TF");
            store = (Store) ret.get("S");
            
            LinkedList<Message> messages = MailUtil.getMessages(email, root);
//            MailUtil.printTwoMessage(messages);

            Console.log("Total messages is " + messages.size());
            for (Message message : messages) {
                try {
                    Map<String, Object> map = _BuildMailData(message); if(map == null) continue;
                    MailData mailData = (MailData) map.get("mailData");
                    if(_FillAnotherDataToMail(mailData, mailServer, mailAccount, connection)) {
                        List<Attachment> mats = (List<Attachment>) map.get("mailAttachment");
                        _FillAnotherDataToAttachment(mats, mailAccount, email);
                        java.sql.Connection conn = null;
                        try { conn = connection.getConnection(); } catch (Exception e) { conn = null; }
                        if(conn == null) return;
                        try {
                            ConnectionUtil.startTransaction(conn);
                            mdc.create(mailData, conn);
                            uploadFileToServer(parameters, connection.getPath(), mailData, mats, ParameterUtil.getCompanyId(parameters));
                            for(Attachment a : mats) {
                                a.setAttachment_target_type(AttachmentTargetType.MAIL.getId());
                                a.setAttachment_target_id(mailData.getMail_data_id());
                                ac.create(a, conn);
                            }
                            ConnectionUtil.commitTransaction(conn);
                        } catch (Exception e) {
                            ConnectionUtil.rollbackTransaction(conn);
                            Console.error(e);
                        } finally {
                            DbUtils.closeQuietly(conn);
                            conn = null; // Giải phóng bộ nhớ.
                        }
                    }
                } catch (Exception e) {
                    Console.error(e);
                }
            } // kết thúc đọc danh sách messages.
        } finally {
            try { FolderUtil.closeFolder(root); } catch (Exception e) { } // close folders
            try { if(store != null) store.close(); } catch (Exception e) { } // close store
            root = null; // release memory.
        }
    }
    
    /***
     * 
     * @param pathToConf
     * @param email
     * @param mailData
     * @param mats
     * @throws Exception 
     */
    private static void uploadFileToServer(Map<String, String> parameters, String pathToConf, MailData mailData, List<Attachment> mats, Integer companyId) throws Exception {
        Integer serverId = ParameterUtil.getServerId(parameters);
        Map<String, Object> data = new HashMap<>();
        UploadBase u = null;
        
        if(serverId == null || serverId <= 0) {
            u = new LocalUpload();
            serverId = -1; // đánh dấu attachment này là không thuộc server nào và được lưu là dưới local.
            data.put("mats", mats);
        } else {
            u = new RemoteUpload();
            data.put("mats", mats);
            data.put("serverId", ParameterUtil.getServerId(parameters));
            data.put("mailData", mailData);
            data.put("pathToConf", pathToConf);
            data.put("companyId", companyId);
        }
        
        for(Attachment attachment : mats)
            attachment.setServer_id(serverId);
        
        u.upload(data);
    }
    
    /**
     * Hàm xử lí bổ sung thông tin bắt buộc cho Attachment.
     * @param mats
     * @param ma
     * @param re
     * @param connection 
     */
    private static void _FillAnotherDataToAttachment(List<Attachment> mats, MailAccount ma, ReadEmail re) {
        for (Attachment mat : mats) {
            mat.setCompany_id(ma.getCompany_id());
            mat.setCreator_id(ma.getCreator_id());
        }
    }

    /**
     * Hàm xử lí bổ sung thông tin bắt buộc cho MailData.
     * @param mailData
     * @param mailServer
     * @param mailAccount
     * @param connection
     * @return 
     */
    private static boolean _FillAnotherDataToMail(MailData mailData, MailServer mailServer, MailAccount mailAccount,
            Connection connection) {
        mailData.setMail_data_account_id(mailAccount.getAccount_id());
        mailData.setMail_data_account_name(mailAccount.getAccount_name());
        mailData.setCreator_id(mailAccount.getCreator_id());
        mailData.setCompany_id(mailAccount.getCompany_id());
        mailData.setMail_data_account_name(mailAccount.getAccount_name());
        mailData.setMail_data_mail_server(mailServer.getServer_name());
        mailData.setMail_data_folder_code(FolderUtil.getFolderCode(InterfaceUtil.Folder.INBOX));
        
        parseFilterFolderMail(connection, mailData);
        
        MailDataConnection mdc = new MailDataConnection(connection);
        if (!mdc.findByUniqueId(mailData).isEmpty()) {
            Console.log("The " + mailData.getMail_data_unique_id() + " is exists in ");
            return false;
        }
        
        return true;
    }
    
    /***
     * Hàm xử lí phân tích sẽ chuyển mail vào folder nào.
     * @param connection
     * @param mailData 
     */
    private static void parseFilterFolderMail(Connection connection, MailData mailData) {
        if(mailData == null || mailData.getCompany_id() == null) return;
        
        MailFilterConnection mfc = new MailFilterConnection(connection);
        List<MailFilter> filters = mfc.findMailFilterByCompanyId(mailData.getCompany_id());
        if(filters == null || filters.isEmpty()) return;
        
        for(MailFilter mf : filters) {
            if(mf.getMail_filter_deleted() != null && mf.getMail_filter_deleted() == 1) continue;
            String mf_confitions = mf.getMail_filter_conditions();
            if(mf_confitions == null || mf_confitions.isEmpty()) continue;
            
            SearchGroup[] groups = new Gson().fromJson(mf_confitions, SearchGroup[].class);
            SearchFilter sf = new SearchFilter(Arrays.asList(groups));
            sf.doSearch(mailData);
            if(!sf.found) continue;
            
            String fc = mf.getMail_filter_move_folder_code();
            if(fc != null && !fc.isEmpty()) {
                mailData.setMail_data_folder_code(fc);
                break;
            }
        }
    }

    /**
     * Hàm xử lí lấy thông tin Mail và dữ liệu Attachment.
     * @param message
     * @param pathToAttachment
     * @return
     * @throws Exception 
     */
    private static Map<String, Object> _BuildMailData(Message message)
            throws Exception {
        Map<String, Object> m = new HashMap<>();
        MailData mailData = new MailData();
        List<Attachment> mailAttachments = new ArrayList<>();
        m.put("mailData", mailData);
        m.put("mailAttachment", mailAttachments);
        
        String header = MessageUtil.getHeader(message);
        String subject = message.getSubject();Console.log("subject: " + subject);
        String from = MessageUtil.getFrom(message);
        String to = MessageUtil.getRecipient(message, Message.RecipientType.TO);
        String cc = MessageUtil.getRecipient(message, Message.RecipientType.CC);
        String bcc = MessageUtil.getRecipient(message, Message.RecipientType.BCC);
        Date sentDate = message.getSentDate(); // Ngày gửi mail.
        if(sentDate == null || sentDate.getTime() <= 0) sentDate = Calendar.getInstance().getTime();
        int dataSize = message.getSize();
        String priotity = MessageUtil.getHeaderValue(message, "X-Priority");
        String messageId = MessageUtil.getMailDataUniqueId(message);
        String returnPath = MessageUtil.getHeaderValue(message, "Return-Path");
        int folderCode = message.getFolder().getType();
        String folderName = message.getFolder().getFullName();
        
        MailBody body = new MailBody();
        readBody(message, mailAttachments, body);
        
        if (!StringUtil.isEmpty(priotity)) {
            mailData.setMail_data_priority(Integer.parseInt(priotity));
        } else {
            mailData.setMail_data_priority(Integer.parseInt(InterfaceUtil.Priority.LOW));
        }
        mailData.setMail_data_unique_id(messageId);
        mailData.setMail_data_header(header);
        
        /* nếu không có subject thiết lập subject với giá trị 'no subject' */
        if(subject == null || subject.isEmpty()) subject = "[no subject]";
        mailData.setMail_data_subject(subject);
        
        mailData.setMail_data_from(from);
        mailData.setMail_data_to(to);
        mailData.setMail_data_cc(cc);
        mailData.setMail_data_bcc(bcc);
        mailData.setMail_data_datetime(sentDate);
        mailData.setMail_data_size(dataSize);
        mailData.setMail_data_body(body.getContent());
        
        // lấy plain-text tư email phục vụ cho việc phân cắt mail.
        mailData.setMail_data_body_plain_text(body.getPlainContent());
        
        mailData.setMail_data_delete_flag(0);
        mailData.setMail_data_is_read(false);
        mailData.setMail_data_reply_return_path(returnPath);
        mailData.setMail_data_folder_code(String.valueOf(folderCode));
        mailData.setMail_data_folder_name(folderName);
        mailData.setCreated_time(Calendar.getInstance().getTime());
        
        return m;
    }

    /***
     * Hàm xử lí đọc nội dung body của mail.
     * @param message
     * @param mailAttachments
     * @param body
     * @throws Exception 
     */
    private static void readBody(Message message, List<Attachment> mailAttachments, MailBody body)
            throws Exception {
        String contentType = MessageUtil.getContentType(message);
        if (contentType.contains("multipart")) {
            Multipart multiPart = MessageUtil.getMultipart(message);
            int noOfPart = multiPart.getCount();
            for (int k = 0; k < noOfPart; k++) {
                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(k);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    
                    String fileName = part.getFileName();
                    fileName = MimeUtility.decodeText(fileName);
                    Attachment mailAttachment = AttachmentUtil.create(fileName, "");
                    mailAttachment.setPart(part);
                    
                    if (part.getContentType().toLowerCase().contains(TEXT_HTML) || part.getContentType().toLowerCase().contains(TEXT_PLAIN)) {
                        AttachmentUtil.parsePrimaryContent(message, body);
                    } else AttachmentUtil.readPart(message, body);
                    
                    mailAttachments.add(mailAttachment);
                } else AttachmentUtil.readPart(message, body);
            }
        } else AttachmentUtil.parsePrimaryContent(message, body);
        
        String content = body.getContent();
        if(content == null || content.isEmpty()) throw new Exception("Can not read body of message " + String.valueOf(message.getSubject()));
    }
}
