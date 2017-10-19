package gnext.service.issue.impl;

import gnext.bean.issue.Customer;
import gnext.dbutils.util.StringUtil;
import gnext.service.impl.AbstractService;
import gnext.service.issue.CustomerService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;
import javax.persistence.EntityTransaction;

/**
 *
 * @author daind
 */
@Stateless
public class CustomerServiceImpl extends AbstractService<Customer> implements CustomerService {
    private static final long serialVersionUID = 7211770847804403580L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public CustomerServiceImpl() { super(Customer.class); }

    @Override
    public List<Map<String, String>> find(int calCount, Integer companyId, Integer first, Integer pageSize, String sortField, String sortOrder, String where,
            int searchType, String keywords, String operator, String lang) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StoredProcedureQuery q = getEntityManager().createStoredProcedureQuery("proc_customer_search");
            q.registerStoredProcedureParameter("p_count", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_search_type", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_keywords", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_operator", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_company_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_display_columns", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_lang", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_sort_field", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_sort_order", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_limit", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_offset", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_where", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_from_date", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_to_date", String.class, ParameterMode.IN);

            LinkedList<String> columns = new LinkedList<>();
            columns.add("cust_id");
            columns.add("cust_cooperation_name");
            columns.add("cust_code");
            columns.add("cust_special_name");
            columns.add("cust_sex_name");
            columns.add("cust_age_name");
            columns.add("cust_memo");
            columns.add("cust_name_kana");
            columns.add("cust_name_hira");
            columns.add("cust_post");
            columns.add("cust_city");
            columns.add("cust_address");
            columns.add("cust_address_kana");
            columns.add("cust_tel");
            columns.add("cust_mobile");
            columns.add("cust_mail");
            columns.add("cust_creator_name");
            columns.add("cust_created_time");
            columns.add("cust_updated_name");
            columns.add("cust_updated_time");
            columns.add("cust_history_memo");
            columns.add("cust_history_last_update");
            
            q.setParameter("p_count", calCount);
            q.setParameter("p_search_type", searchType);
            q.setParameter("p_keywords", keywords);
            q.setParameter("p_operator", operator);
            q.setParameter("p_company_id", companyId);
            q.setParameter("p_display_columns", String.join(",", columns));
            q.setParameter("p_lang", StringUtil.isEmpty(lang)?"":lang);
            q.setParameter("p_sort_field", sortField);
            q.setParameter("p_sort_order", sortOrder);
            q.setParameter("p_limit", first!=null?first:0);
            q.setParameter("p_offset", pageSize!=null?pageSize:0);
            q.setParameter("p_where", where);
            q.setParameter("p_from_date", "");
            q.setParameter("p_to_date", "");
            
            List<Object[]> objects = (List<Object[]>) q.getResultList();

            List<Map<String, String>> results = new ArrayList<>();
            for(Object[] obj : objects) {
                Map<String, String> o = new HashMap<>();

                for(int i=0; i<columns.size();i++) {
                    String col_name = columns.get(i);
                    String col_value = String.valueOf( obj[i]);
                    o.put(col_name, col_value);
                }

                results.add(o);
            }
            
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        
        return new ArrayList<>();
    }
    @Override
    public int total(int calCount, Integer companyId, Integer first, Integer pageSize, String sortField, String sortOrder, String where,
            int searchType, String keywords, String operator, String lang) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StoredProcedureQuery q = getEntityManager().createStoredProcedureQuery("proc_customer_search");
            q.registerStoredProcedureParameter("p_count", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_search_type", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_keywords", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_operator", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_company_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_display_columns", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_lang", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_sort_field", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_sort_order", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_limit", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_offset", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_where", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_from_date", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_to_date", String.class, ParameterMode.IN);
            
            LinkedList<String> columns = new LinkedList<>();
            columns.add("cust_id");
            columns.add("cust_cooperation_name");
            columns.add("cust_code");
            columns.add("cust_special_name");
            columns.add("cust_sex_name");
            columns.add("cust_age_name");
            columns.add("cust_memo");
            columns.add("cust_name_kana");
            columns.add("cust_name_hira");
            columns.add("cust_post");
            columns.add("cust_city");
            columns.add("cust_address");
            columns.add("cust_address_kana");
            columns.add("cust_tel");
            columns.add("cust_mobile");
            columns.add("cust_mail");
            columns.add("cust_creator_name");
            columns.add("cust_created_time");
            columns.add("cust_updated_name");
            columns.add("cust_updated_time");
            columns.add("cust_history_memo");
            columns.add("cust_history_last_update");
            
            q.setParameter("p_count", calCount);
            q.setParameter("p_search_type", searchType);
            q.setParameter("p_keywords", keywords);
            q.setParameter("p_operator", operator);
            q.setParameter("p_company_id", companyId);
            q.setParameter("p_display_columns", String.join(",", columns));
            q.setParameter("p_lang", StringUtil.isEmpty(lang)?"":lang);
            q.setParameter("p_sort_field", sortField);
            q.setParameter("p_sort_order", sortOrder);
            q.setParameter("p_limit", first!=null?first:0);
            q.setParameter("p_offset", pageSize!=null?pageSize:0);
            q.setParameter("p_where", where);
            q.setParameter("p_from_date", "");
            q.setParameter("p_to_date", "");
            
            List<Object[]> objects = (List<Object[]>) q.getResultList();
            Object[] obj = objects.get(0);
            
            return Integer.parseInt(String.valueOf(obj[0]));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return 10;
    }

    @Override
    public void importXls(List<Customer> customers) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx = beginTransaction(em_slave);
            
            if(customers == null || customers.isEmpty()) return;
            for(Customer c : customers) JPAUtils.edit(c, em_slave, false);
            commitAndCloseTransaction(tx);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public Customer search(String custCode, Integer companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "select cust from Customer cust where cust.company.companyId=:companyId and cust.custCode=:custCode";
            Query query = em_slave.createQuery(sql);
            query.setParameter("companyId", companyId);
            query.setParameter("custCode", custCode);
            List<Customer> results = query.getResultList();
            if(results != null || !results.isEmpty()) return results.get(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
}
