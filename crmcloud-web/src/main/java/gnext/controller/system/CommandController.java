/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.system;

import com.google.gson.Gson;
import gnext.bean.Company;
import gnext.bean.attachment.Server;
import gnext.bean.job.Command;
import gnext.bean.job.CommandType;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.model.authority.UserModel;
import gnext.model.search.SearchFilter;
import gnext.model.system.command.mail.MailCommandItem;
import gnext.security.SecurityService;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.CompanyService;
import gnext.service.attachment.ServerService;
import gnext.service.config.ConfigService;
import gnext.service.job.CommandService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.StatusUtil;
import gnext.utils.StringUtil;
import java.text.MessageFormat;
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
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 *
 * @author daind
 */
@ManagedBean(name = "commandController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.SYSTEM, require = true)
public class CommandController extends AbstractController {
    private static final long serialVersionUID = 8614688864523387120L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandController.class);
    
    @ManagedProperty(value = "#{layout}") @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{sec}") @Getter @Setter private SecurityService securityService;
    
    @EJB @Getter @Setter private CommandService commandService;
    @EJB @Getter @Setter private CompanyService companyService;
    @EJB @Getter @Setter private ServerService serverService;
    @EJB @Getter @Setter private ConfigService configService;
    
    @Getter @Setter private List<Company> companys;
    @Getter @Setter private List<Command> commands;
    @Getter @Setter private List<Server> servers;
    @Getter @Setter private Map<String,String> folders;
    @Getter @Setter private Command command;
    @Getter @Setter private int ctype; // loại command khi người dùng thay đổi.
    @Getter @Setter private Integer selectedCommandId;
    @Getter @Setter private MailCommandItem mailCommandItem;
    
    @PostConstruct
    public void init() {
        commands = new ArrayList<>();
        companys = new ArrayList<>();
        servers = new ArrayList<>();
        folders = new HashMap<>();
    }
    
    private void prepareDataForViewList() {
        commands.clear(); commands.addAll(commandService.findByCompanyId(UserModel.getLogined().getCompanyId()));
    }
    
    private void loadFolders() {
        folders.clear();
        folders.put("inbox", mailBundle.getString("label.mail.folder.inbox"));
        folders.put("draft", mailBundle.getString("label.mail.folder.draft"));
        folders.put("trash", mailBundle.getString("label.mail.folder.trash"));
        folders.put("junk", mailBundle.getString("label.mail.folder.junk"));
        folders.put("sent", mailBundle.getString("label.mail.folder.sent"));
    }
    
    private void prepareDataForViewAdd() {
        companys.clear(); companys.addAll(companyService.findAll());
        servers.clear(); servers.addAll(serverService.search(getCurrentCompanyId()));
        loadFolders();
        ctype = CommandType.STANDARD.getId();
        mailCommandItem = new MailCommandItem(); // dữ liệu cho kiểu lệnh đọc mail.
        mailCommandItem.setPath(getMailApi());
        command = new Command();
    }
    
    private void prepareDataForViewEdit(int commandId) {
        companys.clear(); companys.addAll(companyService.findAll());
        servers.clear(); servers.addAll(serverService.search(getCurrentCompanyId()));
        loadFolders();
        command = commandService.find(commandId);
        ctype = command.getCommandType();
        if(!StringUtils.isEmpty(command.getCommandJson()))
            mailCommandItem = new Gson().fromJson(command.getCommandJson(), MailCommandItem.class);
        else {
            mailCommandItem = new MailCommandItem();
            mailCommandItem.setPath(getMailApi());
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        prepareDataForViewList();
        this.layout.setCenter("/modules/system/batch/command/index.xhtml");
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    @Override
    public void show(ActionEvent event) {
        final String showType = this.getParameter("showType");
        if ("addNew".equals(showType)) {
            prepareDataForViewAdd();
            this.layout.setCenter("/modules/system/batch/command/create.xhtml");
        } else if ("edit".equals(showType)) {
            int commandId =  Integer.parseInt(getParameter("commandId"));
            prepareDataForViewEdit(commandId);
            this.layout.setCenter("/modules/system/batch/command/edit.xhtml");
        } else if ("view".equals(showType)) {
            int commandId =  Integer.parseInt(getParameter("commandId"));
            prepareDataForViewEdit(commandId);
            this.layout.setCenter("/modules/system/batch/command/view.xhtml");
        }
    }
    
    @Override
    @SecureMethod(SecureMethod.Method.CREATE)
    public void save(ActionEvent event) {
        try {
            command.setCreatorId(UserModel.getLogined().getUserId());
            command.setCreatedTime(DateUtil.now());
            command.setCommandDeleted((short) 0);
            if(securityService.isMaster()) {
                command.setCompanyId(command.getCompanyId());
                command.setCompany(companyService.find(command.getCompanyId()));
            } else {
                command.setCompanyId(getCurrentCompanyId());
                command.setCompany(companyService.find(getCurrentCompanyId()));
            }
            
            this.parseCommanVlaue();
            commandService.create(command);
            
            this.load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.create.success", command.getCommandName()));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }
    
    @Override
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void update(ActionEvent event) {
        try {
            if(securityService.isMaster()) {
                command.setCompanyId(command.getCompanyId());
                command.setCompany(companyService.find(command.getCompanyId()));
            } else {
                command.setCompanyId(getCurrentCompanyId());
                command.setCompany(companyService.find(getCurrentCompanyId()));
            }
            command.setUpdatedTime(DateUtil.now());
            command.setUpdatedId(UserModel.getLogined().getUserId());
            
            this.parseCommanVlaue();
            commandService.edit(command);
            
            this.load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.update.success", command.getCommandName()));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    @SecureMethod(SecureMethod.Method.DELETE)
    @Override
    public void delete(ActionEvent event) {
        try{
            Integer commandId = selectedCommandId;
            Command _command = commandService.findById(commandId, getCurrentCompanyId());
            _command.setCommandDeleted(StatusUtil.DELETED);
            commandService.edit(_command);
            load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.delete.success", _command.getCommandName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }
    
    private void parseCommanVlaue() {
        if(ctype == CommandType.READ_EMAIL.getId()) {
            String c = mailCommandItem.buildReadMailCommand();
            String mail_api_path = mailCommandItem.getPath();
            if(StringUtils.isEmpty(mail_api_path)) mail_api_path = getMailApi();
            c = MessageFormat.format(c, mail_api_path, StringUtil.DEFAULT_DB_PROPERTIES, command.getCompanyId());
            command.setCommandValue(c);
            command.setCommandJson(new Gson().toJson(mailCommandItem));
        }
    }
    
    private String getMailApi() {
        String mailApi = configService.get("MAIL_API");
        if(StringUtils.isEmpty(mailApi))
            mailApi = "/home/daind/workspace/vnext/sourcecode/web/mail-client/target/mail-client.jar"; // đường dẫn mặc định.
        return mailApi;
    }
    
    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public CommandType[] getCommandTypes() { return CommandType.values(); }
    public List<String> getFlags() { return Arrays.asList("ALL", "UNREAD", "RECENT", "SEEN"); } // , "ANSWERED", "DELETED", "DRAFT"
    public boolean isStandardScreen() { return ctype == CommandType.STANDARD.getId(); }
    public boolean isReadEmailScreen() { return ctype == CommandType.READ_EMAIL.getId(); }
    public void commandTypeSelectionChanged(final AjaxBehaviorEvent event) { this.ctype = this.command.getCommandType(); }
    public String getServerName(Integer serverId) {
        if(serverId == null || serverId < 0) return "";
        return serverService.find(serverId).getServerName();
    }
    
    /**
     * Hàm xử lí trả về tên của COMMAND theo command-id.
     * @param commandType
     * @return 
     */
    public String getCommandTypeLabel(Integer commandType) {
        for(CommandType ct : CommandType.values()) {
            if(ct.getId() == commandType)
                return ct.getName();
        }
        
        return "N/A";
    }
    
    /**
     * Khi người dùng chọn một GROUP trong danh sách để xóa, thông tin command-id được lưu lại.
     * Người dùng accept xóa thì lấy command-id đã lưu để xóa.
     * @param event 
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onChangeSelectCommandId(ActionEvent event){
        String commandId = getParameter("commandId");
        setSelectedCommandId(Integer.parseInt(commandId));
    }
}
