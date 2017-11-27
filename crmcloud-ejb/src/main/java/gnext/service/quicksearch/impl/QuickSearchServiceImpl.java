package gnext.service.quicksearch.impl;

import com.mysql.jdbc.StringUtils;
import gnext.interceptors.QuickSearch;
import gnext.service.impl.AbstractService;
import gnext.service.quicksearch.QuickSearchService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;
import javax.persistence.EntityManager;

/**
 *
 * @author daind
 */
@Stateless
public class QuickSearchServiceImpl extends AbstractService<QuickSearch> implements QuickSearchService {
    private static final long serialVersionUID = -3868900552685033909L;
    private static final Logger LOGGER = LoggerFactory.getLogger(QuickSearchServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public QuickSearchServiceImpl() { super(QuickSearch.class); }

    @Override
    public QuickSearch search(String quickSearchModule, Integer quickSearchTargetId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "SELECT qs FROM QuickSearch qs WHERE qs.quickSearchModule=:quickSearchModule AND qs.quickSearchTargetId=:quickSearchTargetId";
            Query query = em_slave.createQuery(sql);
            query.setParameter("quickSearchModule", quickSearchModule);
            query.setParameter("quickSearchTargetId", quickSearchTargetId);
            List<QuickSearch> quickSearchs = query.getResultList();
            if(!quickSearchs.isEmpty()) return quickSearchs.get(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<QuickSearch> find(int first, int pageSize, String where, String fulltextseach) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "select * from crm_quick_search qs";
            sql = sql + " where 1=1 and " + where;
            
            if(!StringUtils.isNullOrEmpty(fulltextseach))
                sql = sql + " and MATCH (qs.quick_search_content) AGAINST ('"+fulltextseach+"' IN NATURAL LANGUAGE MODE)";
            Query query = em_slave.createNativeQuery(sql, QuickSearch.class);
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public int total(String where, String fulltextseach) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "select count(qs.quick_search_id) from crm_quick_search qs";
            sql = sql + " where 1=1 and " + where;
            
            if(!StringUtils.isNullOrEmpty(fulltextseach))
                sql = sql + " and MATCH (quick_search_content) AGAINST ('"+fulltextseach+"' IN NATURAL LANGUAGE MODE);";
            Query query = em_slave.createNativeQuery(sql);
            Long c = (Long) query.getSingleResult();
            
            return c.intValue();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return 0;
    }
}
