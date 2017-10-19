/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.mail;

import gnext.bean.mail.MailServer;
import gnext.controller.mail.MailServerController;
import gnext.model.AbstractLazyList;
import gnext.service.mail.MailServerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;

/**
 *
 * @author daind
 */
public class MailServerLazyList extends AbstractLazyList<MailServerModel> {
    private static final long serialVersionUID = 2257054639014840275L;

    private List<MailServerModel> models;
    private final MailServerController ctrl;

    public MailServerLazyList(MailServerController ctrl) {
        this.ctrl = ctrl;
    }

    @Override
    public Object getRowKey(MailServerModel model) {
        return model.getMailServer().getServerId();
    }

    @Override
    public MailServerModel getRowData(String serverId) {
        Integer id = Integer.valueOf(serverId);
        for (MailServerModel model : models) {
            if (id.equals(getRowKey(model))) return model;
        }
        return null;
    }

    private MailServerService mailServerService() {
        return ctrl.getMailServerService();
    }

    @Override
    protected int getTotal() {
        return mailServerService().total(ctrl.getQuery());
    }

    @Override
    protected List<MailServerModel> getResults(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<MailServer> mailServers = mailServerService().find(first, pageSize, sortField, sortField, ctrl.getQuery());
        models = new ArrayList<>();
        for (int i = 0; i < mailServers.size(); i++) {
            MailServerModel model = new MailServerModel(mailServers.get(i));
            model.setRowNum(first + i);
            models.add(model);
        }
        return models;
    }
}
