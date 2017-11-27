package gnext.service.role.impl;

import gnext.bean.role.SystemModule;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.impl.AbstractService;
import gnext.service.role.SystemModuleService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 */
@Stateless
public class SystemModuleServiceImpl extends AbstractService<SystemModule> implements SystemModuleService {
    private static final long serialVersionUID = -3108783054180122434L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemModuleServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public SystemModuleServiceImpl() { super(SystemModule.class); }
 
    @Override
    public void remove(SystemModule module) throws Exception {
        module.setModuleDeleted((short)1);
        edit(module);
    }
    
    @Override
    public List<SystemModule> findAllAvailable() {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            
            String sql = "SELECT DISTINCT c FROM SystemModule c INNER JOIN FETCH c.pages p WHERE c.moduleDeleted = 0 AND p.pageDeleted = 0 ORDER BY c.moduleId ASC";
            return em_master.createQuery(sql).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<SystemModule> findAvailableIn(List<Integer> includeList) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            
            if(includeList.isEmpty()) return new ArrayList<>();
            String sql = "SELECT DISTINCT c FROM SystemModule c INNER JOIN FETCH c.pages p WHERE c.moduleId IN :includeList AND c.moduleDeleted = 0 AND p.pageDeleted = 0 ORDER BY c.moduleId ASC";
            return em_master.createQuery(sql).setParameter("includeList", includeList).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    @Override
    public SystemModule save(SystemModule module) throws Exception {
        EntityManager em_master = null;
        EntityTransaction tx_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx_master = beginTransaction(em_master);
            
            if(module == null) return null;
            if( module.getModuleId() == null) {
                module = JPAUtils.create(module, em_master, false);
            } else {
                module = JPAUtils.edit(module, em_master, false);
            }
            
            commitAndCloseTransaction(tx_master);
        } catch (Exception e) {
            rollbackAndCloseTransaction(tx_master);
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return module;
    }
}
