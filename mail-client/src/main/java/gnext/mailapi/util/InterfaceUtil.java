/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util;

/**
 *
 * @author daind
 */
public class InterfaceUtil {
    public interface Folder {

        String INBOX = "INBOX";
        String SENT = "SENT";
        String DRAFT = "DRAFT";
        String JUNK = "JUNK";
        String TRASH = "TRASH";
    }

    public interface Type {

        String SMTP = "SMTP";
        String IMAP = "IMAP";
        String POP3 = "POP3";
    }

    public interface Flag {

        String ALL = "ALL";
        String RECENT = "RECENT";
        String SEEN = "SEEN";
        String ANSWERED = "ANSWERED";
        String DELETED = "DELETED";
        String DRAFT = "DRAFT";
        String UNREAD = "UNREAD";
    }

    public interface Priority {

        String HEIGHEST = "1";
        String HEIGHT = "2";
        String NORMAL = "3";
        String LOW = "4";
        String LOWEST = "5";
    }

    public interface ContentType {

        String TEXT_HTML_UTF_8 = "text/html;charset=utf-8";
        String TEXT_HTML = "text/html";
        
        String TEXT_PLAIN_UTF_8 = "text/plain;charset=utf-8";
        String TEXT_PLAIN = "text/plain";
        
        String MULTIPART_UTF_8 = "multipart/*;charset=utf-8";
        String MULTIPART = "multipart/*";
    }
    
    public interface Config {
        String PATH_LOCAL_FILE_ATTACHMENT = "/mnt/email/attachment/read/";
    }
}
