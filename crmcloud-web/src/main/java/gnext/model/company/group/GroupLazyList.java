/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.company.group;

import gnext.bean.Group;
import gnext.controller.company.GroupController;
import gnext.model.AbstractLazyList;
import gnext.service.GroupService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;

/**
 *
 * @author daind
 */
public class GroupLazyList extends AbstractLazyList<GroupModel> {
    private static final long serialVersionUID = -5756576145544922150L;

    private List<GroupModel> groupModels;
    private final GroupController controller;
    private final GroupService groupService;

    public GroupLazyList(final GroupController controller) {
        this.controller = controller;
        this.groupService = controller.getGroupService();
    }

    @Override
    public Object getRowKey(GroupModel groupModel) {
        return groupModel.getGroup().getGroupId();
    }

    @Override
    public GroupModel getRowData(String groupId) {
        Integer id = Integer.valueOf(groupId);
        for (GroupModel groupModel : groupModels) {
            if (id.equals(getRowKey(groupModel))) return groupModel;
        }
        return null;
    }

    @Override
    protected int getTotal() {
        return groupService.total(controller.getQuery());
    }

    @Override
    protected List<GroupModel> getResults(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<Group> groups = groupService.find(first, pageSize, sortField, sortField, controller.getQuery());
        groupModels = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            GroupModel model = new GroupModel(group);
            model.setRowNum(first + i);
            model.updateRoleInfors(controller, controller.getCurrentCompanyId());
            groupModels.add(model);
        }
        return groupModels;
    }
}
