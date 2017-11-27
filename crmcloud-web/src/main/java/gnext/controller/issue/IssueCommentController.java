package gnext.controller.issue;

import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.filetransfer.TransferType;
import gnext.bean.issue.Escalation;
import gnext.bean.issue.Issue;
import gnext.model.MailMode;
import gnext.model.authority.UserModel;
import gnext.multitenancy.service.MultitenancyService;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.MemberService;
import gnext.service.attachment.AttachmentService;
import gnext.service.attachment.ServerService;
import gnext.service.issue.IssueEscalationService;
import gnext.service.issue.IssueService;
import gnext.util.DateUtil;
import gnext.util.EmailUtil;
import gnext.util.WebFileUtil;
import gnext.util.HTTPResReqUtil;
import gnext.util.InterfaceUtil.ISSUE_TYPE_NAME;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.SelectUtil;
import static gnext.util.StatusUtil.UN_DELETED;
import gnext.util.UploadedFileExt;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.ISSUE_TYPE;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author gnextadmin
 */
@ManagedBean(name = "issueCommentController")
@SessionScoped()
@SecurePage(module = SecurePage.Module.ISSUE)
public class IssueCommentController implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(IssueCommentController.class);

    @EJB private ServerService serverService;
    @EJB private IssueEscalationService escalationService;
    @EJB private IssueService issueService;
    @EJB private MemberService memberService;
    @EJB private AttachmentService attachmentService;
    @EJB private MultitenancyService multitenancyService;
    

    @ManagedProperty(value = "#{issueController}")
    @Getter @Setter
    private IssueController issueController;

    @Getter @Setter
    public Server server;

    @Getter @Setter
    private Map<String, MenuModel> selects;

    @Getter @Setter
    private MailMode comment = new MailMode();

    @Getter @Setter
    public String commentCreateId;

    @Getter @Setter
    public String commentCreateName;

    @Getter @Setter
    private Escalation escalation;

    @Getter @Setter
    private DefaultStreamedContent fileDownload;
    
    private List<Integer> listDeletedAttachs = new ArrayList<>();

    @PostConstruct
    public void init(){
        loadInfo();
    }

    private Server getOneServer() {
        List<Server> servers = this.serverService.getAvailable(
                UserModel.getLogined().getCompanyId(),
                TransferType.FTP.getType(), ServerFlag.ISSUE.getId());
        if(servers == null || servers.isEmpty()) return null;
        return servers.get(0);
    }
    
    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void create() {
        this.server = getOneServer();
        if(this.server == null) this.server = new Server();
        loadInfo();
    }

    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void save() {
        String issueId = HTTPResReqUtil.getRequestParameter("issueId");
        
        if(!NumberUtils.isDigits(issueId) || Integer.valueOf(issueId) < 1) {
            logger.error("[IssueCommentController.save()]", " IssueId is null !!! ");
            if(chkForm()){
                issueController.getIssue().getEscalations().add(new Escalation(0, ISSUE_TYPE.COMMENT));
                issueController.getCallbackIssueCreated().add("issueCommentController.save({ISSUE_ID})");
                HTTPResReqUtil.setCloseDialog();
            }
            return;
        }
        save(Integer.valueOf(issueId));
    }
    
    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void save(int issueId) {
        try {
            String createId = HTTPResReqUtil.getRequestParameter(COLS.COMMENT_PERSON);
           
            Member user = this.memberService.find(Integer.valueOf(createId));
            if(user == null) {
                logger.error("[IssueCommentController.save()]",  " Member is null !!! ");
                return;
            }
            
            if(!chkForm()) return;
            
            this.setCommentCreateId(createId);
            this.setCommentCreateName(user.getMemberNameFull());

            Escalation esc = new Escalation();
            esc.setEscalationMemberId(user);
            esc.setEscalationSendDate(this.comment.getDate());
            esc.setEscalationBody(this.comment.getSendMail().getMessage());
            insertUpdateEscalation(esc, issueId);

            if(esc.getEscalationId() != null && this.server != null) {
                List<UploadedFileExt> requireUploads = WebFileUtil.removeOldFile(this.comment.getAttachs());
                WebFileUtil.uploadFile(
                        esc.getEscalationId()
                        , requireUploads
                        , AttachmentTargetType.ISSUE
                        , ISSUE_TYPE_NAME.COMMENT
                        , this.server.getServerId()
                        , UserModel.getLogined().getCompanyId());
                
                // remove deleted attachments file
                if(!listDeletedAttachs.isEmpty()){
                    listDeletedAttachs.forEach((deletedAttachmentId) -> {
                        attachmentService.deleteAttachment(deletedAttachmentId);
                    });
                }
            }
            
            this.issueController.reloadEscalations(issueId);
            JsfUtil.getResource().alertMsgInfo("label.escalation_3", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.save", ResourceUtil.BUNDLE_ISSUE_NAME);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        HTTPResReqUtil.setCloseDialog();
    }

    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void update() {
        try {
            if(this.escalation == null || this.escalation.getEscalationId() == null) {
                logger.error("[IssueCommentController.update()]",  " Escalation is null !!! ");
                return;
            }
            String createId = HTTPResReqUtil.getRequestParameter(COLS.COMMENT_PERSON);
            String issueId = HTTPResReqUtil.getRequestParameter("issueId");
            if(!NumberUtils.isDigits(createId)) {
                JsfUtil.getResource().alertMsg("label.entry_person", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }
            if(!chkForm()) return;
            if(!NumberUtils.isDigits(issueId)) {
                logger.error("[IssueCommentController.update()]",  " IssueId is null !!! ");
                return;
            }
            Member user = this.memberService.find(Integer.valueOf(createId));
            if(user == null) {
                logger.error("[IssueCommentController.update()]",  " Member is null !!! ");
                return;
            }
            this.escalation.setEscalationMemberId(user);
            this.escalation.setEscalationSendDate(this.comment.getDate());
            this.escalation.setEscalationBody(this.comment.getSendMail().getMessage());

            try {
                if(this.server != null && this.comment != null &&  this.comment.getAttachs() != null) {
                    List<UploadedFileExt> requireUploads = WebFileUtil.removeOldFile(this.comment.getAttachs());
                    WebFileUtil.uploadFile(
                        this.escalation.getEscalationId()
                        , requireUploads
                        , AttachmentTargetType.ISSUE
                        , ISSUE_TYPE_NAME.COMMENT
                        , this.server.getServerId()
                        , UserModel.getLogined().getCompanyId());
                    
                    // remove deleted attachments file
                    if(!listDeletedAttachs.isEmpty()){
                        listDeletedAttachs.forEach((deletedAttachmentId) -> {
                            attachmentService.deleteAttachment(deletedAttachmentId);
                        });
                    }
                }
                insertUpdateEscalation(this.escalation, Integer.valueOf(issueId));
                this.issueController.reloadEscalations();
                JsfUtil.getResource().alertMsgInfo("label.escalation_3", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.update", ResourceUtil.BUNDLE_ISSUE_NAME);
            } catch (Exception ex) {
                logger.error("[IssueCustSupportController.update().fileUpload()]", ex);
                 JsfUtil.getResource().putMessager(
                    JsfUtil.getResource().message(
                            UserModel.getLogined().getCompanyId()
                            ,ResourceUtil.BUNDLE_ISSUE_NAME
                            ,"label.not.upload.file"
                            ,(Object) null));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        HTTPResReqUtil.setCloseDialog();
    }

    @SecureMethod(value=SecureMethod.Method.UPLOAD)
    public void upload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        try {
            if(file == null || file.getFileName() == null) return;
            if(WebFileUtil.fileIsExists(this.comment.getAttachs(), file)) {
                JsfUtil.getResource().putMessager(
                        JsfUtil.getResource().message(
                                UserModel.getLogined().getCompanyId()
                                ,ResourceUtil.BUNDLE_ISSUE_NAME
                                ,"label.same.file"
                                ,(Object) null));
                return;
            }
            UploadedFileExt ext = new UploadedFileExt();
            ext.setFile(file);
            EmailUtil.uploadDowload(this.comment, ext, null);
        } catch (Exception ex) {
            logger.error("[IssueCommentController.upload()]", ex);
        }
    }

    @SecureMethod(value=SecureMethod.Method.DOWNLOAD)
    public void download(String attachId) {
        try{
            if(!NumberUtils.isDigits(attachId)) {
                return;
            }
            Attachment attach = this.attachmentService.find(Integer.valueOf(attachId));
            if(attach == null || attach.getAttachmentId() == null || attach.getServer() == null) {
                JsfUtil.getResource().alertMsg("label.file_data", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.not.exist", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }
        this.fileDownload = WebFileUtil.downloadFromFtpDefaultStreamedContent(attach);
        } catch (NumberFormatException ex) {
            logger.error("[IssueCommentController.deleteFile()]", ex);
        }
    }

    @SecureMethod(value=SecureMethod.Method.DELETEFILE)
    public void deleteFile() {
        String idx = HTTPResReqUtil.getRequestParameter("idx");
        try{
            Integer deletedFileId = EmailUtil.uploadDowload(this.comment, null, idx);
            listDeletedAttachs.add(deletedFileId);
        } catch (Exception ex) {
            logger.error("[IssueCommentController.deleteFile()]", ex);
        }
    }

    private void loadInfo() {
        try {
            this.comment = new MailMode();
            this.selects = new HashMap<>();
            /** 作成者 */
            List<Group> lstGroupOnSlave = multitenancyService.findAllGroupUnderSlave(UserModel.getLogined().getCompanyId());
            MenuModel creatorPerson = SelectUtil.getSelectGroupMember(COLS.COMMENT_PERSON, StringUtils.EMPTY, lstGroupOnSlave);
            if(creatorPerson != null) this.selects.put(COLS.COMMENT_PERSON, creatorPerson);

            String escId = HTTPResReqUtil.getRequestParameter("escId");
            this.escalation = new Escalation();
            if(NumberUtils.isDigits(escId)) {
                this.escalation = this.escalationService.find(Integer.valueOf(escId));
                if(this.escalation != null) {
                    this.comment.setDate(this.escalation.getEscalationSendDate());
                    this.comment.getSendMail().setMessage(this.escalation.getEscalationBody());
                }
            }
            if(this.escalation == null || this.escalation.getEscalationMemberId() == null) {
                this.escalation = new Escalation();
                this.comment.setDate(DateUtil.now());
                this.escalation.setEscalationMemberId(UserModel.getLogined().getMember());
                this.setCommentCreateId(String.valueOf(UserModel.getLogined().getUserId()));
                this.setCommentCreateName(UserModel.getLogined().getMember().getMemberNameFirst() + StringUtils.SPACE + UserModel.getLogined().getMember().getMemberNameLast());
            } else {
                this.setCommentCreateId(String.valueOf(this.escalation.getEscalationMemberId().getMemberId()));
                this.setCommentCreateName(this.escalation.getEscalationMemberId().getMemberNameFirst() + StringUtils.SPACE + this.escalation.getEscalationMemberId().getMemberNameLast());

                List<Attachment> attachs = this.attachmentService.search(
                        UserModel.getLogined().getCompanyId(), AttachmentTargetType.ISSUE.getId(), this.escalation.getEscalationId(), UN_DELETED);
                if(attachs == null || this.server == null) return;
                WebFileUtil.loadFileFromFtp(this.comment, attachs, this.server);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
        // reset deleted attachs list
        listDeletedAttachs.clear();
    }

    private void insertUpdateEscalation(Escalation esc, Integer issueId) {
        Issue issue = this.issueService.find(issueId);
        if(esc == null) esc = new Escalation();
        esc.setEscalationIssueId(issue);
        esc.setEscalationFromEmail(StringUtils.SPACE);
        esc.setEscalationTitle(StringUtils.SPACE);
        esc.setEscalationSendFlag((short)0);
        esc.setEscalationRequestType((short)0);
        esc.setEscalationIsSaved((short)0);

        esc.setEscalationSendType(ISSUE_TYPE.COMMENT);
        esc.setCompanyId(UserModel.getLogined().getCompanyId());
        if(esc.getEscalationId() == null || esc.getEscalationId() <= 0) {
            esc.setEscalationIsDeleted((short)0);
            esc.setCreatorId(UserModel.getLogined().getMember());
            esc.setCreatedTime(DateUtil.now());
        }
        esc.setUpdatedId(UserModel.getLogined().getUserId());
        esc.setUpdatedTime(DateUtil.now());

        try{
            if(esc.getEscalationId() != null) {
                this.escalationService.edit(esc);
            } else {
                this.escalationService.create(esc);
            }
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }
    }

    private boolean chkForm() {
        if(this.comment.getDate() == null) {
            JsfUtil.getResource().alertMsg("label.escalationSendDateName_3", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        if(StringUtils.isBlank(this.comment.getSendMail().getMessage())) {
            JsfUtil.getResource().alertMsg("label.comment_detail", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        if(this.comment.getSendMail().getMessage() != null
                && this.comment.getSendMail().getMessage().length() > 20000) {
            JsfUtil.getResource().alertMsgMaxLength("label.comment_detail", ResourceUtil.BUNDLE_ISSUE_NAME, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 20000);
            return false;
        }
        return true;
    }
}
