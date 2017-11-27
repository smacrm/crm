package gnext.service.customize.impl;

import gnext.service.impl.*;
import gnext.bean.customize.AutoFormItem;
import gnext.bean.customize.AutoFormItemGlobal;
import gnext.caching.Payload;
import gnext.caching.PayloadFactory;
import gnext.service.customize.AutoFormItemService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.service.CompanyService;
import gnext.utils.JPAUtils;
import javax.ejb.EJB;
import javax.persistence.EntityTransaction;

/**
 *
 * @author hungpd
 */
@Stateless
public class AutoFormItemServiceImpl extends AbstractService<AutoFormItem> implements AutoFormItemService {
    private static final long serialVersionUID = 6329739693266542748L;
    private final Logger logger = LoggerFactory.getLogger(AutoFormItemServiceImpl.class);

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    @EJB private CompanyService companyServiceImpl;
    @Inject private PayloadFactory payloadFactory;

    public AutoFormItemServiceImpl() { super(AutoFormItem.class); }
    
    private String getCachedKey(AutoFormItemGlobal item){
        Integer companyId = item.getAutoFormItem().getCompany().getCompanyId();
        Integer itemId = item.getAutoFormItemGlobalPK().getItemId();
        String lang = item.getAutoFormItemGlobalPK().getItemLang();
        String key = String.format("%d_%d_%s", companyId, itemId, lang);
        return key;
    }
    
    @Override
    public void reloadCache() {
        companyServiceImpl.findAll().forEach((company) -> {
            if(company.getCompanyDeleted() == 0){ // Undeleted company
                Integer companyId = company.getCompanyId();
                reloadCache(companyId);
            }
        });
    }
    
    @Override
    public void reloadCache(Integer companyId){
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            Query q = em_slave.createQuery("SELECT a FROM AutoFormItemGlobal a WHERE a.autoFormItem.company.companyId = :companyId");
            q.setParameter("companyId", companyId);
            List<AutoFormItemGlobal> dynamicResults = q.getResultList();
            Payload payload = payloadFactory.getInstance(companyId);
            dynamicResults.forEach((item) -> {
                payload.set(Payload.Alias.DYNAMIC_lABEL.name(), getCachedKey(item), item.getItemName(), String.class);
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public void persitItemGlobal(List<AutoFormItemGlobal> itemGlobal) {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            for(AutoFormItemGlobal item : itemGlobal) {
                JPAUtils.create(item, em_slave, false); 
                payloadFactory.getInstance(tenantHolder.getCompanyId()).set(Payload.Alias.DYNAMIC_lABEL.name(), getCachedKey(item), item.getItemName(), String.class);
            }
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public int removeItemGlobal(AutoFormItem item) {
        int rowOfUpdated = 0;
        EntityManager em_slave = null;
        EntityTransaction tx = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx = beginTransaction(em_slave);
            Query q = em_slave.createQuery("DELETE FROM AutoFormItemGlobal o WHERE o.autoFormItemGlobalPK.itemId = :itemId");
            q.setParameter("itemId", item.getItemId());
            rowOfUpdated = q.executeUpdate();
           
            item.getItemGlobalList().forEach((t) -> {
                payloadFactory.getInstance(tenantHolder.getCompanyId()).remove(Payload.Alias.DYNAMIC_lABEL.name(), getCachedKey(t));
            });
            
            commitAndCloseTransaction(tx);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return rowOfUpdated;
    }

    @Override
    public AutoFormItem edit(AutoFormItem item) throws Exception {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            item.getItemGlobalList().forEach((t) -> {
                payloadFactory.getInstance(tenantHolder.getCompanyId()).set(Payload.Alias.DYNAMIC_lABEL.name(), getCachedKey(t), t.getItemName(), String.class);
            });
            return JPAUtils.edit(item, em_slave, true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public void removeUnused(List<Integer> unusedItemList) {
        EntityManager em_slave = null;
        EntityTransaction tx = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx = beginTransaction(em_slave);
            Query q = em_slave.createQuery("DELETE FROM AutoFormItem WHERE itemId IN :unusedList");
            q.setParameter("unusedList", unusedItemList);
            q.executeUpdate();
            commitAndCloseTransaction(tx);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public List<AutoFormItemGlobal> search(int companyId, String lang) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String sql = "SELECT afig FROM AutoFormItemGlobal afig"
                    + " INNER JOIN afig.autoFormItem afi"
                    + " WHERE afig.autoFormItemGlobalPK.itemLang=:itemLang "
                    + " AND afig.autoFormItem.company.companyId = :companyId "
                    + " AND (afi.itemDeleted=0 OR afi.itemDeleted IS NULL)";
            Query q = em_slave.createQuery(sql, AutoFormItemGlobal.class);
            q.setParameter("itemLang", lang);
            q.setParameter("companyId", companyId);
            return q.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public SelectItem getItemGlobal(int com, int itemId, String locale) {
        if(com <=0 || itemId<=0) return null;
        SelectItem sel = null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Object[] item = (Object[]) em_slave.createNamedQuery(
                    "AutoFormItemGlobal.findAllSelectItemName"
                    ).setParameter("companyId", com
                    ).setParameter("itemId", itemId
                    ).setParameter("itemLang", locale).getSingleResult();
            if(item != null && item.length == 2) {
                sel = new SelectItem(item[0], String.valueOf(item[1]));
            }
        } catch(Exception e) {
            logger.info(e.getMessage());
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return sel;
    }
}
