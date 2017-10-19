/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.impl;

import gnext.bean.MultipleMemberGroupRel;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.MultipleMemberGroupRelService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Stateless
public class MultipleMemberGroupRelServiceImpl extends AbstractService<MultipleMemberGroupRel> implements MultipleMemberGroupRelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleMemberGroupRelServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public MultipleMemberGroupRelServiceImpl() { super(MultipleMemberGroupRel.class); }

    @Override
    public List<MultipleMemberGroupRel> findByMemberId(Integer memberId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM MultipleMemberGroupRel c WHERE c.crmMultipleMemberGroupRelPK.memberId = :memberId ";
            Query query = em_master.createQuery(sql,MultipleMemberGroupRel.class).setParameter("memberId", memberId);
            
            List<MultipleMemberGroupRel> results = query.getResultList();
            if(results != null && !results.isEmpty()) return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
}
