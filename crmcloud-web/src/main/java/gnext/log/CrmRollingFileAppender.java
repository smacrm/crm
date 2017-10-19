/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.log;

import gnext.bean.attachment.Server;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import static gnext.util.StatusUtil.getBoolean;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.log4j.Layout;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 *
 * @author daind
 */
public class CrmRollingFileAppender extends RollingFileAppender {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrmRollingFileAppender.class);

    private String remoteFolder;
    private String remoteFileName;
    private Server server;
    
    
    public CrmRollingFileAppender(Layout layout, String filename, boolean append) throws IOException {
        this(layout, filename, append, null, null, null);
    }
    
    public CrmRollingFileAppender(Layout layout, String filename, boolean append, String remoteFolder, String remoteFileName, Server server) throws IOException {
        super(layout, filename, append);
        this.remoteFolder = remoteFolder;
        this.server = server;
        this.remoteFileName = remoteFileName;
    }
    
    @Override
    public void rollOver() {
        if(!StringUtils.isEmpty(remoteFolder) && !StringUtils.isEmpty(remoteFileName) 
                && server != null && maxBackupIndex > 0) {
            File file = new File(fileName + '.' + maxBackupIndex);
            if (file.exists()) upload2fpt(file);
        }
        super.rollOver();
    }
    
    private void upload2fpt(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            if (bytes == null || bytes.length < 0) return;
            String host = server.getServerHost();
            int port = server.getServerPort();
            String username = server.getServerUsername();
            String password = server.getDecryptServerPassword();
            boolean security = getBoolean(server.getServerSsl());
            String protocol = server.getServerProtocol();
            String servertype = server.getServerType();

            TransferType tt = TransferType.getTransferType(servertype);
            Parameter param = Parameter.getInstance(tt).manualconfig(true);
            param.host(host).port(port).username(username).password(password).security(security).protocol(protocol)
                    .uploadfilename(remoteFileName).uploadpath(remoteFolder).createfolderifnotexists()
                    .storeDb(false);
            FileTransferFactory.getTransfer(param).upload(new ByteArrayInputStream(bytes));

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }
}
