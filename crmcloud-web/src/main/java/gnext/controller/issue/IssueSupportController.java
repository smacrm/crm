package gnext.controller.issue;

import gnext.bean.Group;
import gnext.bean.MailAccount;
import gnext.bean.Member;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.bean.mente.MenteItem;
import gnext.dbutils.model.MailData;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.filetransfer.TransferType;
import gnext.bean.issue.CustTargetInfo;
import gnext.bean.issue.Customer;
import gnext.bean.issue.Escalation;
import gnext.bean.issue.EscalationSample;
import gnext.bean.issue.Issue;
import gnext.controller.common.LocaleController;
import gnext.model.DialogObject;
import gnext.model.MailMode;
import gnext.model.authority.UserModel;
import gnext.multitenancy.service.MultitenancyService;
import gnext.resource.bundle.MailBundle;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.MemberService;
import gnext.service.attachment.AttachmentService;
import gnext.service.attachment.ServerService;
import gnext.service.config.ConfigService;
import gnext.service.issue.IssueEscalationSampleService;
import gnext.service.issue.IssueEscalationService;
import gnext.service.issue.IssueService;
import gnext.service.mail.MailAccountService;
import gnext.service.mente.MenteService;
import gnext.util.DateUtil;
import gnext.util.EmailUtil;
import gnext.util.HTTPResReqUtil;
import gnext.util.ResourceUtil;
import gnext.util.SelectUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import gnext.util.InterfaceUtil.ISSUE_TYPE_NAME;
import gnext.util.JsfUtil;
import static gnext.util.StatusUtil.UN_DELETED;
import gnext.util.StringUtil;
import gnext.util.UploadedFileExt;
import gnext.util.WebFileUtil;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.ISSUE_TYPE;
import gnext.utils.InterfaceUtil.TARGET;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import org.apache.commons.lang3.time.DateUtils;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.TreeNode;

/**
 *
 * @author daind
 */
@ManagedBean(name = "issueSupportController")
@SessionScoped()
@SecurePage(module = SecurePage.Module.ISSUE)
public class IssueSupportController implements Serializable {
    private static final long serialVersionUID = -4249917401198090330L;
    private static final Logger LOGGER = LoggerFactory.getLogger(IssueSupportController.class);

    @EJB private ServerService serverService;
    @EJB private AttachmentService attachmentService;
    @EJB private MenteService menteServiceImpl;
    @EJB private MailAccountService mailAccountService;
    @EJB private MemberService memberService;
    @EJB private IssueService issueService;
    @EJB private IssueEscalationService escalationService;
    @EJB private IssueEscalationSampleService issueEscalationSampleService;
    @EJB private MultitenancyService multitenancyService;
    
    @EJB private ConfigService configService;

    @ManagedProperty(value = "#{mailBundle}")
    @Getter @Setter private MailBundle mailBundle;

    @ManagedProperty(value = "#{issueController}")
    @Getter @Setter private IssueController issueController;
    
    @ManagedProperty(value = "#{localeController}")
    @Getter @Setter private LocaleController localeController;
    
    @Getter @Setter private MailMode requestMail = new MailMode();
    @Getter @Setter public String requestType;
    @Getter @Setter public List<SelectItem> requestTypes;
    @Getter @Setter private MailMode supportMail = new MailMode();
    @Getter @Setter private Integer mailApprovalUserId;
    @Getter @Setter private String mailApprovalUserName;
    @Getter @Setter private boolean mailRequest;
    @Getter @Setter private Integer mailRequestUserId;
    @Getter @Setter private String mailRequestUserName;
    @Getter @Setter private Date mailRequestDate;
    @Getter @Setter private Integer inputHiddenExamplesId;
    @Getter @Setter private String customerName;
    @Getter @Setter private Escalation escalation;
    @Getter @Setter private Map<String, List<SelectItem>> select;
    @Getter @Setter private DualListModel<SelectItem> members;
    @Getter @Setter private Integer targetMailFlag;
    @Getter @Setter private String targetMailTarget = "";
    @Getter @Setter private List<MailAccount> froms = new ArrayList<>();
    @Getter @Setter private List<CustTargetInfo> tos = new ArrayList<>();
    @Getter @Setter private List<MenteItem> escalationSamples = new ArrayList<>();
    @Getter @Setter public Server server;
    
    private List<Integer> listDeletedAttachs = new ArrayList<>();

    @PostConstruct
    public void init() {
        this.members = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
        loadInfo();
    }

    private Server getOneServer() {
        List<Server> servers = this.serverService.getAvailable(UserModel.getLogined().getCompanyId(),
                TransferType.FTP.getType(), ServerFlag.ISSUE.getId());
        if(servers == null || servers.isEmpty()) return null;
        return servers.get(0);
    }
    
    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void create(String type) {
        try {
            String issueId = HTTPResReqUtil.getRequestParameter("issueId");
            if(!NumberUtils.isDigits(issueId)) {
                return;
            }
            this.members = new DualListModel<>();

            loadCreate(type, issueId);
        } catch (NumberFormatException ex) {
            LOGGER.error("IssueSupportController.create()", ex);
        }
    }

    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void save() {
        try {
            String issueId = HTTPResReqUtil.getRequestParameter("issueId");
            if(!NumberUtils.isDigits(issueId)) {
                return;
            }
            if(!chkFormSupport()) return;
            /** 送信先 */
            if(StringUtils.isNoneBlank(this.supportMail.getTo())) {
                this.supportMail.getSendMail().setRecipient(new String[]{ this.supportMail.getTo() });
            }

            /** 履歴情報テーブルに追加 */
            Issue issue = this.issueController.getIssue(Integer.valueOf(issueId));
            if(this.getMailApprovalUserId() != null) {
                this.supportMail.getSendMail().setCc(new String[]{ String.valueOf(this.getMailApprovalUserId()) });
            } else {
                this.supportMail.getSendMail().setCc(null);
            }
            if(this.getMailRequestUserId() != null) {
                this.supportMail.getSendMail().setBcc(new String[]{ String.valueOf(this.getMailRequestUserId()) });
                this.supportMail.setDate(this.getMailRequestDate());
            } else {
                this.supportMail.getSendMail().setBcc(null);
                this.supportMail.setDate(null);
            }
            Escalation esc = insertUpdateEscalation(this.supportMail, ISSUE_TYPE.SUPPORT, "label.escalation_4", (short)1, UserModel.getLogined().getMember(), issue);
            if(esc.getEscalationId() != null && this.server != null) {
                List<UploadedFileExt> requireUploads = WebFileUtil.removeOldFile(this.supportMail.getAttachs());
                /** 添付ファイルFTPへ転送 */
                WebFileUtil.uploadFile(
                        esc.getEscalationId()
                        , requireUploads
                        , AttachmentTargetType.ISSUE
                        , ISSUE_TYPE_NAME.SUPPORT
                        , this.server.getServerId()
                        , UserModel.getLogined().getCompanyId());
                
                // remove deleted attachments file
                if(!listDeletedAttachs.isEmpty()){
                    listDeletedAttachs.forEach((deletedAttachmentId) -> {
                        attachmentService.deleteAttachment(deletedAttachmentId);
//                        WebFileUtil.deleteFileFromFtp("", this.server);
                    });
                }
            }
            this.setEscalation(esc);
            this.issueController.reloadEscalations();
            HTTPResReqUtil.setCloseDialog();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @SecureMethod(value=SecureMethod.Method.SUPPORTMAIL)
    public void edit(String type) {
        String escId = HTTPResReqUtil.getRequestParameter("escId");
        try {
            if("1".equals(type)) {
                if(NumberUtils.isDigits(escId)) {
                    this.escalation = this.escalationService.find(Integer.valueOf(escId));
                    if(this.escalation == null) return;
                    this.supportMail.getSendMail().setMessage(this.escalation.getEscalationBody());
                    this.supportMail.setReplyBody(this.escalation.getEscalationBodyReply());
                }
            }
        } catch (NumberFormatException ex) {
            LOGGER.error("IssueSupportController.edit()", ex);
        }
    }

    @SecureMethod(value=SecureMethod.Method.SUPPORTMAIL)
    public void sendSupport() throws Exception {
        String issueId = HTTPResReqUtil.getRequestParameter("issueId");
        if(!NumberUtils.isDigits(issueId) || Integer.valueOf(issueId) < 1) {
            boolean isValid = chkFormSupport();
            MailAccount ac = EmailUtil.getAccount(this.getFroms(), this.supportMail.getSendMail().getFrom());
            if(ac == null) {
                isValid = false;
            }
            /** 送信先 */
            if(StringUtils.isBlank(this.supportMail.getTo())) {
                isValid = false;
            }
            if(isValid){
                issueController.getIssue().getEscalations().add(new Escalation(0, ISSUE_TYPE.SUPPORT));
                issueController.getCallbackIssueCreated().add("issueSupportController.sendSupport({ISSUE_ID})");
                HTTPResReqUtil.setCloseDialog();
            }
            return;
        }
        sendSupport(Integer.valueOf(issueId));
    }
    
    @SecureMethod(value=SecureMethod.Method.SUPPORTMAIL)
    public void sendSupport(int issueId) throws Exception {
        try {
            if(!chkFormSupport()) return;
            MailAccount ac = EmailUtil.getAccount(this.getFroms(), this.supportMail.getSendMail().getFrom());
            if(ac == null) {
                JsfUtil.getResource().putErrors(
                        UserModel.getLogined().getCompanyId()
                        , ResourceUtil.BUNDLE_ISSUE_NAME
                        , ResourceUtil.SEVERITY_ERROR_NAME
                        , "label.account.not.send.mail"
                        , false
                        , (Object) null);
                return;
            }
            /** 送信先 */
            if(StringUtils.isBlank(this.supportMail.getTo())) {
                JsfUtil.getResource().alertMsg("label.mail.to", ResourceUtil.BUNDLE_MSG, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }
            
            /** 送付ファイル **/
            Map<String, InputStream> attachments = new HashMap<>();
            this.supportMail.getAttachs().forEach((t) -> {
                try {
                    attachments.put(t.getFile().getFileName(), t.getFile().getInputstream());
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            });
            
            /** メール送信 */
            this.supportMail.getSendMail().setRecipient(new String[]{ this.supportMail.getTo() });
            MailData send = null;
            try {
                Server s = getOneServer();
                if(s == null) 
                    throw new Exception("The ftp server for upload the file attachment is does not exists.");
                
                send = EmailUtil.send(this.supportMail.getSendMail(), ac, attachments,
                        UserModel.getLogined().getCompanyId(), s.getServerId(), ac.getAccountId());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                send = null;
            }
            if(send == null) {
                JsfUtil.getResource().alertMsg("label.escalation_2", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not.send.mail", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }

            Issue issue = this.issueController.getIssue(issueId);
            Member user = null;
            if(this.getMailApprovalUserId() != null) {
                user = this.memberService.find(this.getMailApprovalUserId());
            }
            Escalation esc = insertUpdateEscalation(this.supportMail, ISSUE_TYPE.SUPPORT, "label.escalation_4", (short)0, user, issue);
            if(esc.getEscalationId() != null && this.server != null) {
                /** 添付ファイルFTPへ転送 */
                List<UploadedFileExt> requireUploads = WebFileUtil.removeOldFile(this.supportMail.getAttachs());
                WebFileUtil.uploadFile(
                        esc.getEscalationId()
                        , requireUploads
                        , AttachmentTargetType.ISSUE
                        , ISSUE_TYPE_NAME.SUPPORT
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
            HTTPResReqUtil.setCloseDialog();
        } catch (Exception ex) {
            LOGGER.error("IssueSupportController.sendSupport()", ex);
        }
    }
    
    @SecureMethod(value=SecureMethod.Method.SUPPORTMAIL)
    public void sendSupportUpdate() {
        try {
            if(this.escalation == null || this.escalation.getEscalationId() == null) {
                JsfUtil.getResource().alertMsg("label.escalation_1", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.not.exist", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }
            if(StringUtils.isBlank(this.supportMail.getReplyBody())) {
                JsfUtil.getResource().alertMsg("label.mail.comment.reply", ResourceUtil.BUNDLE_MSG, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }
            if(this.supportMail.getReplyBody() != null
                    && this.supportMail.getReplyBody().length() > 20000) {
                JsfUtil.getResource().alertMsgMaxLength("label.mail.comment.reply", ResourceUtil.BUNDLE_MSG, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 20000);
                return;
            }

            this.escalation.setEscalationBodyReply(this.supportMail.getReplyBody());
            this.escalation.setUpdatedId(UserModel.getLogined().getUserId());
            this.escalation.setUpdatedTime(DateUtil.now());
            this.escalationService.edit(this.escalation);
            this.issueController.reloadEscalations();
            JsfUtil.getResource().alertMsgInfo("label.escalation_1", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.update", ResourceUtil.BUNDLE_ISSUE_NAME);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        HTTPResReqUtil.setCloseDialog();
    }

    @SecureMethod(value=SecureMethod.Method.REQUESTMAIL)
    public void sendRequest() throws Exception {
        String issueId = HTTPResReqUtil.getRequestParameter("issueId");
        if(!NumberUtils.isDigits(issueId) || Integer.valueOf(issueId) < 1) {
            boolean isValid = chkFormRequest();
            MailAccount ac = EmailUtil.getAccount(this.getFroms(), this.requestMail.getSendMail().getFrom());
            if(ac == null) {
                isValid = false;
            }

            if(this.requestMail.getTos().isEmpty()) {
                isValid = false;
            }
            if(isValid){
                issueController.getIssue().getEscalations().add(new Escalation(0, ISSUE_TYPE.REQUEST));
                issueController.getCallbackIssueCreated().add("issueSupportController.sendRequest({ISSUE_ID})");
                HTTPResReqUtil.setCloseDialog();
                return;
            }
        }
        sendRequest(Integer.valueOf(issueId));
    }
    
    @SecureMethod(value=SecureMethod.Method.REQUESTMAIL)
    public void sendRequest(int issueId) throws Exception {
        try {
            if(!chkFormRequest()) return;
            MailAccount ac = EmailUtil.getAccount(this.getFroms(), this.requestMail.getSendMail().getFrom());
            if(ac == null) {
                JsfUtil.getResource().putErrors(
                        UserModel.getLogined().getCompanyId()
                        , ResourceUtil.BUNDLE_ISSUE_NAME
                        , ResourceUtil.SEVERITY_ERROR_NAME
                        , "label.account.not.send.mail"
                        , false
                        , (Object) null);
                return;
            }

            if(this.requestMail.getTos().isEmpty()) {
                JsfUtil.getResource().alertMsg("label.mail.to", ResourceUtil.BUNDLE_MSG, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }

            Issue issue = this.issueController.getIssue(issueId);
            if(issue == null) {
                return;
            }
            /* 送信先 */
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            List<String> tos = new ArrayList<>();
            for(SelectItem item:this.requestMail.getTos()) {
                if(item == null) continue;
                String mail = item.getDescription();
                if(!StringUtils.isEmpty(mail)){
                    tos.add(item.getDescription());
                }
            }
            this.requestMail.getSendMail().setRecipient(tos.toArray(new String[tos.size()]));
            /* CC */
            List<String> ccs = new ArrayList<>();
            for(SelectItem item:this.requestMail.getCcs()) {
                if(item == null) continue;
                String mail = item.getDescription();
                if(!StringUtils.isEmpty(mail)){
                    ccs.add(mail);
                }
            }
            this.requestMail.getSendMail().setCc(ccs.toArray(new String[ccs.size()]));
            /* BCC */
            List<String> bccs = new ArrayList<>();
            for(SelectItem item:this.requestMail.getCcs()) {
                if(item == null) continue;
                String mail = item.getDescription();
                if(!StringUtils.isEmpty(mail)){
                    bccs.add(mail);
                }
            }
            this.requestMail.getSendMail().setBcc(bccs.toArray(new String[bccs.size()]));
            /** メール送信 */
            String body = this.requestMail.getSendMail().getMessage();
            this.requestMail.getSendMail().setMessage(
                    StringUtil.getRequestMailUrl(
                            body
                            , issue.getIssueId()
                            , UserModel.getLogined().getCompanyId()
                            , UserModel.getLogined().getUserId()
                            , DateUtils.addDays(new Date(), 7),
                            configService.get("WEBSITE_URL")));
            /** メール送信 */
            MailData send = null;
            try {
                Server s = getOneServer();
                if(s == null) 
                    throw new Exception("The ftp server for upload the file attachment is does not exists.");
                
                send = EmailUtil.send(this.requestMail.getSendMail(), ac, null,
                        UserModel.getLogined().getCompanyId(), s.getServerId(), ac.getAccountId());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                send = null;
            }
            if(send == null) {
                JsfUtil.getResource().alertMsg("label.escalation_2", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not.send.mail", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }
            this.requestMail.getSendMail().setMessage(body);
            
            if(this.mailRequest) {
                this.setMailRequestUserId(UserModel.getLogined().getUserId());
                this.setMailRequestUserName(HTTPResReqUtil.getLoginUserName());
                this.setMailRequestDate(DateUtil.now());
            }
            Escalation esc = insertUpdateEscalation(this.requestMail, ISSUE_TYPE.REQUEST, "label.escalation_2", (short)0, UserModel.getLogined().getMember(), issue);
            
            this.issueController.reloadEscalations(issueId);
        } catch (Exception ex) {
            LOGGER.error("IssueSupportController.sendRequest()", ex);
        }
        HTTPResReqUtil.setCloseDialog();
    }
    

    @SecureMethod(value=SecureMethod.Method.UPLOAD)
    public void upload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        try {
            if(file == null || file.getFileName() == null) return;
            if(WebFileUtil.fileIsExists(this.supportMail.getAttachs(), file)) {
                JsfUtil.getResource().putMessager(
                        JsfUtil.getResource().message(UserModel.getLogined().getCompanyId()
                                ,ResourceUtil.BUNDLE_ISSUE_NAME
                                ,"label.same.file"
                                ,(Object) null));
                return;
            }
            UploadedFileExt ext = new UploadedFileExt();
            ext.setFile(file);
            EmailUtil.uploadDowload(this.supportMail, ext, null);
        } catch (Exception ex) {
            LOGGER.error("[IssueSupportController.upload()]", ex);
        }
    }

    @SecureMethod(value=SecureMethod.Method.DELETE)
    public void delete() {
        try {
            String issueId = HTTPResReqUtil.getRequestParameter("issueId");
            if(!NumberUtils.isDigits(issueId)) {
                return;
            }
            if(this.escalation == null || this.escalation.getEscalationId() == null) {
                return;
            }
            List<Attachment> list = this.attachmentService.search(
                    UserModel.getLogined().getCompanyId()
                    , AttachmentTargetType.ISSUE.getId()
                    , this.escalation.getEscalationId()
                    , UN_DELETED);
            if(list != null && list.size() > 0) {
                String dirPath = list.get(0).getAttachmentPath();
                if(StringUtils.isNoneBlank(dirPath)) {
                    // Delete dir
                }
            }
            this.attachmentService.deleteAttachmentByEscalationId(this.escalation.getEscalationId());
            this.escalationService.remove(this.escalation);
            loadCreate("1", issueId);
        } catch (Exception ex) {
            LOGGER.error("IssueSupportController.delete()", ex);
        }
    }

    @SecureMethod(value=SecureMethod.Method.DELETEFILE)
    public void deleteFile() {
        String idx = HTTPResReqUtil.getRequestParameter("idx");
        Integer deletedAttachmentId = EmailUtil.uploadDowload(this.supportMail, null, idx);
        listDeletedAttachs.add(deletedAttachmentId);
    }

    /** 作成者を選択された時処理 */
    public void getLoginUserName() {
        this.setMailApprovalUserId(UserModel.getLogined().getUserId());
        this.setMailApprovalUserName(HTTPResReqUtil.getLoginUserName());
    }

    public void deleteLoginUserName() {
        this.setMailApprovalUserId(null);
        this.setMailApprovalUserName(null);
    }

    public  void onMailRequest(boolean request) {
        this.setMailRequest(request);
        this.create("2");
    }

    private void loadCreate(String type,String issueId) {
        this.server = getOneServer();
        loadInfo();
        if(this.server == null) this.server = new Server();
        if("1".equals(type)) {
            Issue issue = StringUtils.isBlank(issueId)?null:this.issueController.getIssue(Integer.valueOf(issueId));
            this.setTos(new ArrayList<>());
            if(issue != null) {
                if(issue.getCustomerList() != null && issue.getCustomerList().size() > 0) {
                    this.setCustomerName(issue.getCustomerList().get(0).getCustFullHira());
                    for(Customer cust:issue.getCustomerList()) {
                        if(cust == null || cust.getCustTargetInfoList() == null) continue;
                        for(CustTargetInfo target:cust.getCustTargetInfoList()) {
                            if(target == null
                                    || TARGET.MAIL != target.getCustFlagType()
                                    || StringUtils.isBlank(target.getCustTargetData())) continue;
                            this.tos.add(target);
                        }
                    }
                }
                this.escalation = this.escalationService.findEscalationByIssueId(issue.getIssueId()
                        , UserModel.getLogined().getUserId()
                        , (short)1
                        , ISSUE_TYPE.SUPPORT);
                if(this.escalation != null) {
                    this.supportMail.getSendMail().setFrom(this.escalation.getEscalationFromEmail());
                    this.supportMail.setTo(this.escalation.getEscalationTo());
                    this.supportMail.getSendMail().setSubject(this.escalation.getEscalationTitle());
                    this.supportMail.setHeader(this.escalation.getEscalationHeader());
                    this.supportMail.getSendMail().setMessage(this.escalation.getEscalationBody());
                    this.supportMail.setFooter(this.escalation.getEscalationFooter());
                    if(this.escalation.getEscalationRequestId() != null) {
                        Member user = this.memberService.find(this.escalation.getEscalationRequestId());
                        if(user != null) {
                            this.setMailRequestUserId(user.getMemberId());
                            this.setMailRequestUserName(user.getMemberNameFirst() + " " + user.getMemberNameLast());
                            this.setMailRequestDate(this.escalation.getEscalationRequestDate());
                        }
                    } else {
                        this.setMailRequestUserId(null);
                        this.setMailRequestUserName(null);
                        this.setMailRequestDate(null);
                    }
                    if(this.escalation.getEscalationCc() != null) {
                        Member user = this.memberService.find(Integer.valueOf(this.escalation.getEscalationCc()));
                        if(user != null) {
                            this.setMailApprovalUserId(user.getMemberId());
                            this.setMailApprovalUserName(user.getMemberNameFirst() + " " + user.getMemberNameLast());
                        }
                    } else {
                        deleteLoginUserName();
                    }
                    List<Attachment> attachs = this.attachmentService.search(
                            UserModel.getLogined().getCompanyId(), AttachmentTargetType.ISSUE.getId(), this.escalation.getEscalationId(), UN_DELETED);
                    if(attachs == null || this.server == null) return;
                    WebFileUtil.loadFileFromFtp(this.supportMail, attachs, this.server);
                } else {
                    deleteLoginUserName();
                    this.setMailRequestUserId(null);
                    this.setMailRequestUserName(null);
                    this.setMailRequestDate(null);
                    EscalationSample escs =
                            this.issueEscalationSampleService.getEscalationSampleByTypeIdAndTargetId(
                                    ISSUE_TYPE.SUPPORT
                                    ,null
                                    ,UserModel.getLogined().getCompanyId()
                                    ,UserModel.getLogined().getLanguage());
                    if(escs != null) {
                        this.supportMail.getSendMail().setSubject(escs.getSampleSubject());
                        this.supportMail.setHeader(escs.getSampleHeader());
                        this.supportMail.getSendMail().setMessage(escs.getSampleBody());
                        this.supportMail.setFooter(escs.getSampleFotter());
                    }
                }
            }
        } else {
            if(this.requestTypes != null && !this.requestTypes.isEmpty()) {
                if(NumberUtils.isDigits(String.valueOf(this.requestTypes.get(0).getValue()))) {
                    EscalationSample escs =
                            this.issueEscalationSampleService.getEscalationSampleByTypeIdAndTargetId(
                                    ISSUE_TYPE.REQUEST
                                    ,(Integer) this.requestTypes.get(0).getValue()
                                    ,UserModel.getLogined().getCompanyId()
                                    ,UserModel.getLogined().getLanguage());
                    if(escs != null) {
                        this.requestMail.getSendMail().setSubject(escs.getSampleSubject());
                        this.requestMail.getSendMail().setMessage(escs.getSampleBody());
                        this.requestMail.setFooter(escs.getSampleFotter());
                    }
                }
            }
        }
    }

    private void loadInfo() {
        this.setFroms(new ArrayList<>());
        this.froms = this.mailAccountService.getSendAccountList(UserModel.getLogined().getCompanyId());
        if(this.froms != null && this.froms.size() > 0) {
            String currentAcount = this.memberService.getUserMailFristByUserId(UserModel.getLogined().getUserId());
            if(EmailUtil.getAccount(this.getFroms(), currentAcount) == null && StringUtils.isNotBlank(currentAcount)) {
                MailAccount u = new MailAccount();
                u.setAccountMailAddress(currentAcount);
                if(this.froms != null && this.froms.size() > 0) {
                    u.setAccountName(this.froms.get(0).getAccountName());
                    u.setAccountPassword(this.froms.get(0).getAccountPassword());
                    u.setMailServer(this.froms.get(0).getMailServer());
                }
                this.froms.add(u);
            }
        }

        deleteLoginUserName();
        this.supportMail = new MailMode();
        this.requestMail = new MailMode();
        this.escalation = new Escalation();
        this.requestType = null;
        if(!this.froms.isEmpty()){
            this.supportMail.getSendMail().setFrom(this.froms.get(0).getAccountMailAddress());
        }
        this.requestTypes =
                this.issueService.getList(
                        COLS.MAIL_REQUEST
                        , UserModel.getLogined().getCompanyId()
                        , UserModel.getLogined().getLanguage());
        if(this.requestTypes != null && !this.requestTypes.isEmpty()) {
            this.setRequestType(String.valueOf(this.requestTypes.get(0).getValue()));
        }

        //List<String> fields = new ArrayList<>();
        this.select = new HashMap<>();
        /** 商品（大）分類 */
        //fields.add(COLS.EXAPLE_SENTENCE);
        /** 全てプルダウンがリストを追加 */
        //SelectUtil.addSelectItems(this.select, fields, this.issueService, UserModel.getLogined().getCompanyId(), UserModel.getLogined().getLanguage());
        this.select.put(COLS.EXAPLE_SENTENCE, issueController.getSelect().get(COLS.EXAPLE_SENTENCE));
        //ields.add(COLS.EXAPLE_SENTENCE + "_2");
        //fields.add(COLS.EXAPLE_SENTENCE + "_3");

        this.escalationSamples = this.menteServiceImpl.getAllStaticLevel(COLS.SIGNATURE, UserModel.getLogined().getCompanyId());
        
        //reset deleted attachs
        listDeletedAttachs.clear();
    }

    @SuppressWarnings("UnusedAssignment")
    private Escalation insertUpdateEscalation(MailMode mail, Short escalationType, String errorMsg, short save, Member user, Issue issue)
            throws Exception {
        /** 履歴情報テーブルに追加 */
        mail.setDate(DateUtil.now());
        Escalation esc = new Escalation();
        if(this.getEscalation() != null && this.escalation.getEscalationId() != null) {
            BeanUtils.copyProperties(this.getEscalation(), esc);
        }
        esc = EmailUtil.getEscalation(esc, mail, issue, user, escalationType);
        if(escalationType == ISSUE_TYPE.SUPPORT) {
            if(esc.getEscalationId() == null) {
                esc.setEscalationSendType(escalationType);
                esc.setCreatorId(UserModel.getLogined().getMember());
                esc.setCreatedTime(DateUtil.now());
            }
            if(mail.getSendMail().getBcc() != null && mail.getSendMail().getBcc().length == 1 && NumberUtils.isDigits(mail.getSendMail().getBcc()[0])) {
                esc.setEscalationRequestId(Integer.valueOf(mail.getSendMail().getBcc()[0]));
                esc.setEscalationRequestDate(mail.getDate());
            }
            esc.setEscalationIsSaved(save);
            esc.setUpdatedId(UserModel.getLogined().getUserId());
            esc.setUpdatedTime(DateUtil.now());
            if(esc.getEscalationId() != null && esc.getEscalationId() > 0) {
                this.escalationService.edit(esc);
            } else {
                this.escalationService.create(esc);
            }
        } else {
            esc.setEscalationSendType(escalationType);
            esc.setEscalationIsSaved((short)0);
            esc.setEscalationTo((mail.getSendMail().getRecipient() == null || mail.getSendMail().getRecipient().length == 0 )?null:mail.getSendMail().getRecipient()[0]);
            esc.setEscalationCc((mail.getSendMail().getCc() == null || mail.getSendMail().getCc().length == 0)?null:mail.getSendMail().getCc()[0]);
            esc.setEscalationBcc((mail.getSendMail().getBcc() == null || mail.getSendMail().getBcc().length == 0)?null:mail.getSendMail().getBcc()[0]);
            esc.setCreatorId(UserModel.getLogined().getMember());
            esc.setCreatedTime(DateUtil.now());
            esc.setUpdatedId(UserModel.getLogined().getUserId());
            esc.setUpdatedTime(DateUtil.now());
            this.escalationService.create(esc);
//            this.escalationService.flush();
        }

        if(save != 1) {
            JsfUtil.getResource().alertMsgInfo(errorMsg, ResourceUtil.BUNDLE_ISSUE_NAME, "label.send.mail", ResourceUtil.BUNDLE_ISSUE_NAME);
            return esc;
        }else{
            JsfUtil.getResource().alertMsgInfo(errorMsg, ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.save", ResourceUtil.BUNDLE_ISSUE_NAME);
            return esc;
        }
    }
    
    @Getter @Setter private Map<Long, SelectItem> exampleDialogSelected = new HashMap<>();

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onLinkDoubleSelect() {
        String val = StringUtils.isBlank(this.supportMail.getSendMail().getMessage()) ? "" : this.supportMail.getSendMail().getMessage();
        this.supportMail.getSendMail().setMessage(val + "\r\n" + exampleDialogSelected.get(3L).getLabel());
        RequestContext context = RequestContext.getCurrentInstance();
        context.closeDialog(0);
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onLinkSelect(SelectItem src, Long level) {
        exampleDialogSelected.put(level, src);
        for (long i = level+1; i <= exampleDialogSelected.size(); i++) {
            exampleDialogSelected.remove(i);
        }
    }

    public void onChangeRequestType(AjaxBehaviorEvent event) {
        SelectOneMenu selectMenu = (SelectOneMenu) event.getSource();
        if(selectMenu == null) return;
        String id = String.valueOf(selectMenu.getValue());
        if(!NumberUtils.isDigits(id)) return;
        EscalationSample escs =
        this.issueEscalationSampleService.getEscalationSampleByTypeIdAndTargetId(
                ISSUE_TYPE.REQUEST
                ,Integer.valueOf(id)
                ,UserModel.getLogined().getCompanyId()
                ,UserModel.getLogined().getLanguage());
        if(escs != null) {
            this.requestMail.getSendMail().setSubject(escs.getSampleSubject());
            this.requestMail.getSendMail().setMessage(escs.getSampleBody());
            this.requestMail.setFooter(escs.getSampleFotter());
        } else {
            this.requestMail = new MailMode();
        }
    }

    /** TextTextareaを取得 */
    public void addSelectedText() {
        String val = HTTPResReqUtil.getRequestParameter("selectedText");
        if(StringUtils.isBlank(val)) return;
        String valOld = StringUtils.isBlank(this.supportMail.getSendMail().getMessage())?"":this.supportMail.getSendMail().getMessage();
        String msg = StringUtils.trim(valOld + "\r\n" + val);
        this.supportMail.getSendMail().setMessage(msg);
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onSelectEscalationSample() {
        String escSampleId = HTTPResReqUtil.getRequestParameter("escSampleId");
        Integer escId = NumberUtils.toInt(escSampleId, 0);
        EscalationSample esc = issueEscalationSampleService.getEscalationSampleByTypeIdAndTargetId(ISSUE_TYPE.SIGNATURE, escId, UserModel.getLogined().getCompanyId(), localeController.getLocale());
        String val = "";
        
        if(esc != null){
            val = esc.getSampleBody();
        }
        
        if(StringUtils.isEmpty(val)) return;
        String valOld = StringUtils.isBlank(this.supportMail.getSendMail().getMessage())?"":this.supportMail.getSendMail().getMessage();
        String msg = StringUtils.trim(valOld + "\r\n\r\n>" + val);
        this.supportMail.getSendMail().setMessage(msg);
    }

    private boolean chkFormSupport() {
        if(StringUtils.isBlank(this.supportMail.getSendMail().getSubject())) {
            JsfUtil.getResource().alertMsg("label.mail.subject", ResourceUtil.BUNDLE_MSG, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        if(this.supportMail.getSendMail().getSubject() != null
                && this.supportMail.getSendMail().getSubject().length() > 100) {
            JsfUtil.getResource().alertMsgMaxLength("label.mail.subject", ResourceUtil.BUNDLE_MSG, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 100);
            return false;
        }
        if(this.supportMail.getHeader() != null
                && this.supportMail.getHeader().length() > 300) {
            JsfUtil.getResource().alertMsgMaxLength("label.mail.header", ResourceUtil.BUNDLE_MAIL_NAME, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 300);
            return false;
        }
        if(StringUtils.isBlank(this.supportMail.getSendMail().getMessage())) {
            JsfUtil.getResource().alertMsg("label.mail.comment", ResourceUtil.BUNDLE_MSG, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        if(this.supportMail.getSendMail().getMessage() != null
                && this.supportMail.getSendMail().getMessage().length() > 20000) {
            JsfUtil.getResource().alertMsgMaxLength("label.mail.comment", ResourceUtil.BUNDLE_MSG, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 20000);
            return false;
        }
        if(this.supportMail.getFooter()!= null
                && this.supportMail.getFooter().length() > 500) {
            JsfUtil.getResource().alertMsgMaxLength("label.mail.footer", ResourceUtil.BUNDLE_MAIL_NAME, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 500);
            return false;
        }
        return true;
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onTargetMailFlag(String target) {
        if(StringUtils.isBlank(target)) {
            return;
        }
        List<SelectItem> tItems = new ArrayList<>();
        if(null != target) switch (target) {
            case "to":
                tItems = this.requestMail.getTos();
                break;
            case "cc":
                tItems = this.requestMail.getCcs();
                break;
            case "bcc":
                tItems = this.requestMail.getBccs();
                break;
            default:
                break;
        }

        List<SelectItem> nItems = new ArrayList<>();
        List<SelectItem> items = this.issueController.getSelect().get(COLS.USER);
        for(SelectItem item:items) {
            if(item == null
                    || StringUtils.isBlank(SelectUtil.nullStringToEmpty(item.getDescription()))
                    || StringUtil.isExistsInList((Integer) item.getValue(), tItems)) continue;
            nItems.add(item);
        }
        this.members = new DualListModel<>(nItems, tItems);
        this.setTargetMailTarget(target);
    }

    public void onChangeGroup(NodeSelectEvent event) {
        TreeNode node = event.getTreeNode();
        Group group = (Group)node.getData();
        
        List<SelectItem> newItems = new ArrayList<>();
        if(null != this.targetMailTarget) switch (this.targetMailTarget) {
            case "to":
                newItems = this.requestMail.getTos();
                break;
            case "cc":
                newItems = this.requestMail.getCcs();
                break;
            case "bcc":
                newItems = this.requestMail.getBccs();
                break;
            default:
                return;
        }
        this.members.setTarget(newItems);
        List<Integer> targetMemberId = new ArrayList<>();
        newItems.forEach((i) -> {
            targetMemberId.add(NumberUtils.toInt(i.getValue().toString()));
        });
        this.members.setSource(new ArrayList<>());
        
        List<Member> lstMemberOnSlave = multitenancyService.findMemberOnSlaveByGroupId(UserModel.getLogined().getCompanyId(), group.getGroupId());
        lstMemberOnSlave.forEach((m) -> {
            if(!targetMemberId.contains(m.getMemberId())){
                String mail = this.memberService.getUserMailFristByUserId(m.getMemberId());
                if( !StringUtils.isEmpty(mail) ){
                    this.members.getSource().add(new SelectItem(m, m.getMemberNameFull(), mail));
                }
            }
        });
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void updatePickList() {
        if(this.requestMail.getTos().isEmpty() && this.members.getTarget().isEmpty()) {
            JsfUtil.getResource().alertMsg("label.mail.person.member", ResourceUtil.BUNDLE_MAIL_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
            return;
        }
        if(null != this.targetMailTarget) switch (this.targetMailTarget) {
            case "to":
                this.requestMail.setTos(this.members.getTarget());
                break;
            case "cc":
                this.requestMail.setCcs(this.members.getTarget());
                break;
            case "bcc":
                this.requestMail.setBccs(this.members.getTarget());
                break;
            default:
                break;
        }
        HTTPResReqUtil.setCloseDialog();
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onTransferMember(TransferEvent event) {
        List<SelectItem> existsItems = new ArrayList();
        if(null != this.targetMailTarget) switch (this.targetMailTarget) {
            case "to":
                existsItems = this.requestMail.getTos();
                break;
            case "cc":
                existsItems = this.requestMail.getCcs();
                break;
            case "bcc":
                existsItems = this.requestMail.getBccs();
                break;
        }
        for(Object item : event.getItems()) {
            if(item instanceof SelectItem){
                boolean exists = false;
                for (Iterator<SelectItem> iterator = existsItems.iterator(); iterator.hasNext();) {
                    SelectItem si = iterator.next();
                    if(si.getValue().equals(((SelectItem)item).getValue())){
                        exists = true;
                        iterator.remove();
                        break;
                    }
                }
                if(!exists){
                    existsItems.add((SelectItem)item);
                }
            }
       }
    } 

    private boolean chkFormRequest() {
        if(StringUtils.isBlank(this.requestMail.getSendMail().getSubject())) {
            JsfUtil.getResource().alertMsg("label.mail.subject", ResourceUtil.BUNDLE_MSG, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        if(this.requestMail.getSendMail().getSubject() != null
                && this.requestMail.getSendMail().getSubject().length() > 100) {
            JsfUtil.getResource().alertMsgMaxLength("label.mail.subject", ResourceUtil.BUNDLE_MSG, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 100);
            return false;
        }
        if(StringUtils.isBlank(this.requestMail.getSendMail().getMessage())) {
            JsfUtil.getResource().alertMsg("label.request_comment", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        if(this.requestMail.getSendMail().getMessage() != null
                && this.requestMail.getSendMail().getMessage().length() > 20000) {
            JsfUtil.getResource().alertMsgMaxLength("label.request_comment", ResourceUtil.BUNDLE_ISSUE_NAME, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 20000);
            return false;
        }
        return true;
    }
    
    public TreeNode getGroupTree() {
        try {
            List<Group> dataSource = multitenancyService.findAllGroupUnderSlave(UserModel.getLogined().getCompanyId());
            TreeNode root = new DefaultTreeNode();
            dataSource.forEach((group) -> {
                if(group.getParent() == null){
                    TreeNode node = new DefaultTreeNode(group, root);
                    node.setExpanded(true);
                    if(!group.getChilds().isEmpty()){
                        this.addItemToGroupTree(node, group.getChilds());
                    }
                }
            });
            return root;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
    
    private void addItemToGroupTree(TreeNode parent, List<Group> group){
        group.forEach((item) -> {
            TreeNode node = new DefaultTreeNode(item, parent);
            node.setExpanded(true);
            if(!item.getChilds().isEmpty()){
                this.addItemToGroupTree(node, item.getChilds());
            }
        });
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void openSampleDialog(){
        exampleDialogSelected.clear();
        try {
            String val = HTTPResReqUtil.getRequestParameter("openDialogURL");
            String top = HTTPResReqUtil.getRequestParameter("top");
            String width = HTTPResReqUtil.getRequestParameter("width");
            String height = HTTPResReqUtil.getRequestParameter("height");
            if(!StringUtils.isBlank(val)){
                Map<String,Object> options = new HashMap<>();
                options.put("draggable", true);
                options.put("resizable", false);
                if(NumberUtils.isDigits(top)) {
                    options.put("top", top);
                }
                if(NumberUtils.isDigits(width)) {
                    options.put("contentWidth", width);
                }
                if(NumberUtils.isDigits(height)) {
                    options.put("contentHeight", height);
                }
                options.put("includeViewParams", true);
                DialogObject.openDialog(val, options);
            }
        } catch (Exception ex) {
            LOGGER.error("[DialogController.openDialog()]", ex);
        }
//        return StringUtils.EMPTY;
    }
}
