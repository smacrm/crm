/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.company;

import gnext.bean.Company;
import gnext.controller.company.CompanyController;
import gnext.model.AbstractLazyList;
import gnext.service.CompanyService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;

/**
 *
 * @author daind
 */
public class CompanyLazyList extends AbstractLazyList<CompanyModel> {
    private static final long serialVersionUID = 7456147331510855611L;
    
    private List<CompanyModel> companyModels;
    private final CompanyService companyService;
    private final CompanyController ctrl;
    
    public CompanyLazyList(CompanyController ctrl) {
        this.ctrl = ctrl;
        this.companyService = ctrl.getCompanyService();
    }
    
    @Override
    public Object getRowKey(CompanyModel companyModel) {
        return companyModel.getCompany().getCompanyId();
    }
    
    @Override
    public CompanyModel getRowData(String companyId) {
        Integer id = Integer.valueOf(companyId);
        for (CompanyModel companyModel : companyModels) {
            if (id.equals(companyModel.getCompany().getCompanyId())) return companyModel;
        }
        return null;
    }
    
    @Override
    protected int getTotal() {
        return companyService.total(ctrl.getQuery());
    }

    @Override
    protected List<CompanyModel> getResults(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<Company> results = companyService.find(first, pageSize, sortField, sortField, ctrl.getQuery());
        companyModels = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            CompanyModel companyModel = new CompanyModel(results.get(i));
            companyModel.setRowNum(first + i);
            companyModels.add(companyModel);
        }
        return companyModels;
    }
}
