/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail;

import com.google.gson.Gson;
import gnext.bean.mail.MailExplode;
import gnext.bean.mail.MailFilter;
import gnext.bean.mail.MailFolder;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.model.BaseModel;
import gnext.model.authority.UserModel;
import gnext.model.mail.MailFilterModel;
import gnext.model.mail.MailFolderModel;
import gnext.model.search.SearchField;
import gnext.model.search.SearchFilter;
import gnext.model.search.SearchGroup;
import gnext.resource.bundle.MailBundle;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.mail.MailExplodeService;
import gnext.service.mail.MailFilterService;
import gnext.service.mail.MailFolderService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.event.ActionEvent;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "mailFilterController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.MAIL, require = true)
public class MailFilterController extends AbstractController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailFilterController.class);
    private static final Short ZERO = 0;
    private static final long serialVersionUID = -8918704162081119843L;

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;

    @Getter @Setter private MailFilterModel model;
    @Getter @Setter private List<MailFilterModel> models = new ArrayList<>();
    private String oldMailFilterTitle;

    @EJB private MailFilterService mailFilterService;
    @EJB private MailFolderService mailFolderService;
    @EJB private MailExplodeService mailExplodeService;
    
    @Getter @Setter private List<MailFolder> mailFolders;
    @Getter @Setter private List<MailExplode> mailExplodes;
    @Getter @Setter private boolean displayMailFolderFlag;
    @Getter @Setter private boolean displayMailExplodeFlag;
    
     private void _Load(Integer mailExplodeId) {
        _Load();
        for (MailFilterModel model : models) {
            if (model.getMailFilter().getMailFilterId().intValue()
                    == mailExplodeId.intValue()) {
                this.model = model;
                break;
            }
        }
    }

    private void _Load() {
        this.model = null;
        models.clear();
        List<MailFilter> mailFilters
                = mailFilterService.search(getCurrentCompanyId(),(short)0);
        for (int i = 0; i < mailFilters.size(); i++) {
            MailFilterModel tmp = new MailFilterModel(mailFilters.get(i));
            if(mailFilters.get(i).getMailFilterOrder() != null) {
                tmp.setRowNum(mailFilters.get(i).getMailFilterOrder());
            } else {
                tmp.setRowNum(1);
            }
            this.models.add(tmp);
        }
    }

    @PostConstruct
    public void init() {
        _Load();
    }

    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        init();
        layout.setCenter("/modules/mail/filter/index.xhtml");
    }

    @Override
    protected List<? extends BaseModel> getUFDLModels() {
        return models;
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    @Override
    public void show(ActionEvent event) {
        final String showType = this.getParameter("showType");
        setOriginData();
        if("edit".equals(showType)){
            Integer mailFilterid = Integer.parseInt(this.getParameter("mailFilterId"));
            MailFilterModel mailFilterModel = find(mailFilterid);
            this.model = mailFilterModel;
            this.searchDataJson = this.model.getMailFilter().getMailFilterConditions();
            this.oldMailFilterTitle = mailFilterModel.getMailFilter().getMailFilterTitle();
            setDisableEnableCheckBox();
        }else if("create".equals(showType)){
            model = new MailFilterModel(new MailFilter());
        }
        mailFolders = new ArrayList<>();
        MailBundle msg = new MailBundle();
        mailFolders.add(new MailFolder(Integer.valueOf(MailFolder.DATA_MAIL_FOLDER_TRASH), msg.getString("label.mail.folder.trash")));
        mailFolders.add(new MailFolder(Integer.valueOf(MailFolder.DATA_MAIL_FOLDER_JUNK), msg.getString("label.mail.folder.junk")));
        
        findMailFolder();
        findMailExplode();
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    @Override
    public void save(ActionEvent event) {
        try {
            MailFilter mailFilter = this.model.getMailFilter();
            if(checkDuplicateMailFilterTitle(mailFilter)){
                focusMailFilterTitle(mailFilter.getMailFilterTitle());
                return;
            }
            mailFilter.setCreatedTime(DateUtil.now());
            mailFilter.setCreatorId(UserModel.getLogined().getUserId());
            mailFilter.setMailFilterDeleted((short) 0);
            mailFilter.setCompany(getCurrentCompany());
            String jsonString = getStringJson();
            if ("".equals(jsonString)) {
                JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME,
                        "validator.mail.filter", ""));
                return;
            }
            mailFilter.setMailFilterConditions(jsonString);
            findMailFilterFolderName(mailFilter);
            // set mail filter order
            Integer maxOrder = mailFilterService.getMaxOrder(getCurrentCompanyId());
            if(maxOrder == null) maxOrder = 0;
            mailFilter.setMailFilterOrder(maxOrder + 1);
            
            mailFilterService.create(mailFilter);
            _Load();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG,
                "msg.action.create.success", mailFilter.getMailFilterTitle()));
            _Load();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }
    @Override
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void update(ActionEvent event) {
        try {
            MailFilter mailFilter = this.model.getMailFilter();
            if(checkDuplicateMailFilterTitle(mailFilter)){
                mailFilter.setMailFilterTitle(oldMailFilterTitle);
                focusMailFilterTitle(mailFilter.getMailFilterTitle());
                return;
            }
            mailFilter.setUpdatedTime(DateUtil.now());
            mailFilter.setUpdatedId(UserModel.getLogined().getUserId());
            String jsonString = getStringJson();
            if("".equals(jsonString)){
                JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME,
                    "validator.mail.filter", ""));
                return; 
            }
            mailFilter.setMailFilterConditions(jsonString);
            setValueNotCheckBox(mailFilter);
            mailFilterService.edit(mailFilter);
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG,
                    "msg.action.update.success", mailFilter.getMailFilterTitle()));
            _Load();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    public String getStringJson() {
        Gson gson = new Gson();
        SearchGroup[] groups = gson.fromJson(getSearchDataJson(), SearchGroup[].class);
        for(SearchGroup filter : groups){
            for(SearchField field : filter.getFilters()){
                if(!StringUtils.isEmpty(field.getValue()) || "bl".equals(field.getOperator())){
                    return new Gson().toJson(groups);
                }
            }
        }
        return String.valueOf("");
    }
    
    public void findMailFilterFolderName(MailFilter mailFilter) {
        for (MailFolder mailFolder : mailFolders) {
            if (!StringUtils.isEmpty(mailFilter.getMailFilterMoveFolderCode())
                    && mailFilter.getMailFilterMoveFolderCode().equals(String.valueOf(mailFolder.getMailFolderId()))) {
                mailFilter.setMailFilterMoveFolderName(mailFolder.getMailFolderName());
                break;
            }
        }
    }
    
    private MailFilterModel find(Integer mailFilterId) {
        for (MailFilterModel mailFilter : models) {
            if (mailFilter.getMailFilter().getMailFilterId().intValue()
                    == mailFilterId.intValue()) {
                return mailFilter;
            }
        }
        return null;
    }
    
    private boolean checkDuplicateMailFilterTitle(MailFilter mf){
        // case insert
        if(mf.getMailFilterId() == null && mailFilterService.search(getCurrentCompanyId(), mf.getMailFilterTitle()) != null){
            return true;
        // case edit
        }else {
            if(!mf.getMailFilterTitle().equals(oldMailFilterTitle)){
                if(mailFilterService.search(getCompanyId(), mf.getMailFilterTitle()) != null){
                    return true;
                }
            }
        }
        return false;
    }
    
    private void focusMailFilterTitle(String value){
        UIComponent component = JsfUtil.findComponent("j_mail_filter_title");
        if(component == null) return;
        UIInput uIInput = (UIInput) component;
        uIInput.setValid(false);
        uIInput.setValue(value);
        String title = uIInput.getAttributes().get("title").toString();
        JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.mailfilter.duplicate", title));
    }
    
    @SecureMethod(SecureMethod.Method.COPY)
    public void copy(ActionEvent event) {
        try {
            Integer mailFilterId = Integer.parseInt(getParameter("mailFilterId"));
            MailFilter mailFilter = mailFilterService.find(mailFilterId);
            mailFilter.setMailFilterTitle(mailFilter.getMailFilterTitle() + " COPY");
            mailFilter.setMailFilterId(null);
            mailFilterService.create(mailFilter);
            _Load(mailFilter.getMailFilterId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    @Override
    @SecureMethod(SecureMethod.Method.DELETE)
    public void delete(ActionEvent event) {
        try {
            Integer mailFilterId = Integer.parseInt(getParameter("mailFilterId"));
            MailFilterModel mailFilterModel = find(mailFilterId);

            MailFilter mailFilter = mailFilterModel.getMailFilter();
            mailFilter.setMailFilterDeleted((short) 1);
            mailFilterService.edit(mailFilter);
            models.remove(mailFilterModel);

            // Kiểm tra nếu Filter là đang được chọn thì cần reload lại Page.
            if (this.model != null
                    && this.model.getMailFilter() != null
                    && this.model.getMailFilter().getMailFilterId() != null
                    && mailFilterId.intValue() == this.model.getMailFilter().getMailFilterId().intValue()) {
                _Load();
            }

            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG,
                    "msg.action.delete.success", mailFilter.getMailFilterTitle()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    private int getCompanyId(){
        return UserModel.getLogined().getCompanyId();
    }
    
    private void findMailFolder(){
         List<MailFolder> mMailFolders = mailFolderService.search(getCompanyId(), (short) 0);
         mailFolders.addAll(mMailFolders);
    }
    
    private void findMailExplode(){
        mailExplodes = mailExplodeService.search(getCompanyId(), ZERO);
    }
    
    public void onMailExploderChange(){
        displayMailExplodeFlag = StatusUtil.getBoolean(this.model.getMailFilter().getMailFilterUseSettingExplodeFlag());
    }
    
    public void onMailFolderChange(){
        displayMailFolderFlag = StatusUtil.getBoolean(this.model.getMailFilter().getMailFilterMoveFolderFlag());
    }
    
    public boolean showBackButton() {
        return this.showAddPage() || this.showEditPage();
    }
    
    public boolean showAddPage() {
        return this.model != null
                && this.model.getMailFilter()!= null
                && this.model.getMailFilter().getMailFilterId()== null;
    }

    public boolean showEditPage() {
        return this.model != null
                && this.model.getMailFilter()!= null
                && this.model.getMailFilter().getMailFilterId()!= null;
    }
    
    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean showTipPage() {
        return this.model == null;
    }

    private void setOriginData() {
        this.searchDataJson = StringUtils.EMPTY;
        displayMailFolderFlag = Boolean.FALSE;
        displayMailExplodeFlag = Boolean.FALSE;
    }

    private void setDisableEnableCheckBox() {
        displayMailFolderFlag = StatusUtil.getBoolean(this.model.getMailFilter().getMailFilterMoveFolderFlag());
        displayMailExplodeFlag = StatusUtil.getBoolean(this.model.getMailFilter().getMailFilterUseSettingExplodeFlag());
    }

    private void setValueNotCheckBox(MailFilter mailFilter) {
        if (!StatusUtil.getBoolean(this.model.getMailFilter().getMailFilterMoveFolderFlag())) {
            mailFilter.setMailFilterMoveFolderCode(StringUtils.EMPTY);
            mailFilter.setMailFilterMoveFolderName(StringUtils.EMPTY);
        } else {
            findMailFilterFolderName(mailFilter);
        }
        
        if (!StatusUtil.getBoolean(this.model.getMailFilter().getMailFilterUseSettingExplodeFlag())) {
            mailFilter.setMailFilterMailExplodeId((short) 0);
        }
    }
    
    public String activeMenu(Integer mailFilterId) {
        if (this.model != null
                && this.model.getMailFilter() != null
                && this.model.getMailFilter().getMailFilterId() != null
                && mailFilterId.intValue() == this.model.getMailFilter().getMailFilterId().intValue()) {
            return "bold";
        }

        return "";
    }
    
    @Override
    protected void afterReSort() {
        try {
            for (MailFilterModel mem : models) {
                mem.getMailFilter().setMailFilterOrder(mem.getRowNum());
                mailFilterService.edit(mem.getMailFilter());
            }
            _Load();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
