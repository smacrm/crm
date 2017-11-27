package gnext.controller.system;

import gnext.bean.label.PropertyItemLabel;
import gnext.controller.common.LayoutController;
import gnext.controller.common.LocaleController;
import gnext.exporter.Export;
import gnext.exporter.excel.LabelExport;
import gnext.importer.Import;
import gnext.importer.excel.LabelImport;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.label.LabelService;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration controller
 *
 * @author hungpham
 * @since 2016/10
 */
@ManagedBean(name = "labelController")
@SessionScoped
@SecurePage(module = SecurePage.Module.SYSTEM)
public class LabelController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(LabelController.class);

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @ManagedProperty(value = "#{localeController}")
    @Getter @Setter private LocaleController localeController;
    
    @EJB private LabelService labelServiceImpl;
    
    @Getter @Setter private List<PropertyItemLabel> list;
    @Getter @Setter private PropertyItemLabel label;
    @Getter private Map<String, String> resourceList;
    @Getter private Map<String, String> moduleList;
    
    private int companyId;
    private List<String> dbKeys;
    @Setter @Getter private String currentLanguage;
    
    private String exportFileName;
    @Setter @Getter private UploadedFile uploadedFile;
    
    @PostConstruct
    public void init(){
        companyId = UserModel.getLogined().getCompanyId();
        
        try {
            exportFileName = URLEncoder.encode(JsfUtil.getResource().message(ResourceUtil.BUNDLE_LABEL, "label.manager.download.file"), "UTF-8");
        } catch (UnsupportedEncodingException ex) {}
           
        
        list = labelServiceImpl.findByCompany(companyId);
        currentLanguage = localeController.getLocale();
        
        moduleList = new HashMap<>();
        final Pattern pattern = Pattern.compile("resource\\.([a-z]+)\\.gnext\\.resource\\.([a-z]+)([\\.\\w]*)");
        
        List<String> existsKey = new ArrayList<>();
        list.forEach((t) -> {
            Matcher matcher = pattern.matcher(t.getPk().getItemCode());
            if( matcher.find() ){
                String module = "gnext.resource."+matcher.group(2);
                t.setModule(module);
                moduleList.put(module, JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MSG, JsfUtil.getResource().getPageName(module)));
            }
            existsKey.add(t.getPk().getItemCode());
        });
        
        dbKeys = new ArrayList<>(existsKey);
        
        resourceList = new HashMap<>();
        
        final Pattern pattern2 = Pattern.compile("gnext\\.resource\\.([a-z]+)([\\.\\w]*)");
        
        for(final String bundle: ResourceUtil.AVAILABLE_CACHE_BUNDLES){
            String module = null;
            Matcher matcher = pattern2.matcher(bundle);
            if( matcher.find() ){
                module = "gnext.resource."+matcher.group(1);
                if( !moduleList.containsKey(module) ) moduleList.put(module, JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG, JsfUtil.getResource().getPageName(module)));
            }
            
            for (final Locale locale : ResourceUtil.AVAILABLE_CACHE_LOCALES) {
                ResourceBundle rb = ResourceBundle.getBundle(bundle, locale);
                Enumeration<String> keys = rb.getKeys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    String value = rb.getString(key);
                    String resourceKey = String.format("resource.%s.%s.%s", locale.getLanguage(), bundle, key);
                    resourceList.put(resourceKey, value);
                    
                    if( !existsKey.contains(resourceKey) ){
                        //add to list
                        PropertyItemLabel labelItem = new PropertyItemLabel(resourceKey, companyId);
                        labelItem.setLabelName(value);
                        labelItem.setLabelLanguage(locale.getLanguage());
                        labelItem.setModule(module);
                        list.add(labelItem);
                        existsKey.add(resourceKey);
                        if(module == null){
                            System.err.println(">>" + value);
                        }
                    }
                }
            }
        }
        
        resourceList = new TreeMap<>(resourceList);
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void edit(){}
    
    public void onRowEdit(RowEditEvent event) {
        this.label = (PropertyItemLabel) event.getObject();
        try{
            // Fix issue 201 ko chinh sua resource goc khi edit
            //resourceList.put(this.label.getPk().getItemCode(), this.label.getLabelName());
            if(dbKeys.contains(this.label.getPk().getItemCode())){
                labelServiceImpl.edit(this.label);
            }else{
                labelServiceImpl.create(this.label);
                dbKeys.add(this.label.getPk().getItemCode());
                this.label.setCompany(UserModel.getLogined().getCompany());
            }
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(companyId,
                            ResourceUtil.BUNDLE_SYSTEM, "label.system.label.success"));
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(companyId,
                    ResourceUtil.BUNDLE_SYSTEM, "label.system.label.error"));
        }
    }
     
    public void onRowCancel(RowEditEvent event) {
        
    }
    
    public String getLanguageLabel(String langCode){
        return JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG, "language_"+langCode);
    }
    
    public String getModuleLabel(String module){
        return moduleList.get(module);
    }
    
    @SecureMethod(SecureMethod.Method.DELETE)
    public void delete(PropertyItemLabel c){
        try{
            labelServiceImpl.remove(c);
            reload();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(companyId,
                            ResourceUtil.BUNDLE_SYSTEM, "label.system.label.success"));
        }catch(Exception e){
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(companyId,
                            ResourceUtil.BUNDLE_SYSTEM, "label.system.label.error"));
        }
        reload();
    }
    
    @SecureMethod(SecureMethod.Method.DOWNLOAD)
    public void export() throws Exception {
        Export e = new LabelExport(resourceList, moduleList, list, companyId, exportFileName);
        e.execute();
    }
    
    @SecureMethod(value = SecureMethod.Method.UPLOAD)
    public void importData() throws IOException{
        if(this.uploadedFile == null){ JsfUtil.addErrorMessage("エラーがあるので、完了ができない。"); return; }
        try {
            Import i = new LabelImport(resourceList, dbKeys, labelServiceImpl, companyId);
            i.execute(this.uploadedFile.getInputstream());
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_SYSTEM, "label.system.label.success"));
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_SYSTEM, "label.system.label.error"));
        }
        reload();
    }
    
    public void fileUploadListener(FileUploadEvent event) {
        this.uploadedFile = event.getFile();
    }
    
    /**
     * Flush all label to redis cache
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void flush(){
        labelServiceImpl.reloadCache();
        JsfUtil.addSuccessMessage("設定をフラッシュしました。");
    }
    
    @SecureMethod(SecureMethod.Method.SEARCH)
    public void reload(){
        init();
        load();
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load(){
        layout.setCenter("/modules/system/label/list.xhtml");
    }
    
    @SecureMethod(value = SecureMethod.Method.PRINT)
    public void print(){}
}
