/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.services;

import gnext.dbutils.model.Attachment;
import gnext.dbutils.util.SqlUtil;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public class AttachmentConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentConnection.class);
    private final Connection connection;
    public AttachmentConnection(Connection connection) { this.connection = connection; }
    
    /**
     * Hàm xử lí lưu attachment.
     * @param attachment
     * @throws Exception 
     */
    public void save(final Attachment attachment) throws Exception {
        java.sql.Connection conn = null;
        try {
            conn = connection.getConnection();
            this.create(attachment, conn);
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally { DbUtils.closeQuietly(conn); }
    }
    
    /**
     * Hàm xử lí lưu Attachment cùng java.sql.Connection làm tham số.
     * Tăng tính mềm dẻo cho hàm, người dùng có thể sử dụng transaction.
     * 
     * @param mailAttachment
     * @param conn
     * @return
     * @throws Exception 
     */
    public Attachment create(final Attachment mailAttachment, final java.sql.Connection conn) throws Exception {
        Long pk = SqlUtil.insert(conn, Attachment.class, mailAttachment);
        mailAttachment.setAttachment_id(pk.intValue());
        return mailAttachment;
    }
}
