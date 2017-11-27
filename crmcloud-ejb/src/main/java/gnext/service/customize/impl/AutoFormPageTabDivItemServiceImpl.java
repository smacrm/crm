package gnext.service.customize.impl;

import gnext.bean.customize.AutoFormItemGlobal;
import gnext.service.impl.*;
import gnext.bean.customize.AutoFormPageTabDivItemRel;
import gnext.bean.customize.AutoFormTab;
import gnext.service.customize.AutoFormPageTabDivItemService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;

/**
 *
 * @author hungpd
 */
@Stateless
public class AutoFormPageTabDivItemServiceImpl extends AbstractService<AutoFormPageTabDivItemRel> implements AutoFormPageTabDivItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoFormPageTabDivItemServiceImpl.class);
    private static final long serialVersionUID = -1192474566351927319L;
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public AutoFormPageTabDivItemServiceImpl() { super(AutoFormPageTabDivItemRel.class); }

    @Override
    public List<AutoFormPageTabDivItemRel> findRelList(int pageId, int pageType, int companyId) {
        if(pageId <= 0) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String query = "SELECT DISTINCT e FROM AutoFormPageTabDivItemRel e INNER JOIN e.page p INNER JOIN e.tab t WHERE p.pageId = :pageId and p.pageType = :pageType and e.autoFormPageTabDivItemRelPK.companyId = :companyId ORDER BY e.tabOrder ASC, e.divOrder ASC, e.itemOrder ASC";
            Query q = em_slave.createQuery(query, AutoFormPageTabDivItemRel.class);
            q.setParameter("pageId", pageId);
            q.setParameter("pageType", pageType);
            q.setParameter("companyId", companyId);
            q.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    /**
     * Ham xu li tra ve danh sach cac TAB cung voi cac ITEMS cho moi TAB.
     * @param companyId cong ty dang nhap.
     * @param itemLang ngon ngu nguoi dung chon.
     * @return 
     */
    @Override
    public Map<AutoFormTab, List<AutoFormItemGlobal>> getCustomizeList(int companyId, String itemLang) {
        Map<AutoFormTab, List<AutoFormItemGlobal>> m = new HashMap();
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            StringBuilder query = new StringBuilder();
            query.append(" SELECT DISTINCT ig, t FROM AutoFormPageTabDivItemRel e INNER JOIN e.tab t INNER JOIN e.item i INNER JOIN i.itemGlobalList ig ");
            query.append(" WHERE e.autoFormPageTabDivItemRelPK.companyId = :companyId ");
            query.append(" AND ig.autoFormItemGlobalPK.itemLang = :itemLang ");
            query.append(" ORDER BY e.tabOrder ASC, e.divOrder ASC, e.itemOrder ASC ");

            Query q = em_slave.createQuery(query.toString());
            q.setParameter("companyId", companyId);
            q.setParameter("itemLang", itemLang);
            List<Object[]> re = q.getResultList();
            
            if(re != null && !re.isEmpty()) {
                for(Object[] objs : re) {
                    AutoFormTab aft = (AutoFormTab)objs[1];
                    AutoFormItemGlobal afig = (AutoFormItemGlobal)objs[0];
                    
                    if(!m.containsKey(aft)) m.put(aft, new ArrayList<>());
                    m.get(aft).add(afig);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return m;
    }
    
    @Override
    public List<Integer> listDynamicItems(int companyId, int pageId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query q = em_slave.createQuery("SElECT o.item.itemId FROM AutoFormPageTabDivItemRel o WHERE o.company.companyId = :companyId AND o.page.pageId = :pageId");
            q.setParameter("companyId", companyId);
            q.setParameter("pageId", pageId);
            
            List<Integer> itemIds = q.getResultList();
            return itemIds;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
}
