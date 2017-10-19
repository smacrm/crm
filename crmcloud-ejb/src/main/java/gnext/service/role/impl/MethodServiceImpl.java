/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role.impl;

import gnext.service.role.MethodService;
import javax.ejb.Stateless;
import gnext.bean.role.Method;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.impl.AbstractService;
import gnext.utils.JPAUtils;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 */
@Stateless
public class MethodServiceImpl extends AbstractService<Method> implements MethodService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public MethodServiceImpl() { super(Method.class); }

    @Override
    public Method find(String methodName) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            
            String sql = "SELECT m FROM Method m WHERE m.methodName = :methodName";
            return em_master.createQuery(sql, Method.class).setParameter("methodName", methodName).getResultList().get(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }
}
