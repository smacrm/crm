/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.company;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gnext.bean.Company;
import gnext.bean.CompanyTargetInfo;
import gnext.bean.CompanyTargetInfoPK;
import gnext.bean.DatabaseServer;
import gnext.bean.Prefecture;
import gnext.bean.UnionCompanyRel;
import gnext.bean.UnionCompanyRelModel;
import gnext.bean.ZipCode;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.dbutils.util.FileUtil;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.model.FileUploader;
import gnext.model.authority.UserModel;
import gnext.model.company.CompanyLazyList;
import gnext.model.company.CompanyModel;
import gnext.model.search.SearchFilter;
import gnext.resource.bundle.CompanyBundle;
import gnext.security.SecurityService;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.CompanyService;
import gnext.service.CompanyTargetInfoService;
import gnext.service.DatabaseServerService;
import gnext.service.PrefectureService;
import gnext.service.UnionCompanyRelService;
import gnext.service.ZipCodeService;
import gnext.service.attachment.AttachmentService;
import gnext.service.attachment.ServerService;
import gnext.util.JsfUtil;
import gnext.util.LayoutUtil;
import gnext.util.StatusUtil;
import gnext.util.StringUtil;
import gnext.utils.EncoderUtil;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIInput;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "companyController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.COMPANY, require = true)
public class CompanyController extends AbstractController {
    private static final long serialVersionUID = 3732832077282373596L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyController.class);
    private static final Integer MASK_COMPANYID = -1;

    @EJB @Getter @Setter private CompanyService companyService; 
    @EJB private ZipCodeService zipCodeService;
    @EJB private PrefectureService prefectureService; 
    @EJB private CompanyTargetInfoService companyTargetInfoService;
    @EJB private UnionCompanyRelService unionCompanyRelService;
    @EJB private ServerService serverService;
    @EJB private AttachmentService attachmentService;
    
    
    @ManagedProperty(value = "#{layout}") @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{sec}") @Getter @Setter private SecurityService sec;
    
    @ManagedProperty(value = "#{companyBundle}") @Getter @Setter private CompanyBundle companyBundle;
    
    @Getter @Setter private CompanyModel companyModel;
    @Getter @Setter private List<Prefecture> prefectures;
    @Getter @Setter private LazyDataModel<CompanyModel> groupCompanys;
    
    @Getter @Setter private String query;
    @Getter @Setter private List<UnionCompanyRelModel> unionCompanyRelModels;
    @Getter @Setter private UnionCompanyRelModel companyRelModelSelected = new UnionCompanyRelModel();
    @Getter @Setter private List<Company> companys;
    @Setter @Getter private List<SelectItem> themes;
    @Setter @Getter private FileUploader fileUploader;
    
    @Setter @Getter private String basicAuthUser = StringUtils.EMPTY;
    @Setter @Getter private String basicAuthPassword = StringUtils.EMPTY;
    
    @EJB private DatabaseServerService databaseServerService;
    @Setter @Getter private List<DatabaseServer> databaseServers = new ArrayList<>();
    @Setter @Getter private DatabaseServer dbServer = new DatabaseServer();
    /***
     * Hàm xử lí bổ sung thêm các dữ liệu liên quan tới company.
     * các thông tin khác bao gồm: logo, city, union, phone-fax-email-homepage...
     * @param type 
     */
    private void _UpdateCompanyInfor(String type) {
        fileUploader.reset();
        companyModel.cleanExtraInfo();
        
        boolean isLoadOldData = "edit".equals(type) || "view".equals(type);
        if (isLoadOldData) companyModel.initInfos(companyTargetInfoService);
        
        loadLogoRemote();
        
        companyModel.updateCity(getLocale(), prefectureService);
        companyModel.addEmptyInfos();
        
        // _BuildPrefectureFromZipCode(companyModel.getCompany().getCompanyPost());
        _BuildUnionCompany(companyModel);
    }
    
    private Server getOneServer(final List<Server> servers) {
        if(servers == null || servers.isEmpty()) return null;
        for(Server server : servers)
            if(server.getServerDeleted() == null || server.getServerDeleted() == 0) return server;
        return null;
    }

    /***
     * Hàm xử lí tải logo từ FTP server.
     */
    private void loadLogoRemote() {
        try {
            Integer companyId = companyModel.getCompany().getCompanyId();
            if(companyId == null) return;
            
            List<Server> servers = serverService.getAvailable(companyId, TransferType.FTP.getType(), ServerFlag.COMMON.getId());
            if(servers == null || servers.isEmpty()) return;
            
            int attachmentTargetType = AttachmentTargetType.COMPANY.getId();
            
            List<Attachment> attachments = attachmentService.search(companyId, attachmentTargetType, companyId, (short)0);
            if(attachments == null || attachments.isEmpty()) return;
            
            Server server = getOneServer(servers);
            if(server == null) return;
            Attachment attachment = attachments.get(0);
            
            String host = server.getServerHost();
            int port = server.getServerPort();
            String username = server.getServerUsername();
            String password = server.getDecryptServerPassword();
            boolean security = StatusUtil.getBoolean(server.getServerSsl());
            String protocol = server.getServerProtocol();
            String servertype = server.getServerType();
            
            TransferType tt = TransferType.getTransferType(servertype);
            Parameter param_ftp = Parameter.getInstance(tt).manualconfig(true).storeDb(false);;
            param_ftp.host(host).port(port).username(username).password(password).security(security).protocol(protocol);
            
            String remotePath2File = attachment.getAttachmentPath() + File.separator + attachment.getAttachmentName();
            InputStream input = FileTransferFactory.getTransfer(param_ftp).download(remotePath2File);
            fileUploader.setStreamBytes(FileUtil.copyFromInputStream(input));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }
    
    /***
     * Hàm xử lí group các công ty theo mã company_union_key.
     * @param model 
     */
    private void _BuildUnionCompany(CompanyModel model) {
        companyRelModelSelected = new UnionCompanyRelModel();
        if (unionCompanyRelModels == null) {
            unionCompanyRelModels = new ArrayList<>();
        } else {
            unionCompanyRelModels.clear();
        }
        
        List<Integer> execludeIds = new ArrayList<>();
        
        // lấy tất cả union-key trong hệ thống.
        List<String> companyUnionKeys = unionCompanyRelService.findAllUnionKey();
        for (String companyUnionKey : companyUnionKeys) {
            List<Integer> ids = new ArrayList<>();
            
            // ứng với mỗi union-key lấy toàn bộ groups công ty.
            List<UnionCompanyRel> unionCompanyRels = unionCompanyRelService.findByUnionKey(companyUnionKey);
            // lấy id của từng công ty.
            for (UnionCompanyRel unionCompanyRel : unionCompanyRels) ids.add(unionCompanyRel.getUnionCompanyRelPK().getCompanyId());
            
            // lưu những ids công ty là groups.
            execludeIds.addAll(ids);
            
            unionCompanyRelModels.add(new UnionCompanyRelModel(companyUnionKey, ids, companys));
        }
        
        // lấy những công ty chưa phải là groups.
        List<Company> companyWithExecludeIds = null;
        if (execludeIds.isEmpty()) {
            companyWithExecludeIds = companyService.findAll();
        } else {
            companyWithExecludeIds = companyService.findWithExecludeIds(execludeIds);
        }
        for (Company c : companyWithExecludeIds) {
            List<Integer> ids = new ArrayList<>();
            ids.add(c.getCompanyId());
            unionCompanyRelModels.add(new UnionCompanyRelModel(c.getCompanyId().toString(), ids, companys));
        }

        // trường hợp là tạo mới công ty. Tôi cũng lưu với union-key là -1
        if (model.getCompany().getCompanyId().equals(MASK_COMPANYID)) {
            List<Integer> ids = new ArrayList<>();
            ids.add(model.getCompany().getCompanyId());
            unionCompanyRelModels.add(new UnionCompanyRelModel(model.getCompany().getCompanyId().toString(), ids, companys));
        }
    }
    
    private void loadAllCompanys() {
        this.companys = companyService.findAll();
    }
    
    private void loadPrefectures() {
        if(prefectures == null) prefectures = new ArrayList<>();
        else prefectures.clear();
        prefectures.addAll(prefectureService.findCities(getLocale()));
    }

    public void loadDatabaseServers() {
        databaseServers.clear();
        databaseServers.addAll(databaseServerService.findAll());
    }
        
    /***
     * Hàm xử lí lấy địa chỉ kana, kannji từ mã zipcode.
     * Nếu có trong db thì mới lấy.
     * @param zipCode 
     */
    private void _BuildPrefectureFromZipCode(String zipCode) {
        if(StringUtils.isEmpty(zipCode)) return;
        //prefectures.clear();
        String locale = UserModel.getLogined().getLanguage();
        ZipCode zc = zipCodeService.findByZipCode(zipCode, locale);
        if(zc == null || zc.getCodeId() == null) return;
        String cityKaneJii = zc.getCityKannji();
        List<Prefecture> listPrefecture = prefectureService.findCities(locale, cityKaneJii);
        if(!listPrefecture.isEmpty()) companyModel.getCompany().setCompanyCity(listPrefecture.get(0).getPrefectureId());
        if(StringUtils.isEmpty(companyModel.getCompany().getCompanyAddress())){
            companyModel.getCompany().setCompanyAddress(String.format("%s%s", zc.getAddressKannji(), zc.getDistrictKanj()));
        }
        if(StringUtils.isEmpty(companyModel.getCompany().getCompanyAddressKana())){
            companyModel.getCompany().setCompanyAddressKana(String.format("%s%s%s", zc.getCityKana(), zc.getAddressKana(), zc.getDistrictKana()));
        }
    }

    /***
     * Hàm chung xử lí lấy thông tin company.
     * @param companyId
     * @param type 
     */
    private void _Load(Integer companyId, String type) {
        loadPrefectures();
        loadAllCompanys();
        loadDatabaseServers();
        
        if ("view".equals(type) || "edit".equals(type)) {
            if("edit".equals(type) && !this.databaseServers.isEmpty()) {
                this.setDbServer(this.databaseServerService.findOneDatabaseServer(companyId));
            }
            this.companyModel = new CompanyModel(companyService.find(companyId));
        } else {
            this.companyModel = new CompanyModel();
            companyModel.getCompany().setCompanyId(MASK_COMPANYID);
        } // end if;
        
        basicAuthUser = companyModel.getCompany().getCompanyBasicLoginId();
        _UpdateCompanyInfor(type);
    }
    
    private void _ParseGlobalIp() {
        StringBuilder sz = new StringBuilder();
        List<CompanyTargetInfo> companyIps = companyModel.getCompanyIps();
        for(CompanyTargetInfo cti : companyIps) {
            if(StringUtils.isEmpty(cti.getCompanyTargetData())) continue;
            sz.append(cti.getCompanyTargetData()).append(",");
        }
        companyModel.getCompany().setCompanyGlobalIp(sz.toString());
    }
    
    @PostConstruct
    public void init() {
        this.groupCompanys = new CompanyLazyList(this);
        this.prefectures = new ArrayList<>();
        this.query = "1=1";
        this.themes = LayoutUtil.getThemeRolle();
        this.fileUploader = new FileUploader();
    }

    @SecureMethod(SecureMethod.Method.VIEW)
    @Override
    public void show(ActionEvent event) {
        try {
            final String showType = this.getParameter("showType");
            if ("view".equals(showType)) {
                _Load(Integer.parseInt(this.getParameter("companyId")), showType);
                layout.setCenter("/modules/company/view.xhtml");
            } else if ("edit".equals(showType)) {
                _Load(Integer.parseInt(this.getParameter("companyId")), showType);
                layout.setCenter("/modules/company/edit.xhtml");
            } else if ("create".equals(showType)) {
                _Load(null, showType);
                layout.setCenter("/modules/company/create.xhtml");
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    @SecureMethod(SecureMethod.Method.UPDATE)
    @Override
    public void update(ActionEvent event) {
        try {
            _ParseGlobalIp();
            Company company = companyModel.getCompany();
            byte[] bytes = fileUploader.getBytes();
            companyService.edit(UserModel.getLogined().getCompany(), company, UserModel.getLogined().getMember(), fileUploader.getBytes(),
                    companyModel.getCompanyPhones(), companyModel.getCompanyPhonesDeleted(),
                    companyModel.getCompanyFaxs(), companyModel.getCompanyFaxsDeleted(),
                    companyModel.getCompanyEmails(), companyModel.getCompanyEmailsDeleted(),
                    companyModel.getCompanyHomepages(), companyModel.getCompanyHomepagesDeleted(),
                    unionCompanyRelModels);
            if(UserModel.getLogined().getCompany() != null && UserModel.getLogined().getCompany().getCompanyId().equals(company.getCompanyId())) {
                UserModel.getLogined().setStreamBytes(bytes);
            }
            this.load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.update.success", company.getCompanyName()));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            JsfUtil.addErrorMessage(e, "システムにエラーが発生している");
        }
    }

    /**
     * Hàm xử lí loại bỏ công ty hiện tại đang xem khỏi groups nếu nó đang là group công ty.
     * @param event 
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void leftGroup(ActionEvent event) {
        _BuildUnionCompany(companyModel);
        
        // loai cong ty hien tai dang xem khoi danh sach groups.
        for (UnionCompanyRelModel ucrm : unionCompanyRelModels) {
            if (ucrm.getIds().contains(companyModel.getCompany().getCompanyId())) {
                ucrm.getIds().remove(companyModel.getCompany().getCompanyId());
            }
        }

        // tach cong ty dang xem la 1 item rieng biet.
        List<Integer> ids = new ArrayList<>();
        ids.add(companyModel.getCompany().getCompanyId());
        
        List<Company> cl = companyService.findAll();
        unionCompanyRelModels.add(new UnionCompanyRelModel(companyModel.getCompany().getCompanyId().toString(), ids, cl));
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void handleFileUpload(FileUploadEvent event) {
        fileUploader.load(event.getFile());
        companyModel.getCompany().setCompanyLogo( event.getFile().getFileName());
        JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.upload.success", event.getFile().getFileName()));
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onBlurCompanyPost(AjaxBehaviorEvent event) {
        UIInput input = (UIInput) event.getSource();
        String zipCode = StringUtils.EMPTY;
        if (input.getValue() != null) {
            zipCode = input.getValue().toString();
        }
        _BuildPrefectureFromZipCode(zipCode);
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void addInfo(ActionEvent event) {
        String type = getParameter("type");
        List<CompanyTargetInfo> ctis = companyModel.getExtraInfo(type);
        ctis.add(new CompanyTargetInfo(new CompanyTargetInfoPK(0 - (ctis.size() + 1))));
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void removeInfos(CompanyTargetInfo cti) {
        cti.setCompanyTargetDeleted((short) 1);
        String type = getParameter("type");
        List<CompanyTargetInfo> companyTargetInfos = companyModel.getExtraInfo(type);
        List<CompanyTargetInfo> companyTargetInfosDeleted = companyModel.getExtraInfoMarkDeleted(type);
        companyTargetInfosDeleted.add(cti);
        companyTargetInfos.remove(cti);
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    @Override
    public void save(ActionEvent event) {
        try {
            _ParseGlobalIp();
            this.companyModel.getCompany().setCompanyId(null);
            Company company = this.companyModel.getCompany();
            companyService.insert(UserModel.getLogined().getCompany(), company, UserModel.getLogined().getMember(), fileUploader.getBytes(),
                    companyModel.getCompanyPhones(), companyModel.getCompanyPhonesDeleted(),
                    companyModel.getCompanyFaxs(), companyModel.getCompanyFaxsDeleted(),
                    companyModel.getCompanyEmails(), companyModel.getCompanyEmailsDeleted(),
                    companyModel.getCompanyHomepages(), companyModel.getCompanyHomepagesDeleted(),
                    unionCompanyRelModels);
            load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.create.success", company.getCompanyName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, "システムにエラーが発生している");
        }
    }

    @SecureMethod(SecureMethod.Method.DELETE)
    @Override
    public void delete(ActionEvent event) {
        try {
            String companyId = this.getParameter("companyId");
            Company company = companyService.find(Integer.valueOf(companyId));
            company.setCompanyDeleted((short) 1);
            companyService.edit(company);

            load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.delete.success", company.getCompanyName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        if (sec.isMaster()) {
            this.layout.setCenter("/modules/company/index.xhtml");
        } else {
            _Load(UserModel.getLogined().getCompanyId(), "view");
            this.layout.setCenter("/modules/company/view.xhtml");
        }
    }

    @SecureMethod(value=SecureMethod.Method.SEARCH) public void reload() {}
    @Override
    protected void doSearch(SearchFilter filter) {
        query = filter != null && !StringUtils.isEmpty(filter.getQuery()) ? filter.getQuery() : "1=1";
    }

    @Override
    @SecureMethod(value=SecureMethod.Method.DOWNLOAD)
    public void download(String tblName, String fileName) {
        super.download(tblName, fileName);
    }
    
    /**
     * Hàm trả về danh sách {@link Prefecture} sử dụng trong search module.
     * @return 
     */
    public String getPrefectureList() {
        List<Prefecture> prefectureList = prefectureService.findAll();
        JsonArray json = new JsonArray();
        prefectureList.forEach((t) -> {
            JsonObject jo = new JsonObject();
            jo.addProperty("value", t.getPrefectureCode());
            jo.addProperty("label", t.getPrefectureName());

            json.add(jo);
        });

        Gson gson = new Gson();
        return gson.toJson(json);
    }

    /**
     * Ham xu li khi chon mot group nao do cho cong ty dang xem.
     * @param event 
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onUnionCompanySelect(SelectEvent event) {
        // lay group ma nguoi dung chon tu danh sach groups.
        UnionCompanyRelModel ucrmSel = ((UnionCompanyRelModel) event.getObject());

        // lay group ma cong ty hien tai dang ton tai.
        UnionCompanyRelModel updated = null;
        for (UnionCompanyRelModel crm : unionCompanyRelModels) {
            if (crm.getIds().contains(companyModel.getCompany().getCompanyId())) {
                updated = crm; break;
            }
        }

        // chuyen tat ca cac cong ty thuoc group nguoi dung chon sang group cong ty dang xem.
        if (updated != null) {
            updated.getIds().addAll(ucrmSel.getIds());
            // not remove from list, need get old unionkey for delete.
            ucrmSel.setIds(new ArrayList<>());
        }
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onViewCompanyChange() {
        _Load(companyModel.getViewCompanyId(), "view");
    }

    public String getDisplayUnionCompany() {
        if (companyModel.getCompany().getCompanyId() != null) {
            for (UnionCompanyRelModel model : unionCompanyRelModels) {
                if (model.getIds().size() > 1
                        && model.getIds().contains(companyModel.getCompany().getCompanyId())) {
                    return model.getDisplayGroup();
                }
            }
        }
        return StringUtils.EMPTY;
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void updateBasicAuthPassword(){
        try {
            Integer companyId = this.companyModel.getCompany().getCompanyId();
            if(basicAuthUser == null) basicAuthUser = StringUtils.EMPTY;
            if(basicAuthUser.length() < 6 || basicAuthUser.length() > 15 || companyService.checkExistBasicAuth(companyId, basicAuthUser)){
                JsfUtil.addErrorMessage(companyBundle.getString("label.company.basic.user.failure"));
                return;
            }
            if(basicAuthPassword == null) basicAuthPassword = StringUtils.EMPTY;
            if(StringUtil.validatePassword(basicAuthPassword)){
                String encodePassword = EncoderUtil.getPassEncoder().encode(basicAuthPassword);
                int returnFlag = companyService.saveCompanyBasicAuth(companyId, basicAuthUser, encodePassword);
                if(returnFlag == 1){
                    //success
                    JsfUtil.addSuccessMessage(companyBundle.getString("label.company.basic.update.success"));
                    RequestContext.getCurrentInstance().execute("PF('basicAuthPassword').hide()");
                }else{
                    JsfUtil.addErrorMessage(companyBundle.getString("label.company.basic.update.failure"));
                }
            }else{
                JsfUtil.addErrorMessage(companyBundle.getString("label.company.basic.require"));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e.getMessage());
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.PRINT)
    public void print(){};
}
