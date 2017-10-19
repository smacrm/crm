/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail.common;

import com.mysql.jdbc.StringUtils;
import gnext.bean.Company;
import gnext.bean.MailAccount;
import gnext.bean.mail.MailServer;
import gnext.bean.issue.Issue;
import gnext.model.FileUploader;
import gnext.model.authority.UserModel;
import gnext.resource.bundle.MailBundle;
import gnext.security.annotation.SecureMethod;
import gnext.service.MemberService;
import gnext.service.issue.IssueService;
import gnext.service.mail.MailAccountService;
import gnext.util.HTTPResReqUtil;
import gnext.util.StatusUtil;
import gnext.utils.InterfaceUtil.COLS;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "sendEmailController")
@SessionScoped()
public class SendEmailController implements Serializable {
    private static final long serialVersionUID = -4249917401198090330L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SendEmailController.class);
    
    @Getter @Setter private List<SelectItem> lstMailTo = new ArrayList<>();
    @Getter @Setter private List<SelectItem> lstMailRequestType = new ArrayList<>();
    @Getter @Setter private Integer mailToId;
    @Getter @Setter private Integer mailRequestType;
    @Getter @Setter private String subject;
    @Getter @Setter private String body;
    @Getter @Setter private Issue issue;
    @Getter @Setter private Integer issueId;
    @Setter @Getter private FileUploader fileUploader;
    
    @EJB @Getter @Setter private MailAccountService mailAccountService;
    @EJB @Getter @Setter private MemberService memberService;
    @EJB @Getter @Setter private IssueService issueService;
    
    @ManagedProperty(value = "#{mailBundle}")
    @Getter @Setter private MailBundle mailBundle;
    
    @PostConstruct
    public void init() {
        LOGGER.info("Initialize SendEmailController...");
        fileUploader = new FileUploader();
    }
    
    private Company getCompany() {
        return UserModel.getLogined().getCompany();
    }
    
    private Integer getCid() {
        return getCompany().getCompanyId();
    }
    
    private Integer getLoginId() {
        return UserModel.getLogined().getUserId();
    }
    
    private String getLang() {
        return UserModel.getLogined().getLanguage();
    }
    
    private void _load() {
        if(this.issueId == null) return;
        lstMailTo.clear();lstMailRequestType.clear();fileUploader.reset();
        List<MailAccount> accs = mailAccountService.search(getCid());
        for(MailAccount ma : accs) {
            if(StatusUtil.getBoolean(ma.getAccountIsDeleted())) continue;
            lstMailTo.add(new SelectItem(ma.getAccountId(), ma.getAccountMailAddress()));
        }
        issue = issueService.findByIssueId(this.issueId, UserModel.getLogined().getLanguage());
        subject = "対応依頼:"+issue.getIssueViewCode();
        List<SelectItem> requesttypes = issueService.getList(COLS.PROPOSAL, getCid(), getLang());
        lstMailRequestType.addAll(requesttypes);
    }
    
    public void onRequestTypeChange(final AjaxBehaviorEvent event)  {
        if(StringUtils.isNullOrEmpty(body)) body = "";
        if(body.contains(mailBundle.getString("label.sent.dialog.from_issue.request_type")))
            body = body.substring(body.indexOf("\n") + 1);
        for (SelectItem item : lstMailRequestType) {
            if(item.getValue().equals(mailRequestType)) {
                body = mailBundle.getString("label.sent.dialog.from_issue.request_type") + ":" + item.getLabel() + "\n\n" + body;
            }
        }
    }
    
    /***
     * using this method to open the dialog send email.
     */
    public void sendEmailFromIssue() {
        issueId = Integer.parseInt(HTTPResReqUtil.getRequestParameter("issue_id"));
        _load();
        Map<String,Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("width", 640);
        options.put("height", 500);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        RequestContext.getCurrentInstance().openDialog("/modules/mail/common/dialog/sendemail_issue", options, null);
    }
    
    public void onSendEmail() {
        if(mailToId == null) return;
        MailAccount macc = mailAccountService.find(mailToId);
        if(macc == null) return;
        MailServer ms = macc.getMailServer();
        Map<String, String> conf = buildConf(macc, ms);
        _sendMail(conf);
    }
    
    private Map<String, String> buildConf(MailAccount macc, MailServer ms) {
        Map<String, String> conf = new HashMap();
        conf.put("action", "send");
        conf.put("host", ms.getServerHost());
        conf.put("port", ms.getServerSmtpPort().toString());
        conf.put("user", macc.getAccountUserName());
        conf.put("pass", macc.getAccountPassword());
        conf.put("ssl", Boolean.valueOf(ms.getServerSsl()).toString());
        conf.put("auth", Boolean.valueOf(ms.getServerAuth()).toString());
        conf.put("from", macc.getAccountMailAddress());
        conf.put("to", "daind1@vnext.vn");
        conf.put("subject", this.subject);
        conf.put("body", this.body);
        conf.put("db_path", "/mnt/email/db/mail.properties");
        return conf;
    }
    
    private void _sendMail(Map<String, String> conf) {
        try {
            if(conf == null || conf.isEmpty()) return;
//            MailData p_md = MailClient._SendMail(conf);
//            if(p_md != null) {
//                MailData md = ModelUtil.convertFrom(p_md);
//                md.setCompany(getCompany());
//                md.setCreatorId(getLoginId());
//
//                // FIXME need to using ejb bean to saving to database.
//                md.setMailDataId(12);
//
//                // param for ftp protocol
//                Parameter param_ftp = Parameter.getInstance(TransferType.FTP)
//                        .serverid(33)
//                        .uploadfilename("abc.png")
//                        .createfolderifnotexists()
//                        .attachmentTargetType(AttachmentTargetType.MAIL)
//                        .attachmentTargetId(md.getMailDataId());
//                FileTransferFactory.getTransfer(param_ftp).upload(new ByteArrayInputStream(fileUploader.getStreamBytes()));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void handleFileUpload(FileUploadEvent event) {
        fileUploader.load(event.getFile());
    }
}
