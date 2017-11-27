/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.util.FileUtil;
import gnext.filetransfer.DeleteParameter;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.model.MailMode;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.utils.StringUtil;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author HUONG
 */
public class WebFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(WebFileUtil.class);
    
    public static String OCTET_STREAM = "application/octet-stream";
    public static String BASE_FOLDER_LOG_PATH = "/mnt/logs/batch/";

    public static boolean isFile(String path) {
       File file = new File(path);
       return file.isFile();
    }

    /**
     * FTPサーバへファイルをアップロード
     * @param targetType
     * @param targetId
     * @param folder
     * @param attachs
     * @param conf
     * @param serverId 
     */
    public static void uploadToFtp(AttachmentTargetType targetType, Integer targetId, String folder, List<UploadedFileExt> attachs, String conf, Integer serverId, Integer companyId) {
        if(targetType == null
                | targetId == null
                || StringUtils.isBlank(folder)
                || attachs == null || attachs.size() <= 0
                || serverId == null) return;
        if(!isFile(conf)) {
            logger.error("[FileUtil.uploadToFtp()]", conf + " is not exists !!!");
        }
        try {
            for(UploadedFileExt ext:attachs) {
                if(ext == null || ext.getFile().getFileName() == null || ext.getFile().getInputstream() == null) continue;
                Parameter ftp = Parameter.getInstance(TransferType.FTP
                    ).manualconfig(false
                    ).storeDb(true
                    ).companyid(companyId        
                    ).serverid(serverId
                    ).conf(conf
                    ).uploadfilename(ext.getFile().getFileName()
                    ).appendFolder(folder
                    ).createfolderifnotexists(
                    ).attachmentTargetType(targetType
                    ).attachmentTargetId(targetId);
                FileTransferFactory.getTransfer(ftp).upload(ext.getFile().getInputstream());
                ext.setOldId(ftp.getAttachmentTargetId());
            }
        } catch (Exception ex) {
            logger.error("[FileUtil.uploadToFtp()]", ex);
        }
    }

    /**
     * FTPサーバからファイルをダウンロード
     * @param attach：オブジェクト「DBから取得」
     * @return 
     */
    public static DefaultStreamedContent downloadFromFtpDefaultStreamedContent(Attachment attach){
        Server server = attach.getServer();
        if(server == null) return null;
        DefaultStreamedContent download = null;
        try {
            String host = server.getServerHost();
            int port = server.getServerPort();
            String username = server.getServerUsername();
            String password = server.getDecryptServerPassword();
            boolean security = StatusUtil.getBoolean(server.getServerSsl());
            String protocol = server.getServerProtocol();
            String servertype = server.getServerType();
            TransferType tt = TransferType.getTransferType(servertype);
            Parameter param_ftp = Parameter.getInstance(tt).manualconfig(true).storeDb(false);;
            param_ftp.host(host).port(port).username(username).password(password).security(security).protocol(protocol);

            String path = attach.getAttachmentPath() + File.separator + attach.getAttachmentName();
            InputStream input = FileTransferFactory.getTransfer(param_ftp).download(path);

            String fn = attach.getAttachmentName();
            fn = StringUtil.getDownloadFileName(fn);
            FacesContext fc = FacesContext.getCurrentInstance();
            if(fc == null) return null;
            ExternalContext ec = fc.getExternalContext();
            download = new DefaultStreamedContent(input, ec.getMimeType(fn), URLEncoder.encode(fn, "UTF-8"));
        } catch (Exception ex) {
            logger.error("[FileUtil.downloadFromFtp()]", ex);
            return null;
        }
        return download;
    }

    public static void downloadFromFtp(Attachment attach){
        Server server = attach.getServer();
        if(server == null) return;
        try {
            String host = server.getServerHost();
            int port = server.getServerPort();
            String username = server.getServerUsername();
            String password = server.getDecryptServerPassword();
            boolean security = StatusUtil.getBoolean(server.getServerSsl());
            String protocol = server.getServerProtocol();
            String servertype = server.getServerType();
            TransferType tt = TransferType.getTransferType(servertype);
            Parameter param_ftp = Parameter.getInstance(tt).manualconfig(true).storeDb(false);;
            param_ftp.host(host).port(port).username(username).password(password).security(security).protocol(protocol);

            String path = attach.getAttachmentPath() + File.separator + attach.getAttachmentName();
            InputStream input = FileTransferFactory.getTransfer(param_ftp).download(path);

            download(input, attach.getAttachmentName());
        } catch (Exception ex) {
            logger.error("[FileUtil.downloadFromFtp()]", ex);
        }
    }

    /**
     * InputStreamやファイル名でクライアントへRequestを送る
     * @param in：InputStream
     * @param name：ファイル名
     */
    public static void download(InputStream in, String name) {
        if(in == null || StringUtils.isBlank(name)) return;
        FacesContext fc = FacesContext.getCurrentInstance();
        if(fc == null) return;
        ExternalContext ec = fc.getExternalContext();
        ec.responseReset();
        ec.setResponseContentType("application/octet-stream; charset=UTF-8");
        ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + StringUtil.getDownloadFileName(name) + "\"");

        HttpServletResponse res = (HttpServletResponse) fc.getExternalContext().getResponse();
        OutputStream out = null;
        try {
            out = res.getOutputStream();
            int length = in.available();
            ec.setResponseContentLength(length);

            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            out.flush();
        } catch (IOException ex) {
            logger.error("[FileUtil.download()]", ex);
        } finally {
            try {
                out.close();
                if(in != null) in.close();
            } catch (IOException ex) {
                logger.error("[FileUtil.download().out.close()]", ex);
            }
            fc.responseComplete();
            fc.renderResponse();
        }
    }

    @SuppressWarnings("UnusedAssignment")
    public static List<UploadedFileExt> removeOldFile(List<UploadedFileExt> list) {
        if(list == null || list.size() <= 0) return new ArrayList<>();
        List<UploadedFileExt> relist = new ArrayList<>();
        for(UploadedFileExt ext:list) {
            if(ext == null || (ext != null && ext.getOldId() != null)) continue;
            relist.add(ext);
        }
        return relist;
    }

    public static void uploadFile(Integer escId, List<UploadedFileExt> attachs, AttachmentTargetType type, String targetType, Integer serverId, Integer comId) {
        if(escId == null || escId <= 0 || attachs == null || type == null || StringUtils.isBlank(targetType) || serverId == null || comId == null) return;
        if(serverId != null) {
            String folder = WebFileUtil.path(type, targetType, comId, String.valueOf(escId));
            WebFileUtil.uploadToFtp(type, escId, folder, attachs, StringUtil.DEFAULT_DB_PROPERTIES, serverId, comId);
        } else {
            JsfUtil.getResource().alertMsgInfo("label.ftp_server", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.not.exist", ResourceUtil.BUNDLE_ISSUE_NAME);
        }
    }
    
    public static void deleteFileFromFtp(String path, Server server){
        try{
            TransferType tt = TransferType.getTransferType(server.getServerType());
            DeleteParameter delParam = DeleteParameter.getInstance(tt);
            delParam
                    .host(server.getServerHost())
                    .port(server.getServerPort())
                    .username(server.getServerUsername())
                    .password(server.getDecryptServerPassword())
                    .security(StatusUtil.getBoolean(server.getServerSsl()))
                    .protocol(server.getServerProtocol());
            delParam.deletePath(path).folder(false);        
            FileTransferFactory.getTransfer(delParam).delete();
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }
    }

    public static String path(AttachmentTargetType type, String targetType, Integer comId, String escId) {
        if(type == null || StringUtils.isBlank(targetType) || comId == null || !NumberUtils.isDigits(escId)) return null;
        return String.format("%s%s%s%s%s%s%s%s", comId, File.separator, type.getName(), File.separator, targetType, File.separator, escId, File.separator);
    }

    public static void loadFileFromFtp(MailMode mail, List<Attachment> attachs, Server server) {
        if(mail == null || attachs == null || server == null || server.getServerId() == null) return;
        TransferType tt = TransferType.getTransferType(server.getServerType());
        Parameter param_ftp = Parameter.getInstance(tt).manualconfig(true).storeDb(false);;
        param_ftp
                .host(server.getServerHost())
                .port(server.getServerPort())
                .username(server.getServerUsername())
                .password(server.getDecryptServerPassword())
                .security(StatusUtil.getBoolean(server.getServerSsl()))
                .protocol(server.getServerProtocol());
        for(Attachment at:attachs) {
            if(at == null || at.getAttachmentId() == null) continue;
            String path = at.getAttachmentPath() + File.separator + at.getAttachmentName();
            InputStream input;
            try {
                input = FileTransferFactory.getTransfer(param_ftp).download(path);
                byte [] data = FileUtil.copyFromInputStream(input);
                UploadedFile oldFile = new ByteArrayUploadedFile(data, at.getAttachmentName(), WebFileUtil.OCTET_STREAM);
                UploadedFileExt ext = new UploadedFileExt();
                //System.err.println(oldFile.getSize());
                ext.file = oldFile;
                ext.setOldId(at.getAttachmentId());
                ext.setSizeView(ByteUtil.getSizeToString(ext.file.getSize()));
                mail.getAttachs().add(ext);
            } catch (Exception ex) {
                logger.error("WebFileUtil.loadFileFromFtp()", ex);
                break;
            }
        }
    }

    public static void forceDownload(String fileName, Workbook workbook) throws IOException {
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);

        ec.responseReset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
        ec.setResponseContentType("application/vnd.ms-excel"); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
        ec.setResponseContentLength(byteArrayOutputStream.size()); // Set it with the file size. This header is optional. It will work if it's omitted, but the download progress will be unknown.
        ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\""); // The Save As popup magic is done here. You can give it any file name you want, this only won't work in MSIE, it will use current request URL as file name instead.
        
        workbook.write(ec.getResponseOutputStream());

        fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.
    }
    
    public static void forceDownload(String fileName, byte[] data) throws IOException{
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
        }
        
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        
        ec.responseReset();
        ec.setResponseContentType("application/vnd.ms-excel");
        ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        
        final OutputStream os = ec.getResponseOutputStream();
        
        IOUtils.write(data, os);
        
        ec.setResponseContentLength(data.length);
        fc.responseComplete();
    }
    
    /**
     * リストの中に同じファイルを存在するかチェック
     * @param list：ファイルリスト
     * @param file：チェックファイル
     * @return「true、false」
     */
    public static boolean fileIsExists(List<UploadedFileExt> list, UploadedFile file) {
        if(list == null || list.size() <= 0 || file == null) return false;
        for(UploadedFileExt uF:list) {
            if(!uF.getFile().getFileName().equals(file.getFileName()) || uF.getFile().getSize() != file.getSize()) continue;
            return true;
        }
        return false;
    }
}
