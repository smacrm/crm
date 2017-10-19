package gnext.service.issue.impl;

import gnext.bean.Prefecture;
import gnext.bean.issue.CustDataSpecial;
import gnext.bean.issue.Customer;
import gnext.bean.issue.Issue;
import gnext.interceptors.QuickSearchDbInterceptor;
import gnext.interceptors.annotation.QuickSearchAction;
import gnext.service.PrefectureService;
import gnext.service.impl.AbstractService;
import gnext.service.issue.IssueCustomerService;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * 
 * @author gnextadmin
 */
@Interceptors ({QuickSearchDbInterceptor.class})
@Stateless
public class IssueCustomerServiceImpl extends AbstractService<Customer> implements IssueCustomerService {

    private static final long serialVersionUID = 3337147374015117169L;
    private final Logger LOGGER = LoggerFactory.getLogger(IssueCustomerServiceImpl.class);

    @EJB private PrefectureService prefectureService;
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public IssueCustomerServiceImpl() { super(Customer.class); }

    /**
     * Search customer by phone number (twilio)
     * 
     * @author hungpham
     * @param phoneNumber
     * @return 
     */
    @Override
    public List<Customer> searchByPhoneNumber(Integer companyId, String phoneNumber) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query q = em_slave.createQuery("SELECT c FROM Customer c, c.custTargetInfoList t WHERE c.company.companyId = :companyId AND t.custFlagType IN (1, 2) AND t.custTargetData LIKE :phoneNumber")
                .setParameter("phoneNumber", "%"+phoneNumber+"%")
                .setParameter("companyId", companyId);
            List<Customer> list = q.getResultList();
            return list;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    /**
     * Search First issue relate by phone number (twilio)
     * 
     * @author hungpham
     * @param phoneNumber
     * @return 
     */
    @Override
    public Issue getIssueRelate(Integer companyId, String phoneNumber) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query q = em_slave.createQuery("SELECT c FROM Customer c, c.custTargetInfoList t WHERE c.company.companyId = :companyId AND t.custFlagType IN (1, 2) AND t.custTargetData = :phoneNumber")
                    .setParameter("phoneNumber", phoneNumber)
                    .setParameter("companyId", companyId);
            List<Customer> list = q.getResultList();
            for(Customer c : list){
                if(c.getIssueList() != null){
                    for(Issue issue : c.getIssueList()){
                        return issue;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<Customer> findNearSameCustomer(Integer companyId, String custFullHira, String custAddress, String custCity) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Query q = em_slave.createQuery("SELECT DISTINCT c FROM Customer c, c.custCity city WHERE c.company.companyId = :companyId AND (CONCAT(c.custFirstHira, c.custLastHira) = :custFullHira OR CONCAt(c.custFirstKana, c.custLastKana) = :custFullHira) AND (c.custAddress = :custAddress OR c.custAddressKana = :custAddress) AND city.prefectureName = :custCity");
            q.setParameter("custFullHira", custFullHira);
            q.setParameter("custAddress", custAddress);
            q.setParameter("custCity", custCity);
            q.setParameter("companyId", companyId);

            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public void removeCustomer(Customer customer) throws Exception {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            customer.setCustDeleted(Boolean.TRUE);
            JPAUtils.edit(customer, em_slave, true);
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw  e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    @QuickSearchAction(action = QuickSearchAction.CREATE)
    public Customer createCustomer(Customer customer) {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            if(customer.getCustId() != null && customer.getCustId() <= 0)
                customer.setCustId(null);
            
            Customer newCustomer = JPAUtils.create(customer, em_slave, false);
            
            // sau khi thêm mới lấy lại các prefecture-name sử dụng trong quick-search.
            manualPrefectureCustSpecial(newCustomer);
            
            commitAndCloseTransaction(tx_slave);
            return newCustomer;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return customer;
    }
    
    @Override
    @QuickSearchAction(action = QuickSearchAction.UPDATE)
    public Customer updateCustomer(Customer customer) {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            if(customer.getCustId() != null && customer.getCustId() < 0)
                throw new IllegalArgumentException("The cusId is invalid!");
            
            Customer editCustomer = JPAUtils.edit(customer, em_slave, false);
            
            // sau khi chỉnh sửa lấy lại các prefecture-name sử dụng trong quick-search.
            manualPrefectureCustSpecial(editCustomer);
//            editCustomer.initSpecicalData();
            
            commitAndCloseTransaction(tx_slave);
            return editCustomer;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return customer;
    }
    
    private void manualPrefectureCustSpecial(Customer customer) {
        for (Iterator<CustDataSpecial> iterator = customer.getCustDataSpecials().iterator(); iterator.hasNext();) {
            CustDataSpecial custDataSpecial = iterator.next();
            if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_ADDRESS) {
                if(!StringUtils.isEmpty(custDataSpecial.getCustData2())) {
                    Integer prefectureId = Integer.parseInt(custDataSpecial.getCustData2());
                    Prefecture prefecture = prefectureService.find(prefectureId);
                    if(prefecture != null)
                        custDataSpecial.setCustCityNameData2(prefecture.getPrefectureName());
                }
            }
        }
    }
}
