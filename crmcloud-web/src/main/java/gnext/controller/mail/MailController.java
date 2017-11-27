/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail;

import gnext.bean.mail.MailData;
import gnext.bean.mail.MailPerson;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.controller.issue.IssueController;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.model.authority.UserModel;
import gnext.model.mail.EmailList;
import gnext.model.mail.MailDataModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.attachment.AttachmentService;
import gnext.service.config.ConfigService;
import gnext.service.issue.IssueService;
import gnext.service.mail.MailDataService;
import gnext.service.mail.MailPersonService;
import gnext.util.EmailUtil;
import gnext.util.JsfUtil;
import gnext.util.StatusUtil;
import gnext.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author HUONG
 */
@ManagedBean(name = "mailController")
@SessionScoped
@SecurePage(module = SecurePage.Module.MAIL, require = true)
public class MailController extends AbstractController<MailDataModel> {

    private static final long serialVersionUID = 1940765421050394503L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MailController.class);

    @ManagedProperty(value = "#{mailListController}")
    @Getter @Setter private MailListController mailListController;
    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{issueController}")
    @Getter @Setter private IssueController issueController;

    @EJB @Getter @Setter private MailDataService mailDataService;
    @EJB @Getter @Setter private ConfigService configService;
    @EJB private AttachmentService attachmentService;
    @EJB private MailPersonService mailPersonService;
    @EJB private IssueService issueService;

    @Getter @Setter private List<EmailList> mails;
    @Getter @Setter private String[] classBtn = new String[5];
    @Getter @Setter private String currentMailType;
    @Getter @Setter private MailDataModel mailDataModel;
    @Getter @Setter private List<Integer> mailReadMarked = new ArrayList<>();
    
    @Getter @Setter private Integer mailPersonId;
    @Getter @Setter private List<MailPerson> mailPersons = new ArrayList<>();
    
    private void clearData() {
        mailReadMarked.clear();mailPersons.clear();
        mailPersonId = null;
    }
    
    private void loadPersonCharge() {
        int cid = getCurrentCompanyId();
        mailPersons.addAll(mailPersonService.search(cid, (short) 0));
    }
    
    private void activeButton() {
        for (int i = 0; i < classBtn.length; i++) classBtn[i] = "btn btn-default btn-flat";
        switch (currentMailType.toUpperCase()) {
            case EmailUtil.INBOX: classBtn[0] = "btn btn-primary btn-flat"; break;
            case EmailUtil.SENT: classBtn[1] = "btn btn-primary btn-flat"; break;
            case EmailUtil.DRAFT: classBtn[2] = "btn btn-primary btn-flat"; break;
            case EmailUtil.JUNK: classBtn[3] = "btn btn-primary btn-flat"; break;
            case EmailUtil.TRASH: classBtn[4] = "btn btn-primary btn-flat"; break;
        }
    }
    
    private String getFolderCode() {
        return EmailUtil.getExplodeType(currentMailType);
    }
    
    @PostConstruct
    public void init() {
        onChangeType(EmailUtil.INBOX);
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onPersonChange() {
        try {
            String store = getParameter("store");
            if(!StringUtils.isEmpty(store) && "1".equals(store)) {
                if(mailPersonId == null) mailDataModel.getMailData().setMailPerson(null);
                else mailDataModel.getMailData().setMailPerson(mailPersonService.search(getCurrentCompanyId(), mailPersonId));
                
                mailDataService.changeMailPerson(mailDataModel.getMailData(), getCurrentCompany());

                JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.update.success", mailDataModel.getMailData().getMailDataSubject()));
            } else if(!StringUtils.isEmpty(store) && "0".equals(store)) {
                if(mailDataModel.getMailData().getMailPerson() != null)
                    mailPersonId = mailDataModel.getMailData().getMailPerson().getMailPersonId();
                else
                    mailPersonId = null;
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            JsfUtil.addErrorMessage(e.getLocalizedMessage());
        }
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onChangeType(String btnType) {
        if (mails == null) mails = new ArrayList<>(); else  mails.clear();

        currentMailType = btnType;
        activeButton();

        Integer limit = configService.getInt("NUMBER_OF_MAIL_IN_SIDEBAR");
        if (limit == null || limit == 0) limit = 10;

        List<MailData> mailDatas = mailDataService.searchByCompanyId(getFolderCode(), getCurrentCompanyId(), limit);
        int i = 0;
        for (MailData data : mailDatas) {
            EmailList el = new EmailList(data);
            el.setRowNum(i++);
            mails.add(el);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void gotoDetails() {
        mailListController.displayDetail();
        clearData();loadPersonCharge();
        Integer mailId = Integer.parseInt(getParameter("mailId"));
        Integer rowNum = Integer.parseInt(getParameter("rowNum"));
        boolean fromList = Boolean.parseBoolean(getParameter("fromList"));
        setup(rowNum, fromList);
    }
    
    private void setup(Integer rowNum, boolean fromList) {
        if(!fromList) mailListController.mailFolderController.changeFolder(getFolderCode());
        getPaginator().setLimit(1);
        getPaginator().setCurrentPage(rowNum);
        afterPaging();
    }
    
    @Override
    protected void afterPaging() {
        _LoadData();
        mailDataModel = getPaginator().iterator().next();
        if(mailDataModel.getMailData().getMailPerson() != null)
            mailPersonId = mailDataModel.getMailData().getMailPerson().getMailPersonId();
        mailListController.getDataSelected().put(mailDataModel.getMailData().getMailDataId(), Boolean.TRUE);
        
        MailData md = mailDataModel.getMailData();
        if(!StatusUtil.getBoolean(md.getMailDataIsRead())) mailReadMarked.add(md.getMailDataId());
        
        this.loadExplodes();
        this.loadIssueRelated();
        this.load();
    }
    
    private void loadExplodes() {
        if(mailDataModel == null) return;
        if(mailDataModel.getMailData() == null) return;
        mailDataModel.setMailExplodes(mailDataService.searchExplodes(mailDataModel.getMailData()));
    }
    
    private void loadIssueRelated() {
        if(mailDataModel == null) return;
        if(mailDataModel.getMailData() == null) return;
        mailDataModel.setIssueRelated(mailDataService.searchIssueRelated(mailDataModel.getMailData()));
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void checkoutIssue(Integer issueId) {
        try {
            if(issueId == null || issueId <=0) return;
            mailDataService.checkoutIssue(issueId, mailDataModel.getMailData(), UserModel.getLogined().getMember());
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.update.success", mailDataModel.getMailData().getMailDataSubject()));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            JsfUtil.addErrorMessage(e.getLocalizedMessage());
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void gotoList() throws Exception {
        markedReadMail();
        mailListController.load();
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void goBackFromDetailToList() throws Exception {
        markedReadMail();
        mailListController.view();
    }
    
    private void markedReadMail() throws Exception {
        mailDataService.markIsRed(getCurrentCompany(), mailReadMarked);
    }
    
    private void _LoadData() {
        String customizeSql = mailListController.buildQuery();
        List<MailData> mds = mailDataService.find(getCurrentCompanyId(), getPaginator().getFirst(), getPaginator().getLimit(), mailListController.getSort(), customizeSql);
        int total = mailDataService.total(getCurrentCompanyId(), customizeSql);
        List<MailDataModel> mdms = new ArrayList<>();
        for(MailData md : mds) {
            MailDataModel mdm = new MailDataModel(md);
            mdm.setAttachments(attachmentService.search(getCurrentCompanyId(), AttachmentTargetType.MAIL.getId(), md.getMailDataId(), (short)0));
            mdms.add(mdm);
        }
        getPaginator().setup(total, mdms);
    }
    
    public void autoReadmail() {
        if (StringUtils.isEmpty(currentMailType)) currentMailType = EmailUtil.INBOX;
        onChangeType(currentMailType);
    }
    
    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        mailListController.displayDetail();
    }
    
    public String viewAttachmentName(String attachmentName) {
        if(StringUtils.isEmpty(attachmentName)) return StringUtils.EMPTY;
        return StringUtil.getDownloadFileName(attachmentName);
    }
}
