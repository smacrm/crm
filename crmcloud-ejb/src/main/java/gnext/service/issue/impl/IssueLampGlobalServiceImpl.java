/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue.impl;

import gnext.bean.issue.IssueLampGlobal;
import gnext.service.impl.AbstractService;
import gnext.service.issue.IssueLampGlobalService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;

/**
 *
 * @author tungdt
 */
@Stateless
public class IssueLampGlobalServiceImpl extends AbstractService<IssueLampGlobal> implements IssueLampGlobalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IssueLampGlobalServiceImpl.class);

    public IssueLampGlobalServiceImpl() { super(IssueLampGlobal.class); }

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    @Override
    public IssueLampGlobal findByPK(Integer itemId, String locale) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String sql = "SELECT ilg FROM IssueLampGlobal ilg WHERE ilg.crmIssueLampGlobalPK.itemId = :itemId AND ilg.crmIssueLampGlobalPK.itemLang = :locale";
            IssueLampGlobal ilg = em_slave.createQuery(sql, IssueLampGlobal.class)
                    .setParameter("itemId", itemId)
                    .setParameter("locale", locale)
                    .getSingleResult();
            return ilg;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public boolean checkExistIssueLampGlobal(Integer itemId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String sql = "SELECT EXISTS ( SELECT * FROM crm_issue_lamp_global ilg WHERE ilg.item_id = #itemId)";
            Long flag = (Long) em_slave.createNativeQuery(sql)
                    .setParameter("itemId", itemId)
                    .getSingleResult();
            return flag == 1;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return false;
    }

    @Override
    public List<IssueLampGlobal> findById(Integer itemId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String sql = "SELECT ilg FROM IssueLampGlobal ilg WHERE ilg.crmIssueLampGlobalPK.itemId = :itemId";
            List<IssueLampGlobal> results = em_slave.createQuery(sql, IssueLampGlobal.class)
                    .setParameter("itemId", itemId)
                    .getResultList();
            if(results != null && !results.isEmpty()) return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
}
