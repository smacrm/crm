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
import gnext.util.HTTPResReqUtil;
import gnext.util.InterfaceUtil.ISSUE_TYPE_NAME;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.SelectUtil;
import static gnext.util.StatusUtil.UN_DELETED;
import gnext.util.UploadedFileExt;
import gnext.util.WebFileUtil;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.ISSUE_TYPE;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author gnextadmin
 */
@ManagedBean(name = "issueCustSupportController")
@SessionScoped()
@SecurePage(module = SecurePage.Module.ISSUE)
public class IssueCustSupportController implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(IssueCustSupportController.class);

    @EJB private ServerService serverService;
    @EJB private IssueEscalationService escalationService;
    @EJB private AttachmentService attachmentService;
    @EJB private MemberService memberService;
    @EJB private IssueService issueService;
    @EJB private MultitenancyService multitenancyService;
    
    @ManagedProperty(value = "#{issueController}")
    @Getter @Setter
    private IssueController issueController;

    @Getter @Setter
    private Escalation escalation;

    @Getter @Setter
    private Map<String, List<SelectItem>> select;

    @Getter @Setter
    private Map<String, MenuModel> selects;

    @Getter @Setter
    private MailMode custSupport = new MailMode();

    @Getter @Setter
    public String custSupportCreateId;

    @Getter @Setter
    public String custSupportCreateName;

    @Getter @Setter
    public Server server;
    
    private List<Integer> listDeletedAttachs = new ArrayList<>();

    @PostConstruct
    public void init(){
        loadInfo();
    }

    private Server getOneServer() {
        List<Server> servers = this.serverService.getAvailable(
                UserModel.getLogined().getCompanyId(), TransferType.FTP.getType(), ServerFlag.ISSUE.getId());
        if(servers == null || servers.isEmpty()) return null;
        return servers.get(0);
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    public void create() {
        this.server = getOneServer();
        loadInfo();
        String escId = HTTPResReqUtil.getRequestParameter("escId");
        this.escalation = new Escalation();
        if(!NumberUtils.isDigits(escId)) return;
        this.escalation = this.escalationService.find(Integer.valueOf(escId));
        if(this.escalation == null) return;

        this.custSupport.setHeader(this.escalation.getEscalationTo());
        this.custSupport.setFooter(this.escalation.getEscalationCc());
        this.custSupport.setDate(this.escalation.getEscalationSendDate());
        this.custSupport.getSendMail().setMessage(this.escalation.getEscalationBody());
        List<Attachment> attachs = this.attachmentService.search(
                UserModel.getLogined().getCompanyId(), AttachmentTargetType.ISSUE.getId(), this.escalation.getEscalationId(), UN_DELETED);
        if(attachs == null || this.server == null) return;
        WebFileUtil.loadFileFromFtp(this.custSupport, attachs, this.server);
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    public void save() {
        String issueId = HTTPResReqUtil.getRequestParameter("issueId");
        if(!NumberUtils.isDigits(issueId) || Integer.valueOf(issueId) < 1) {
            if(chkFormCustSupport()){
                issueController.getIssue().getEscalations().add(new Escalation(0, ISSUE_TYPE.CUSTOMER));
                issueController.getCallbackIssueCreated().add("issueCustSupportController.save({ISSUE_ID})");
                HTTPResReqUtil.setCloseDialog();
            }
            return;
        }
        save(Integer.valueOf(issueId));
    }

    @SecureMethod(SecureMethod.Method.CREATE)
    public void save(int issueId) {
        try {
            
            String createId = HTTPResReqUtil.getRequestParameter(COLS.CUSTOMER_SUPPORT_PERSON);
            boolean isValid = chkFormCustSupport();
            
            if(!isValid) return;
            
            Member user = this.memberService.find(Integer.valueOf(createId));
            if(user == null) {
                return;
            }
            this.setCustSupportCreateId(createId);

            Issue issue = this.issueService.find(issueId);
            Escalation esc = new Escalation();
            esc.setEscalationIssueId(issue);
            esc.setEscalationFromEmail(StringUtils.SPACE);
            esc.setEscalationTitle(StringUtils.SPACE);
            esc.setEscalationSendFlag((short)0);
            esc.setEscalationRequestType((short)0);

            esc.setEscalationSendType(ISSUE_TYPE.CUSTOMER);
            esc.setEscalationIsSaved((short)0);
            esc.setEscalationMemberId(user);
            esc.setEscalationTo(this.custSupport.getHeader());
            esc.setEscalationCc(this.custSupport.getFooter());
            esc.setEscalationSendDate(this.custSupport.getDate());
            esc.setEscalationBody(this.custSupport.getSendMail().getMessage());
            esc.setEscalationIsDeleted(UN_DELETED);
            esc.setCompanyId(UserModel.getLogined().getCompanyId());
            esc.setCreatorId(UserModel.getLogined().getMember());
            esc.setCreatedTime(DateUtil.now());
            esc.setUpdatedId(UserModel.getLogined().getUserId());
            esc.setUpdatedTime(DateUtil.now());

            this.escalationService.create(esc);
            if(esc.getEscalationId() != null) {
                try {
                    if(this.server != null) {
                        this.custSupport.setAttachs(WebFileUtil.removeOldFile(this.custSupport.getAttachs()));
                        List<UploadedFileExt> requireUploads = WebFileUtil.removeOldFile(this.custSupport.getAttachs());
                        WebFileUtil.uploadFile(
                            esc.getEscalationId()
                            , requireUploads
                            , AttachmentTargetType.ISSUE
                            , ISSUE_TYPE_NAME.CUSTOMER
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
                    JsfUtil.getResource().alertMsgInfo("label.escalation_4", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.save", ResourceUtil.BUNDLE_ISSUE_NAME);
                } catch (Exception ex) {
                    logger.error("[IssueCustSupportController.save().fileUpload()]", ex);
                     JsfUtil.getResource().putMessager(
                        JsfUtil.getResource().message(
                                UserModel.getLogined().getCompanyId()
                                ,ResourceUtil.BUNDLE_ISSUE_NAME
                                ,"label.not.upload.file"
                                ,(Object) null));
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        HTTPResReqUtil.setCloseDialog();
    }

    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void update() {
        try {
            String issueId = HTTPResReqUtil.getRequestParameter("issueId");
            if(!NumberUtils.isDigits(issueId)
                    || this.escalation.getEscalationId() == null
                    || (this.escalation.getEscalationIssueId() != null
                    && !Objects.equals(Integer.valueOf(issueId), this.escalation.getEscalationIssueId().getIssueId()))) {
                return;
            }
            
            String createId = HTTPResReqUtil.getRequestParameter(COLS.CUSTOMER_SUPPORT_PERSON);
            
            boolean isValid = chkFormCustSupport();
            if(!isValid) return;
            
            Member user = this.memberService.find(Integer.valueOf(createId));
            if(user == null) {
                return;
            }

            Escalation esc = this.escalation;
            esc.setEscalationMemberId(user);
            esc.setEscalationTo(this.custSupport.getHeader());
            esc.setEscalationCc(this.custSupport.getFooter());
            esc.setEscalationSendDate(this.custSupport.getDate());
            esc.setEscalationBody(this.custSupport.getSendMail().getMessage());
            esc.setUpdatedId(UserModel.getLogined().getUserId());
            esc.setUpdatedTime(DateUtil.now());

            try {
                if(this.server != null && this.custSupport.getAttachs() != null) {
                    this.custSupport.setAttachs(WebFileUtil.removeOldFile(this.custSupport.getAttachs()));
                    List<UploadedFileExt> requireUploads = WebFileUtil.removeOldFile(this.custSupport.getAttachs());
                    WebFileUtil.uploadFile(
                        esc.getEscalationId()
                        , requireUploads
                        , AttachmentTargetType.ISSUE
                        , ISSUE_TYPE_NAME.CUSTOMER
                        , this.server.getServerId()
                        , UserModel.getLogined().getCompanyId());
                    
                    // remove deleted attachments file
                    if(!listDeletedAttachs.isEmpty()){
                        listDeletedAttachs.forEach((deletedAttachmentId) -> {
                            attachmentService.deleteAttachment(deletedAttachmentId);
                        });
                    }
                    
                }
                this.escalationService.edit(esc);
                this.issueController.reloadEscalations();
                JsfUtil.getResource().alertMsgInfo("label.escalation_4", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.update", ResourceUtil.BUNDLE_ISSUE_NAME);
            } catch (Exception ex) {
                logger.error("[IssueCustSupportController.update().fileUpload()]", ex);
                 JsfUtil.getResource().putMessager(
                    JsfUtil.getResource().message(
                            UserModel.getLogined().getCompanyId()
                            ,ResourceUtil.BUNDLE_ISSUE_NAME
                            ,"label.not.upload.file"
                            ,(Object) null));
            }
        } catch (NumberFormatException ex) {
            logger.error("[IssueCustSupportController.update()]", ex);
        }
        HTTPResReqUtil.setCloseDialog();
    }

    @SecureMethod(value=SecureMethod.Method.UPLOAD)
    public void upload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        try{
            if(file == null || file.getFileName() == null) return;
            if(WebFileUtil.fileIsExists(this.custSupport.getAttachs(), file)) {
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
            EmailUtil.uploadDowload(this.custSupport, ext, null);
        } catch(Exception ex) {
            logger.error("[IssueCustSupportController.upload()]", ex);
        }
    }

    @SecureMethod(value=SecureMethod.Method.DELETEFILE)
    public void deleteFile() {
        String idx = HTTPResReqUtil.getRequestParameter("idx");
        try{
            Integer deletedFileId = EmailUtil.uploadDowload(this.custSupport, null, idx);
            listDeletedAttachs.add(deletedFileId);
        } catch(Exception ex) {
            logger.error("[IssueCustSupportController.deleteFile()]", ex);
        }
    }

    private void loadInfo() {
        try {
            this.custSupport = new MailMode();
            this.escalation = new Escalation();
            this.select = new HashMap<>();
            this.selects = new HashMap<>();

            this.select.put(COLS.SUPPORT_METHOD, issueController.getSelect().get(COLS.SUPPORT_METHOD));
            this.select.put(COLS.SUPPORT_CLASS, issueController.getSelect().get(COLS.SUPPORT_CLASS));

            this.custSupport.setDate(DateUtil.now());
            this.setCustSupportCreateId(String.valueOf(UserModel.getLogined().getUserId()));
            this.setCustSupportCreateName(UserModel.getLogined().getMember().getMemberNameFirst() + StringUtils.SPACE + UserModel.getLogined().getMember().getMemberNameLast());
            List<SelectItem> itemMethods = this.select.get(COLS.SUPPORT_METHOD);
            if(itemMethods != null && !itemMethods.isEmpty()) {
                this.custSupport.setHeader(String.valueOf(itemMethods.get(0).getValue()));
            }
            List<SelectItem> itemClasss = this.select.get(COLS.SUPPORT_CLASS);
            if(itemClasss != null && !itemClasss.isEmpty()) {
                this.custSupport.setFooter(String.valueOf(itemClasss.get(0).getValue()));
            }
            
            /** 作成者 */
            List<Group> groupList = multitenancyService.findAllGroupUnderSlave(UserModel.getLogined().getCompanyId());
            MenuModel creatorPerson = SelectUtil.getSelectGroupMember(COLS.CUSTOMER_SUPPORT_PERSON, StringUtils.EMPTY, groupList);
            if(creatorPerson != null) this.selects.put(COLS.CUSTOMER_SUPPORT_PERSON, creatorPerson);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
        // reset deleted attachs list
        listDeletedAttachs.clear();
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private boolean chkNotSelect(String id, String label) {
        if(!NumberUtils.isDigits(id)) {
            JsfUtil.getResource().alertMsg(label, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        return true;
    }

    private boolean chkFormCustSupport() {
        boolean isValid = true;
        if(this.custSupport.getDate() == null) {
            JsfUtil.getResource().alertMsg("label.escalationSendDateName_4", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
            isValid = false;
        }
        if(StringUtils.isBlank(this.custSupport.getSendMail().getMessage())) {
            JsfUtil.getResource().alertMsg("label.support_comment", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
            isValid = false;
        }
        if(this.custSupport.getSendMail().getMessage() != null
                && this.custSupport.getSendMail().getMessage().length() > 20000) {
            JsfUtil.getResource().alertMsgMaxLength("label.support_comment", ResourceUtil.BUNDLE_ISSUE_NAME, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 20000);
            isValid = false;
        }
        
        String createId = HTTPResReqUtil.getRequestParameter(COLS.CUSTOMER_SUPPORT_PERSON);
        isValid &= /** 対応者 */ chkNotSelect(createId, "label.support_person");
        isValid &= /** 対応方法 */ chkNotSelect(this.custSupport.getHeader(), "label.support_method");
        isValid &= /** 対応種別 */ chkNotSelect(this.custSupport.getFooter(), "label.support_class");
        return isValid;
    }
}
