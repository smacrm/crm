/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer.ftp;

import gnext.dbutils.model.Attachment;
import gnext.dbutils.model.Server;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.services.AttachmentConnection;
import gnext.dbutils.services.Connection;
import gnext.dbutils.services.ServerConnection;
import gnext.dbutils.util.FileUtil;
import gnext.filetransfer.BaseFileTransfer;
import gnext.filetransfer.DeleteParameter;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.exceptions.FileTransferDownloadException;
import gnext.filetransfer.exceptions.FileTransferUploadException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 * What are the settings to correct vsftpd “500 OOPS: cannot change directory” error?
 *      Run this one command, no need to restart any service & server:
 *          # setenforce 0
 *      edit the file /etc/sysconfig/selinux to include
 *          SELINUX=disabled
 * 
 * Transfer Speed:
 *      No added meta-data in the sent files, just the raw binary
 *      Never chunked encoding "overhead"
 */
public class FtpFileTransfer extends BaseFileTransfer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FtpFileTransfer.class);
    public FtpFileTransfer(Parameter param) { super(param); }
    private Server server = null;
    
    private static final Integer FTP_CONNECT_TIMEOUT = 2 * 1000; // 2 seconds
    private static final String UTF_8 = "UTF-8";
    
    //*****************************************************************************************************************
    //******************************************** PRIVATE METHODS API*************************************************
    //*****************************************************************************************************************    
    /**
     * Hàm xử lí lấy thông tin kết nối FTP server từ DB.
     * @throws FileTransferUploadException 
     */
    private void lookupServer() throws Exception {
        Parameter param = getParam();
        findOneServer(param.getServerid(), param.getConf(), param.getCompanyid());
        if(this.server == null) return;
        
        param.host(server.getServer_host()).username(server.getServer_username()).password(this.server.getDecryptServerPassword());
        if(this.server.getServer_ssl() != null && this.server.getServer_ssl() == 1) param.usesecurity().protocol(this.server.getServer_protocol());
        if(this.server.getServer_port() != null) param.port(this.server.getServer_port());
    }
    
    /***
     * Hàm xử lí tìm kiếm server trong dabtabse.
     * @param serverId
     * @param stringconnection
     * @return
     * @throws FileTransferUploadException 
     */
    private Server findOneServer(Integer serverId, String stringconnection, Integer companyId) throws Exception {
        Parameter param = getParam();
        
        // trường hợp người dùng manual cấu hình FTP và không lưu dữ liệu vào db thì bỏ qua.
        if(param.isManualconfig() && !param.isStoreDb()) return null;
        
        // 
        if(server != null) return server;
        if(serverId == null) throw new FileTransferUploadException("Please provider the actual server id.");
        if(companyId == null) throw new FileTransferUploadException("Please provider the actual company id.");
        if(StringUtils.isEmpty(stringconnection)) throw new FileTransferUploadException("Please provider the configuration file.");
        
        Connection connection = new Connection(stringconnection, companyId);
        ServerConnection serverConnection = new ServerConnection(connection);
        List<Server> servers = serverConnection.findServerById(serverId);
        this.server = lookupFromList(servers);
        if(this.server == null) this.server = findOneServerGnext(connection);
        if(this.server == null) throw new FileTransferUploadException("Can not retrieve server infos.");
        
        return server;
    }
    
    private Server findOneServerGnext(Connection connection) throws Exception {
        if(connection == null) throw new FileTransferUploadException("connection must not empty.");
        ServerConnection serverConnection = new ServerConnection(connection);
        List<Server> servers = serverConnection.findServerGnext();
        return lookupFromList(servers);
    }
    
    private Server lookupFromList(List<Server> servers) {
        if(servers == null || servers.isEmpty()) return null;
        for(Server _server : servers) {
            if(_server.getServer_deleted() == null || _server.getServer_deleted() == 0) return _server;
        }
        return null;
    }
    
    /**
     * Lưu dữ liệu vào DB.
     * @param in
     * @throws FileTransferUploadException 
     */
    private void _saveattachment(InputStream in) throws Exception {
        try {
            Parameter p = getParam();
            findOneServer(p.getServerid(), p.getConf(), p.getCompanyid());
            
            Attachment attachment = new Attachment();
            attachment.setAttachment_name(p.getUploadfilename());
            String fileChecksum = FileUtil.calChecksum(in, p.getUploadfilename());
            if(!StringUtils.isEmpty(fileChecksum)) attachment.setAttachment_hash_name(fileChecksum);
            attachment.setAttachment_extension(FilenameUtils.getExtension(p.getUploadfilename()));
            attachment.setAttachment_mime_type(FileUtil.getMimeType(in, p.getUploadfilename()));
            
            String pathFile = getActualFolder(p.getUploadpath());
            if(this.server != null) pathFile = getActualFolder(server.getServer_folder());
            pathFile = FileUtil.smoothingPath(pathFile);
            
            attachment.setAttachment_path(pathFile);
            attachment.setAttachment_file_size(String.valueOf(FileUtil.calSize(in, p.getUploadfilename())));
            attachment.setAttachment_deleted((short) 0);
            
            if(p.isStoreDb()) {
                AttachmentTargetType attachmentTargetType = p.getAttachmentTargetType();
                attachment.setAttachment_target_type(attachmentTargetType.getId());
                attachment.setAttachment_target_id(p.getAttachmentTargetId());
            }
            
            if(server != null) {
                attachment.setServer_id(this.server.getServer_id());
                attachment.setCompany_id(this.server.getCompany_id());
                attachment.setCreator_id(this.server.getCreator_id());
            }
            attachment.setCreated_time(Calendar.getInstance().getTime());
            
            if(p.isStoreDb()) new AttachmentConnection(getConn(getParam().getConf(), getParam().getCompanyid())).save(attachment);
            p.setAttachment(attachment);
        } catch (Exception e) {
            throw new FileTransferUploadException(e);
        } finally {
            try { in.reset(); } catch (Exception e) { }
        }
    }
    
    /**
     * Tạo mới Folder trên FTP server.
     * 
     * @param client
     * @param dirTree
     * @return
     * @throws FileTransferUploadException 
     */
    private String _createfolder(final FTPClient client, final String dirTree) throws Exception {
        try {
            String actualDir = dirTree;
            if (!client.changeWorkingDirectory("/"))
                throw new FileTransferUploadException("Unable to change into newly created remote directory '/'.  error='" + client.getReplyString() + "'");
            
            boolean dirExists = true;
            String[] directories = actualDir.split("/");
            for (String dir : directories) {
                if (!dir.isEmpty()) {
                    if (dirExists) dirExists = client.changeWorkingDirectory(dir);
                    if (!dirExists) {
                        if (!client.makeDirectory(dir))
                            throw new FileTransferUploadException("Unable to create remote directory '" + dir + "'.  error='" + client.getReplyString() + "'");
                        if (!client.changeWorkingDirectory(dir))
                            throw new FileTransferUploadException("Unable to change into newly created remote directory '" + dir + "'.  error='" + client.getReplyString() + "'");
                    }
                }
            }
            return actualDir;
        } catch (Exception e) {
            throw new FileTransferUploadException(e);
        }
    }
    
    private String getActualFolder(final String dirTree) {
        if(dirTree == null) return null;
        String actualDir = dirTree;
        if(!StringUtils.isEmpty(getParam().getAppendFolder())) actualDir = actualDir + File.separator + getParam().getAppendFolder() + File.separator;
        return actualDir;
    }
    
    private String getDirectory(FTPClient ftpClient) throws Exception {
        String pathFile = getActualFolder(getParam().getUploadpath());
        findOneServer(getParam().getServerid(), getParam().getConf(), getParam().getCompanyid());
        
        if(this.server != null) pathFile = getActualFolder(server.getServer_folder());
        
        String homeFolder = ftpClient.printWorkingDirectory();
        if(!pathFile.startsWith(homeFolder)) pathFile = homeFolder + pathFile;
        
        return pathFile;
    }
    
    private String getPath(FTPClient ftp) throws Exception {
        String pathFile = getDirectory(ftp);
        if(StringUtils.isEmpty(pathFile)) throw new FileTransferUploadException("The path to server is not set."); 
        if(getParam().isCreatefolder()) pathFile = _createfolder(ftp, pathFile);
        
        pathFile = pathFile + File.separator + getParam().getUploadfilename();
        if(StringUtils.isEmpty(pathFile)) throw new FileTransferUploadException("The path to server is not set."); 
        return pathFile;
    }
    
    private void getActualFileName(FTPClient ftp) throws Exception {
        String dir = getDirectory(ftp);
        String fn = getParam().getUploadfilename();
        FTPFile[] files = ftp.listFiles(dir);
        boolean check = false;
        for(FTPFile file : files) {
            if(file.isDirectory()) continue;
            if(fn.contains(file.getName())) {
                check = true; break;
            }
        }
        if(check) {
            fn = "__CRMCLOUD_REPLACE_" + System.currentTimeMillis() + "__" + fn;
            getParam().uploadfilename(fn);
        }
    }

    //*****************************************************************************************************************
    //********************************************** UPLOAD API *******************************************************
    //*****************************************************************************************************************    
    @Override
    protected void checkUploadparam() throws FileTransferUploadException {
        Parameter param = getParam();
        try {
            lookupServer();
        } catch (Exception e) {
            throw new FileTransferUploadException(e);
        }
        
        String field = null;
        if(StringUtils.isEmpty(param.getHost()))                field = "Host";
        else if(StringUtils.isEmpty(param.getUsername()))       field = "Username";
        else if(StringUtils.isEmpty(param.getPassword()))       field = "Password";
        else if(StringUtils.isEmpty(param.getUploadfilename())) field = "Uploadfilename";
        
        /** Nếu yêu cầu lưu file vào database thì yêu cầu cung cấp conf.properties và attachemtn target cùng serverid. */
        if(param.isStoreDb()) {
            if(StringUtils.isEmpty(param.getConf()))           field = "conf";
            else if(param.getAttachmentTargetType() == null)   field = "attachmentTargetType";
            else if(param.getAttachmentTargetId() == null)     field = "attachmentTargetId";
            else if(param.getServerid() == null)               field = "serverid";
        }
        
        /** Nếu không vượt qua được check tham số đầu vào thì throw exception tới người dùng. */
        if(!StringUtils.isEmpty(field)) {
            String m = MessageFormat.format("The param {0} is incorrect.", field);
            throw new FileTransferUploadException(m);
        }
    }
    
    @Override
    protected void executeUpload(InputStream in) throws FileTransferUploadException {
        try {
            if(in == null) throw new FileTransferUploadException("The data(inputstream) is null.");
            if(getParam().isSecurity()) exeUploadSec(in); else exeUploadNor(in);
            _saveattachment(in);
        } catch (Exception e) {
            throw new FileTransferUploadException(e);
        }
    }
    
    private void exeUploadNor(InputStream in) throws FileTransferUploadException {
        FTPClient ftp = new FTPClient();
        try {
            ftp.setControlEncoding(UTF_8);
            ftp.setConnectTimeout(FTP_CONNECT_TIMEOUT);
            ftp.connect(getParam().getHost(), getParam().getPort());
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode()))
                throw new FileTransferUploadException("FTP server refused connection.");
            if(!ftp.login(getParam().getUsername(), getParam().getPassword()))
                throw new FileTransferUploadException("FTP server refused connection.");
            
            ftp.setAutodetectUTF8( true );
            
            ftp.setFileType(FTP.BINARY_FILE_TYPE); // for inputstream.
            // Enter local passive mode
            ftp.enterLocalPassiveMode();
            
            getActualFileName(ftp);
            String pathFile = getPath(ftp);
            boolean done = ftp.storeFile(pathFile, in);
            if(!done) throw new FileTransferUploadException();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new FileTransferUploadException(e);
        } finally {
            try { in.reset(); } catch (Exception e) { } // chuyển con trỏ về đầu stream.
            try { ftp.logout(); ftp.disconnect(); } catch (Exception e) { }
        }
    }
    
    private void exeUploadSec(InputStream in) throws FileTransferUploadException {
        FTPSClient ftps = new FTPSClient(getParam().getProtocol(), false); // using FTP over SSL (Explicit)
        try {
            ftps.setControlEncoding(UTF_8);
            ftps.setConnectTimeout(FTP_CONNECT_TIMEOUT);
            ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
            ftps.connect(getParam().getHost(), getParam().getPort());
            if (!FTPReply.isPositiveCompletion(ftps.getReplyCode()))
                throw new FileTransferUploadException("FTP server refused connection.");
            if(!ftps.login(getParam().getUsername(), getParam().getPassword()))
                throw new FileTransferUploadException("FTP server refused connection.");
            
            ftps.setAutodetectUTF8( true );
            
            ftps.setFileType(FTP.BINARY_FILE_TYPE); // for inputstream.
            // Set protection buffer size
            ftps.execPBSZ(0);
            // Set data channel protection to private
            ftps.execPROT("P");
            // Enter local passive mode
            ftps.enterLocalPassiveMode();
            
            getActualFileName(ftps);
            String pathFile = getPath(ftps);
            boolean done = ftps.storeFile(pathFile, in);
            if(!done) throw new FileTransferUploadException("Can not upload file to server.");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new FileTransferUploadException(e);
        } finally {
            try { in.reset(); } catch (Exception e) { } // chuyển con trỏ về đầu stream.
            try { ftps.logout(); ftps.disconnect(); } catch (Exception e) { }
        }
    }
    
    //*****************************************************************************************************************
    //******************************************** DOWNLOAD API *******************************************************
    //*****************************************************************************************************************    
    @Override
    protected void checkDownloadparam() throws FileTransferDownloadException {
        Parameter param = getParam();
        try { lookupServer(); } catch (Exception e) { throw new FileTransferDownloadException(e); }
        
        String field = null;
        if(StringUtils.isEmpty(param.getHost()))                field = "Host";
        else if(StringUtils.isEmpty(param.getUsername()))       field = "Username";
        else if(StringUtils.isEmpty(param.getPassword()))       field = "Password";
        
        if(!StringUtils.isEmpty(field)) {
            String m = MessageFormat.format("The param '{0}' is incorrect.", field);
            throw new FileTransferDownloadException(m);
        }
    }

    @Override
    protected InputStream executeDowload(String pathToFile) throws FileTransferDownloadException {
        if(getParam().isSecurity()) {
            return exeDownloadInputStreamSec(pathToFile);
        }
        return exeDownloadInputStreamNor(pathToFile);
    }
    
    private InputStream exeDownloadInputStreamSec(String pathToFile) throws FileTransferDownloadException {
        FTPSClient ftpClient = new FTPSClient(getParam().getProtocol(), false);
        // {@link http://stackoverflow.com/questions/9535925/filename-encoding}
        try {
            ftpClient.setControlEncoding(UTF_8);
            ftpClient.setConnectTimeout(FTP_CONNECT_TIMEOUT);
            ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
            ftpClient.connect(getParam().getHost(), getParam().getPort());
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode()))
                throw new FileTransferUploadException("FTP server refused connection.");
            if(!ftpClient.login(getParam().getUsername(), getParam().getPassword()))
                throw new FileTransferUploadException("FTP server refused connection.");
            
            // Set protection buffer size
            ftpClient.execPBSZ(0);
            // Set data channel protection to private
            ftpClient.execPROT("P");
//            ftpClient.cwd("/home/crmcloud/upload/mail");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // for inputstream.
            // Enter local passive mode
            ftpClient.enterLocalPassiveMode(); // important!
            
            String pathFile = pathToFile;
            String homeFolder = ftpClient.printWorkingDirectory();
            if(!pathFile.startsWith(homeFolder)) pathFile = homeFolder + pathFile;
            
            return ftpClient.retrieveFileStream(pathFile);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw new FileTransferDownloadException(e);
        } finally {
//            try {
//                if (ftpClient.isConnected()) {
//                    ftpClient.logout();
//                    ftpClient.disconnect();
//                }
//            } catch (IOException ex) { }
        }
    }
    
    private InputStream exeDownloadInputStreamNor(String pathToFile) throws FileTransferDownloadException {
        FTPClient ftpClient = new FTPClient();
        // {@link http://stackoverflow.com/questions/9535925/filename-encoding}
        try {
            ftpClient.setControlEncoding(UTF_8);
            ftpClient.setConnectTimeout(FTP_CONNECT_TIMEOUT);
            ftpClient.connect(getParam().getHost(), getParam().getPort());
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode()))
                throw new FileTransferUploadException("FTP server refused connection.");
            if(!ftpClient.login(getParam().getUsername(), getParam().getPassword()))
                throw new FileTransferUploadException("FTP server refused connection.");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            String pathFile = pathToFile;
            String homeFolder = ftpClient.printWorkingDirectory();
            if(!pathFile.startsWith(homeFolder)) pathFile = homeFolder + pathFile;
        
            return ftpClient.retrieveFileStream(pathFile);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw new FileTransferDownloadException(e);
        } finally {
//            try {
//                if (ftpClient.isConnected()) {
//                    ftpClient.logout();
//                    ftpClient.disconnect();
//                }
//            } catch (IOException ex) { }
        }
    }
    
    //*****************************************************************************************************************
    //********************************************** DELETE API *******************************************************
    //*****************************************************************************************************************    
    @Override
    protected void checkDeleteparam() throws FileTransferUploadException {
        DeleteParameter delParam = (DeleteParameter) getParam();
        String field = null;
        if(StringUtils.isEmpty(delParam.getHost()))                field = "Host";
        else if(StringUtils.isEmpty(delParam.getUsername()))       field = "Username";
        else if(StringUtils.isEmpty(delParam.getPassword()))       field = "Password";
        else if(StringUtils.isEmpty(delParam.getDeletePath()))     field = "deletePath";
        if(StringUtils.isEmpty(delParam.getDeletePath()))
            throw new FileTransferUploadException(MessageFormat.format("The param {0} is incorrect.", field));
    }

    @Override
    protected void executeDelete() throws FileTransferUploadException {
        DeleteParameter delParam = (DeleteParameter) getParam();
        if(delParam.isSecurity()) exeDeleteSec(); else exeDeleteNor();
    }
    
    private void exeDeleteNor() throws FileTransferUploadException {
        DeleteParameter delParam = (DeleteParameter) getParam();
        FTPClient ftp = new FTPClient();
        try {
            ftp.setConnectTimeout(FTP_CONNECT_TIMEOUT);
            ftp.connect(delParam.getHost(), delParam.getPort());
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode()))
                throw new FileTransferUploadException("FTP server refused connection.");
            if(!ftp.login(delParam.getUsername(), delParam.getPassword()))
                throw new FileTransferUploadException("FTP server refused connection.");
            
            ftp.setFileType(FTP.BINARY_FILE_TYPE); // for inputstream.
            // Enter local passive mode
            ftp.enterLocalPassiveMode();
            
            String deletePath = delParam.getDeletePath();
            
            String pathFile = deletePath;
            String homeFolder = ftp.printWorkingDirectory();
            if(!pathFile.startsWith(homeFolder)) pathFile = homeFolder + pathFile;
            
            boolean re = false;
            if(delParam.isFolder())
                FtpUtil.removeDirectory(ftp, pathFile, "");
            else
                re = ftp.deleteFile(pathFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new FileTransferUploadException(e);
        } finally {
            try { ftp.logout(); ftp.disconnect(); } catch (Exception e) { }
//            try { in.close(); } catch (Exception e) { }
        }
    }
    
    private void exeDeleteSec() throws FileTransferUploadException {
        DeleteParameter delParam = (DeleteParameter) getParam();
        FTPSClient ftps = new FTPSClient(delParam.getProtocol(), false); // using FTP over SSL (Explicit)
        try {
            ftps.setConnectTimeout(FTP_CONNECT_TIMEOUT);
            ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
            ftps.connect(delParam.getHost(), delParam.getPort());
            if (!FTPReply.isPositiveCompletion(ftps.getReplyCode()))
                throw new FileTransferUploadException("FTP server refused connection.");
            if(!ftps.login(delParam.getUsername(), delParam.getPassword()))
                throw new FileTransferUploadException("FTP server refused connection.");

            ftps.setFileType(FTP.BINARY_FILE_TYPE); // for inputstream.
            // Set protection buffer size
            ftps.execPBSZ(0);
            // Set data channel protection to private
            ftps.execPROT("P");
            // Enter local passive mode
            ftps.enterLocalPassiveMode();
            
            String deletePath = delParam.getDeletePath();
            
            String pathFile = deletePath;
            String homeFolder = ftps.printWorkingDirectory();
            if(!pathFile.startsWith(homeFolder)) pathFile = homeFolder + pathFile;
            
            boolean re = false;
            if(delParam.isFolder())
                FtpUtil.removeDirectory(ftps, pathFile, "");
            else
                re = ftps.deleteFile(pathFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new FileTransferUploadException(e);
        } finally {
            try { ftps.logout(); ftps.disconnect(); } catch (Exception e) { }
//            try { in.close(); } catch (Exception e) { }
        }
    }
    
    //*****************************************************************************************************************
    //******************************************** TEST CONNECTION API ************************************************
    //*****************************************************************************************************************   
    @Override
    protected void checkTestConnectionParam() throws Exception {
        Parameter param = getParam();
        String field = null;
        if(StringUtils.isEmpty(param.getHost()))                field = "Host";
        else if(StringUtils.isEmpty(param.getUsername()))       field = "Username";
        else if(StringUtils.isEmpty(param.getPassword()))       field = "Password";
        if(!StringUtils.isEmpty(field)) {
            String m = MessageFormat.format("The param {0} is incorrect.", field);
            throw new FileTransferUploadException(m);
        }
    }
    
    @Override
    protected void testConnection() throws Exception {
        if(getParam().isSecurity()) exeTestSec(); else exeTestNor();
    }
    
    private void exeTestSec() throws Exception {
        FTPSClient ftpClient = new FTPSClient(getParam().getProtocol(), false);
        try {
            ftpClient.setControlEncoding(UTF_8);
            ftpClient.setConnectTimeout(FTP_CONNECT_TIMEOUT);
            ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
            ftpClient.connect(getParam().getHost(), getParam().getPort());
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode()))
                throw new Exception("FTP server refused connection.");
            if(!ftpClient.login(getParam().getUsername(), getParam().getPassword()))
                throw new Exception("FTP server refused connection.");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw new FileTransferDownloadException(e);
        } finally {
            try { if (ftpClient.isConnected()) { ftpClient.logout(); ftpClient.disconnect(); } } catch (IOException ex) { }
        }
    }
    
    private void exeTestNor() throws Exception {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.setControlEncoding(UTF_8);
            ftpClient.setConnectTimeout(FTP_CONNECT_TIMEOUT);
            ftpClient.connect(getParam().getHost(), getParam().getPort());
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode()))
                throw new Exception("FTP server refused connection.");
            if(!ftpClient.login(getParam().getUsername(), getParam().getPassword()))
                throw new Exception("FTP server refused connection.");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw new Exception(e);
        } finally {
            try { if (ftpClient.isConnected()) { ftpClient.logout(); ftpClient.disconnect(); } } catch (IOException ex) { }
        }
    }
}