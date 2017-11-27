/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util;

import gnext.dbutils.util.Console;
import gnext.dbutils.util.StringUtil;
import gnext.mailapi.mail.DateRange;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public class MessageUtil {
    
    public static final String RECEIVED_HEADER_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
    public static final String RECEIVED_HEADER_REGEXP = "^[^;]+;(.+)$";
    
    /**
     * Hàm xử lí sắp xếp mail theo ngày gửi.
     * chú ý: sử dụng trong trường hợp là nhận theo giao thức POP3.
     * @param messages
     * @param range
     * @return
     * @throws Exception 
     */
    public static Message[] getByRangeBaseOnSentDate(Message[] messages, DateRange range) throws Exception {
        LinkedList<Message> reMess = new LinkedList<>();
        
        // TODO: sử dụng thuật toán quicksearch để cải thiện performace.
        for(int i=messages.length-1; i >= 0; i--) {
            Message message = messages[i];
            
            long compare_min = compareTimes(message.getSentDate(), range.getMinDate());
            long compare_max = compareTimes(message.getSentDate(), range.getMaxDate());
            if(compare_min >= 0 && compare_max <= 0) {
                reMess.add(message);
            }/* else {
                return reMess.toArray(new Message[reMess.size()]);
            } */
        }
        
//        for (Message message : messages) {
//            if (message.getSentDate().after(range.getMinDate()) && message.getSentDate().before(range.getMaxDate())) {
//                reMess.add(message);
//            }
//        }

        return reMess.toArray(new Message[reMess.size()]);
    }
    
    private static long getDatePart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    private static long compareTimes(Date d1, Date d2) {
        long t1 = getDatePart(d1);
        long t2 = getDatePart(d2);
        return (t1 - t2);
    }
    
    /**
     * Hàm xử lí lấy content-type, nếu không lấy được thì đọc từ Header.
     * @param message
     * @return
     * @throws Exception 
     */
    public static String getContentType(Part message) throws Exception {
        MimeMessage mimeMessage = (MimeMessage) message;
        return mimeMessage.getContentType();
    }
    
    /**
     * Hàm xử lí lấy nội dung body của MAIL.
     * @param message
     * @return
     * @throws Exception 
     */
    public static Multipart getMultipart(Part message) throws Exception {
        MimeMessage mimeMessage = (MimeMessage) message;
        return (Multipart) mimeMessage.getContent();
    }
    
    /**
     * @deprecated 
     * Lấy ngày nhận mail từ trong the Header nếu không lấy đươc theo API của the MimeMessage.
     * @param message
     * @return
     * @throws MessagingException 
     */
    public static Date resolveReceivedDate(Message message) throws MessagingException {
        if (message.getReceivedDate() != null) return message.getReceivedDate();
        
        String[] receivedHeaders = message.getHeader("Received");
        if (receivedHeaders == null) return (Calendar.getInstance().getTime());
        
        SimpleDateFormat sdf = new SimpleDateFormat(RECEIVED_HEADER_DATE_FORMAT);
        Date finalDate = Calendar.getInstance().getTime();
        finalDate.setTime(0l);
        boolean found = false;
        for (String receivedHeader : receivedHeaders) {
            Pattern pattern = Pattern.compile(RECEIVED_HEADER_REGEXP);
            Matcher matcher = pattern.matcher(receivedHeader);
            if (matcher.matches()) {
                String regexpMatch = matcher.group(1);
                if (regexpMatch != null) {
                    regexpMatch = regexpMatch.trim();
                    try {
                        Date parsedDate = sdf.parse(regexpMatch);
                        if (parsedDate.after(finalDate)) {
                            finalDate = parsedDate;
                            found = true;
                        }
                    } catch (Exception e) { }
                } else {
                    //LogMF.warn(log, "Unable to match received date in header string {0}", receivedHeader);
                }
            }
        }
        return found ? finalDate : Calendar.getInstance().getTime();
    }
    
    /**
     * Trả về giá trị của headername.
     * @param message
     * @param headerName
     * @return
     * @throws Exception 
     */
    public static String getHeaderValue(Part message, String headerName)
            throws Exception {
        Enumeration headers = message.getAllHeaders();
        while (headers.hasMoreElements()) {
            Header header = (Header) headers.nextElement();
            if (headerName.equalsIgnoreCase(header.getName())) {
                return header.getValue();
            }
        }
        return null;
    }

    /**
     * Trả về uniqueid của mail trên mailserver.
     * @param message
     * @return
     * @throws Exception 
     */
    public static String getMailDataUniqueId(Part message) throws Exception {
        return getHeaderValue(message, "Message-ID");
    }

    /**
     * Hàm trả về thông tin người gửi đầy đủ.
     * @param address
     * @return Ex: Nguyen Dinh Dai<daind1@vnext.vn>
     */
    private static String getFullMailAndPersonal(InternetAddress address) {
        if (address == null) return "";
        String email = address.getAddress(); if(StringUtils.isEmpty(email)) return "";
        String personalName = address.getPersonal();
        if(StringUtils.isEmpty(personalName)) return email;
        return personalName + " <" + email + ">";
    }
    
    /**
     * Lấy địa chỉ mail người gửi.
     * @param message
     * @return
     * @throws Exception 
     */
    public static String getFrom(Message message) throws Exception {
        StringBuilder h = new StringBuilder();
        Address[] froms = message.getFrom();
        if (froms == null) return h.toString();
        for (Address from : froms) {
            h.append(getFullMailAndPersonal((InternetAddress) from)).append(",");
        }
        return StringUtil.killLastCharacter(h, ",");
    }

    /**
     * Lấy danh sách địa chỉ mail nhận.
     * @param message
     * @param recipientType
     * @return
     * @throws Exception 
     */
    public static String getRecipient(Message message, Message.RecipientType recipientType)
            throws Exception {
        StringBuilder h = new StringBuilder();
        Address[] recipients = message.getRecipients(recipientType);
        if (recipients == null) return h.toString();
        for (Address recipient : recipients) {
            h.append(getFullMailAndPersonal((InternetAddress) recipient)).append(",");
        }
        return StringUtil.killLastCharacter(h, ",");
    }

    /**
     * Lấy tên người gửi.
     * @param from
     * @return 
     */
    public static String getFromName(String from) {
        if (from == null || from.isEmpty()) return "";
        String[] x = from.split("@")[0].split("<");
        return x[0];
    }

    /**
     * Lấy toàn bộ header của 1 mail.
     * @param message
     * @return
     * @throws Exception 
     */
    public static String getHeader(Part message) throws Exception {
        if(message == null) return null;
        Enumeration<?> headers = message.getAllHeaders();
        if(headers == null) return null;
        
        StringBuilder h = new StringBuilder();
        while (headers.hasMoreElements()) {
            Header header = (Header) headers.nextElement();
            h.append(header.getName()).append(":").append(header.getValue()).append(",");
        }
        return StringUtil.killLastCharacter(h, ",");
    }
    
    /**
     * 
     * @param allHeaders
     * @return 
     */
    public static List<Header> getHeader(Enumeration<?> allHeaders) {
        List<Header> result = new LinkedList<>();
        while (allHeaders.hasMoreElements()) {
            javax.mail.Header h = (javax.mail.Header) allHeaders.nextElement();
            result.add(new Header(h.getName(), h.getValue()));
        }
        return result;
    }
    
    /**
     * 
     * @param header
     * @return 
     */
    public static String headerStripper(String header) {
        if(StringUtil.isEmpty(header)) return header;
        String s = header.trim();
        if (s.matches("^<.*>$")) return header.substring(1, header.length() - 1);
        return header;
    }

    /**
     * Hàm xử lí xóa mail trên mailserver.
     * @param message
     * @throws Exception 
     */
    public static void deleteMessage(Message message) throws Exception {
        String subject = message.getSubject();
        message.setFlag(Flags.Flag.DELETED, true);
        Console.log("Marked DELETE for message: " + subject);
    }
    
    /**
     * Tập tin EML là định dạng được phát triển bởi Microsoft dành cho Outlook và Outlook Express.
     * EML là một định dạng tập tin rộng lưu trữ tất cả các dữ liệu liên quan đến e-mail như nội dung của thư điện tử,
     * siêu dữ liệu, tập tin đính kèm... Hầu hết các ứng dụng thư điện tử đều hỗ trợ đọc tập tin EML
     * @param path
     * @param msg
     * @return
     * @throws Exception 
     */
    public static String writeToEML(String path, Message msg) throws Exception {
        OutputStream out = null;
        try {
            File eml = File.createTempFile("mailservice-output-" + System.currentTimeMillis(), ".eml");
            out = new FileOutputStream(eml);
            msg.writeTo(out);
            return eml.getAbsolutePath();
        } catch (Exception e) {
            // Just not implement. :(
        } finally {
            try { IOUtils.closeQuietly(out); } catch (Exception e) { }
        }
        return null;
    }
}
