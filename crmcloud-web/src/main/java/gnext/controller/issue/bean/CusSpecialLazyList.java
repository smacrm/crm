package gnext.controller.issue.bean;

import gnext.controller.issue.CustomerController;
import gnext.model.AbstractLazyList;
import gnext.model.authority.UserModel;
import gnext.service.issue.CustomerService;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;
import org.springframework.util.StringUtils;

/**
 *
 * @author daind
 */
public class CusSpecialLazyList extends AbstractLazyList<Map<String, String>> {
    private static final long serialVersionUID = 4302923156375303729L;
    
    private final CustomerService customerService;
    private final CustomerController ctrl;

    public CusSpecialLazyList(CustomerController ctrl) {
        this.ctrl = ctrl;
        this.customerService = ctrl.getCustomerService();
    }

    @Override
    protected int getTotal() {
        int companyId = UserModel.getLogined().getCompanyId();
        String lang = UserModel.getLogined().getLanguage();
        
        return customerService.total(1, companyId, null, null, "", "", ctrl.getQuery(),
                ctrl.getSearchType(), ctrl.getKeyqord(), ctrl.getOperator(), lang);
    }

    @Override
    protected List<Map<String, String>> getResults(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        if(StringUtils.isEmpty(sortField))
            sortField = "";
        else
            sortField = sortField.replaceAll("-", "\\.");
        
        String szSortOrder = "";
        if(sortOrder != null) szSortOrder = sortOrder==SortOrder.ASCENDING?"asc":"desc";
        
        int companyId = UserModel.getLogined().getCompanyId();
        String lang = UserModel.getLogined().getLanguage();
        
        return customerService.find(0, companyId, first, pageSize, sortField, szSortOrder, ctrl.getQuery(),
                ctrl.getSearchType(), ctrl.getKeyqord(), ctrl.getOperator(), lang);
    }
}
