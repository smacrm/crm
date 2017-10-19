/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.mail.upload;

import gnext.dbutils.model.Attachment;
import gnext.dbutils.model.MailData;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 *
 * @author daind
 */
public class RemoteUpload implements UploadBase {

    private String getFolderOnServer(MailData mailData) {
        if(mailData.getCompany_id() != null && mailData.getCompany_id() > 0) {
            return "/" + mailData.getCompany_id() + "/mail/" + mailData.getMail_data_id() + File.separator;
        }
        return "/mail/" + mailData.getMail_data_id() + File.separator;
    }
    
    @Override
    public void upload(Map<String, Object> data) throws Exception {
        List<Attachment> mats = (List<Attachment>) data.get("mats");
        
        Integer serverId = (int) data.get("serverId");
        MailData mailData = (MailData) data.get("mailData");
        String pathToConf = (String) data.get("pathToConf");
        Integer companyId = (int) data.get("companyId");
        
        for(Attachment attachment : mats) {
            Parameter param_ftp = Parameter.getInstance(TransferType.FTP).manualconfig(false).storeDb(false)
                        .serverid(serverId)
                        .companyid(companyId)
                        .uploadfilename(attachment.getAttachment_name())
                        .appendFolder(getFolderOnServer(mailData))
                        .createfolderifnotexists()
                        .attachmentTargetType(AttachmentTargetType.MAIL)
                        .attachmentTargetId(mailData.getMail_data_id())
                        .conf(pathToConf);
            InputStream in = null;
            if(attachment.getPart() != null) in = attachment.getPart().getInputStream();
            if(attachment.getInputStream() != null) in = attachment.getInputStream();
            if(in != null) FileTransferFactory.getTransfer(param_ftp).upload(in);

            Attachment ftpAttachment = param_ftp.getAttachment();
            if(ftpAttachment == null) return;
            attachment.setAttachment_name(ftpAttachment.getAttachment_name());
            attachment.setAttachment_hash_name(ftpAttachment.getAttachment_hash_name());
            attachment.setAttachment_extension(ftpAttachment.getAttachment_extension());
            attachment.setAttachment_mime_type(ftpAttachment.getAttachment_mime_type());
            attachment.setAttachment_path(ftpAttachment.getAttachment_path());
            attachment.setAttachment_file_size(ftpAttachment.getAttachment_file_size());
        }
    }
    
}
