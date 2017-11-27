/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail;

import gnext.mailapi.MailClient;
import gnext.bean.MailAccount;
import gnext.bean.mail.MailServer;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.mailapi.EmailBuilder;
import gnext.mailapi.datastructure.TreeFolder;
import gnext.mailapi.mail.ReadEmail;
import gnext.mailapi.mail.SendEmail;
import gnext.model.authority.UserModel;
import gnext.model.mail.MailAccountLazyList;
import gnext.model.mail.MailAccountModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.mail.MailAccountService;
import gnext.service.mail.MailServerService;
import gnext.util.DateUtil;
import gnext.mailapi.util.FolderUtil;
import gnext.mailapi.util.InterfaceUtil;
import gnext.util.JsfUtil;
import gnext.mailapi.util.ParameterUtil;
import gnext.util.ExceptionUtils;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.event.ActionEvent;
import javax.mail.Store;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "mailAccountController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.MAIL, require = true)
public class MailAccountController extends AbstractController {
    private static final long serialVersionUID = 8032502569181502876L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MailAccountController.class);

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @EJB @Getter @Setter private MailAccountService mailAccountService;
    @EJB @Getter @Setter private MailServerService mailServerService;

    @Getter @Setter private String query;
    @Getter @Setter private LazyDataModel<MailAccountModel> models;
    @Getter @Setter private MailAccountModel model;
    @Getter @Setter private Integer selectMailAccountId;
    @Getter @Setter private List<MailServer> mailServers;
    
    private String oldMailAccountAddress;
    private String oldMailAccountName;

    private int _GetCompanyId() {
        return UserModel.getLogined().getCompanyId();
    }

    private void _Load(Integer accountId) {
        this.mailServers = mailServerService.search(_GetCompanyId(), Boolean.FALSE);
        if (accountId != null) {
            MailAccount mailAccount = mailAccountService.find(accountId);
            this.model = new MailAccountModel(mailAccount);
            this.model.setAccountDeleteReceivedDays(mailAccount.getAccountDeleteReceivedDays() != null);
        } else {
            this.model = new MailAccountModel();
        }
    }

    @PostConstruct
    public void init() {
        models = new MailAccountLazyList(this);
        query = "1=1 and ma.company.companyId=" + _GetCompanyId();
    }

    @SecureMethod(value=SecureMethod.Method.SEARCH) public void reload() {}
    @Override
    public void doSearch(SearchFilter filter) {
        query = "1=1 AND ma.company.companyId=" + _GetCompanyId() + " AND ";
        query = query + (filter != null && !StringUtils.isEmpty(filter.getQuery()) ? filter.getQuery() : "1=1");
    }
    
    @SecureMethod(value = SecureMethod.Method.PRINT) public void print(){}
    
    @Override
    @SecureMethod(value=SecureMethod.Method.DOWNLOAD)
    public void download(String tblName, String fileName) {
        super.download(tblName, fileName);
    }
    
    @Override
    protected void afterPaging() {
        models.setRowIndex(currentRowNum);
        model = models.getRowData();
    }

    @SecureMethod(SecureMethod.Method.VIEW)
    @Override
    public void show(ActionEvent event) {
        final String showType = this.getParameter("showType");
        if ("view".equals(showType)) {
            this.currentRowNum = Integer.parseInt(this.getParameter("rowNum"));
            _Load(Integer.parseInt(this.getParameter("accountId")));
            this.layout.setCenter("/modules/mail/account/view.xhtml");
        } else if ("edit".equals(showType)) {
            _Load(Integer.parseInt(this.getParameter("accountId")));
            oldMailAccountName = model.getMailAccount().getAccountName();
            oldMailAccountAddress = model.getMailAccount().getAccountMailAddress();
            
            this.layout.setCenter("/modules/mail/account/edit.xhtml");
        } else if ("create".equals(showType)) {
            _Load(null);
            this.layout.setCenter("/modules/mail/account/create.xhtml");
        }
    }

    public void onChangeSelectGroupId(ActionEvent event) {
        this.selectMailAccountId = Integer.parseInt(getParameter("accountId"));
    }

    @Override
    @SecureMethod(SecureMethod.Method.CREATE)
    public void save(ActionEvent event) {
        try {
            MailAccount mailAccount = model.getMailAccount();
            if(_CheckDuplicateMailAccountName(mailAccount)){
                _FocusMailAccountName(mailAccount.getAccountName());
                return;
            }
            if(_CheckDuplicateMailAddress(mailAccount)){
                _FocusMailAccountAddress(mailAccount.getAccountMailAddress());
                return;
            }
            mailAccount.setCreator(UserModel.getLogined().getMember());
            mailAccount.setCreatedTime(DateUtil.now());
            mailAccount.setAccountIsDeleted(StatusUtil.UN_DELETED);
            mailAccountService.create(mailAccount);
            load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.update.success", mailAccount.getAccountName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    @SecureMethod(SecureMethod.Method.DELETE)
    @Override
    public void delete(ActionEvent event) {
        try {
            String mailAccountId = this.getParameter("accountId");
            MailAccount ma = mailAccountService
                    .find(Integer.valueOf(mailAccountId));
            ma.setAccountIsDeleted(StatusUtil.DELETED);
            mailAccountService.edit(ma);

            load();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(_GetCompanyId(),
                    ResourceUtil.BUNDLE_MSG,
                    "msg.action.delete.success", ma.getAccountName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    @Override
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void update(ActionEvent event) {
        try {
            MailAccount ma = model.getMailAccount();
            if(_CheckDuplicateMailAccountName(ma)){
                _FocusMailAccountName(ma.getAccountName());
                return;
            }
            if(_CheckDuplicateMailAddress(ma) && !oldMailAccountAddress.equals(ma.getAccountMailAddress())){
                _FocusMailAccountAddress(ma.getAccountMailAddress());
                return;
            }
            ma.setMailServer(mailServerService.find(ma.getMailServer().getServerId()));
            ma.setUpdator(UserModel.getLogined().getMember());
            ma.setUpdatedTime(DateUtil.now());
            mailAccountService.edit(ma);
            load();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(_GetCompanyId(),
                    ResourceUtil.BUNDLE_MSG, "msg.action.update.success",
                    ma.getAccountName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.REQUESTMAIL, require = false)
    public void onTestReadMail(ActionEvent event) {
        TreeFolder root = null;
        Store store = null;
        try {
            MailAccount ma = model.getMailAccount();
            if(!StatusUtil.getBoolean(ma.getAccountReceiveFlag())) return;
            if(ma.getMailServer().getServerId() == null) return;
            MailServer ms = mailServerService.find(ma.getMailServer().getServerId());
            if(ms == null) return;
            
            EmailBuilder<ReadEmail> builder = new EmailBuilder<>(new ReadEmail());
            builder.type(ms.getServerType()).host(ms.getServerHost()).port(ms.getServerPort())
                    .auth(Boolean.valueOf(ms.getServerAuth())).ssl(Boolean.valueOf(ms.getServerSsl()))
                    .username(ma.getAccountUserName()).password(ma.getAccountPassword());
            
            ReadEmail e = builder.builder();
            
            // lấy toàn bộ FOLDERS từ MAIL-SERVER.
            Map<String, Object> ret = FolderUtil.getTreeFolder(e);
            root = (TreeFolder) ret.get("TF");
            store = (Store) ret.get("S");
            
            if(root.getChildren() == null || root.getChildren().isEmpty()) {
                JsfUtil.addErrorMessage(mailBundle.getString("label.mail.account.test.read.error"));
            } else {
                JsfUtil.addSuccessMessage(mailBundle.getString("label.mail.account.test.read.success"));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if(ExceptionUtils.checkIsThrowable(e, java.net.ConnectException.class)) {
                JsfUtil.addErrorMessage("接続拒否");
            } else if(ExceptionUtils.checkIsThrowable(e, javax.mail.AuthenticationFailedException.class)) {
                JsfUtil.addErrorMessage("無効な資格情報");
            } else {
                JsfUtil.addErrorMessage(e, e.getMessage());
            }
        } finally {
            try { FolderUtil.closeFolder(root); } catch (Exception e) { } // close folders
            try { if(store != null) store.close(); } catch (Exception e) { } // close store
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.REQUESTMAIL, require = false)
    public void onTestSendMail(ActionEvent event) {
        try {
            MailAccount ma = model.getMailAccount();
            if(!StatusUtil.getBoolean(ma.getAccountSendFlag())) return;
            if(ma.getMailServer().getServerId() == null) return;
            MailServer ms = mailServerService.find(ma.getMailServer().getServerId());
            if(ms == null) return;
            
            EmailBuilder<SendEmail> builder = new EmailBuilder(new SendEmail(), InterfaceUtil.Type.SMTP);
            builder = builder.host(ms.getServerSmtp()).port(ms.getServerSmtpPort())
                    .auth(Boolean.valueOf(ms.getServerAuth())).ssl(Boolean.valueOf(ms.getServerSsl()))
                    .username(ma.getAccountUserName()).password(ma.getAccountPassword())
                    .timeout(2000); // miliseconds;
            SendEmail se = builder.builder();
            
            se.setFrom(ma.getAccountMailAddress());
            se.setRecipient(new String[]{"gnext383838@gmail.com"});
            se.setSubject("[CRMCLOUD] Send mail.");
            se.setMessage("This mail is just for test send mail from account");
            
            // gửi mail chỉ dùng cho test nên không cần lưu thông tin mail này vào trong cơ sở dữ liệu.
            String[] args = { "action:send" };
            if(MailClient._SendMail(se, ParameterUtil.buildParameters(args)) != null)
                JsfUtil.addSuccessMessage(mailBundle.getString("label.mail.account.test.sent.success"));
            else
                JsfUtil.addErrorMessage(mailBundle.getString("label.mail.account.test.sent.error"));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if(ExceptionUtils.checkIsThrowable(e, java.net.ConnectException.class)) {
                JsfUtil.addErrorMessage("接続拒否");
            } else if(ExceptionUtils.checkIsThrowable(e, javax.mail.AuthenticationFailedException.class)) {
                JsfUtil.addErrorMessage("ユーザー名とパスワードは受け入れられません");
            } else {
                JsfUtil.addErrorMessage(e, e.getMessage());
            }
        }
    }
    
    public void onChangeAccountDeleteReceivedDays() {
        if (!this.model.isAccountDeleteReceivedDays()) {
            this.model.getMailAccount().setAccountDeleteReceivedDays(null);
        } else {
            this.model.getMailAccount().setAccountDeleteReceivedDays(0);
        }
    }

    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        this.layout.setCenter("/modules/mail/account/index.xhtml");
    }
    
     private boolean _CheckDuplicateMailAddress(MailAccount ma){
         return mailAccountService.isMaillAddressExist(ma.getAccountMailAddress(), getCurrentCompanyId());
     }
    
    private boolean _CheckDuplicateMailAccountName(MailAccount ma) {
        MailAccount account = mailAccountService.search(getCurrentCompanyId(), ma.getAccountName());
        // case insert
        if (ma.getAccountId() == null && account != null) {
            return true;
        } // case edit
        else if (oldMailAccountName != null && !oldMailAccountName.equals(ma.getAccountName())) {
            if (account != null) {
                return true;
            }
        }
        return false;
    }
    
    private void _FocusMailAccountName(String value) {
        UIComponent component = JsfUtil.findComponent("j_account_name");
        if(component == null) return;
        UIInput uIInput = (UIInput) component;
        uIInput.setValid(false);
        uIInput.setValue(value);
        String title = uIInput.getAttributes().get("title").toString();
        JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.accountname.duplicate", title));
    }
    
    private void _FocusMailAccountAddress(String value) {
        UIComponent component = JsfUtil.findComponent("j_account_address");
        if(component == null) return;
        UIInput uIInput = (UIInput) component;
        uIInput.setValid(false);
        uIInput.setValue(value);
        String title = uIInput.getAttributes().get("title").toString();
        JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.accountaddress.duplicate", title));
    }
}
