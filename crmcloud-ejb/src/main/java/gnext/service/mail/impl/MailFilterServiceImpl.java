/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail.impl;

import gnext.bean.mail.MailFilter;
import gnext.service.impl.AbstractService;
import gnext.service.mail.MailFilterService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;

/**
 *
 * @author daind
 */
@Stateless
public class MailFilterServiceImpl extends AbstractService<MailFilter> implements MailFilterService {
    private static final long serialVersionUID = 722659097226933915L;
    private final Logger LOGGER = LoggerFactory.getLogger(MailFilterServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public MailFilterServiceImpl() { super(MailFilter.class); }

    @Override
    public List<MailFilter> search(Integer companyId, Short mailFilterDeleted) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StringBuilder sql = new StringBuilder("SELECT mf FROM MailFilter mf WHERE mf.company.companyId=:companyId ");
            if (mailFilterDeleted != null && mailFilterDeleted == 1) {
                sql.append(" AND mf.mailFilterDeleted=1");
            } else {
                sql.append(" AND (mf.mailFilterDeleted=0 OR mf.mailFilterDeleted IS NULL)");
            }
            sql.append(" ORDER BY mf.mailFilterOrder DESC");
            
            Query query = em_slave.createQuery(sql.toString());
            query.setParameter("companyId", companyId);
            List<MailFilter> results = query.getResultList();
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public MailFilter search(Integer companyId, String mailFilterTitle) {
        EntityManager em_slave = null;
        try{
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String query = "SELECT c FROM MailFilter c WHERE c.mailFilterTitle=:mailFilterTitle AND c.company.companyId=:companyId";
            List<MailFilter> mailFilters = em_slave.createQuery(query).setParameter("mailFilterTitle", mailFilterTitle).setParameter("companyId", companyId).getResultList();
            
            if(mailFilters != null && !mailFilters.isEmpty() ) return mailFilters.get(0);
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public Integer getMaxOrder(Integer companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "SELECT MAX(mf.mailFilterOrder) FROM MailFilter mf WHERE mf.company.companyId = :companyId";
            
            Integer maxOrder = em_slave.createQuery(sql,Integer.class).setParameter("companyId", companyId).getSingleResult();
            return maxOrder;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return 0;
    }
}
