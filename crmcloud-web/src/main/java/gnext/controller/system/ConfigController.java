/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.system;

import gnext.bean.config.Config;
import gnext.controller.AbstractController;
import gnext.controller.authority.AuthorityController;
import gnext.controller.common.LayoutController;
import gnext.controller.issue.AdvanceIssueSearchController;
import gnext.model.authority.PageModel;
import gnext.model.authority.UserModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.config.ConfigService;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.utils.InterfaceUtil.SERVER_KEY;
import gnext.validator.BaseValidator;
import gnext.validator.IpValidator;
import gnext.validator.NumberValidator;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration controller
 *
 * @author hungpham
 * @since 2016/10
 */
@ManagedBean(name = "configController")
@SessionScoped
@SecurePage(module = SecurePage.Module.SYSTEM)
public class ConfigController extends AbstractController implements Serializable {
    private static final long serialVersionUID = -3390411487047994210L;
    private final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @ManagedProperty(value = "#{layout}") @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{ais}") @Getter @Setter private AdvanceIssueSearchController ais;
    @ManagedProperty(value = "#{authorityController}") @Getter @Setter private AuthorityController authority;
    
    @EJB private ConfigService configServiceImpl;
    
    @Getter @Setter private List<Config> list;
    @Getter @Setter private Config config;
    
    @Getter @Setter private List<SelectItem> modules;
    
    @Setter @Getter private Integer itemViewIndex = 0;
    private String exportFileName;
    
    private static final Map<String, BaseValidator> cfgValidator = new HashMap<String, BaseValidator>();
    private static final Map<String, String> cfgMessageText = new HashMap<String, String>();
    
    static {
        cfgValidator.put("ELASTIC_PORT", new NumberValidator());
        cfgValidator.put("ELASTIC_HOST", new IpValidator());
        cfgValidator.put("REDIS_HOST", new IpValidator());
        cfgValidator.put("REDIS_PORT", new NumberValidator());

        cfgMessageText.put("ELASTIC_HOST", JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.ip.invalid", "ELASTIC_HOST"));
        cfgMessageText.put("ELASTIC_PORT", JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.digit.negative.notallow", "ELASTIC_PORT"));
        cfgMessageText.put("REDIS_HOST", JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.ip.invalid", "REDIS_HOST"));
        cfgMessageText.put("REDIS_PORT", JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.digit.negative.notallow", "REDIS_PORT"));
    }
    
    @PostConstruct
    public void init(){
        list = configServiceImpl.search(null);
        
        
        try {
            exportFileName = URLEncoder.encode("設定リスト", "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
        }
           
        
        modules = new ArrayList<>();
        TreeNode root = authority.getAuthList();
        int idx = 0;
        for (TreeNode module : root.getChildren()) {
            if(idx++ == 0) continue;
            PageModel moduleData = (PageModel) module.getData();
            if(module.getChildCount() > 0){
                SelectItemGroup group = new SelectItemGroup(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.menu." + moduleData.getName()));
                List<SelectItem> childs = new ArrayList<>();
                module.getChildren().forEach((page) -> {
                    PageModel pageData = (PageModel) page.getData();
                    childs.add(new SelectItem(pageData.getSource(), JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.menu." + pageData.getName())));
                });
                group.setSelectItems(childs.toArray(new SelectItem[]{}));
                modules.add(group);
            }else{
                modules.add(new SelectItem(moduleData.getName(), moduleData.getName()));
            }
        }
    }
        
    @SecureMethod(SecureMethod.Method.DOWNLOAD)
    public String getExportFileName() {
        return exportFileName;
    }
    
    @SecureMethod(value = SecureMethod.Method.CREATE, require = false)
    public void create(){
        this.config = new Config();
        this.config.setCreatorId(UserModel.getLogined().getUserId());
        this.config.setUpdatedId(UserModel.getLogined().getUserId());
        this.config.setConfigDeleted((short)0);
        showForm();
    }
    
    @SecureMethod(value = SecureMethod.Method.CREATE, require = false)
    public void onCreate(){
        try{
            BaseValidator baseValidator = cfgValidator.get(this.config.getConfigKey());
            if (baseValidator != null && !baseValidator.doValidate(config.getConfigValue())) {
                _FocusConfigValue();
                return;
            }
            this.config.setCreatedTime(new Date());
            this.config.setUpdatedTime(new Date());
            configServiceImpl.create(this.config);
            JsfUtil.addSuccessMessage("編集しました。");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JsfUtil.addErrorMessage("エラーがありますので、確認してください。");
        }
        reload();
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void update(Config c, Integer itemViewIndex){
        this.itemViewIndex = itemViewIndex;
        this.config = c;
        showForm();
    }
    
    /**
     * Show create/update form by item index (next/prev)
     * @param itemViewIndex 
     */
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void updateByIndex(Integer itemViewIndex){
        if(list.size() > itemViewIndex){
            config = list.get(itemViewIndex);
            this.update(config, itemViewIndex);
        }
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void onUpdate(){
        try{
            BaseValidator baseValidator = cfgValidator.get(this.config.getConfigKey());
            if (baseValidator != null && !baseValidator.doValidate(config.getConfigValue())) {
                _FocusConfigValue();
                return;
            }
            this.config.setUpdatedId(UserModel.getLogined().getUserId());
            this.config.setUpdatedTime(new Date());
            
            configServiceImpl.edit(this.config);
            
            if(this.config.getConfigKey().equals(SERVER_KEY.ELASTIC)){
                ais.setQuickSearch(this.config.getConfigValue().equalsIgnoreCase("true"));
            }
            
            JsfUtil.addSuccessMessage("変更しました。");
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            JsfUtil.addErrorMessage("エラーがありますので、確認してください。");
        }
        reload();
    }
    
//    @SecureMethod(SecureMethod.Method.UPDATE)
//    public void quickUpdateProperty(String key, String value){
//        this.config = configServiceImpl.findByKey(key);
//        this.config.setConfigValue(value);
//        try{
//            this.config.setUpdatedId(UserModel.getLogined().getUserId());
//            this.config.setUpdatedTime(new Date());
//            
//            configServiceImpl.edit(this.config);
//            
//            if(this.config.getConfigKey().equals(SERVER_KEY.ELASTIC)){
//                ais.setQuickSearch(this.config.getConfigValue().equalsIgnoreCase("true"));
//            }
//            
//        }catch(Exception e){
//            logger.error(e.getMessage(), e);
//        }
//    }
    
    @SecureMethod(SecureMethod.Method.DELETE)
    public void delete(Config c){
        try{
            configServiceImpl.remove(c);
            reload();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), 
                    ResourceUtil.BUNDLE_SYSTEM, 
                    "label.system.config.delete.success"));
        }catch(Exception e){
            JsfUtil.addSuccessMessage("エラーがありますので、確認してください。");
        }
        reload();
    }
    
    private void showForm(){
        layout.setCenter("/modules/system/config/form.xhtml");
    }
    
    /**
     * Flush all setting to redis cache
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void flush(){
        configServiceImpl.reloadCache();
        JsfUtil.addSuccessMessage("設定をフラッシュしました。");
    }
    
    @SecureMethod(SecureMethod.Method.SEARCH)
    public void reload(){
        load();
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load(){
        layout.setCenter("/modules/system/config/list.xhtml");
        init();
    }

    @Override
    protected void doSearch(SearchFilter filter) {
        String query = filter != null ? filter.getQuery() : "";
        
        list = configServiceImpl.search(query);
    }
    
    public String getConfig(String key){
        return configServiceImpl.get(key);
    }
    
    /**
     * Hàm xử lí lấy message từ {@link ConfigController.modules} theo key.
     * @param key
     * @param value
     * @return 
     */
    public String getMessage(String key, String value) {
        if(!"DEFAULT_PAGE".equals(key)) return value;
        
        for (SelectItem selectItemGroup : modules) {
            SelectItemGroup itemGroup = (SelectItemGroup) selectItemGroup;
            for (SelectItem selectItem : itemGroup.getSelectItems()) {
                if (value.equals(selectItem.getValue())) {
                    return selectItem.getLabel();
                }
            }
        }
        return value;
    }
    
    /**
     * Thông báo lỗi ở input config_value
     */
    private void _FocusConfigValue() {
        UIComponent component = JsfUtil.findComponent("config_value");
        if(component == null) return;
        UIInput uIInput = (UIInput) component;
        uIInput.setValid(false);
        uIInput.setValue(config.getConfigValue());
        JsfUtil.addErrorMessage(cfgMessageText.get(config.getConfigKey()));
    }
    
    @SecureMethod(value = SecureMethod.Method.PRINT, require = false)
    public void print(){}
}
