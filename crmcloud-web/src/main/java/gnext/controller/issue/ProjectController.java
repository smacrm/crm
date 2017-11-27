package gnext.controller.issue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gnext.bean.Company;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.customize.AutoFormPageTabDivItemRel;
import gnext.bean.project.DynamicColumn;
import gnext.bean.project.ProjectCustColumnWidth;
import gnext.bean.project.ProjectCustSearch;
import gnext.bean.project.ProjectRoleRel;
import gnext.bean.project.ProjectRoleRelPK;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.bean.issue.IssueLamp;
import gnext.model.authority.UserModel;
import gnext.model.search.SearchFilter;
import gnext.model.search.SearchGroup;
import gnext.multitenancy.service.MultitenancyService;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.CompanyService;
import gnext.service.config.ConfigService;
import gnext.service.customize.AutoFormPageTabDivItemService;
import gnext.service.issue.IssueService;
import gnext.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.service.project.ProjectService;
import gnext.util.DateUtil;
import gnext.util.ResourceUtil;
import gnext.util.SelectUtil;
import gnext.util.StringUtil;
import gnext.utils.InterfaceUtil;
import java.util.Arrays;
import java.util.HashMap;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.ColumnResizeEvent;
import org.primefaces.event.TransferEvent;

/**
 * CRUD for project module
 *
 * @author hungpham
 * @since 2016/10
 */
@ManagedBean(name = "projectController", eager = true)
@SessionScoped
@SecurePage(module = SecurePage.Module.ISSUE)
public class ProjectController extends AbstractController implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @ManagedProperty(value = "#{ais}")
    @Getter @Setter private AdvanceIssueSearchController ais;
    
    @EJB private AutoFormPageTabDivItemService pageTabServiceImpl;
    @EJB private ProjectService projectServiceImpl;
    @EJB private CompanyService companyServiceImpl;
//    @EJB private GroupService groupServiceImpl;
//    @EJB private MemberService memberServiceImpl;
    @EJB private IssueService issueService;
    @EJB private ConfigService configServiceImpl;
    @EJB private MultitenancyService multitenancyService;
    
    @Setter @Getter private List<ProjectCustSearch> projects;
    @Setter @Getter private List<ProjectCustSearch> searches;
    
    @Setter @Getter private String columnViewData;
    @Setter @Getter private String extraFieldNames; //Luu tru label cua cac field conditions, trong Th field condition khong nam trong danh sach cac view column thi se khong co label
    @Setter @Getter private ProjectCustSearch project;
    @Setter @Getter private List<Group> groupList;
    @Setter @Getter private List<String> selectedGroup;
    @Setter @Getter private List<Member> memberList;
    @Setter @Getter private List<String> selectedMember;
    @Setter @Getter private List<Map<String, Object>> issues = new ArrayList<>();
    @Setter @Getter private DualListModel<DynamicColumn> columns;
    
    private final List<SelectItem> liveSearchVisibleColumns = new ArrayList<>();
    private List<DynamicColumn> listAllVisibleColumn;

    @Setter @Getter private String listId;
    @Setter @Getter private String keyword;
    @Setter @Getter private Boolean isSearchPeriod;
    @Setter @Getter private Integer searchPeriod;
    @Setter @Getter private Boolean searchPeriodBeforeAfter;
    @Setter @Getter private String keywordCondition = "OR";
    
    @Setter @Getter
    private List<IssueLamp> issueLamps = new ArrayList<>();
    
    @Setter @Getter
    private Map<String, Integer> columnWidth = new HashMap<>();
    
    private Integer companyId;
    private Integer customizePageId;

    @PostConstruct
    public void init(){
        this.project = null;
        this.columns = new DualListModel<>();
        this.companyId = UserModel.getLogined().getCompanyId();
        
        projects = projectServiceImpl.findAllAvaiable(companyId, UserModel.getLogined().getMember());
        searches = projectServiceImpl.findSearchAvaiableList(companyId);
        this.setIssueLamps(this.issueService.getIssueLamps(companyId));
        
        columnWidth.clear();
        List<ProjectCustColumnWidth> widthList = projectServiceImpl.getColumnWidthList(companyId);
        for(ProjectCustColumnWidth w : widthList){
            columnWidth.put(w.getColumnId(), w.getColumnWidth());
        }
    }
    
    /**
     * Show form for CRUD
     */
    public void showForm(){
        try {
            groupList = multitenancyService.findAllGroupUnderSlave(UserModel.getLogined().getCompanyId());
            memberList = multitenancyService.findAllMemberOnSlave(UserModel.getLogined().getCompanyId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
//        memberList = memberServiceImpl.findByGroupList(groupList, (short)0, new ArrayList<>());

        if(this.isSearchPeriod == null || !this.isSearchPeriod) {
            this.setSearchPeriod(null);
            this.setSearchPeriodBeforeAfter(null);
        }
        this.layout.setCenter("/modules/project/form.xhtml");
    }
    
    /**
     * Action remove project
     * actually is change project's status
     * 
     * @param p 
     */
    @SecureMethod(value=SecureMethod.Method.DELETE)
    public void remove(ProjectCustSearch p){
        try{
            projectServiceImpl.remove(p);
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MSG, "msg.action.delete.success", p.getListName()));
            reload();
            
        }catch(Exception e){
            JsfUtil.addSuccessMessage("エラーがありますので、確認してください。");
        }
    }
    
    /**
     * show update form
     * 
     * @param p 
     */
    @SecureMethod(value=SecureMethod.Method.UPDATE)
    @SuppressWarnings("empty-statement")
    public void update(ProjectCustSearch p){
        this.project = p;
        /* 期間で検索場合 */
        if(project.getListSearchPeriodFlag() != null
                && project.getListSearchPeriodFlag()==1
                && NumberUtils.isNumber(project.getListSearchPeriod())) {
            this.setIsSearchPeriod(true);
            if(StringUtils.isNoneBlank(project.getListSearchPeriod())
                    && project.getListSearchPeriod().startsWith("-")) {
                this.setSearchPeriodBeforeAfter(false);
                this.setSearchPeriod(Integer.valueOf(project.getListSearchPeriod().replace("-", "")));
            } else {
                this.setSearchPeriodBeforeAfter(true);
                this.setSearchPeriod(Integer.valueOf(project.getListSearchPeriod().replace("+", "")));
            }
        }

        selectedGroup = new ArrayList<>();
        selectedMember = new ArrayList<>();
        
        this.project.getProjectRoleRel().forEach((rel) -> {
            if(rel.getProjectRoleRelPK().getGroupMemberFlag() == 0){
                selectedGroup.add(String.valueOf(rel.getProjectRoleRelPK().getGroupMemberId()));
            }else{
                selectedMember.add(String.valueOf(rel.getProjectRoleRelPK().getGroupMemberId()));
            }
        });
        
        reorderPickListDynamicCol(true, "update");
        
        ais.search(p);
        showForm();
    }
    
    private void reorderPickListDynamicCol(boolean isfirst, String type) {
        List<DynamicColumn> allVisibleCol = getListAllVisibleColumn();
        
        if(isfirst) {
            columns = new DualListModel<>(allVisibleCol, new ArrayList<>());
            
            // nhung dynamic-column da lua chon roi.
            if("update".equals(type)) {
                List<DynamicColumn> targetCols = new ArrayList<>();
                List<String> dbColumnData = new Gson().fromJson(this.project.getListViewData(), List.class);
                dbColumnData.forEach((col) -> {
                    targetCols.add(new DynamicColumn(col, "label."+col));
                });
                columns.setTarget(targetCols);
            }
        }
        
        // loai bo nhung dynamic-column da nua chon.
        final List<DynamicColumn> sourceCols = new ArrayList<>();
        allVisibleCol.forEach((item) -> {
            if(!columns.getTarget().stream().map((other) -> {
                return other;
            }).anyMatch((other) -> {
                return other.equals(item);
            })){
                sourceCols.add(item);
            };
        });
        columns.setSource(sourceCols);
    }
    
    private boolean isValid(){
        if(this.columns.getTarget().isEmpty()){
            JsfUtil.getResource().alertMsg("label.project_list_column", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }

        if(this.isSearchPeriod) {
            if(this.getSearchPeriod()==null || this.getSearchPeriod()==0) {
                JsfUtil.getResource().alertMsg("label.issue_search_from_now_errors", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
                return false;
            }
            if(this.getSearchPeriodBeforeAfter()==null) {
                JsfUtil.getResource().alertMsg("label.issue_search_months", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
                return false;
            }
        }
        return true;
    }
    
    /**
     * Update project info action
     */
    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void doUpdate(){
        
        if(!isValid()) return;
        
        this.project.setCompany(new Company(UserModel.getLogined().getCompanyId()));
        this.project.setUpdatedTime(new Date());
        this.project.setUpdatedId(UserModel.getLogined().getUserId());
        this.project.setListDeleted((short)0);
        this.project.setListSearchPeriodFlag(this.isSearchPeriod==true?(short)1:(short)0);
        /* 期間で検索設定 */
        setSreachPeriod(this.project);

        this.project.getProjectRoleRel().clear();
        
        for(String groupId : selectedGroup){
            ProjectRoleRel rel = new ProjectRoleRel();
            rel.setCreatorId(UserModel.getLogined().getUserId());
            rel.setProjectRoleRelPK(new ProjectRoleRelPK(this.project.getListId(), UserModel.getLogined().getCompanyId(), (short)0, Integer.parseInt(groupId)));
            this.project.getProjectRoleRel().add(rel);
        }
        for(String memberId : selectedMember){
            ProjectRoleRel rel = new ProjectRoleRel();
            rel.setCreatorId(UserModel.getLogined().getUserId());
            rel.setProjectRoleRelPK(new ProjectRoleRelPK(this.project.getListId(), UserModel.getLogined().getCompanyId(), (short)1, Integer.parseInt(memberId)));
            this.project.getProjectRoleRel().add(rel);
        }
        
        List<String> selectedColumn = new ArrayList<>();
        this.columns.getTarget().forEach((dc) -> {
            selectedColumn.add(dc.getId());
        });
        this.project.setListViewData(new Gson().toJson(selectedColumn));
        
        try{
            projectServiceImpl.edit(this.project);
            
            //saveColumnWidth();
            columnWidth.forEach((column, width) -> {
                projectServiceImpl.saveColumnWidth(column, width, UserModel.getLogined().getCompanyId(), UserModel.getLogined().getUserId());
            });
            
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MSG, "msg.action.update.success", this.project.getListName()));
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            JsfUtil.addErrorMessage("エラーがありますので、確認してください。");
        }
        
        goback();
    }
    
    /**
     * Show create form
     * @param listType
     */
    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void create(Short listType){
        this.project = new ProjectCustSearch();
        this.project.setListType(listType);
        selectedGroup = new ArrayList<>();
        selectedMember = new ArrayList<>();
        
        reorderPickListDynamicCol(true, "create");
        showForm();
    }
    
    /**
     * Create new project info action
     */
    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void doCreate(){
        if(!isValid()) return;
        
        this.project.setCompany(new Company(UserModel.getLogined().getCompanyId()));
        this.project.setCreatedTime(new Date());
        this.project.setCreator(UserModel.getLogined().getMember());
        this.project.setListDeleted((short)0);
        /* 期間で検索設定 */
        setSreachPeriod(this.project);

        List<String> selectedColumn = new ArrayList<>();
        this.columns.getTarget().forEach((dc) -> {
            selectedColumn.add(dc.getId());
        });
        this.project.setListViewData(new Gson().toJson(selectedColumn));
        
        try {
            projectServiceImpl.create(this.project);
            for (String groupId : selectedGroup) {
                ProjectRoleRel rel = new ProjectRoleRel();
                rel.setCreatorId(UserModel.getLogined().getUserId());
                rel.setProjectRoleRelPK(new ProjectRoleRelPK(this.project.getListId(), UserModel.getLogined().getCompanyId(), (short) 0, Integer.parseInt(groupId)));
                this.project.getProjectRoleRel().add(rel);
            }
            for (String memberId : selectedMember) {
                ProjectRoleRel rel = new ProjectRoleRel();
                rel.setCreatorId(UserModel.getLogined().getUserId());
                rel.setProjectRoleRelPK(new ProjectRoleRelPK(this.project.getListId(), UserModel.getLogined().getCompanyId(), (short) 1, Integer.parseInt(memberId)));
                this.project.getProjectRoleRel().add(rel);
            }
            projectServiceImpl.edit(this.project);

            if(project.getListType() == 3){
                this.searches.add(project);
            }else if(project.getListType() == 1){
                this.projects.add(project);
            }
            
            //saveColumnWidth();
            columnWidth.forEach((column, width) -> {
                projectServiceImpl.saveColumnWidth(column, width, UserModel.getLogined().getCompanyId(), UserModel.getLogined().getUserId());
            });
            
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MSG, "msg.action.create.success", this.project.getListName()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JsfUtil.addErrorMessage("エラーがありますので、確認してください。");
        }
        goback();
    }
    
    private void saveColumnWidth(DataTable tabler){
        tabler.getColumns().forEach((column) -> {
            String name = column.getField();
            String width = column.getWidth();
            ((UIComponent)column).getAttributes();
        });
        
    }
    
    /**
     * Get all visible column
     * @return 
     */
    public List<DynamicColumn> getListAllVisibleColumn() {
        listAllVisibleColumn = new ArrayList<>();
        if(this.customizePageId == null){
            this.customizePageId = issueService.getCustomizePageId("IssueController", companyId);
        }
        if(this.customizePageId == null) this.customizePageId = 0;
        List<AutoFormPageTabDivItemRel> dynamicColumns = this.pageTabServiceImpl.findRelList(customizePageId, InterfaceUtil.PAGE.DYNAMIC, UserModel.getLogined().getCompanyId());
        listAllVisibleColumn = SelectUtil.getViewDynamicColumns(dynamicColumns);
        return listAllVisibleColumn;
    }

    /**
     * Get company list for display search
     * @return 
     */
    public String getCompanyList(){
        List<Company> companyList = companyServiceImpl.findAll();
        JsonArray json = new JsonArray();
        companyList.forEach((t) -> {
            JsonObject jo = new JsonObject();
            jo.addProperty("value", t.getCompanyId());
            jo.addProperty("label", t.getCompanyName());
            
            json.add(jo);
        });
        
        Gson gson = new Gson();
        return gson.toJson(json);
    }

    /**
     * Get All visible column belong on current selected project
     * 
     * @return 
     */
    public List<SelectItem> getLiveSearchVisibleColumns() {
        return liveSearchVisibleColumns;
    }
    
    /**
     * Index method for this page
     */
    @SecureMethod(value=SecureMethod.Method.INDEX, require = false)
    public void loadProjectList() {
        String menuClick = getParameter("menuClick");
        if("1".equals(menuClick)) {
            this.layout.setCenter("/modules/project/issue_list.xhtml");
        }
        if(this.project != null && !projects.isEmpty()){ //load current project
            project = projects.get(0);
            this.search(project);
        }else if(projects.size() > 0){ //load first project
            this.search(projects.get(0));
        }else{ //load all issues
            this.search(null);
        }
    }
    
    /**
     * Go back to current project with issue list
     */
    public void goback(){
        projects = projectServiceImpl.findAllAvaiable(companyId, UserModel.getLogined().getMember());
        searches = projectServiceImpl.findSearchAvaiableList(companyId);
        
        columnWidth.clear();
        List<ProjectCustColumnWidth> widthList = projectServiceImpl.getColumnWidthList(companyId);
        for(ProjectCustColumnWidth w : widthList){
            columnWidth.put(w.getColumnId(), w.getColumnWidth());
        }
        this.search(project);
    }
    
    /**
     * Reload display data and render datatable
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void reload(){
        init();
        loadProjectList();
    }
        
    /**
     * View Issue list for selected project
     * 
     * @param p 
     */
    @SecureMethod(SecureMethod.Method.SEARCH)
    public void search(ProjectCustSearch p) {
        this.project = p;
        this.customizePageId = issueService.getCustomizePageId("IssueController", companyId);
        //get live search columns
        startMeasure("ProjectController.getLiveSearchVisibleColumns");
        liveSearchVisibleColumns.clear();
        List<Integer> avaiableItems = new ArrayList<>();
        ais.init();
        if(this.project != null){
            avaiableItems.addAll(pageTabServiceImpl.listDynamicItems(companyId, customizePageId));
            List<String> dbColumnData = new Gson().fromJson(this.project.getListViewData(), List.class);
            dbColumnData.forEach((col) -> {
                if(col.startsWith(InterfaceUtil.FIELDS.DYNAMIC)){
                    Integer dynamicItemId = NumberUtils.toInt(col.replace(InterfaceUtil.FIELDS.DYNAMIC, ""));
                    if(!avaiableItems.contains(dynamicItemId)){
                        return;
                    }
                }
                SelectItem item = new SelectItem();
                item.setValue(col);
                item.setLabel(col.contains("cust_") ? 
                        JsfUtil.getResource().message( companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME, "label." + col)
                        : JsfUtil.getResource().message( companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label." + col));
                //System.err.print(col);
//                if(StringUtil.isExistsInArray(col, SelectUtil.getviewMemoTextarea())) {
//                    item.setDescription(FIELDS.MEMO_VIEW);
//                }
                liveSearchVisibleColumns.add(item);
            });
        }else if(this.columns != null){
            this.columns.getSource().forEach((t) -> {
                SelectItem item = new SelectItem();
                item.setValue(t.getId());
                item.setLabel(t.getName());
                //System.err.print(t.getId());
//                if(StringUtil.isExistsInArray(t.getId(), SelectUtil.getviewMemoTextarea())) {
//                    item.setDescription(FIELDS.MEMO_VIEW);
//                }
                liveSearchVisibleColumns.add(item);
            });
        }
        stopMeasure("ProjectController.getLiveSearchVisibleColumns");
        
        if(this.project != null && this.project.getListId() != null){
            this.setKeyword("");
            this.setSearchDataJson("");
            

            List<String> dbColumnData = new Gson().fromJson(this.project.getListViewData(), List.class);
            //dynamic columns parsed
            List<DynamicColumn> allCols = new ArrayList();
            List<String> allStrCols = new ArrayList();
            dbColumnData.forEach((col) -> {
                if(!avaiableItems.isEmpty() && col.startsWith(InterfaceUtil.FIELDS.DYNAMIC)){
                    Integer dynamicItemId = NumberUtils.toInt(col.replace(InterfaceUtil.FIELDS.DYNAMIC, ""));
                    if(!avaiableItems.contains(dynamicItemId)){
                        return;
                    }
                }
                if(!StringUtils.isEmpty(col)){
                    allCols.add(new DynamicColumn(col, "label." + col));
                    allStrCols.add(col);
                }
            });

            // get all display fields
            JsonArray json = new JsonArray();
            allCols.forEach((t) -> {
                json.add(ais.getFieldJson(t));
            });
            
            //get all remain conditions fields
            SearchGroup[] groups = new Gson().fromJson(this.project.getListSearchData(), SearchGroup[].class);
            SearchFilter sf = new SearchFilter(Arrays.asList(groups));
            List<String> conditionFields = sf.getConditionFields();
            JsonArray jsonConds = new JsonArray();
            conditionFields.forEach((cf) -> {
                if(cf != null && !allStrCols.contains(cf)){
                    JsonObject jo = new JsonObject();
                    jo.addProperty("label", cf.contains("cust_") 
                                    ? JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME, "label." + cf) 
                                    : JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label." + cf));
                    jo.addProperty("name", cf);
                    jsonConds.add(jo);

                    DynamicColumn dc = SelectUtil.getDynamicColumn(allCols, cf);
                    if(dc == null || StringUtils.isEmpty(dc.getId())) {
                        DynamicColumn addDc = new DynamicColumn(cf, "label." + cf);
                        json.add(ais.getFieldJson(addDc));
                    }
                }
            });
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            this.columnViewData = gson.toJson(json);
            this.extraFieldNames = gson.toJson(jsonConds);
            ais.search(p);
            this.layout.setCenter("/modules/project/issue_list.xhtml");
        }else{
            this.layout.setCenter("/modules/project/blank.xhtml");
        }
    }

    /**
     * Filter Issue 
     * @param filter 
     */
    @Override
    protected void doSearch(SearchFilter filter) {
        
        if(this.project != null && this.project.getListType() == 1){ //案件リスト場合はデータベスデータとフォームデータをマージします。
            SearchGroup[] recentGroups = new Gson().fromJson(this.project.getListSearchData(), SearchGroup[].class);
            filter.merge(new SearchFilter(Arrays.asList(recentGroups)));
        }
        boolean quickSearch = configServiceImpl.get("ELASTIC_ENABLE").equalsIgnoreCase("true");
        filter.setModule(SecurePage.Module.ISSUE);
        String query = filter != null ? filter.getQuery(quickSearch) : "";
        List<String> conditionFields = filter != null ? filter.getConditionFields() : new ArrayList<>();
        
        Date fromDateSearch = null, toDateSearch = null;
        if(project == null){
            toDateSearch = DateUtil.getAddSupDate(0);
            fromDateSearch = DateUtil.getAddSupDate(-6);
//            Calendar c = Calendar.getInstance();
//            toDateSearch = c.getTime();
//            c.add(Calendar.MONTH, -6);
//            toDateSearch = c.getTime();
        }else{
            fromDateSearch = project.getDateFrom();
            toDateSearch = project.getDateTo();
        }
        
        ais.setIssues(projectServiceImpl.advanceSearch(
                UserModel.getLogined().getCompanyBusinessFlag(),
                UserModel.getLogined().getCompanyId(), 
                UserModel.getLogined().getUserId(), 
                quickSearch, 
                query, conditionFields,
                keyword,
                keywordCondition,
                this.getLiveSearchVisibleColumns(),
                fromDateSearch,
                toDateSearch,
                getLocale())
        );
        JsfUtil.clearStateOfDataTable("issueListData");
    }
    
    public void onResize(ColumnResizeEvent event) {
        String clientId = event.getColumn().getClientId();
        String column = clientId.substring(clientId.lastIndexOf(":")+1);
        int width = event.getWidth() + 22;
        
        saveColumnWidth((DataTable)event.getSource());
        
        //final boolean[] isMaxWidth = {true};
//        columnWidth.forEach((c, w) -> {
//            if(width < w){
//                isMaxWidth[0] = false;
//            }
//        });
        //if(!isMaxWidth[0]) 
            columnWidth.put(column, width);
    }

    public void onTransferPickListDynamicCol(TransferEvent event) {
        reorderPickListDynamicCol(false, "");
    }

    public void changeSearchPeriod() {
        if(this.isSearchPeriod==null || !this.isSearchPeriod) {
            this.setSearchPeriodBeforeAfter(null);
        } else {
            if(this.searchPeriodBeforeAfter==null) this.setSearchPeriodBeforeAfter(false);
            if(this.searchPeriod==null) this.setSearchPeriod(3);
        }
    }

    private void setSreachPeriod(ProjectCustSearch project) {
        if(this.isSearchPeriod==true && this.searchPeriod > 0) {
            project.setListSearchPeriodFlag((short)1);
            project.setListSearchPeriod(StringUtil.getMysqlDateAddSup(this.searchPeriodBeforeAfter, this.searchPeriod));
        } else {
            project.setListSearchPeriodFlag((short)0);
            project.setListSearchPeriod(null);
            this.setSearchPeriodBeforeAfter(false);
            this.setSearchPeriod(null);
        }
    }
}
