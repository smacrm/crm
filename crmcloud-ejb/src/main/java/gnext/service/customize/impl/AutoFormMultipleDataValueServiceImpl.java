/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.customize.impl;

import gnext.service.impl.*;
import gnext.bean.customize.AutoFormMultipleDataValue;
import gnext.service.customize.AutoFormMultipleDataValueService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpd
 */
@Stateless
public class AutoFormMultipleDataValueServiceImpl extends AbstractService<AutoFormMultipleDataValue> implements AutoFormMultipleDataValueService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoFormMultipleDataValueServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public AutoFormMultipleDataValueServiceImpl() { super(AutoFormMultipleDataValue.class); }
    
    @Override
    public boolean isExists(AutoFormMultipleDataValue entity) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query q = em_slave.createNamedQuery("AutoFormMultipleDataValue.findByKey");
            q.setParameter("companyId", entity.getAutoFormMultipleDataValuePK().getCompanyId())
                    .setParameter("itemId", entity.getAutoFormMultipleDataValuePK().getItemId())
                    .setParameter("pageId", entity.getAutoFormMultipleDataValuePK().getPageId())
                    .setParameter("targetId", entity.getAutoFormMultipleDataValuePK().getTargetId());
            if(q.getResultList().size() == 1){
                entity = (AutoFormMultipleDataValue) q.getSingleResult();
                return true;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return false;
    }

    @Override
    public Map<Integer, String> findItemData(int pageId, int targetId, int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query q = em_slave.createNamedQuery("AutoFormMultipleDataValue.findItemData", AutoFormMultipleDataValue.class);
            q.setParameter("companyId", companyId)
                    .setParameter("pageId", pageId)
                    .setParameter("targetId", targetId);
            List<AutoFormMultipleDataValue> dataList = q.getResultList();

            Map<Integer, String> dataMap = new HashMap<>();
            for(AutoFormMultipleDataValue d : dataList){
                dataMap.put(d.getAutoFormMultipleDataValuePK().getItemId(), d.getItemData());
            }

            return dataMap;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new HashMap<>();
    }

    @Override
    public Integer removeNoDataItemExcludes(int companyId, int pageId, int pageType, int targetId, List<Integer> itemIdList) {
        if(itemIdList.isEmpty()) return 0;
        int rowOfRemoved = 0;
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            Query q = em_slave.createNamedQuery("AutoFormMultipleDataValue.deleteByExcludedKey");
            q.setParameter("companyId", companyId)
                    .setParameter("itemId", itemIdList)
                    .setParameter("pageId", pageId)
                    .setParameter("targetId", targetId);
            rowOfRemoved = q.executeUpdate();
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return rowOfRemoved;
    }
}
