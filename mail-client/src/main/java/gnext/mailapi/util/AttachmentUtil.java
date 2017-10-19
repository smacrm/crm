/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util;

import gnext.dbutils.model.Attachment;
import gnext.mailapi.mail.MailBody;
import static gnext.mailapi.util.InterfaceUtil.ContentType.MULTIPART;
import static gnext.mailapi.util.InterfaceUtil.ContentType.TEXT_HTML;
import static gnext.mailapi.util.InterfaceUtil.ContentType.TEXT_PLAIN;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import javax.mail.Multipart;
import javax.mail.Part;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author daind
 */
public class AttachmentUtil {

    /***
     * Hàm xử lí trả về {@link Attachment}.
     * @param name
     * @param path
     * @return 
     */
    public static Attachment create(String name, String path) {
        Attachment mailAttachment = new Attachment();
        mailAttachment.setAttachment_name(name);
        mailAttachment.setAttachment_deleted((short) 0);
        mailAttachment.setAttachment_extension(FilenameUtils.getExtension(name));
        mailAttachment.setAttachment_path(path);
        mailAttachment.setCreated_time(Calendar.getInstance().getTime());
        return mailAttachment;
    }
    
    /***
     * Hàm xử lí đọc nội dung Mail khi mail có chứa file attachment.
     * @param p
     * @param body
     * @throws Exception 
     */
    public static void readPart(Part p, MailBody body) throws Exception {
        if (p.isMimeType(TEXT_PLAIN) || p.isMimeType(TEXT_HTML)) {
            parsePrimaryContent(p, body);
        } else if (p.isMimeType(MULTIPART)) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) readPart(mp.getBodyPart(i), body);
        } else if (p.isMimeType("message/rfc822")) {
            readPart((Part) p.getContent(), body);
        } else if (p.isMimeType("image/jpeg")) {
            InputStream x = (InputStream) p.getContent();
            byte[] bArray = new byte[x.available()];
            while (true) {
                int result = (int) x.read(bArray);
                if (result == -1) break;
            }
            OutputStream os = new FileOutputStream("/tmp/image.jpg");
            os.write(bArray);
        } else {
            // Console.log("This is an unknown type: " + p.getContentType());
        }
    }
    
    /**
     * Hàm xử lí đọc nội dung mail, sẽ lấy nội dung chính tương tự goole.
     * @param part
     * @param body
     * @throws Exception 
     */
    public static void parsePrimaryContent(Part part, MailBody body) throws Exception {
        String ct = part.getContentType().toLowerCase();
        if(ct.contains(TEXT_HTML)) {
            String html = String.valueOf(part.getContent().toString());
            body.setHtml(html);
        } else if(ct.contains(TEXT_PLAIN)) {
            body.setText(part.getContent().toString());
        }
    }
}
