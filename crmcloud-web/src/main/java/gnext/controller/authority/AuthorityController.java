package gnext.controller.authority;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gnext.bean.Company;
import gnext.security.SecurityService;
import gnext.model.authority.PageModel;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.role.Role;
import gnext.bean.role.RolePageMethodRel;
import gnext.bean.role.SystemUseAuthRel;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.model.authority.UserModel;
import gnext.model.search.SearchFilter;
import gnext.multitenancy.service.MultitenancyService;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.security.annotation.SecurePage.Module;
import gnext.service.CompanyService;
import gnext.service.GroupService;
import gnext.service.MemberService;
import gnext.service.role.RolePageMethodService;
import gnext.service.role.RoleService;
import gnext.service.role.SystemModuleService;
import gnext.service.role.SystemUseAuthRelService;
import gnext.util.ComponentUtil;
import gnext.util.DateUtil;
import gnext.util.InterfaceUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StringUtil;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.TreeNode;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage roles & generate top menu
 *
 * @author hungpham
 * @since 2016/10
 */
@ManagedBean
@SecurePage(module = Module.COMPANY)
@SessionScoped() 
public class AuthorityController extends AbstractController implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(AuthorityController.class);
    
    private final String[] IGNORE_MENU = {
        SecurePage.Module.ISSUE.name(),
        SecurePage.Module.REPORT.name(),
        SecurePage.Module.SPEECHAPI.name(),
    };

    private final String[] isAdminUse = {
            "JobController"
            ,"ConfigController"
    };

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @ManagedProperty(value = "#{sec}")
    @Getter @Setter private SecurityService securityService;

    @EJB private GroupService groupServiceImpl;
    @EJB private SystemModuleService systemModuleImpl;
    @EJB private MemberService memberServiceImpl;
    @EJB private RoleService roleServiceImpl;
    @EJB private CompanyService companyServiceImpl;
    @EJB private RolePageMethodService rolePageMethodServiceImpl;
    @EJB private SystemUseAuthRelService systemUseAuthRelService;
    @EJB private MultitenancyService multitenancyService;
    
    @Setter @Getter private List<Group> groupList;
    @Setter @Getter private List<String> selectedGroup;
    @Setter @Getter private List<Member> memberList;
    @Setter @Getter private List<String> selectedMember;
    @Setter @Getter private TreeNode authList;
    @Setter @Getter private String roleName;
    @Setter @Getter private Map<String, Boolean> selectedAuths;
    @Setter @Getter private Map<String, Boolean> roleColumns;
    @Setter @Getter private List<String> roleColumnLabels;
    @Setter @Getter private Map<Integer, Boolean> roleRows;
    @Setter @Getter private List<Role> roleList;
    @Setter @Getter private Role selectedRole;
    @Setter @Getter private MenuModel moduleMenuModel;
    
    @Setter @Getter private Integer itemViewIndex = 0;
    private String exportFileName;

    @PostConstruct
    public void init() {
        try {
            exportFileName = URLEncoder.encode("ロールリスト", "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
        }
        doSearch(null);
        reloadData();
    }

    @SecureMethod(SecureMethod.Method.DOWNLOAD)
    public String getExportFileName() {
        return exportFileName;
    }
    
    public void reloadData(){
        roleColumnLabels = new ArrayList<>();
        roleRows = new HashMap<>();
        roleColumns = new HashMap<>();
        authList = ComponentUtil.createTreePermisson(roleColumnLabels, systemModuleImpl, securityService, roleColumns, roleRows);
        moduleMenuModel = buildMenu();
    }
    
    /**
     * Create System menu on left-top
     * 
     * @return 
     */
    private MenuModel buildMenu(){
        MenuModel model = new DefaultMenuModel();
        DefaultSubMenu subMenu;
        DefaultMenuItem menuItem;
        int nodeIndex = 0;
        for (TreeNode module : authList.getChildren()) {
            if(0 == nodeIndex++) continue; //ignore first treenode
            
            PageModel moduleData = (PageModel) module.getData();
            if(checkIgnoreModuleOnMenu(moduleData.getName())) continue;
            
            if(module.getChildren().size() == 1) { //if menu has only one node, display that node
                TreeNode page = module.getChildren().get(0);
                menuItem = setMenuItem(moduleData, (PageModel) page.getData(), true);
                if(menuItem == null) continue;
                model.addElement(menuItem);
            }else{ //display multiple level menu
                int idx = 0;
                subMenu = new DefaultSubMenu(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.menu." + moduleData.getName()));
                subMenu.setIcon(InterfaceUtil.menuIcon(moduleData.getName()));
                for (TreeNode page : module.getChildren()) {
                    if (idx == 0 && module.getChildren().size() == 1 ) subMenu = null;
                    
                    PageModel pageData = (PageModel) page.getData();
                    if(ignoreSpecialPages(moduleData, pageData)) continue;
                    
                    menuItem = setMenuItem(moduleData, (PageModel) page.getData(), false);
                    if(menuItem == null) continue;
                    if(subMenu != null){
                        subMenu.addElement(menuItem);
                    }
                    idx++;
                }
                if(subMenu != null) model.addElement(subMenu);
            }
        }
        return model;
    }
    
    private boolean ignoreSpecialPages(PageModel moduleData, PageModel pageData) {
        if( StringUtils.isEmpty(pageData.getName()) || ignoreSpecialModules(moduleData)
            || !securityService.hasPage(moduleData.getName(), pageData.getName())
            || ComponentUtil.checkIgnoreModule(pageData.getName())
            || (!UserModel.getLogined().isSuperAdmin() && StringUtil.isExistsInArray(pageData.getName(), this.isAdminUse)))
                return true;
        
        return false;
    }
    
    private boolean ignoreSpecialModules(PageModel moduleData) {
        if( !securityService.hasModule(moduleData.getName())
            || ComponentUtil.checkIgnoreModule(moduleData.getName())
            || (!UserModel.getLogined().isSuperAdmin() && StringUtil.isExistsInArray(moduleData.getName(), this.isAdminUse))) 
                return true;
        
        return false;
    }
    
    private boolean checkIgnoreModuleOnMenu(String moduleOrPageName) {
        return StringUtil.isExistsInArray(moduleOrPageName, this.IGNORE_MENU);
    }

    /**
     * Get Submenu object with his own property
     * @param moduleData
     * @param pageData
     * @param onePage
     * @return 
     */
    private DefaultMenuItem setMenuItem(PageModel moduleData, PageModel pageData, boolean onePage) {
        DefaultMenuItem menuItem = new DefaultMenuItem(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.menu." + pageData.getName()));
        
        StringBuilder command = new StringBuilder(StringUtils.uncapitalize(pageData.getName()));
        boolean isHasLoadMethod = false;
        try {
            java.lang.reflect.Method[] methodList = Class.forName(pageData.getSource()).getDeclaredMethods();
            for (final java.lang.reflect.Method method : methodList) {
                if (method.isAnnotationPresent(SecureMethod.class)) {
                    SecureMethod m = method.getAnnotation(SecureMethod.class);
                    if(m.value() == SecureMethod.Method.INDEX){
                        command.append(".").append(method.getName());
                        isHasLoadMethod = true;
                        break;
                    }
                }
            }
        } catch (ClassNotFoundException | SecurityException e) {
            menuItem.setIcon("fa fa-exclamation-triangle");
        }

        if(isHasLoadMethod){
            menuItem.setProcess("@this");
            menuItem.setCommand("#{"+command.toString()+"}");
            menuItem.setParam("force", true);
            menuItem.setAjax(true);
            menuItem.setIcon(InterfaceUtil.menuIcon(pageData.getName()));
        }else{
            menuItem.setIcon("fa fa-exclamation-triangle");
            return null;
        }

        /** 1つページがあった場合、メニュー自動を閉じるように */
        if(onePage) menuItem.setOnclick("PF('widgetVarNavList').hide()");
        
        menuItem.setUpdate(":mainContent:,:growl");
        menuItem.setOnstart("start()");
        menuItem.setOnsuccess("finish()");
        menuItem.setRendered(securityService.hasPage(moduleData.getName(), pageData.getName()));
        
        return menuItem;
    }
    
    /**
     * Remove selected role
     * @param selectedRole 
     */
    @SecureMethod(SecureMethod.Method.DELETE)
    public void remove(Role selectedRole) {
        try{
//            roleServiceImpl.removeRelation(selectedRole.getRoleId(), 2, null);
            roleServiceImpl.delete(UserModel.getLogined().getMember(), selectedRole.getRoleId());
            JsfUtil.addSuccessMessage("削除しました。");
            reload();
        }catch(Exception e) {
            logger.error(e.getMessage(), e);
            JsfUtil.addErrorMessage("エラーがありますので、確認してください。");
        }
    }
    
    /**
     * Show create form
     * 
     * @param event 
     */
    @SecureMethod(SecureMethod.Method.CREATE)
    public void showCreateForm(ActionEvent event) {
        selectedRole = null;
        for (TreeNode module : authList.getChildren()) {
            for (TreeNode page : module.getChildren()) {
                PageModel pageData = (PageModel) page.getData();
                for (PageModel.ActionModel action : pageData.getAction()) {
                    action.setSelected(false);
                }
                this.roleRows.put(pageData.getId(), Boolean.FALSE);
            }
        }
        this.roleColumns.forEach((label, value) -> {
            this.roleColumns.put(label, Boolean.FALSE);
        });
        
        this.showForm(selectedRole, 0);
    }
    
    private void prepareShowForm() {
        // Làm mới roleName trong trường hợp thêm mới.
        if(selectedRole == null) roleName = null;
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    public void viewForm(Role selectedRole, Integer itemViewIndex){
        showForm(selectedRole, itemViewIndex);
        layout.setCenter("/modules/authority/view.xhtml");
    }
    
    /**
     * Show create/update form
     */
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void showForm() {
        layout.setCenter("/modules/authority/form.xhtml");
    }
    
    /**
     * Show create/update form
     * @param selectedRole 
     * @param itemViewIndex 
     */
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void showForm(Role selectedRole, Integer itemViewIndex) {
        this.itemViewIndex = itemViewIndex;
        int companyLoined = UserModel.getLogined().getCompanyId();
        try {
            // danh sách group của công ty logined trên master.
            groupList = groupServiceImpl.findByCompanyId(companyLoined, (short)0);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        // lấy thêm các member có thể logined vào công ty hiện tại.
        // danh sách group của công ty logined trên slave.
        List<Integer> memberIds = new ArrayList<>();
        if( !securityService.isLoginedCompanyAdmin() ) {
            memberIds.addAll(multitenancyService.findAllMemberIdsOnSlave(companyLoined));
        }
        
        memberList = memberServiceImpl.findByGroupList(groupList, (short)0, memberIds);
        this.selectedRole = selectedRole;
        
        if(null == selectedGroup) selectedGroup = new ArrayList<>(); else selectedGroup.clear();
        if(null == selectedMember) selectedMember = new ArrayList<>(); else selectedMember.clear();
        
        if(selectedRole != null){
            List<SystemUseAuthRel> systemUseAuthList = systemUseAuthRelService.find(companyLoined, selectedRole.getRoleId());
            systemUseAuthList.forEach((item) -> {
                switch(item.getSystemUseAuthRelPK().getGroupMemberFlag()){
                    case 0:
                        selectedGroup.add(String.valueOf(item.getSystemUseAuthRelPK().getGroupMemberId()));
                        break;
                    case 1:
                        selectedMember.add(String.valueOf(item.getSystemUseAuthRelPK().getGroupMemberId()));
                        break;
                }
            });
            
            List<RolePageMethodRel> rolePageMethodRefList = rolePageMethodServiceImpl.findByRoleId(selectedRole.getRoleId());
            Map<String, Boolean> tmpRoleColumnState = new HashMap<>();
            this.roleColumns.forEach((key, value) -> {
                tmpRoleColumnState.put(key, Boolean.TRUE);
            });

            for (TreeNode module : authList.getChildren()) {
                for (TreeNode page : module.getChildren()) {
                    PageModel pageData = (PageModel) page.getData();
                    boolean isAllChecked = true;
                    int idx = 0;
                    for (PageModel.ActionModel action : pageData.getAction()) {
                        action.setSelected(false);
                        for(RolePageMethodRel mapping : rolePageMethodRefList){
                            if( mapping.getRolePageMethodRelPK().getPageId() == pageData.getId() &&
                                mapping.getRolePageMethodRelPK().getMethodId() == action.getId() ){
                                action.setSelected(true);
                            }
                        }
                        tmpRoleColumnState.put(action.getName(), tmpRoleColumnState.get(action.getName()) && action.isSelected());
                        idx++;
                        isAllChecked &= action.isSelected();
                    }
                    if (isAllChecked) {
                        this.roleRows.put(pageData.getId(), Boolean.TRUE);
                    } else {
                        this.roleRows.put(pageData.getId(), Boolean.FALSE);
                    }
                }
            }
            
            tmpRoleColumnState.forEach((key, value) -> { this.roleColumns.put(key, value); });
        }
        
        prepareShowForm();
        layout.setCenter("/modules/authority/form.xhtml");
    }
    
    /**
     * Show create/update form by item index (next/prev)
     * @param itemViewIndex 
     */
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void showFormByIndex(Integer itemViewIndex){
        if(roleList.size() > itemViewIndex){
            selectedRole = roleList.get(itemViewIndex);
            this.showForm(selectedRole, itemViewIndex);
            if( !(selectedRole.getCreatorId() == Member.SUPER_ADMIN_MEMBER_ID && UserModel.getLogined().isSuperAdmin()) ){
                layout.setCenter("/modules/authority/view.xhtml");
            }
        }
    }
    
    /**
     * Show  form by item index (next/prev)
     * @param itemViewIndex 
     */
    @SecureMethod(SecureMethod.Method.VIEW)
    public void viewFormByIndex(Integer itemViewIndex){
        if(roleList.size() > itemViewIndex){
            selectedRole = roleList.get(itemViewIndex);
            this.showForm(selectedRole, itemViewIndex);
            layout.setCenter("/modules/authority/view.xhtml");
        }
    }
    /**
     * Save Role
     * Using for create/update action
     * 
     * @throws Exception 
     */
    @SecureMethod(SecureMethod.Method.CREATE)
    public void save() throws Exception {
        try {
            prepareRoleForUpdateOrCreate();
            Map<Integer, List<Integer>> rolePageMethodRels = new HashMap<>();
            for (TreeNode module : authList.getChildren()) {
                for (TreeNode page : module.getChildren()) {
                    PageModel pageData = (PageModel) page.getData();
                    for (PageModel.ActionModel action : pageData.getAction()) {
                        if (action.isSelected()) {
                            if(rolePageMethodRels.containsKey(pageData.getId())) {
                                List<Integer> actions = rolePageMethodRels.get(pageData.getId());
                                actions.add(action.getId());
                            } else {
                                List<Integer> actions = new ArrayList<>();
                                actions.add(action.getId());
                                rolePageMethodRels.put(pageData.getId(), actions);
                            }
                        }
                    }
                }
            }
            
            roleServiceImpl.createOrUpdate(UserModel.getLogined().getMember(), selectedRole, selectedGroup, selectedMember, rolePageMethodRels);
            this.reload(); //reload list after save action.
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e.getMessage());
        }
    }

    private void prepareRoleForUpdateOrCreate() {
        if (selectedRole != null && selectedRole.getRoleId() != null && selectedRole.getRoleId() > 0) {
            selectedRole.setRoleDeleted((short) 0);
            selectedRole.setUpdatedId(UserModel.getLogined().getUserId());
            selectedRole.setUpdatedTime(DateUtil.now());
        } else {
            selectedRole = new Role();
            selectedRole.setRoleName(roleName);
            selectedRole.setRoleFlag(Role.ROLE_UN_HIDDEN);
            selectedRole.setCompanyId(UserModel.getLogined().getCompanyId());
            selectedRole.setRoleDeleted((short) 0);
            selectedRole.setCreatorId(UserModel.getLogined().getUserId());
            selectedRole.setCreatedTime(DateUtil.now());
        }
    }
    
    /**
     * Action toggle one row
     * 
     * @param event 
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void toggleRoleRow(AjaxBehaviorEvent event) {
        ComponentUtil.togglePermissionRow(event, authList, roleColumnLabels, roleColumns);
    }

    /**
     * Action toggle one column
     * 
     * @param label 
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void toggleRoleColumn(String label) {
        ComponentUtil.togglePermissionColumn(label, authList, roleRows, roleColumns);
    }

    /**
     * Action toggle to each cells
     * 
     * @param event 
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void toggleRoleCell(AjaxBehaviorEvent event) {
        ComponentUtil.togglePermissionCell(authList, roleColumnLabels, roleRows, roleColumns);
    }

    /**
     * Load screen
     */
    @SecureMethod(value=SecureMethod.Method.INDEX, require = false)
    public void loadRoleList() {
        this.layout.setCenter("/modules/authority/list.xhtml");
    }
    
    /**
     * Reload data
     */
    @SecureMethod(value=SecureMethod.Method.SEARCH)
    public void reload() {
        init();
        loadRoleList();
    }
    
    /**
     * Get company list for search-filter
     * 
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
     * Do search with search-filter
     * @param filter 
     */
    @Override
    protected void doSearch(SearchFilter filter) {
        String query = filter != null ? filter.getQuery() : "";
        roleList = roleServiceImpl.search(UserModel.getLogined().getCompanyId(), query);
    }
}
