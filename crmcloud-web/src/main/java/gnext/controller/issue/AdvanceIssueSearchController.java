package gnext.controller.issue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gnext.bean.Prefecture;
import gnext.bean.customize.AutoFormItem;
import gnext.bean.customize.AutoFormPageTabDivItemRel;
import gnext.bean.mente.MenteItem;
import gnext.bean.project.DynamicColumn;
import gnext.bean.project.ProjectCustSearch;
import gnext.controller.AbstractController;
import gnext.model.authority.UserModel;
import gnext.model.customize.Field;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.PrefectureService;
import gnext.service.config.ConfigService;
import gnext.service.customize.AutoFormItemService;
import gnext.service.customize.AutoFormPageTabDivItemService;
import gnext.service.issue.IssueService;
import gnext.service.mente.MenteService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.service.project.ProjectService;
import gnext.util.DateUtil;
import gnext.utils.InterfaceUtil.FIELDS;
import gnext.util.IssueUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.SelectUtil;
import gnext.util.StringUtil;
import gnext.utils.InterfaceUtil;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.COMPANY_TYPE;
import gnext.utils.InterfaceUtil.SERVER_KEY;
import java.util.Date;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
/**
 * Advance search using elastic search
 *
 * @author hungpham
 * @since 2016/10
 */
@ManagedBean(name = "ais", eager = true)
@SessionScoped
@SecurePage(module = SecurePage.Module.ISSUE, require = false)
public class AdvanceIssueSearchController  extends AbstractController implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LoggerFactory.getLogger(AdvanceIssueSearchController.class);

    @EJB private AutoFormItemService autoFormItemServiceImpl;
    @EJB private AutoFormPageTabDivItemService pageTabServiceImpl;
    @EJB private ProjectService projectCusSearchServiceImpl;
    @EJB private ConfigService configServiceImpl;
    @EJB private IssueService issueService;
    @EJB private MenteService menteService;
    @EJB private PrefectureService prefectureService;
    
    @Setter @Getter private ProjectCustSearch project;
    @Setter @Getter private List<Map<String, Object>> issues = new ArrayList<>();
    
    private final List<SelectItem> liveSearchVisibleColumns = new ArrayList<>();
    private String columnJson;
    private List<SelectItem> tmpColumns;

    @Setter @Getter 
    private boolean quickSearch;

    @Getter @Setter
    private Integer pageId;
    
    private Integer companyId;
    private List<MenteItem> menteList;
    private List<Prefecture> prefectureList;
    private String locale;
    
    @PostConstruct
    public void init(){
        this.project = null;
        this.columnJson = null;
        quickSearch = configServiceImpl.get(SERVER_KEY.ELASTIC).equalsIgnoreCase("true");
        
        this.companyId = UserModel.getLogined().getCompanyId();
        menteList = menteService.getAllLevels(this.companyId);
        prefectureList = prefectureService.findAll();
        locale = getLocale();
    }
    
    @PreDestroy
    public void destroy(){
        
    }
    
    /**
     * Get visible column JSON to display on datatable
     * 
     * @return 
     */
    public String getColumnJson(){
        if(columnJson == null){
            this.pageId = this.issueService.getCustomizePageId("IssueController", getCurrentCompanyId());
            JsonArray json = new JsonArray();
            List<AutoFormPageTabDivItemRel> dynamicColumns =
                    this.pageTabServiceImpl.findRelList(this.pageId, InterfaceUtil.PAGE.DYNAMIC, UserModel.getLogined().getCompanyId());
            List<DynamicColumn> allCols = SelectUtil.getViewDynamicColumns(dynamicColumns);
            if(allCols != null) {
                allCols.forEach((t) -> {
                    json.add(getFieldJson(t));
                });
            }

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            columnJson = gson.toJson(json);
        }
        return columnJson;
    }
    
    /**
     * Get field JSON Object from DynamicColumn pattern
     * 
     * @param col
     * @return 
     */
    public JsonObject getFieldJson(DynamicColumn col){
        JsonObject jo = new JsonObject();
        
        String fieldType = getFieldType(col.getId(), jo);
        jo.addProperty("label", col.getName().contains(".cust_") 
                        ? JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME, col.getName()) 
                        : JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME, col.getName()));
        jo.addProperty("name", col.getId());
        
        jo.addProperty("type", fieldType);

        jo.add("operator", getFieldOperator(col.getId(), fieldType));
            
        return jo;
    }
    
    /**
     * Get field Type for each field on DB by name
     * 
     * @param field
     * @param json
     * @return 
     */
    private String getFieldType(String field, JsonObject json){
        JsonArray template  = new JsonArray();
        JsonObject templateItem;
        if("issue_deleted".equals(field)) {
            templateItem = new JsonObject();
            templateItem.addProperty("value", "0");
            templateItem.addProperty("label", JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MSG, "label.state.deleted"));
            template.add(templateItem);

            templateItem = new JsonObject();
            templateItem.addProperty("value", "1");
            templateItem.addProperty("label", JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MSG, "label.state.using"));
            template.add(templateItem);

            json.add("data", template);

            return "select";
        } else if(StringUtils.endsWithAny(field, "_time", "_date")) {
            return "date";
        } else if("PERSON".equals(IssueUtil.COL_TYPE.getType(field))){
            UserModel.getLogined().getCompany().getGroupList().forEach((group) -> {
                if(group.getGroupDeleted() == 0){
                    group.getMembers().forEach((member) -> {
                        if(member.getMemberDeleted() == 0){
                            JsonObject jsonItem = new JsonObject();
                            jsonItem.addProperty("value", member.getMemberNameFull());
                            jsonItem.addProperty("label", member.getMemberNameFull());
                            template.add(jsonItem);
                        }
                    });
                }
            });
            json.add("data", template);
            return "select";
        } else if("GROUP".equals(IssueUtil.COL_TYPE.getType(field))){
            UserModel.getLogined().getCompany().getGroupList().forEach((group) -> {
                if(group.getGroupDeleted() == 0){
                    JsonObject jsonItem = new JsonObject();
                    jsonItem.addProperty("value", group.getGroupName());
                    jsonItem.addProperty("label", group.getGroupName());
                    template.add(jsonItem);
                }
            });
            json.add("data", template);
            return "select";
        } else if("MAINTENANCE".equals(IssueUtil.COL_TYPE.getType(field))){
            field = field.replaceAll("_name$", "_id").replace("_proposal_name", "_proposal_id").replace("_product_name", "_product_id");
            if(StringUtils.containsAny(field, "proposal", "product")){
                Integer level = NumberUtils.toInt(field.substring(field.lastIndexOf("_") + 1));
                for(MenteItem m : menteList){
                    if(field.startsWith(m.getItemName()) && m.getItemLevel().equals(level)){
                        String name = m.getItemViewData(locale);
                        JsonObject jsonItem = new JsonObject();
                        jsonItem.addProperty("value", name);
                        jsonItem.addProperty("label", name);
                        template.add(jsonItem);
                    }
                }
                json.add("data", template);
            } else {
                for(MenteItem m : menteList){
                    if(field.equals(m.getItemName())){
                        String name = m.getItemViewData(locale);
                        JsonObject jsonItem = new JsonObject();
                        jsonItem.addProperty("value", name);
                        jsonItem.addProperty("label", name);
                        template.add(jsonItem);
                    }
                }
                json.add("data", template);
            }
            return "select";
        } else if("CITY".equals(IssueUtil.COL_TYPE.getType(field))){
            List<String> prefectureAdded = new ArrayList<>();
            prefectureList.forEach((p) -> {
                if(!prefectureAdded.contains(p.getPrefectureCode())){
                    JsonObject jsonItem = new JsonObject();
                    jsonItem.addProperty("value", p.getPrefectureName());
                    jsonItem.addProperty("label", p.getPrefectureName());
                    template.add(jsonItem);
                    prefectureAdded.add(p.getPrefectureCode());
                }
            });
            json.add("data", template);
            return "select";
        } else if(field.startsWith(FIELDS.DYNAMIC)) {
            String[] ids = field.split("_");
            if(ids == null || ids.length != 2) return "string";
            String id = ids[ids.length - 1];
            if(!NumberUtils.isDigits(id)) return "string";
            SelectItem item = this.autoFormItemServiceImpl.getItemGlobal(
                    UserModel.getLogined().getCompanyId()
                    , Integer.valueOf(id)
                    , UserModel.getLogined().getLanguage());
            if(item == null || !NumberUtils.isDigits(String.valueOf(item.getValue()))) return "string";
            Integer itemId = Integer.valueOf(String.valueOf(item.getValue()));
            if(Field.Type.DATE == itemId) {
                return "date";
            } else if(Field.Type.SELECT == itemId
                        || Field.Type.RADIO_GROUP == itemId
                        || Field.Type.CHECKBOX_GROUP == itemId) {
                Gson gson = new Gson();
                JsonArray jsonArray = gson.fromJson(item.getLabel(), JsonArray.class);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject obj = (JsonObject) jsonArray.get(i);
                    String value = obj.get("value").getAsString();
                    String label = obj.get("label").getAsString();
                    if(StringUtils.isEmpty(value) && StringUtils.isEmpty(label)) continue;
                    
                    if(COLS.USER.equals(value)){
                        getFieldType(IssueUtil.COL_TYPE.PERSON.issue_creator_name.name(), json);
                    }else if(COLS.GROUP.equals(value)){
                        getFieldType(IssueUtil.COL_TYPE.GROUP.issue_receive_person_group_name.name(), json);
                    }else if(IssueUtil.COL_TYPE.isExists(value.replace("_id", "_name"))){
                        getFieldType(value.replace("_id", "_name"), json);
                    }else{
                        templateItem = new JsonObject();
                        templateItem.addProperty("value", value);
                        templateItem.addProperty("label", label);
                        template.add(templateItem);
                    }
                }
                if(template.size() > 0) json.add("data", template);
            }
            return json.size() <= 0 ? "string" : "select";
        } else if(field.equals(COLS.CUST_STATUS)){
            List<SelectItem> items = SelectUtil.getCustStatusList();
            for(SelectItem item:items) {
                if(item == null || item.getValue() == null) continue;
                JsonObject jsonItem = new JsonObject();
                jsonItem.addProperty("value", String.valueOf(item.getValue()));
                jsonItem.addProperty("label", item.getLabel());
                template.add(jsonItem);
            }
            json.add("data", template);
            return "select";
        } else {
            return "string";
        }
    }
    
    /**
     * Get field operator for each fields by name
     * 
     * @param field
     * @param type
     * @return 
     */
    private JsonArray getFieldOperator(String field, String type){
        JsonArray operator  = new JsonArray();
        switch(type){
            case "select":
                operator.add(new JsonPrimitive("EQ"));
                operator.add(new JsonPrimitive("BLANK"));
                operator.add(new JsonPrimitive("NOT_BLANK"));
                break;
            case "date":
                operator.add(new JsonPrimitive("EQ"));
                operator.add(new JsonPrimitive("NE"));
                operator.add(new JsonPrimitive("LT"));
                operator.add(new JsonPrimitive("LE"));
                operator.add(new JsonPrimitive("GT"));
                operator.add(new JsonPrimitive("GE"));
                operator.add(new JsonPrimitive("BLANK"));
                break;
        }
        return operator;
    }

    /**
     * Get all visible column belong to current project
     * 
     * @return 
     */
    public List<SelectItem> getLiveSearchVisibleColumns() {
        startMeasure("AdvanceIssueSearchController.getLiveSearchVisibleColumns");
        if(this.tmpColumns != null && this.tmpColumns.size() > 0) return this.tmpColumns;
        if(this.project != null && this.project.getListId() != null){
            liveSearchVisibleColumns.clear();
            List<String> dbColumnData = new Gson().fromJson(this.project.getListViewData(), List.class);
            List<Integer> avaiableItems = pageTabServiceImpl.listDynamicItems(companyId, 7);
            dbColumnData.forEach((col) -> {
                SelectItem item = new SelectItem();
                if(!avaiableItems.isEmpty() && col.startsWith(InterfaceUtil.FIELDS.DYNAMIC)){
                    Integer dynamicItemId = NumberUtils.toInt(col.replace(InterfaceUtil.FIELDS.DYNAMIC, ""));
                    if(!avaiableItems.contains(dynamicItemId)){
                        return;
                    }
                }
                item.setValue(col);
                item.setLabel(col.contains("cust_") ? 
                        JsfUtil.getResource().message( companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME, "label." + col)
                        : JsfUtil.getResource().message( companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label." + col));
                if(StringUtil.isExistsInArray(col, SelectUtil.getViewMemoTextarea())
                        || (UserModel.getLogined().getCompanyBusinessFlag() == COMPANY_TYPE.CUSTOMER && StringUtil.isExistsInArray(col, SelectUtil.getViewMemoCustSpecialTextarea()))) {
                    item.setDescription(FIELDS.MEMO_VIEW);
                } else if(col.startsWith(FIELDS.DYNAMIC)) {
                    String itemId = col.substring(FIELDS.DYNAMIC.length(), col.length());
                    if(NumberUtils.isDigits(itemId)) {
                        AutoFormItem m = autoFormItemServiceImpl.find(Integer.valueOf(itemId));
                        if(m != null && m.getItemType() == Field.Type.TEXTAREA) {
                            item.setDescription(FIELDS.MEMO_VIEW);
                        }
                    }
                }
                liveSearchVisibleColumns.add(item);
            });
        }
        stopMeasure("AdvanceIssueSearchController.getLiveSearchVisibleColumns");
        return liveSearchVisibleColumns;
    }
    
    @Override
    public String getSearchDataJson() {
        if(this.project == null){
            return "[]";
        }
        return this.project.getListSearchData();
    }
    
    @SecureMethod(SecureMethod.Method.SEARCH)
    public void search(ProjectCustSearch p) {
        this.project = p;
        this.tmpColumns = null;
        this.search();
    }
    
    /**
     * Search action for CRUD Project Form
     * 
     * @param p 
     * @param columns 
     */
    @SecureMethod(SecureMethod.Method.SEARCH)
    public void search(ProjectCustSearch p, DualListModel<DynamicColumn> columns) {
        this.project = p;
        this.tmpColumns = new ArrayList<>();
        columns.getTarget().forEach((dc) -> {
            SelectItem item = new SelectItem();
            item.setValue(dc.getId());
            item.setLabel(dc.getId().contains("cust_") 
                                    ? JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME, "label." + dc.getId()) 
                                    : JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label." + dc.getId()));
            tmpColumns.add(item);
        });
        this.search();
    }

    /**
     * Do search using elastic search and query parameter from callback
     * 
     * @param filter 
     */
    @Override
    protected void doSearch(SearchFilter filter) {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        boolean quickSearch = configServiceImpl.get(SERVER_KEY.ELASTIC).equalsIgnoreCase("true");
        filter.setModule(SecurePage.Module.ISSUE);
        String query = filter != null ? filter.getQuery(quickSearch) : "";
        List<String> conditionFields = filter != null ? filter.getConditionFields() : new ArrayList<>();
        
        Date fromDateSearch = null, toDateSearch = null;
        if(project == null){
            toDateSearch = DateUtil.getAddSupDate(0);
            fromDateSearch = DateUtil.getAddSupDate(-6);
        }else{
            /* 期間で検索場合 */
            if(project.getListSearchPeriodFlag() != null
                    && project.getListSearchPeriodFlag()==1) {
                if(StringUtils.isNoneBlank(project.getListSearchPeriod())
                        && project.getListSearchPeriod().startsWith("-")) {
                    project.setDateFrom(DateUtil.getAddSupDate(Integer.valueOf(project.getListSearchPeriod())));
                    project.setDateTo(DateUtil.getAddSupDate(0));
                } else {
                    project.setDateFrom(DateUtil.getAddSupDate(0));
                    project.setDateTo(DateUtil.getAddSupDate(Integer.valueOf(project.getListSearchPeriod())));
                }
            }
            fromDateSearch = project.getDateFrom();
            toDateSearch = project.getDateTo();
        }
        
        issues = projectCusSearchServiceImpl.advanceSearch(
                UserModel.getLogined().getCompanyBusinessFlag(),
                UserModel.getLogined().getCompanyId(), 
                UserModel.getLogined().getUserId(),
                quickSearch, 
                query, conditionFields, 
                "", "OR",
                this.getLiveSearchVisibleColumns(),
                fromDateSearch,
                toDateSearch,
                getLocale()
        );
        JsfUtil.clearStateOfDataTable("issueListData");
    }
}
