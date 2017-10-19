/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail;

import gnext.bean.mail.MailServer;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.model.authority.UserModel;
import gnext.model.mail.MailServerLazyList;
import gnext.model.mail.MailServerModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.mail.MailServerService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
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
@ManagedBean(name = "mailServerController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.MAIL, require = true)
public class MailServerController extends AbstractController {
    private static final long serialVersionUID = -1359907914119454247L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MailServerController.class);

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    @EJB @Getter @Setter private MailServerService mailServerService;
    @Getter @Setter private String query;
    @Getter @Setter private LazyDataModel<MailServerModel> models;
    @Getter @Setter private MailServerModel model;
    private String oldServerName;
    @Getter @Setter private Integer selectMailServerId;

    private void _Load(Integer mailServerId) {
        if (mailServerId != null) {
            MailServer mailServer = mailServerService.find(mailServerId);
            this.model = new MailServerModel(mailServer);
        } else {
            MailServer mailServer = new MailServer();
            mailServer.setCompany(getCurrentCompany());
            this.model = new MailServerModel(mailServer);
        }
    }

    @PostConstruct
    public void init() {
        models = new MailServerLazyList(this);
        query = "1=1 and ms.company.companyId=" + getCurrentCompanyId();
    }

    @SecureMethod(SecureMethod.Method.VIEW)
    @Override
    public void show(ActionEvent event) {
        final String showType = this.getParameter("showType");
        if ("view".equals(showType)) {
            this.currentRowNum = Integer.parseInt(this.getParameter("rowNum"));
            _Load(Integer.parseInt(this.getParameter("serverId")));
            this.layout.setCenter("/modules/mail/server/view.xhtml");
        } else if ("edit".equals(showType)) {
            _Load(Integer.parseInt(this.getParameter("serverId")));
            oldServerName = model.getMailServer().getServerName();
            this.layout.setCenter("/modules/mail/server/edit.xhtml");
        } else if ("create".equals(showType)) {
            _Load(null);
            this.layout.setCenter("/modules/mail/server/create.xhtml");
        }
    }
    
    @Override
    protected void afterPaging() {
        models.setRowIndex(currentRowNum);
        model = models.getRowData();
    }

    @SecureMethod(value=SecureMethod.Method.SEARCH) public void reload() {}
    @Override
    protected void doSearch(SearchFilter filter) {
        query = "1=1 and ms.company.companyId=" + getCurrentCompanyId() + " and ";
        query = query + (filter != null && !StringUtils.isEmpty(filter.getQuery()) ? filter.getQuery() : "1=1");
    }
    
    @Override
    @SecureMethod(value=SecureMethod.Method.DOWNLOAD)
    public void download(String tblName, String fileName) {
        super.download(tblName, fileName);
    }

    @SecureMethod(SecureMethod.Method.UPDATE)
    @Override
    public void update(ActionEvent event) {
        try {
            MailServer mailServer = model.getMailServer();
            if (checkDuplicateServerName(mailServer)) {
                focusServerNameValidate(mailServer.getServerName());
                return;
            }
            mailServer.setUpdator(UserModel.getLogined().getMember());
            mailServer.setUpdatedTime(DateUtil.now());
            mailServerService.edit(mailServer);
            load();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(getCurrentCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.update.success", mailServer.getServerName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    @SecureMethod(SecureMethod.Method.DELETE)
    @Override
    public void delete(ActionEvent event) {
        try {
            String mailServerId = this.getParameter("serverId");
            MailServer mailServer = mailServerService
                    .find(Integer.valueOf(mailServerId));
            mailServer.setServerDeleted(StatusUtil.DELETED);
            mailServerService.edit(mailServer);

            load();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(getCurrentCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.delete.success", mailServer.getServerName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    @SecureMethod(SecureMethod.Method.CREATE)
    @Override
    public void save(ActionEvent event) {
        try {
            MailServer mailServer = model.getMailServer();
            if(checkDuplicateServerName(mailServer)){
                focusServerNameValidate(mailServer.getServerName());
                return;
            }
            mailServer.setCreator(UserModel.getLogined().getMember());
            mailServer.setCreatedTime(DateUtil.now());
            mailServer.setServerDeleted((short) 0);
            mailServerService.create(mailServer);
            
            load();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(getCurrentCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.update.success", mailServer.getServerName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }
    
    @SecureMethod(SecureMethod.Method.PRINT)
    public void print(){};

    public void onChangeSelectGroupId(ActionEvent event) { this.selectMailServerId = Integer.parseInt(getParameter("serverId")); }

    public void onChangeServerType(ValueChangeEvent event) {
        String serverType = String.valueOf(event.getNewValue());

        boolean exists = this.model.getMailServer().getServerPort() != null;
        if ("POP3".equalsIgnoreCase(serverType)) {
            this.model.getMailServer().setServerPort(110);
        } else if ("IMAP".equalsIgnoreCase(serverType)) {
            this.model.getMailServer().setServerPort(143);
        }
    }

    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        this.layout.setCenter("/modules/mail/server/index.xhtml");
    }
    
    private boolean checkDuplicateServerName(MailServer mailServer){
        // case insert 
        MailServer server = mailServerService.search(getCurrentCompanyId(), mailServer.getServerName());
        if(null == mailServer.getServerId() && server != null){
            return true;
        }
        // case edit
        else {
             if(oldServerName != null && !oldServerName.equals(mailServer.getServerName())){
                if(server != null){
                    return true;
                }
            }
        }
        return false;
    }
    
    private void focusServerNameValidate(String value){
        UIComponent component = JsfUtil.findComponent("j_server_name");
        if(component == null) return;
        UIInput uIInput = (UIInput) component;
        uIInput.setValid(false);
        uIInput.setValue(value);
        String title = uIInput.getAttributes().get("title").toString();
        JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.servername.duplicate", title));
    }
}
