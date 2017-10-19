/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import gnext.bean.role.Method;
import gnext.bean.role.Page;
import gnext.bean.role.RolePageMethodRel;
import gnext.bean.role.SystemModule;
import gnext.model.authority.PageModel;
import gnext.model.authority.UserModel;
import gnext.security.SecurityService;
import gnext.service.role.SystemModuleService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author daind
 */
public final class ComponentUtil {
    
    /**
     * Hàm xử lí trả về ma trận phân quyền chức năng cho MEMBER hoặc GROUP.
     * @param roleColumnLabels
     * @param systemModuleImpl
     * @param securityService
     * @param roleColumns
     * @param roleRows
     * @return 
     */
    public static TreeNode createTreePermisson(List<String> roleColumnLabels, SystemModuleService systemModuleImpl, SecurityService securityService, Map<String, Boolean> roleColumns, Map<Integer, Boolean> roleRows) {
        roleColumnLabels.clear();
        roleColumns.clear();
        roleRows.clear();
        
        Boolean isMaster = UserModel.getLogined().isSuperAdmin();
        int maxColumnCount = 0;
        List<SystemModule> modules = systemModuleImpl.findAllAvailable();
        PageModel authTreeRootDocument = new PageModel("ROOT");
        TreeNode authTreeRoot = new DefaultTreeNode(authTreeRootDocument, null);
        
        PageModel rowCheckItemDocument = new PageModel("FIRST_ROW");
        new DefaultTreeNode(rowCheckItemDocument, authTreeRoot); //create new node an add to tree root
        for (SystemModule module : modules) {
            if(!isMaster && !securityService.hasModule(module.getModuleName())) continue; //neu khong nam trong danh sach cho phep thi bo qua
            if(checkIgnoreModule(module.getModuleName())) continue;
            
            TreeNode moduleTreeNode = new DefaultTreeNode(new PageModel(module.getModuleId(), module.getModuleName()), authTreeRoot);
            List<Page> pages = module.getPages();
            for (Page page : pages) {
                if(!isMaster && !securityService.hasPage(module.getModuleName(), page.getPageName())) continue; //neu khong nam trong danh sach cho phep thi bo qua
                if(checkIgnoreModule(page.getPageName())) continue;
                
                List<Method> methods = page.getMethodList();
                List<PageModel.ActionModel> actionList = new ArrayList<PageModel.ActionModel>();
                PageModel pageItemData = new PageModel(page.getPageId(), page.getPageName());
                pageItemData.setSource(page.getPageSource());
                for (Method method : methods) {
                    if(!isMaster && !securityService.hasMethod(module.getModuleName(), page.getPageName(), method.getMethodName())) continue; //neu khong nam trong danh sach cho phep thi bo qua
                    
                    actionList.add(pageItemData.
                        new ActionModel(method.getMethodId(), method.getMethodName()
                    ));
                    if(!roleColumnLabels.contains(method.getMethodName())) roleColumnLabels.add(method.getMethodName());
                }
                roleRows.put(page.getPageId(), Boolean.FALSE);
                pageItemData.setAction(actionList);
                new DefaultTreeNode(pageItemData, moduleTreeNode);
                if (methods.size() > maxColumnCount) {
                    maxColumnCount = methods.size();
                }
            }
            if (pages.size() > 0) {
                moduleTreeNode.setExpanded(true);
            }
        }

        List<PageModel.ActionModel> firstRowActionList = new ArrayList<>();
        for (int i = 0; i < roleColumnLabels.size(); i++) {
            roleColumns.put(roleColumnLabels.get(i), Boolean.FALSE);
            firstRowActionList.add(rowCheckItemDocument.new ActionModel(i));
        }
        rowCheckItemDocument.setAction(firstRowActionList);

        return authTreeRoot;
    }
    
    public static void fillPermissionForTree(List<RolePageMethodRel> rolePageMethodRefList, Map<String, Boolean> roleColumns, Map<Integer, Boolean> roleRows, TreeNode authList) {
        Map<String, Boolean> tmpRoleColumnState = new HashMap<>();
        roleColumns.forEach((key, value) -> { tmpRoleColumnState.put(key, Boolean.TRUE); });
        for (TreeNode module : authList.getChildren()) {
            for (TreeNode page : module.getChildren()) {
                PageModel pageData = (PageModel) page.getData();
                boolean isAllChecked = true;
                for (PageModel.ActionModel action : pageData.getAction()) {
                    action.setSelected(false);
                    for (RolePageMethodRel mapping : rolePageMethodRefList) {
                        if (mapping.getRolePageMethodRelPK().getPageId() == pageData.getId()
                                && mapping.getRolePageMethodRelPK().getMethodId() == action.getId()) {
                            action.setSelected(true);
                        }
                    }
                    tmpRoleColumnState.put(action.getName(), tmpRoleColumnState.get(action.getName()) && action.isSelected());
                    isAllChecked &= action.isSelected();
                }
                if (isAllChecked) {
                    roleRows.put(pageData.getId(), Boolean.TRUE);
                } else {
                    roleRows.put(pageData.getId(), Boolean.FALSE);
                }
            }
        }
        tmpRoleColumnState.forEach((key, value) -> { roleColumns.put(key, value); });
    }
    
    public static void togglePermissionRow(AjaxBehaviorEvent event, TreeNode authList, List<String> roleColumnLabels, Map<String, Boolean> roleColumns) {
        SelectBooleanCheckbox permit = (SelectBooleanCheckbox) event.getComponent();
        boolean checked = (Boolean) permit.getValue();
        FacesContext context = FacesContext.getCurrentInstance();
        int refId = context.getApplication().evaluateExpressionGet(context, "#{document.id}", Integer.class);
        Map<String, Boolean> tmpRoleColumnState = new HashMap<String, Boolean>();
        for (String label : roleColumnLabels) {
            tmpRoleColumnState.put(label, Boolean.TRUE);
        }
        for (TreeNode module : authList.getChildren()) {
            for (TreeNode page : module.getChildren()) {
                PageModel pageData = (PageModel) page.getData();
                for (PageModel.ActionModel action : pageData.getAction()) {
                    if (refId == pageData.getId()) {
                        action.setSelected(checked);
                    }
                    tmpRoleColumnState.put(action.getName(), tmpRoleColumnState.get(action.getName()) && action.isSelected());
                }
            }
        }

        for (int i = 0; i < roleColumnLabels.size(); i++) {
            roleColumns.put(roleColumnLabels.get(i), tmpRoleColumnState.get(roleColumnLabels.get(i)));
        }
    }
    
    public static void togglePermissionCell(TreeNode authList, List<String> roleColumnLabels, Map<Integer, Boolean> roleRows, Map<String, Boolean> roleColumns) {
        Map<String, Boolean> tmpRoleColumnState = new HashMap<>();
        for (String label : roleColumnLabels) {
            tmpRoleColumnState.put(label, Boolean.TRUE);
        }
        for (TreeNode module : authList.getChildren()) {
            for (TreeNode page : module.getChildren()) {
                PageModel pageData = (PageModel) page.getData();
                boolean isAllChecked = true;
                for (PageModel.ActionModel action : pageData.getAction()) {
                    isAllChecked &= action.isSelected();
                    tmpRoleColumnState.put(action.getName(), tmpRoleColumnState.get(action.getName()) && action.isSelected());
                }
                roleRows.put(pageData.getId(), isAllChecked);
            }
        }
        for (int i = 0; i < roleColumnLabels.size(); i++) {
            roleColumns.put(roleColumnLabels.get(i), tmpRoleColumnState.get(roleColumnLabels.get(i)));
        }
    }
    
    public static void togglePermissionColumn(String label, TreeNode authList, Map<Integer, Boolean> roleRows, Map<String, Boolean> roleColumns) {
        boolean checked = roleColumns.get(label);
        for (TreeNode module : authList.getChildren()) {
            for (TreeNode page : module.getChildren()) {
                PageModel pageData = (PageModel) page.getData();
                int colIdx = 0;
                boolean isAllChecked = true;
                for (PageModel.ActionModel action : pageData.getAction()) {
                    if (label.equalsIgnoreCase(action.getName())) {
                        action.setSelected(checked);
                    }
                    isAllChecked &= action.isSelected();
                }
                if (isAllChecked) {
                    roleRows.put(pageData.getId(), Boolean.TRUE);
                } else {
                    roleRows.put(pageData.getId(), Boolean.FALSE);
                }
            }
        }
    }
    
    private static final String[] IGNORE_ROLES = {
        "SpeechAPIController"
        ,"PageScanController"
        ,"ForgetpassController"
    };
    public static boolean checkIgnoreModule(String moduleOrPageName) {
        return StringUtil.isExistsInArray(moduleOrPageName, IGNORE_ROLES);
    }
}
