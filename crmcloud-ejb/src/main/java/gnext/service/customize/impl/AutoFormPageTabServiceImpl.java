/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.customize.impl;

import gnext.service.impl.*;
import gnext.bean.customize.AutoFormPageTab;
import gnext.service.customize.AutoFormPageTabService;
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
import javax.persistence.EntityTransaction;

/**
 *
 * @author hungpd
 */
@Stateless
public class AutoFormPageTabServiceImpl extends AbstractService<AutoFormPageTab> implements AutoFormPageTabService {
    private static final long serialVersionUID = -627877414526719000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoFormPageTabServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public AutoFormPageTabServiceImpl() { super(AutoFormPageTab.class); }
    
    @Override
    public void cleanUnusedItems(int pageId, int companyId){
        EntityManager em_slave = null;
	EntityTransaction tx = null;
	try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx = beginTransaction(em_slave);
            
            Query q = em_slave.createNativeQuery("CALL proc_update_project_dynamic_fields(?, ?)");
            q.setParameter(1, companyId);
            q.setParameter(2, pageId);
            q.executeUpdate();
            commitAndCloseTransaction(tx);
	} catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    /**
     * {@inheritDoc }
     * @param companyId
     * @return 
     */
    @Override
    public List<AutoFormPageTab> search(int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Query q = em_slave.createNamedQuery("AutoFormPageTab.findByCompanyId");
            q.setParameter("companyId", companyId);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc }
     * @param companyId
     * @param pageName
     * @return 
     */
    @Override
    public List<AutoFormPageTab> search(Integer companyId, String pageName) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String sql = "SELECT afpt FROM AutoFormPageTab afpt WHERE afpt.company.companyId = :companyId AND afpt.pageName = :pageName";
            Query query = em_slave.createQuery(sql);
            query.setParameter("companyId", companyId);
            query.setParameter("pageName", pageName);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
}
