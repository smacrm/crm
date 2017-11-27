/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.company.group;

import gnext.bean.Group;
import gnext.bean.role.Role;
import gnext.bean.role.SystemUseAuthRel;
import gnext.controller.company.GroupController;
import gnext.model.BaseModel;
import gnext.service.role.SystemUseAuthRelService;
import gnext.util.DateUtil;
import gnext.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public class GroupModel extends BaseModel<Group> {
    private static final long serialVersionUID = 2196415454129036688L;

    @Getter @Setter private Group group;
    @Getter @Setter private Integer[] roleIds;
    @Getter @Setter private String displayRoleName;
    @Getter private String viewRoleName;
    private List<Role> roles;

    public GroupModel(final Group group) {
        this.group = group;
        if (group.getParent() == null) {
            group.setParent(new Group());
        }
    }

    public void updateRoleInfors(GroupController groupController, int cid) {
        this.roles = new ArrayList<>();
        this.roleIds = new Integer[0];
        SystemUseAuthRelService authRelService = groupController.getSystemUseAuthRelService();
        //Tìm tất cả các role được phân quyền cho group loại bỏ các role có trạng thái là ẩn.
        if (group != null && group.getGroupId() != null) {
            List<SystemUseAuthRel> systemUseAuthRels = authRelService.findByRoleFlag(cid, group.getGroupId(), SystemUseAuthRel.GROUP_FLAG, Role.ROLE_UN_HIDDEN);
            if (systemUseAuthRels != null && !systemUseAuthRels.isEmpty()) {
                this.roleIds = new Integer[systemUseAuthRels.size()];
                for (int i = 0; i < systemUseAuthRels.size(); i++) {
                    this.roleIds[i] = systemUseAuthRels.get(i).getSystemUseAuthRelPK().getRoleId();
                    this.roles.add(groupController.getRoleService().find(this.roleIds[i]));
                }
            }
        }
        this.populateDisplayRoleName(groupController);
        this.viewRoleName = _BuildRoleName(groupController);
    }
    
    public String getDisplayCreateTime() {
        return DateUtil.getDateToString(group.getCreatedTime(), DateUtil.PATTERN_JP_SLASH);
    }

    public String getDisplayUpdateTime() {
        return DateUtil.getDateToString(group.getUpdatedTime(), DateUtil.PATTERN_JP_SLASH);
    }

    public boolean isDeleted() {
        return this.group.getGroupDeleted() == 1;
    }

    public void populateDisplayRoleName(GroupController groupController) {
        this.displayRoleName = _BuildRoleName(groupController);
        if (StringUtils.isEmpty(this.displayRoleName)) {
            displayRoleName = "ロール";
        }
    }

    private String _BuildRoleName(GroupController groupController) {
        StringBuilder sb = new StringBuilder();
        if (roleIds != null && roleIds.length > 0) {
            for (int i = 0; i < roleIds.length; i++) {
                Role role = groupController.getRoleService().find(roleIds[i]);
                sb.append(role.getRoleName()).append(",");
            }
        }
        return StringUtil.killLastCharacter(sb, ",");
    }
}
