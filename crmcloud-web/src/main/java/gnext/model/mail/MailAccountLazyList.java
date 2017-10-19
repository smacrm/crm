/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.mail;

import gnext.bean.MailAccount;
import gnext.controller.mail.MailAccountController;
import gnext.model.AbstractLazyList;
import gnext.service.mail.MailAccountService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;

/**
 *
 * @author daind
 */
public class MailAccountLazyList extends AbstractLazyList<MailAccountModel> {
    private static final long serialVersionUID = -587109432041179631L;

    private List<MailAccountModel> models;
    private MailAccountController ctrl;

    public MailAccountLazyList(MailAccountController ctrl) {
        this.ctrl = ctrl;
    }

    @Override
    public Object getRowKey(MailAccountModel model) {
        return model.getMailAccount().getAccountId();
    }

    @Override
    public MailAccountModel getRowData(String accountId) {
        Integer id = Integer.valueOf(accountId);
        for (MailAccountModel model : models) {
            if (id.equals(getRowKey(model))) return model;
        }
        return null;
    }

    private MailAccountService mailAccountService() {
        return ctrl.getMailAccountService();
    }

    @Override
    protected int getTotal() {
        return mailAccountService().total(ctrl.getQuery());
    }

    @Override
    protected List<MailAccountModel> getResults(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<MailAccount> mailAccounts = mailAccountService().find(first, pageSize, sortField, sortField, ctrl.getQuery());
        models = new ArrayList<>();
        for (int i = 0; i < mailAccounts.size(); i++) {
            MailAccountModel model = new MailAccountModel(mailAccounts.get(i));
            model.setRowNum(first + i);
            models.add(model);
        }
        return models;
    }
}
