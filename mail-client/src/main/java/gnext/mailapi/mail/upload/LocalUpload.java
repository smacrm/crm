/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.mail.upload;

import com.mysql.jdbc.StringUtils;
import gnext.dbutils.model.Attachment;
import gnext.dbutils.util.FileUtil;
import gnext.mailapi.util.InterfaceUtil;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author daind
 */
public class LocalUpload implements UploadBase {
    @Override
    public void upload(Map<String, Object> data) throws Exception {
        List<Attachment> mats = (List<Attachment>) data.get("mats");
        for(Attachment attachment : mats) {
            attachment.setAttachment_path(InterfaceUtil.Config.PATH_LOCAL_FILE_ATTACHMENT);
            InputStream in = attachment.getInputStream();
            if(attachment.getPart() != null) in = attachment.getPart().getInputStream();
            
            InputStream cloneIn = FileUtil.cloneStream(in);
            String fileChecksum = FileUtil.calChecksum(cloneIn, attachment.getAttachment_name());
            if(!StringUtils.isNullOrEmpty(fileChecksum)) attachment.setAttachment_hash_name(fileChecksum);
            attachment.setAttachment_extension(FilenameUtils.getExtension(attachment.getAttachment_name()));
            attachment.setAttachment_mime_type(FileUtil.getMimeType(cloneIn, attachment.getAttachment_name()));
            attachment.setAttachment_file_size(String.valueOf(FileUtil.calSize(cloneIn, attachment.getAttachment_name())));
            attachment.setAttachment_deleted((short) 0);
            FileUtils.copyInputStreamToFile(cloneIn, new File(InterfaceUtil.Config.PATH_LOCAL_FILE_ATTACHMENT + attachment.getAttachment_name()));
        }
    }
}
