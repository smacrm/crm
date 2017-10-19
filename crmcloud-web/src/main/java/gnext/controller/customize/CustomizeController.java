package gnext.controller.customize;

import com.google.gson.Gson;
import gnext.bean.Company;
import gnext.bean.customize.AutoFormDiv;
import gnext.bean.customize.AutoFormItem;
import gnext.bean.customize.AutoFormPageTab;
import gnext.bean.customize.AutoFormPageTabDivItemRel;
import gnext.bean.customize.AutoFormTab;
import gnext.bean.role.Page;
import gnext.bean.role.SystemModule;
import gnext.controller.common.LayoutController;
import gnext.controller.common.LocaleController;
import gnext.controller.common.LoginController;
import gnext.controller.issue.ProjectController;
import gnext.model.authority.UserModel;
import gnext.model.customize.Div;
import gnext.model.customize.Field;
import gnext.model.customize.Tab;
import gnext.security.SecurityService;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.security.annotation.SecurePage.Module;
import gnext.service.customize.AutoFormDivService;
import gnext.service.customize.AutoFormItemService;
import gnext.service.customize.AutoFormMultipleDataValueService;
import gnext.service.customize.AutoFormPageTabService;
import gnext.service.customize.AutoFormTabService;
import gnext.service.role.PageService;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StringUtil;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Customize module
 *
 * @author hungpham
 * @since 2016/10
 */

@ManagedBean
@SecurePage(module = Module.SYSTEM)
@SessionScoped() 
public class CustomizeController implements Serializable {
    private static final long serialVersionUID = -3111592361662910085L;
    private final Logger logger = LoggerFactory.getLogger(CustomizeController.class);
    
    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @ManagedProperty(value = "#{localeController}")
    @Getter @Setter private LocaleController locale;
    
    @ManagedProperty(value = "#{projectController}")
    @Getter @Setter private ProjectController projectController;
    
    @ManagedProperty(value = "#{auth}")
    @Getter @Setter private SecurityService securityService;
    
    @ManagedProperty(value = "#{loginController}")
    @Getter @Setter private LoginController loginController;
    
    @EJB private PageService staticPageServiceImpl;
    @EJB private AutoFormPageTabService pageTabServiceImpl;
    @EJB private AutoFormItemService itemServiceImpl;
    @EJB private AutoFormMultipleDataValueService dataValueServiceImpl;
    @EJB private AutoFormTabService tabServiceImpl;
    @EJB private AutoFormDivService divServiceImpl;
    
    @Getter @Setter private String autoFormContent;
    @Getter @Setter private String autoFormDynamicContent;
    @Getter @Setter private List<AutoFormPageTab> pageList;
    @Getter @Setter private LinkedHashMap<String, String> staticPages;
    @Getter @Setter private Map<String, String> staticPageLabel;
    @Getter @Setter private Map<String, Boolean> staticPageUsed;
    @Getter @Setter private AutoFormPageTab selectedPage;
    @Getter @Setter private String originPageName;
    
    private boolean isCreatedForm = false;
    
    @Setter @Getter private Integer itemViewIndex = 0;
    private String exportFileName;
    
    @PostConstruct
    public void init(){
        pageList = loadPageList();
        givenAExportFilename();
        loadStaticPages();
    }
    
    private Integer getCid() {
        return UserModel.getLogined().getCompany().getCompanyId();
    }
    
    private static String[] PAGE_ALLOW_LIST = { "IssueController" };
    
    /**
     * Hàm xử lí lấy tất cả các PAGES có trong hệ thống.
     * những PAGE nào đã được sử dụng để tạo DYNAMIC-FORM thì loại bỏ.
     */
    private void loadStaticPages() {
        staticPages = new LinkedHashMap<>();
        staticPageLabel = new HashMap<>();
        staticPageUsed = new HashMap<>();
        staticPageServiceImpl.findAll().forEach((p) -> {
            String pageName = JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.menu." + p.getPageName());
            staticPages.put(pageName, p.getPageId().toString());
            staticPageLabel.put(p.getPageId().toString(), pageName);
            staticPageUsed.put(p.getPageId().toString(), Boolean.FALSE);
            
            for(String pageAlow : PAGE_ALLOW_LIST) {
                if(!pageAlow.equals(p.getPageName())) staticPageUsed.put(p.getPageId().toString(), Boolean.TRUE);
            }
            
            // TODO: Hiện thời hệ thống sẽ chỉ support cho MODULE ISSUES nên các menu khác sẽ được DISABLED.
            if(p.getModuleList() == null || p.getModuleList().isEmpty())
                staticPageUsed.put(p.getPageId().toString(), Boolean.TRUE);
            else
                for(SystemModule module : p.getModuleList()) {
                    if(!module.getModuleName().equals(SecurePage.Module.ISSUE.toString())) {
                        staticPageUsed.put(p.getPageId().toString(), Boolean.TRUE);
                    }
                }
        });
        staticPageServiceImpl.findCorrelativeTheDynamicForm(getCid()).forEach((p) -> {
            staticPageUsed.put(p.toString(), Boolean.TRUE);
        });
    }
    
    private void givenAExportFilename() {
        try {
            exportFileName = URLEncoder.encode("カスタマイズ リスト", "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    
    @SecureMethod(SecureMethod.Method.DOWNLOAD)
    public String getExportFileName() {
        return exportFileName;
    }
    
    private List<AutoFormPageTab> loadPageList(){
        return pageTabServiceImpl.search(getCid());
    }
    
    public String getPageName(AutoFormPageTab p) {
        if(!p.isDynamic()) return p.getPageName();
        if(staticPageLabel.containsKey(p.getPageName())) return staticPageLabel.get(p.getPageName());
        return p.getPageName();
    }
    
    public boolean isDisable(String pageId) {
        if(selectedPage != null && selectedPage.getPageType() != null && selectedPage.isDynamic() && selectedPage.getPageName().equals(pageId)) return false;
        if(staticPageUsed.containsKey(pageId)) return staticPageUsed.get(pageId);
        return true;
    }

    /**
     * Show create form
     */
    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void showCreateForm(){
        selectedPage = new AutoFormPageTab();
        
        // kiểm tra trong danh sách MODULE cho phép thêm mới DYNAMIC-TAB nếu
        // cho phép thì lựa chọn nó đầu tiên.
        for (Map.Entry<String, Boolean> entry : staticPageUsed.entrySet()) {
            if(!entry.getValue()) {
                selectedPage.setPageId(Integer.parseInt(entry.getKey()));
                break;
            }
        }
        
        autoFormContent = null;
        isCreatedForm = true;
        layout.setCenter("/modules/customize/form.xhtml");
    }

    /**
     * Show update form
     * 
     * @param page 
     * @param itemViewIndex 
     */
    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void showUpdateForm(AutoFormPageTab page, Integer itemViewIndex){
        this.itemViewIndex = itemViewIndex;
        selectedPage = page;
        originPageName = selectedPage.getPageName();
        isCreatedForm = false;
        autoFormContent = getFormBuilderData(page, 0);
        layout.setCenter("/modules/customize/form.xhtml");
    }
    
    /**
     * Show update form by index next/prev
     * 
     * @param itemViewIndex 
     */
    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void showUpdateFormByIndex(Integer itemViewIndex){
        if(pageList.size() > itemViewIndex){
            selectedPage = pageList.get(itemViewIndex);
            this.showUpdateForm(selectedPage, itemViewIndex);
        }
    }
    
    /**
     * Get form builder data for update form
     * 
     * @param page
     * @param refId
     * @return 
     */
    private String getFormBuilderData(AutoFormPageTab page, int refId){
        if(null == page) return null;
        
        Map<Integer, String> dataMap =  refId > 0  ? dataValueServiceImpl.findItemData(page.getPageId(), refId, loginController.getMember().getCompanyId()) : new HashMap<>();
        
        List<AutoFormPageTabDivItemRel> listItemRel =  selectedPage.getAutoFormPageTabDivItemRelList();
        Gson gson = new Gson();
        LinkedHashMap<Integer, Tab> tabMaps = new LinkedHashMap<>();
        LinkedHashMap<Integer, Div> divMaps = new LinkedHashMap<>();
        if( null != listItemRel && listItemRel.size() > 0){
            for(AutoFormPageTabDivItemRel itemRel : listItemRel){
                AutoFormItem item = itemRel.getItem();
                Tab tabItem;
                if( !tabMaps.containsKey(itemRel.getTab().getTabId()) ){
                    tabItem = new Tab();
                    tabItem.setId(itemRel.getTab().getTabId());
                    tabItem.setName(itemRel.getTab().getTabName());
                    tabItem.setOrder(itemRel.getTabOrder());
                    tabMaps.put(tabItem.getId(), tabItem);
                }else{
                    tabItem = tabMaps.get(itemRel.getTab().getTabId());
                }
                
                Div divItem;
                if( !divMaps.containsKey(itemRel.getDiv().getDivId()) ){
                    divItem = new Div();
                    divItem.setCol(itemRel.getDiv().getDivCol());
                    divItem.setId(itemRel.getDiv().getDivId());
                    divItem.setName(itemRel.getDiv().getDivName());
                    tabItem.addDiv(divItem);
                    divMaps.put(divItem.getId(), divItem);
                }else{
                    divItem = divMaps.get(itemRel.getDiv().getDivId());
                }
                
                Field f = new Field();
                f.setId(String.valueOf(item.getItemId()));
                f.setLabel(item.getItemName());
                f.setMultiple(item.getItemMultiple() == 1);
                f.setRequired(item.getItemRequired() == 1);
                f.setName(StringUtil.FIELD_NAME_PREFIX + item.getItemId());
                f.setType(Field.getItemTypeAsString(item.getItemType()));
                f.setClassName(item.getItemClass());
                f.setValue(dataMap.containsKey(item.getItemId()) ? dataMap.get(item.getItemId()) : "");
                f.setValueFromBean(locale.getLocale(), item.getItemGlobalList());
                divItem.addField(f);
            }
        }
        Collection<Tab> tabList = tabMaps.values();
        return gson.toJson(tabList.toArray());
    }
    
    /**
     * Remove one page, actually is change page object status
     * 
     * @param page 
     */
    @SecureMethod(value=SecureMethod.Method.DELETE)
    public void remove(AutoFormPageTab page){
        try{
            pageTabServiceImpl.remove(page);
            pageTabServiceImpl.cleanUnusedItems(page.getPageId(), UserModel.getLogined().getCompanyId());
            projectController.reload();
            JsfUtil.addSuccessMessage("削除しました。");
        }catch(Exception e){
            JsfUtil.addErrorMessage("エラーがありますので、確認してください。");
        }
        reload();
    }
    
    /**
     * Kiểm tra nếu người dùng không chọn MODULE nào để gắn với DYNAMIC-FORM.
     * nếu chọn gắn với MODULE(company, issue, ...) thì page-type là 2
     * ngược lại không gắn với MODULE nào thì page-type là 1.
     * @param afpt 
     */
    private boolean decidedPageType(final AutoFormPageTab afpt) {
        if(afpt == null || StringUtils.isEmpty(afpt.getPageName())) return false;
        boolean isNum = StringUtils.isNumeric(afpt.getPageName());
        if(!isNum) {
            afpt.setPageType(AutoFormPageTab.PAGE_TYPE_IRRELEVANTIVE_DYNAMIC_FORM);
        } else {
            Page p = staticPageServiceImpl.find(Integer.valueOf(afpt.getPageName()));
            if(p == null) {
                afpt.setPageType(AutoFormPageTab.PAGE_TYPE_IRRELEVANTIVE_DYNAMIC_FORM);
            } else {
                if(!afpt.getPageName().equals(originPageName) && !pageTabServiceImpl.search(getCid(), afpt.getPageName()).isEmpty()) {
                    UIComponent component = JsfUtil.findComponent("pageName");
                    if(component != null) {
                        UIInput uIInput = (UIInput) component;
                        uIInput.setValid(false);
                        uIInput.setValue(StringUtils.EMPTY);
                        afpt.setPageName(StringUtils.EMPTY);
                        JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_CUSTOMIZE_NAME, "label.customize.form.exists", uIInput.getAttributes().get("title")));
                    }
                    return false;
                } else {
                    afpt.setPageType(AutoFormPageTab.PAGE_TYPE_CORRELATIVE_DYNAMIC_FORM);
                }
            }
        }
        return true;
    }
    
    /**
     * Save from builder and save to DB
     * 
     */
    @SecureMethod(value = SecureMethod.Method.CREATE)
    public void saveFormBuilder() {
        try {
            if(!decidedPageType(selectedPage)) return;
            
            Gson gson = new Gson();
            Tab[] tabs = gson.fromJson(autoFormContent, Tab[].class);
            List<AutoFormPageTabDivItemRel> autoFormPageTabDivItemRelList = new ArrayList<>();
            if (isCreatedForm) {
                selectedPage.setPageDeleted(Boolean.FALSE);
                selectedPage.setCompany(new Company(loginController.getMember().getCompanyId()));
                selectedPage.setCreatorId(loginController.getMember().getUserId());
                selectedPage.setCreatedTime(new Date());
                selectedPage = pageTabServiceImpl.create(selectedPage);
            } else {
                selectedPage.setPageDeleted(Boolean.FALSE);
                selectedPage.setCompany(new Company(loginController.getMember().getCompanyId()));
                selectedPage.setUpdatedId(loginController.getMember().getUserId());
                selectedPage.setUpdatedTime(new Date());
                selectedPage = pageTabServiceImpl.edit(selectedPage);
            }

            for (Tab tabModel : tabs) {
                //update tab
                AutoFormTab tab = tabServiceImpl.find(tabModel.getId());
                boolean tabUpdateFlag = true;
                if (tab == null) {
                    tab = new AutoFormTab();
                    tabUpdateFlag = false;
                }
                tab.setTabName(tabModel.getName());
                tab.setCompany(loginController.getMember().getCompany());
                tab.setTabDeleted(Boolean.FALSE);
                if (tabUpdateFlag) {
                    tab.setUpdatedId(loginController.getMember().getUserId());
                    tabServiceImpl.edit(tab);
                } else {
                    tab.setCreatorId(loginController.getMember().getUserId());
                    tabServiceImpl.create(tab);
                }
                List<Div> divs = tabModel.getDivs();
                for (int divIndex = 0; divIndex < divs.size(); divIndex++) {
                    AutoFormDiv div = divServiceImpl.find(divs.get(divIndex).getId());
                    boolean divUpdateFlag = true;
                    if (div == null) {
                        div = new AutoFormDiv();
                        divUpdateFlag = false;
                    }
                    div.setDivCol(divs.get(divIndex).getCol());
                    div.setDivName(divs.get(divIndex).getName());
                    div.setCompany(loginController.getMember().getCompany());
                    div.setDivDeleted(Boolean.FALSE);

                    if (divUpdateFlag) {
                        div.setUpdatedId(loginController.getMember().getUserId());
                        div.setUpdatedTime(new Date());
                        divServiceImpl.edit(div);
                    } else {
                        div.setCreatorId(loginController.getMember().getUserId());
                        div.setCreatedTime(new Date());
                        divServiceImpl.create(div);
                    }

                    List<Field> fields = divs.get(divIndex).getFields();
                    AutoFormPageTabDivItemRel rel = null;

                    for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
                        Field field = fields.get(fieldIndex);
                        AutoFormItem item = null;
                        int itemId = this.getIdFromFieldName(field.getName());

                        if (itemId == 0) {
                            item = field.getItem();
                            item.setCompany(loginController.getMember().getCompany());
                            item.setCreatorId(loginController.getMember().getUserId());
                            item.setCreatedTime(new Date());
                            item.setItemClass(field.getClassName());
                            item = itemServiceImpl.create(item);
                        } else {
                            item = itemServiceImpl.find(itemId);
                            item.setItemRequired((short) (field.isRequired() ? 1 : 0));
                            item.setItemMultiple((short) (field.isMultiple() ? 1 : 0));
                            item.setItemType(Field.getItemType(field.getType()));
                            item.setCompany(loginController.getMember().getCompany());
                            item.setUpdatedId(loginController.getMember().getUserId());
                            item.setUpdatedTime(new Date());
                            item.setItemClass(field.getClassName());
                            item = itemServiceImpl.edit(item);

                            itemServiceImpl.removeItemGlobal(item);
                        }

                        itemServiceImpl.persitItemGlobal(field.getItemGlobal(item));

                        rel = new AutoFormPageTabDivItemRel(selectedPage.getPageId(), div.getDivId(), tab.getTabId(), item.getItemId(), loginController.getMember().getCompanyId());
                        rel.setItem(item);
                        rel.setItemOrder(fieldIndex);
                        rel.setDivOrder(divIndex);
                        rel.setTabOrder(tabModel.getOrder());
                        rel.setPage(selectedPage);

                        autoFormPageTabDivItemRelList.add(rel);
                    }
                    selectedPage.setAutoFormPageTabDivItemRelList(autoFormPageTabDivItemRelList);
                }
            }
            pageTabServiceImpl.edit(selectedPage);
            pageTabServiceImpl.cleanUnusedItems(selectedPage.getPageId(), UserModel.getLogined().getCompanyId());
            projectController.reload();
            JsfUtil.addSuccessMessage("完了しました。");
            reload();
        } catch (Exception e) {
            JsfUtil.addErrorMessage("エラーがあるので、完了ができない。");
            logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * Get field ID
     * 
     * @param fieldName
     * @return 
     */
    private int getIdFromFieldName(String fieldName){
        if(StringUtils.isEmpty(fieldName)) return 0;
        Pattern r = Pattern.compile(StringUtil.FIELD_ID_PATTERN);
        Matcher m = r.matcher(fieldName);
        if(m.find() && StringUtils.isNumeric(m.group(1))){
            try{
                return Integer.parseInt(m.group(1));
            }catch(NumberFormatException nfe){
                return 0;
            }
        }
        return 0;
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    public void reload(){
        init();
        load();
    }
    
    @SecureMethod(value=SecureMethod.Method.INDEX, require = false)
    public void load(){
        layout.setCenter("/modules/customize/list.xhtml");
    }
}
