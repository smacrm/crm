/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role.impl;

import gnext.bean.role.RolePageMethodRel;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.impl.AbstractService;
import gnext.service.role.RolePageMethodService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 */
@Stateless
public class RolePageMethodServiceImpl extends AbstractService<RolePageMethodRel> implements RolePageMethodService {
    private static final long serialVersionUID = -7330222730867407919L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RolePageMethodServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public RolePageMethodServiceImpl() { super(RolePageMethodRel.class); }

    @Override
    public List<RolePageMethodRel> findByRoleId(Integer roleId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            
            String sql = "SELECT r FROM RolePageMethodRel r WHERE r.rolePageMethodRelPK.roleId = :roleId";
            Query q = em_master.createQuery(sql, List.class).setParameter("roleId", roleId);
            return  q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<RolePageMethodRel> findByRoleListId(List<Integer> roleListId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            
            if(roleListId.isEmpty()) return new ArrayList<>();
            String sql = "SELECT r FROM RolePageMethodRel r WHERE r.rolePageMethodRelPK.roleId IN :roleListId";
            Query q = em_master.createQuery(sql, List.class).setParameter("roleListId", roleListId);
            return  q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
}
