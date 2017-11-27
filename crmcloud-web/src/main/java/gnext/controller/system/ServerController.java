/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.system;

import gnext.bean.attachment.Server;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.ProtocolType;
import gnext.filetransfer.TransferType;
import gnext.model.authority.UserModel;
import gnext.model.search.SearchFilter;
import gnext.model.system.ServerLazyModelList;
import gnext.model.system.ServerModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.attachment.ServerService;
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
 * @author tungdt
 */
@ManagedBean(name = "serverController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.SYSTEM, require = true)
public class ServerController extends AbstractController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerController.class);
    private static final long serialVersionUID = -2204012957322153125L;
    
    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    @EJB @Getter @Setter private ServerService serverService;
    @Getter @Setter private String query;
    @Getter @Setter LazyDataModel<ServerModel> models;
    @Getter @Setter ServerModel model;
    @Getter @Setter private Integer selectServerId;
    @Getter @Setter private ServerFlag serverFlagEnum;
    @Getter @Setter private TransferType serverTypeEnum;
    @Getter @Setter private ProtocolType protocolTypeEnum;
    
    // dùng kiểm tra sự trùng lặp tên server trong cùng 1 công ty.
    private String oldServerName;

    @PostConstruct
    public void init() {
        models = new ServerLazyModelList(this);
        query = "1=1 and sv.company.companyId=" + _GetCompanyId();
    }

    @SecureMethod(SecureMethod.Method.VIEW)
    @Override
    public void show(ActionEvent event) {
        final String showType = this.getParameter("showType");
        if ("view".equals(showType)) {
            this.currentRowNum = Integer.parseInt(this.getParameter("rowNum"));
            _Load(Integer.parseInt(this.getParameter("serverId")));
            this.layout.setCenter("/modules/system/server/view.xhtml");
        } else if ("edit".equals(showType)) {
            _Load(Integer.parseInt(this.getParameter("serverId")));
            oldServerName = model.getServer().getServerName();
            this.layout.setCenter("/modules/system/server/edit.xhtml");
        } else if ("create".equals(showType)) {
            _Load(null);
            this.layout.setCenter("/modules/system/server/create.xhtml");
        }
    }

    @SecureMethod(SecureMethod.Method.CREATE)
    @Override
    public void update(ActionEvent event) {
        try {
            Server server = model.getServer();
            if (checkDuplicateServerName(server)) {
                focusServerNameValidate(server.getServerName());
                return;
            }
            if (serverTypeEnum != null) {
                server.setServerType(serverTypeEnum.getType());
            }
            if(StatusUtil.getBoolean(server.getServerSsl()) && protocolTypeEnum != null){
                server.setServerProtocol(protocolTypeEnum.getType());
            }
            server.setUpdatedId(UserModel.getLogined().getMember().getMemberId());
            server.setUpdatedTime(DateUtil.now());
            //server.setServerDeleted(StatusUtil.UN_DELETED);
            server.setServerFlag(serverFlagEnum.getId());
            if (!StatusUtil.getBoolean(server.getServerDeleted()) && !_TestConnection(server, serverTypeEnum)) {
                JsfUtil.addErrorMessage(JsfUtil.getResource().message(_GetCompanyId(), ResourceUtil.BUNDLE_SYSTEM, "label.system.server.test", server.getServerName()));
                return;
            }
            serverService.edit(server);
            load();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(_GetCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.update.success", server.getServerName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    @SecureMethod(SecureMethod.Method.UPDATE)
    @Override
    public void save(ActionEvent event) {
        try {
            Server server = model.getServer();
            if (checkDuplicateServerName(server)) {
                focusServerNameValidate(server.getServerName());
                return;
            }
            if (serverTypeEnum != null) {
                server.setServerType(serverTypeEnum.getType());
            }
            if(StatusUtil.getBoolean(server.getServerSsl())){
                server.setServerProtocol(protocolTypeEnum.getType());
            }
            server.setCompany(UserModel.getLogined().getCompany());
            server.setCreatorId(UserModel.getLogined().getMember().getMemberId());
            server.setCreatedTime(DateUtil.now());
            server.setServerDeleted(StatusUtil.UN_DELETED);
            server.setServerFlag(serverFlagEnum.getId());
            
            if(!_TestConnection(server, serverTypeEnum)){
                JsfUtil.addErrorMessage(JsfUtil.getResource().message(_GetCompanyId(), ResourceUtil.BUNDLE_SYSTEM, "label.system.server.test", server.getServerName()));
                return;
            }
            serverService.create(server);
            load();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(_GetCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.update.success", server.getServerName()));
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
            Server server = serverService.find(Integer.valueOf(mailServerId));
            server.setServerDeleted(StatusUtil.DELETED);
            serverService.edit(server);

            load();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(_GetCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.delete.success", server.getServerName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }
    
    public void onChangeSelectServerId(ActionEvent event) {
        this.selectServerId = Integer.parseInt(getParameter("serverId"));
    }

    public void onChangeServerFlag(ValueChangeEvent event) throws InterruptedException {
        int serverFlagId = Integer.valueOf(String.valueOf(event.getNewValue()));
        serverFlagEnum = _GetServerFlagById(serverFlagId);
        model.getServer().setServerFolder(serverFlagEnum.getFolder());
    }
    
    public void onChangeSecurity(ValueChangeEvent event){
        boolean ssl = Boolean.parseBoolean(String.valueOf(event.getNewValue()));
        if(!ssl){
            model.getServer().setServerProtocol(null);
        }
    }
    
    private int _GetCompanyId() {
        return UserModel.getLogined().getCompanyId();
    }

    private void _Load(Integer serverId) {
        if (serverId != null) {
            Server server = serverService.find(serverId);
            this.model = new ServerModel(server);
            this.serverFlagEnum = _GetServerFlagById(model.getServer().getServerFlag());
            this.serverTypeEnum = _GetServerTypeByType(model.getServer().getServerType());
            this.protocolTypeEnum = _GetServerProtocolByType(model.getServer().getServerProtocol());
        } else {
            Server server = new Server();
            server.setCompany(UserModel.getLogined().getCompany());
            this.model = new ServerModel(server);
            serverFlagEnum = ServerFlag.COMMON;
            model.getServer().setServerFolder(serverFlagEnum.getFolder());
            serverTypeEnum = TransferType.FTP;
            protocolTypeEnum = ProtocolType.SSL;
        }
    }

    private ServerFlag _GetServerFlagById(int serverFlagId) {
        for (ServerFlag serverFlag : ServerFlag.values()) {
            if (serverFlag.getId() == serverFlagId) {
                return serverFlag;
            }
        }
        return null;
    }
    
    private TransferType _GetServerTypeByType(String serverType) {
        for (TransferType transferType : TransferType.values()) {
            if (transferType.getType().equals(serverType)) {
                return transferType;
            }
        }
        return null;
    }
    
    private ProtocolType _GetServerProtocolByType(String protocol) {
        for (ProtocolType protocolType : ProtocolType.values()) {
            if (protocolType.getType().equals(protocol)) {
                return protocolType;
            }
        }
        return null;
    }
    
    private boolean _TestConnection(Server server, TransferType type){
        try {
            Parameter param = Parameter.getInstance(type).manualconfig(true).storeDb(false);
            param.type(type).host(server.getServerHost())
                    .port(server.getServerPort())
                    .username(server.getServerUsername())
                    .password(server.getDecryptServerPassword());
            
            if(StatusUtil.getBoolean(server.getServerSsl())){
                param.security(true);
            }else {
                param.security(false);
            }
            
            FileTransferFactory.getTransfer(param).test();
            return true;
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @SecureMethod(value=SecureMethod.Method.SEARCH) public void reload() {}
    @Override
    protected void doSearch(SearchFilter filter) {
        query = "1=1 and sv.company.companyId=" + _GetCompanyId() + " and ";
        query = query + (filter != null && !StringUtils.isEmpty(filter.getQuery()) ? filter.getQuery() : "1=1");
    }

    @Override
    @SecureMethod(value=SecureMethod.Method.DOWNLOAD)
    public void download(String tblName, String fileName) {
        super.download(tblName, fileName);
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        this.layout.setCenter("/modules/system/server/index.xhtml");
    }

    @Override
    protected void afterPaging() {
        models.setRowIndex(currentRowNum);
        model = models.getRowData();
    }

    public ServerFlag[] getServerFlag() {
        return ServerFlag.values();
    }
    
    public TransferType[] getTransferType() {
        return TransferType.values();
    }
    
    public ProtocolType[] getProtocolType(){
        return ProtocolType.values();
    }

    private boolean checkDuplicateServerName(Server sv) {
        // case insert 
        Server server = serverService.search(sv.getServerName(), getCurrentCompanyId());
        if (null == sv.getServerId() && server != null) {
            return true;
        } // case edit
        else {
            if (oldServerName != null && !oldServerName.equals(sv.getServerName()) && server != null) {
                return true;
            }
        }
        return false;
    }

    private void focusServerNameValidate(String value) {
        UIComponent component = JsfUtil.findComponent("j_server_name");
        if(component == null) return;
        UIInput uIInput = (UIInput) component;
        uIInput.setValid(false);
        uIInput.setValue(value);
        String title = uIInput.getAttributes().get("title").toString();
        JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.servername.duplicate", title));
    }
}
