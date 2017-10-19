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
import gnext.bean.Group;
import gnext.bean.role.Role;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.model.authority.UserModel;
import gnext.model.company.group.GroupLazyList;
import gnext.model.company.group.GroupModel;
import gnext.model.search.SearchFilter;
import gnext.multitenancy.service.MultitenancyService;
import gnext.security.SecurityService;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.CompanyService;
import gnext.service.GroupService;
import gnext.service.MemberService;
import gnext.service.role.RoleService;
import gnext.service.role.SystemUseAuthRelService;
import gnext.util.JsfUtil;
import java.util.Arrays;
import java.util.List;
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
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "groupController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.COMPANY, require = true)
public class GroupController extends AbstractController {

    private static final long serialVersionUID = 1003240022504686431L;
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupController.class);

    @EJB @Getter @Setter private GroupService groupService;
    @EJB @Getter @Setter private MemberService memberService;
    @EJB @Getter @Setter private RoleService roleService;
    @EJB @Getter @Setter private CompanyService companyService;
    @EJB @Getter @Setter private SystemUseAuthRelService systemUseAuthRelService;
    @EJB private MultitenancyService multitenancyService;
    
    @ManagedProperty(value = "#{layout}") @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{sec}") @Getter @Setter private SecurityService sec;
    
    @Getter @Setter private LazyDataModel<GroupModel> groupModels;
    @Getter @Setter private GroupModel groupModel;
    @Getter @Setter private List<Group> groups;
    @Getter @Setter private String query;
    @Getter @Setter private Integer selectGroupId;
    @Getter @Setter private List<Role> roles;
    @Getter @Setter private List<Company> listCompany;
    @Getter @Setter private String showType;
            
    private void _Load(Integer gid) {
        _FindGroupByCompany(getCurrentCompanyId(), gid);
        _FindRoleByCompany(getCurrentCompanyId());
        _FindAllCompany(); // lấy tất cả công ty có trong database.
        
        if (gid != null) { // trường hợp chỉnh sửa.
            Group group = groupService.findByGroupId(gid);
            this.groupModel = new GroupModel(group);
        } else { // trường hợp thêm mới.
            Group newGroup = new Group();
            newGroup.setCompany(new Company(-1));
            groupModel = new GroupModel(newGroup);
        }
        this.groupModel.updateRoleInfors(this, getCurrentCompanyId());
    }

    private void _FindGroupByCompany(int cid, Integer gid) {
        if (gid == null) {
            groups = groupService.findGroupTree(cid, null);
        } else {
            List<Group> disableGroups = groupService.findGroupTree(null, Arrays.asList(groupService.findByGroupId(gid)));
            groups = groupService.findGroupTree(cid, disableGroups);
        }
    }

    // Tìm tất cả các role của công ty trừ các role có trạng thái ẩn.
    private List<Role> _FindRoleByCompany(int cid) {
        roles = roleService.search(cid, "c.roleFlag=" + Role.ROLE_UN_HIDDEN +" AND c.roleDeleted = 0");
        return this.roles;
    }
    
    private List<Company> _FindAllCompany(){
        return listCompany = companyService.findAll();
    }
    
    @PostConstruct
    public void init() {
        groupModels = new GroupLazyList(this);
        query = "1=1 AND g.company.companyId = " + getCurrentCompanyId();
    }

    /**
     * Phụ thuôc kiểu để hiển thị FORM cho việc thêm mới hay chỉnh sửa.
     * @param event 
     */
    @SecureMethod(SecureMethod.Method.VIEW)
    @Override
    public void show(ActionEvent event) {
        try {
            showType = this.getParameter("showType");
            if ("view".equals(showType)) {
                String rowNum = this.getParameter("rowNum");
                this.currentRowNum = Integer.parseInt(rowNum);

                _Load(Integer.parseInt(this.getParameter("groupId")));
                this.layout.setCenter("/modules/company/group/view.xhtml");
            } else if ("edit".equals(showType)) {
                _Load(Integer.parseInt(this.getParameter("groupId")));
                this.layout.setCenter("/modules/company/group/edit.xhtml");
            } else if ("create".equals(showType)) {
                _Load(null);
                this.layout.setCenter("/modules/company/group/create.xhtml");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    @Override
    protected void afterPaging() {
        groupModels.setRowIndex(currentRowNum);
        groupModel = groupModels.getRowData();
        groupModel.updateRoleInfors(this, getCurrentCompanyId());
    }

    @SecureMethod(SecureMethod.Method.DELETE)
    @Override
    public void delete(ActionEvent event) {
        try {
            String groupId = this.getParameter("groupId");
            Group group = groupService.findByGroupId(Integer.valueOf(groupId));
            multitenancyService.deleteGroupOnSlaveDB(UserModel.getLogined().getCompanyId(), group);
            
            load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.delete.success", group.getGroupName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    @SecureMethod(SecureMethod.Method.UPDATE)
    @Override
    public void update(ActionEvent event) {
        try {
            Group g = groupModel.getGroup();
            lookupGroupParent(g);
            decidedCompanyForGroup(g);
            groupService.update(g, UserModel.getLogined().getMember(), groupModel.getRoleIds());
            load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.update.success", g.getGroupName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getLocalizedMessage());
        }
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    @Override
    public void save(ActionEvent event) {
        try {
            Group g = groupModel.getGroup();
            lookupGroupParent(g);
            decidedCompanyForGroup(g);
            Group saved = groupService.insert(g, UserModel.getLogined().getMember(), groupModel.getRoleIds());
            load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.create.success", saved.getGroupName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getLocalizedMessage());
        }
    }
    
    /**
     * trường hợp GROUP có chọn cha thì cần cập nhật thông tin của GROUP cha đó cho GROUP đang sửa.
     * @param g 
     */
    private void lookupGroupParent(final Group g) {
        if(this.groupModel.getGroup().getParent().getGroupId() != null && this.groupModel.getGroup().getParent().getGroupId() > 0) {
            Group gp = groupService.find(this.groupModel.getGroup().getParent().getGroupId());
            if(gp != null) {
                g.setParent(gp);
                g.setGroupTreeId(gp.getGroupTreeId());
            }
        }
    }
    
    /**
     * Đối với user là SUPER-ADMIN sẽ có quyền chọn công ty mà GROUP sẽ thuộc vào.
     * @param g 
     */
    private void decidedCompanyForGroup(Group g) {
        if(g.getCompany() != null && g.getCompany().getCompanyId() != null && g.getCompany().getCompanyId() > 0) {
            g.setCompany(companyService.find(g.getCompany().getCompanyId()));
        } else {
            g.setCompany(getCurrentCompany());
        }
    }
    
    /**
     * Khi người dùng chọn một GROUP trong danh sách để xóa, thông tin group-id được lưu lại.
     * Người dùng accept xóa thì lấy group-id đã lưu để xóa.
     * @param event 
     */
    public void onChangeSelectGroupId(ActionEvent event) {
        String groupId = getParameter("groupId");
        setSelectGroupId(Integer.parseInt(groupId));
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        init();
        this.layout.setCenter("/modules/company/group/index.xhtml");
    }
    
    @SecureMethod(value=SecureMethod.Method.SEARCH) public void reload() {}
    @Override
    protected void doSearch(SearchFilter filter) {
        query = "1=1 AND g.company.companyId = " + getCurrentCompanyId() + " AND ";
        query = query + (filter != null && !StringUtils.isEmpty(filter.getQuery()) ? filter.getQuery() : "1=1");
    }
    
    @Override
    @SecureMethod(value=SecureMethod.Method.DOWNLOAD)
    public void download(String tblName, String fileName) {
        super.download(tblName, fileName);
    }
    
    /**
     * Trả về danh sách tên GROUP phục vụ cho chức năng tìm kiếm nâng cao tại màn hình danh sách.
     * @return 
     */
    public String getGroupList() {
        List<Group> _groups = groupService.findAll();
        JsonArray json = new JsonArray();
        _groups.forEach((g) -> {
            JsonObject jo = new JsonObject();
            jo.addProperty("value", g.getGroupId());
            jo.addProperty("label", g.getGroupName());

            json.add(jo);
        });

        Gson gson = new Gson();
        return gson.toJson(json);
    }
    
    /**
     * Xử lí sự kiện chọn ROLES từ danh sách.
     */
    public void handleChangeRole() {
        this.groupModel.populateDisplayRoleName(this);
    }
    
    public void onSuperAdminChangeCompany(final AjaxBehaviorEvent event) {
        Integer cid = groupModel.getGroup().getCompany().getCompanyId();
        if(cid == null || cid <= 0) return;
        
        Integer gid = null;
        if ("edit".equals(showType)) gid = groupModel.getGroup().getGroupId();
        
        _FindGroupByCompany(cid, gid);
        _FindRoleByCompany(cid);
        groupModel.updateRoleInfors(this, cid);
    }
    
    @SecureMethod(SecureMethod.Method.PRINT)
    public void print(){}
}
