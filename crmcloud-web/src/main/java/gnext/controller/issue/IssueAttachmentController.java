package gnext.controller.issue;

import com.google.gson.Gson;
import gnext.bean.Company;
import gnext.bean.Member;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.controller.common.LayoutController;
import gnext.controller.customize.RenderController;
import gnext.bean.issue.IssueAttachment;
import gnext.bean.mente.MenteItem;
import gnext.bean.softphone.Twilio;
import gnext.bean.softphone.TwilioConfig;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.MemberService;
import gnext.service.attachment.ServerService;
import gnext.service.issue.IssueAttachmentService;
import gnext.service.mente.MenteService;
import gnext.service.softphone.TwilioConfigService;
import gnext.util.DateUtil;
import gnext.util.HTTPResReqUtil;
import gnext.util.JsfUtil;
import static gnext.util.JsfUtil.executeClientScript;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import gnext.utils.MapObjectUtil;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 *
 * @author hungpham
 * @since Jan 16, 2017
 */
@ManagedBean(name = "issueAttachmentController")
@SessionScoped()
@SecurePage(module = SecurePage.Module.ISSUE)
public class IssueAttachmentController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(IssueAttachmentController.class);
    
    private final String CATEGORY_ID = "attachment_category_id";
    
    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;

    @ManagedProperty(value = "#{renderController}")
    @Getter @Setter private RenderController renderController;

    @ManagedProperty(value = "#{issueController}")
    @Getter @Setter private IssueController issueController;

    @EJB private MenteService categoryServiceImpl;
    @EJB private IssueAttachmentService issueAttachmentService;
    @EJB private TwilioConfigService twilioConfigService;
    @EJB private MemberService memberService;
    @EJB private ServerService serverService;
    
    @Getter @Setter private IssueAttachment issueAttachment;
    @Getter @Setter private Integer currentItemIndex;
    @Getter private List<MenteItem> categoryList = new ArrayList<>();
    
    /////////////////// Search ////////////////
    @Getter @Setter private String idSearch;
    @Getter @Setter private Integer categoryIdSearch;
    @Getter @Setter private Date fromDateSearch;
    @Getter @Setter private Date toDateSearch;
    @Getter @Setter private String creatorIdSearch;
    @Getter @Setter private String callerPhoneSearch;
    @Getter @Setter private List<Member> creatorAvailableListSearch;
    @Getter @Setter private List<Twilio> list = new ArrayList<>();
    
    @PostConstruct
    public void init(){
        categoryList = categoryServiceImpl.getAllStaticLevel(CATEGORY_ID, UserModel.getLogined().getCompanyId());
        List<TwilioConfig> listTwilioConfig = twilioConfigService.getByCompanyId(UserModel.getLogined().getCompanyId());
        List<Integer> allowMemberIdList = new ArrayList<>();
        listTwilioConfig.forEach((cfg) -> {
            try{
                List<Double> tmpAllowMemberIdList = new Gson().fromJson(cfg.getAllowMemberList(), List.class);
                tmpAllowMemberIdList.forEach((item) -> {
                    allowMemberIdList.add(item.intValue());
                });
            }catch(Exception e){}
        });
        creatorAvailableListSearch = memberService.findListByIds(allowMemberIdList);
    }
    
    public String getCreatorName(String loginId){
        for(Member m : creatorAvailableListSearch){
            if(m.getMemberLoginId().equals(loginId)){
                return m.getMemberNameFirst() + " " + m.getMemberNameLast();
            }
        }
        return loginId;
    }
    
    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void showForm(IssueAttachment item, Integer index){
        if(item == null){
            this.issueAttachment = new IssueAttachment();
            this.issueAttachment.setIssue(this.issueController.getIssue());
            currentItemIndex = null;
        }else{
            this.issueAttachment = item;
            this.currentItemIndex = index;
        }
    }

    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void insert(String mode) {
        try {
            if(!chkFormRequest()) return;
            this.issueAttachment.setAttachmentDeleted(StatusUtil.UN_DELETED);
            this.issueAttachment.setCreator(UserModel.getLogined().getMember());
            this.issueAttachment.setCreatedTime(DateUtil.now());
            this.issueAttachment.setUpdatedId(UserModel.getLogined().getUserId());
            this.issueAttachment.setUpdatedTime(DateUtil.now());
            if(this.issueAttachment.getUploadedFile() != null){
                UploadedFile f = (UploadedFile)this.issueAttachment.getUploadedFile();
                this.issueAttachment.setAttachment(uploadMediaFileToFtpServer(this.issueAttachment.getIssue().getIssueId(), f.getFileName(), f.getInputstream(), UserModel.getLogined().getCompanyId()));
            }
            if("view".equals(mode)){
                this.issueAttachment = this.issueAttachmentService.create(this.issueAttachment);
            }
            this.issueController.getIssue().getIssueAttachmentList().add(issueAttachment);
            executeClientScript("PF('attachmentDialog').hide()");
            JsfUtil.getResource().alertMsgInfo("label.attachment.dialog", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.add", ResourceUtil.BUNDLE_ISSUE_NAME);
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }
        HTTPResReqUtil.setCloseDialog();
    }

    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void update() {
        try {
            if(!chkFormRequest()) return;
            if(this.issueAttachment.getAttachmentId() != null){
                this.issueAttachment.setUpdatedId(UserModel.getLogined().getUserId());
                this.issueAttachment.setUpdatedTime(DateUtil.now());
            }else{
                this.issueAttachment.setCreator(UserModel.getLogined().getMember());
                this.issueAttachment.setCreatedTime(DateUtil.now());
            }
            
            if(this.issueAttachment.getUploadedFile() != null){
                UploadedFile f = (UploadedFile)this.issueAttachment.getUploadedFile();
                this.issueAttachment.setAttachment(uploadMediaFileToFtpServer(this.issueAttachment.getIssue().getIssueId(), f.getFileName(), f.getInputstream(), UserModel.getLogined().getCompanyId()));
            }
            
            this.issueController.getIssue().getIssueAttachmentList().set(this.currentItemIndex, issueAttachment);
            executeClientScript("PF('attachmentDialog').hide()");
            JsfUtil.getResource().alertMsgInfo("label.attachment.dialog", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.update", ResourceUtil.BUNDLE_ISSUE_NAME);
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }
        HTTPResReqUtil.setCloseDialog();
    }

    @SecureMethod(value=SecureMethod.Method.DELETE)
    public void delete(IssueAttachment item, Integer index) {
        this.issueController.getIssue().getIssueAttachmentList().remove(index.intValue());
        JsfUtil.getResource().alertMsgInfo("label.attachment.dialog", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.delete", ResourceUtil.BUNDLE_ISSUE_NAME);
    }

    private boolean chkFormRequest() {
        if(this.issueAttachment.getAttachment() == null && this.issueAttachment.getUploadedFile() == null
                && this.issueAttachment.getTwilio() == null) {
            JsfUtil.getResource().alertMsg("label.attachment.name", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        return true;
    }
    
    /************ UPLOAD FILE TO FPT
     * @param event **************/
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void fileUploadListener(FileUploadEvent event) {
        this.issueAttachment.setUploadedFile(event.getFile());
    }
    
    private Attachment uploadMediaFileToFtpServer(Integer issueId, String fileName, InputStream inputstream, Integer companyId) throws Exception {
        
        List<Server> servers = serverService.getAvailable(companyId, TransferType.FTP.getType(), ServerFlag.SOUND.getId());
        if(servers == null || servers.isEmpty()) return null;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy"+File.separator+"MM"+File.separator+"dd"+File.separator);
        
        Server server = servers.get(0);
        String path = server.getServerFolder();
        path = path + File.separator + companyId + File.separator + AttachmentTargetType.SOUND.getName() + File.separator + sdf.format(new Date());
        String host = server.getServerHost();
        int port = server.getServerPort();
        String username = server.getServerUsername();
        String password = server.getDecryptServerPassword();
        boolean security = getBoolean(server.getServerSsl());
        String protocol = server.getServerProtocol();
        
        Parameter param_ftp = Parameter.getInstance(TransferType.FTP).manualconfig(true).storeDb(false);
        param_ftp.host(host).port(port).username(username).password(password).security(security).protocol(protocol)
                .uploadfilename(fileName + ".mp3").uploadpath(path).createfolderifnotexists();
        
        FileTransferFactory.getTransfer(param_ftp).upload(inputstream);
        
        gnext.dbutils.model.Attachment attachment = param_ftp.getAttachment();
        if(attachment == null) throw new Exception("Upload recording error.");
        Attachment ea = MapObjectUtil.convert(attachment);
        ea.setAttachmentTargetType(AttachmentTargetType.SOUND.getId());
        ea.setAttachmentTargetId(issueId);
        ea.setServer(server);
        ea.setCompany(new Company(companyId));
        ea.setCreatorId(1);
        
        return ea;
    }
    
    private boolean getBoolean(Short flag) {
        return (flag != null && flag == 1);
    }
    
    ///////////// Search ////////////////////
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void onDateSelect(SelectEvent event) {
        fromDateSearch = (Date)event.getObject();
    }
    
    @SecureMethod(value=SecureMethod.Method.SEARCH)
    public void search(){
        callerPhoneSearch = StringUtils.isEmpty(callerPhoneSearch) ? "" : callerPhoneSearch;
        list = issueAttachmentService.search(idSearch, categoryIdSearch, fromDateSearch, toDateSearch, creatorIdSearch, callerPhoneSearch, UserModel.getLogined().getCompanyId());
    }
}
