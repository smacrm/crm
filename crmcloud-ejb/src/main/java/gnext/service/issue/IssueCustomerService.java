/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue;

import gnext.bean.issue.Customer;
import gnext.bean.issue.Issue;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 * 
 * @author gnextadmin
 */
@Local
public interface IssueCustomerService extends EntityService<Customer> {
    /**
     * Search customer by phone number (twilio)
     * 
     * @author hungpham
     * @param companyId
     * @param phoneNumber
     * @return 
     */
    public List<Customer> searchByPhoneNumber(Integer companyId, String phoneNumber);
    
    /**
     * Search First issue relate by phone number (twilio)
     * 
     * @author hungpham
     * @param companyId
     * @param phoneNumber
     * @return 
     */
    public Issue getIssueRelate(Integer companyId, String phoneNumber);

    /**
     * Tim kiem cac khach hang trung ten & dia chi, phuc vu cho viec import, tim kiem nhung khach hang tuong tu nhau
     * @param companyId
     * @param custCode
     * @param custFullHira
     * @param custAddress
     * @param custCity
     * @return 
     */
    public List<Customer> findNearSameCustomer(Integer companyId, String custCode, String custFullHira, String custFullKana, String custAddress, String custCity);
    
    /**
     * Remove customer update cust_deleted = 1
     * @param customer 
     */
    public void removeCustomer(Customer customer) throws Exception;
    public Customer createCustomer(Customer customer);
    public Customer updateCustomer(Customer customer);
}
