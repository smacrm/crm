/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.system;

import gnext.bean.attachment.Server;
import gnext.controller.system.ServerController;
import gnext.model.AbstractLazyList;
import gnext.service.attachment.ServerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;

/**
 *
 * @author tungdt
 */
public class ServerLazyModelList extends AbstractLazyList<ServerModel> {
    private static final long serialVersionUID = -8768793869843738460L;

    private List<ServerModel> models;
    private final ServerController ctrl;

    public ServerLazyModelList(ServerController ctrl) {
        this.ctrl = ctrl;
    }

    @Override
    public Object getRowKey(ServerModel object) {
        return object.getServer().getServerId();
    }

    @Override
    public ServerModel getRowData(String serverId) {
        Integer id = Integer.valueOf(serverId);

        for (ServerModel model : models) {
            if (id.equals(getRowKey(model))) {
                return model;
            }
        }
        return null;
    }

    private ServerService getServerService() {
        return ctrl.getServerService();
    }

    @Override
    protected int getTotal() {
        return getServerService().total(ctrl.getQuery());
    }

    @Override
    protected List<ServerModel> getResults(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<Server> servers = getServerService().find(first, pageSize, sortField, sortField, ctrl.getQuery());
        models = new ArrayList<>();
        for (int i = 0; i < servers.size(); i++) {
            ServerModel model = new ServerModel(servers.get(i));
            model.setRowNum(first + i);
            models.add(model);
        }
        return models;
    }
}
