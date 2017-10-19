/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.common;

import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.controller.AbstractController;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecurePage;
import gnext.service.attachment.AttachmentService;
import gnext.util.StatusUtil;
import gnext.utils.StringUtil;
import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultStreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "downloadController", eager = true)
@RequestScoped()
@SecurePage(value = "Download", module = SecurePage.Module.NONE, require = false)
public class DownloadController extends AbstractController {
    private static final long serialVersionUID = 7388716078932767351L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadController.class);
    
    @EJB @Getter @Setter private AttachmentService attachmentService;
    @Getter @Setter private DefaultStreamedContent fileDownload;
    
    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void prepDownloadAttachment(Integer attachId) {
        try {
            Attachment attachment = attachmentService.search(getCurrentCompanyId(), attachId);
            if(attachment == null) return;
            Server server = attachment.getServer();
            if(server == null) return;
            
//            gnext.dbutils.model.Server pojo = MapObjectUtil.convert(server);
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
            
            String path = attachment.getAttachmentPath() + File.separator + attachment.getAttachmentName();
            InputStream input = FileTransferFactory.getTransfer(param_ftp).download(path);
            
            ExternalContext ec = getFacesContext().getExternalContext();
            String fn = attachment.getAttachmentName();
            fn = StringUtil.getDownloadFileName(fn);
            this.fileDownload = new DefaultStreamedContent(input, ec.getMimeType(fn), URLEncoder.encode(fn, "utf-8"));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }
}
