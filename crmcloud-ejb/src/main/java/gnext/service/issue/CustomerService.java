/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue;

import gnext.bean.issue.Customer;
import gnext.service.EntityService;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface CustomerService extends EntityService<Customer> {
    public List<Map<String, String>> find(int calCount, Integer companyId, Integer first, Integer pageSize, String sortField, String sortOrder, String where,
            int searchType, String keywords, String operator, String lang);
    public int total(int calCount, Integer companyId, Integer first, Integer pageSize, String sortField, String sortOrder, String where,
            int searchType, String keywords, String operator, String lang);
    
    public Customer search(String custCode, Integer companyId);
    public void importXls(List<Customer> customers) throws Exception;
}
