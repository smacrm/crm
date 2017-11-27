package gnext.controller.customize;

import com.google.gson.Gson;
import gnext.bean.customize.AutoFormItem;
import gnext.bean.customize.AutoFormMultipleDataValue;
import gnext.bean.customize.AutoFormPageTab;
import gnext.bean.customize.AutoFormPageTabDivItemRel;
import gnext.controller.common.LayoutController;
import gnext.controller.common.LocaleController;
import gnext.controller.common.LoginController;
import gnext.model.authority.UserModel;
import gnext.model.customize.Div;
import gnext.model.customize.Field;
import gnext.model.customize.Tab;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.security.annotation.SecurePage.Module;
import gnext.service.customize.AutoFormMultipleDataValueService;
import gnext.service.customize.AutoFormPageTabDivItemService;
import gnext.util.StringUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Render controller
 * Saving data for customize form
 *
 * @author hungpham
 */
@ManagedBean(name = "renderController")
@SecurePage(module = Module.CUSTOMIZE, require = false)
@SessionScoped() 
public class RenderController implements Serializable{
    private static final long serialVersionUID = 8655721305278555707L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderController.class);
    
    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{loginController}")
    @Getter @Setter private LoginController loginController;
    @ManagedProperty(value = "#{localeController}")
    @Getter @Setter private LocaleController locale;
    
    @Getter @Setter private List<AutoFormPageTab> pageList;
    @Getter @Setter private AutoFormPageTab page;
    
    @EJB private AutoFormPageTabDivItemService pageTabDivItemServiceImpl;
    @EJB private AutoFormMultipleDataValueService dataValueServiceImpl;
    
//    final private String fieldNamePrefix = "field-";
    
    protected Map<String, Long> measure = new HashMap<>(); //Su dung de do performance
    
    public void startMeasure(String key){
        measure.put(key, System.currentTimeMillis());
    }
    
    public void stopMeasure(String key){
        Long start = measure.containsKey(key) ? measure.get(key) : System.currentTimeMillis();
        System.err.println(">>> MEASURE ["+key+"] " + (System.currentTimeMillis() - start));
        measure.remove(key);
    }
    
    @SecureMethod(value=SecureMethod.Method.VIEW, require = false)
    public void renderForm(AutoFormPageTab page, int refId){
        this.page = page;
        //System.err.println(">>" + autoFormContent);
        layout.setCenter("/modules/customize/render.xhtml");
    }
    
    /**
     * Create customize tab for dynamic page 
     * This function using in customize/tab.xhtml
     * 
     * @param pageId
     * @param pageType
     * @param targetId
     * @return 
     */
    public Collection<Tab> tabs(int pageId, int pageType, int targetId){
        startMeasure("tabs");
        Map<Integer, String> dataMap = pageId > 0 && targetId > 0 ? dataValueServiceImpl.findItemData(pageId, targetId, UserModel.getLogined().getCompanyId()) : new HashMap<>();
        stopMeasure("tabs");
        return tabsWithData(pageId, pageType, targetId, dataMap);
    }
    
    /**
     * Create customize tab for dynamic page with data mapping values
     * This function using in customize/tab.xhtml
     * 
     * @param pageId
     * @param pageType
     * @param targetId
     * @param dataMap
     * @return 
     */
    public Collection<Tab> tabsWithData(int pageId, int pageType, int targetId, Map<Integer, String> dataMap){
        startMeasure("tabsWithData");
        Collection<Tab> tabList;
        
        List<AutoFormPageTabDivItemRel> listItemRel =  pageTabDivItemServiceImpl.findRelList(pageId, pageType, UserModel.getLogined().getCompanyId());
        LinkedHashMap<Integer, Tab> tabMaps = new LinkedHashMap<>();
        LinkedHashMap<Integer, Div> divMaps = new LinkedHashMap<>();
        if( null != listItemRel && listItemRel.size() > 0){
            listItemRel.forEach((AutoFormPageTabDivItemRel itemRel) -> {
                AutoFormItem item = itemRel.getItem();
                Tab tabItem;
                if( !tabMaps.containsKey(itemRel.getTab().getTabId()) ){
                    tabItem = new Tab();
                    tabItem.setId(itemRel.getTab().getTabId());
                    tabItem.setName(itemRel.getTab().getTabName());
                    tabMaps.put(tabItem.getId(), tabItem);
                }else{
                    tabItem = tabMaps.get(itemRel.getTab().getTabId());
                }
                
                Div divItem;
                if( !divMaps.containsKey(itemRel.getDiv().getDivId()) ){
                    divItem = new Div();
                    divItem.setId(itemRel.getDiv().getDivId());
                    divItem.setCol(itemRel.getDiv().getDivCol());
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
                f.setValue(dataMap != null && !dataMap.isEmpty() && dataMap.containsKey(item.getItemId()) ? dataMap.get(item.getItemId()) : "");
                f.setValueFromBean(locale.getLocale(), item.getItemGlobalList());
                divItem.addField(f);
            });
        }
        tabList = tabMaps.values();
        stopMeasure("tabsWithData");
        return tabList;
    }
    
    /**
     * Save data from dynamic render from
     * Just calling this method after save master form
     * 
     * @param pageId
     * @param pageType
     * @param targetId
     */
    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void saveFormData(int pageId, int pageType, int targetId){
        try {
            if(targetId != 0){
                List<Integer> dataSavedList = new ArrayList<>();
                Map<String, String[]> requestParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap();

                Pattern r = Pattern.compile(StringUtil.FIELD_ID_PATTERN);
                AutoFormMultipleDataValue dataValue;
                UserModel userMode = loginController.getMember();
                for (Map.Entry<String, String[]> field : requestParams.entrySet()) {
                    String key = field.getKey();
                    String[] values = field.getValue();
                    Matcher m = r.matcher(key);
                    if(m.find() && !isValueEmpty(values)){
                        String id = m.group(1);

                        dataValue = new AutoFormMultipleDataValue(targetId, pageId, pageType, Integer.parseInt(id), userMode.getCompanyId());

                        if(dataValueServiceImpl.isExists(dataValue)){
                            dataValue.setItemData(getFieldValue(values));
                            dataValue.setUpdatedId(userMode.getUserId());
                            dataValue.setUpdatedTime(new Date());
                            dataValue = dataValueServiceImpl.edit(dataValue);
                        }else{
                            dataValue.setItemData(getFieldValue(values));
                            dataValue.setCreatorId(userMode.getUserId());
                            dataValue.setCreatedTime(new Date());
                            dataValue = dataValueServiceImpl.create(dataValue);
                        }
                        dataSavedList.add(dataValue.getAutoFormMultipleDataValuePK().getItemId());
                    }
                }

                //remove all item data exclude item on dataSavedList
                dataValueServiceImpl.removeNoDataItemExcludes(userMode.getCompanyId(), pageId, pageType, targetId, dataSavedList);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    /**
    * Save data from dynamic render from with target-id as parameter
    * Just calling this method after save master form
     * @param pageId
     * @param pageType
    * @param targetId 
     * @param values 
    */
    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void saveFormData(int pageId, int pageType, int targetId, Map<Integer, String> values) {
        try {
            if(targetId != 0){
                List<Integer> dataSavedList = new ArrayList<>();

                Pattern r = Pattern.compile(StringUtil.FIELD_ID_PATTERN);
                AutoFormMultipleDataValue dataValue;
                UserModel userMode = loginController.getMember();
                for (Map.Entry<Integer, String> field : values.entrySet()) {
                    Integer key = field.getKey();
                    String value = field.getValue();
                    if( !StringUtils.isEmpty(value) ){

                        dataValue = new AutoFormMultipleDataValue(targetId, pageId, pageType, key, userMode.getCompanyId());

                        if(dataValueServiceImpl.isExists(dataValue)){
                            dataValue.setItemData(value);
                            dataValue.setUpdatedId(userMode.getUserId());
                            dataValue.setUpdatedTime(new Date());
                            dataValue = dataValueServiceImpl.edit(dataValue);
                        }else{
                            dataValue.setItemData(value);
                            dataValue.setCreatorId(userMode.getUserId());
                            dataValue.setCreatedTime(new Date());
                            dataValue = dataValueServiceImpl.create(dataValue);
                        }
                        dataSavedList.add(dataValue.getAutoFormMultipleDataValuePK().getItemId());
                    }
                }

                //remove all item data exclude item on dataSavedList
                dataValueServiceImpl.removeNoDataItemExcludes(userMode.getCompanyId(), pageId, pageType, targetId, dataSavedList);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    private boolean isValueEmpty(String[] values){
        return values.length == 1 && StringUtils.isEmpty(values[0]);
    }

    private String getFieldValue(String[] values){
        if(values.length == 1) return values[0];
        return new Gson().toJson(values);
    }
}
