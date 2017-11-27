package gnext.service.mente.impl;

import gnext.bean.mente.MenteItem;
import gnext.bean.mente.MenteOptionDataValue;
import gnext.caching.Payload;
import gnext.caching.PayloadFactory;
import gnext.service.impl.AbstractService;
import gnext.service.mente.MenteService;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.COMPANY_TYPE;
import java.util.Arrays;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.service.CompanyService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import javax.ejb.EJB;
import javax.persistence.EntityTransaction;

/**
 *
 * @author hungpham
 * @since Nov 7, 2016
 */
@Stateless
public class MenteServiceImpl extends AbstractService<MenteItem> implements MenteService {
    private static final long serialVersionUID = -1855794739211672598L;
    private final Logger logger = LoggerFactory.getLogger(MenteServiceImpl.class);

    final private String[] MULTIPLE_LEVEL = { COLS.PROPOSAL, COLS.PRODUCT, COLS.EXAPLE_SENTENCE };
    final private String[] CUSTOMER_LIST = { COLS.COOPERATION, COLS.SPECIAL, COLS.SEX, COLS.AGE, COLS.SESSION };
    
    @EJB private CompanyService companyServiceImpl;
    
    @Inject private PayloadFactory payloadFactory;
    
    public static enum KEYS{
      MAINTE_ALL_LEVELS_, // All levels
    };
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    @Override
    public void reloadCache(){
        companyServiceImpl.findAll().forEach((company) -> {
            if(company.getCompanyDeleted() == 0){ // Undeleted company
                Integer companyId = company.getCompanyId();
                reloadCache(companyId);
            }
        });
    }
    
    @Override
    public void reloadCache(Integer companyId){
        // Get cache instance of company id
        Payload payload = payloadFactory.getInstance(companyId);
        if(payload == null) return;
        
        // ALL Levels
        List<MenteItem> allLevels = this.getAllLevels(companyId);
        if(allLevels == null) return;
        
        // push to cache with key MAINTE_ALL_LEVELS_<company-id>
        payload.set(Payload.Alias.MAINTE.name(), String.format("%s_%d", KEYS.MAINTE_ALL_LEVELS_.name(), companyId), allLevels, List.class);
    }

    public MenteServiceImpl() { super(MenteItem.class); }

    @Override
    public MenteItem create(MenteItem t) throws Exception {
        t = super.create(t);
        
        // Push to cache
        Integer companyId = t.getCompany().getCompanyId();
        List<MenteItem> allLevels = getAllLevels(companyId);
        allLevels.add(t);
        
        Payload payload = payloadFactory.getInstance(companyId);
        payload.set(Payload.Alias.MAINTE.name(), String.format("%s_%d", KEYS.MAINTE_ALL_LEVELS_.name(), companyId), allLevels, List.class);
        
        return t;
    }

    @Override
    public MenteItem edit(MenteItem t) throws Exception {
        t = super.edit(t);
        
        // Update new data to cache
        Integer companyId = t.getCompany().getCompanyId();
        List<MenteItem> allLevels = getAllLevels(companyId);
        allLevels.remove(t);
        if ( !t.getItemDeleted() ) {
            allLevels.add(t);
        }
        
        Payload payload = payloadFactory.getInstance(companyId);
        payload.set(Payload.Alias.MAINTE.name(), String.format("%s_%d", KEYS.MAINTE_ALL_LEVELS_.name(), companyId), allLevels, List.class);
        
        return t;
    }

    @Override
    public void remove(MenteItem t) throws Exception {
        t.setItemDeleted(Boolean.TRUE);
        
        // Update new data to cache
        Integer companyId = t.getCompany().getCompanyId();
        List<MenteItem> allLevels = getAllLevels(companyId);
        allLevels.remove(t);
        
        Payload payload = payloadFactory.getInstance(companyId);
        payload.set(Payload.Alias.MAINTE.name(), String.format("%s_%d", KEYS.MAINTE_ALL_LEVELS_.name(), companyId), allLevels, List.class);
        
        super.edit(t);
    }
    
    @Override
    public List<String> getDynamicRoot(){
        return Arrays.asList(MULTIPLE_LEVEL);
    }

    @Override
    public List<MenteItem> getAllDynamicLevel(String module, int companyId) {
        return getAllDynamicLevel(module, null, companyId);
    }
    
    @Override
    public List<MenteItem> getAllDynamicLevel(String module, Integer parentId, int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String query = "SELECT m FROM MenteItem m WHERE m.itemDeleted = 0 AND m.itemParent IS NULL AND m.itemName = :name AND m.company.companyId = :companyId ORDER BY m.itemOrder ASC, m.itemId ASC";
            if(parentId != null){
                query = "SELECT m FROM MenteItem m, m.itemParent p WHERE m.itemDeleted = 0 AND p.itemDeleted = 0 AND p.itemId = :parentId AND m.itemName = :name AND m.company.companyId = :companyId ORDER BY m.itemOrder ASC, m.itemId ASC";
            }
            Query q = em_slave.createQuery(query);
            q.setParameter("companyId", companyId);
            q.setParameter("name", module);
            q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
            if(parentId != null){
                q.setParameter("parentId", parentId);
            }
            return q.getResultList();
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<String> getAllStaticLevel(short comFlag, int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String query;
            if(comFlag == COMPANY_TYPE.CUSTOMER) {
                query = "SELECT DISTINCT m.itemName FROM MenteItem m WHERE m.itemDeleted = 0 AND m.itemName IN :rejectedName and m.company.companyId = :companyId ORDER BY m.itemOrder ASC, m.itemId ASC";
            } else {
                query = "SELECT DISTINCT m.itemName FROM MenteItem m WHERE m.itemDeleted = 0 AND m.itemName NOT IN :rejectedName and m.company.companyId = :companyId ORDER BY m.itemOrder ASC, m.itemId ASC";
            }
            Query q = em_slave.createQuery(query);

            if(comFlag == COMPANY_TYPE.CUSTOMER) {
                q.setParameter("rejectedName", Arrays.asList(CUSTOMER_LIST));
            } else {
                q.setParameter("rejectedName", Arrays.asList(MULTIPLE_LEVEL));
            }
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
    public List<MenteItem> getAllStaticLevel(String parent, int companyId){
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String query = "SELECT m FROM MenteItem m WHERE m.itemDeleted = 0 AND m.itemName = :parent and m.itemName NOT IN :rejectedName and m.company.companyId = :companyId ORDER BY m.itemOrder ASC, m.itemId ASC";
            Query q = em_slave.createQuery(query);
            q.setParameter("parent",  parent);
            q.setParameter("rejectedName", Arrays.asList(MULTIPLE_LEVEL));
            q.setParameter("companyId", companyId);
            q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
            return q.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<MenteItem> getAllMenteItemStaticLevel(int itemLevel, int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String query = "SELECT m FROM MenteItem m WHERE (m.itemDeleted is null OR m.itemDeleted = 0) AND m.itemLevel=:itemLevel AND m.company.companyId = :companyId ORDER BY m.itemOrder ASC ";
            Query q = em_slave.createQuery(query);
            q.setParameter("itemLevel", itemLevel);
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
    public List<MenteOptionDataValue> getAllDataValueOfMeteItem(int menteItemId, int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String sql = "SELECT * FROM crm_mente_option_data_value WHERE item_id=? and company_id=?";
            Query q = em_slave.createNativeQuery(sql);
            q.setParameter(1, menteItemId).setParameter(2, companyId);
            return q.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<MenteItem> getRootLevels(String node, int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String query = "SELECT m FROM MenteItem m WHERE m.itemDeleted = 0 AND m.itemParent IS NULL AND m.itemName = :node and m.company.companyId = :companyId ORDER BY m.itemOrder ASC, m.itemId ASC";
            Query q = em_slave.createQuery(query);
            q.setParameter("node", node);
            q.setParameter("companyId", companyId);
            q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
            return q.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<MenteItem> findByName(String name, int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String query = "select * from crm_mente_item where item_name=? and company_id=? and (item_deleted is null or item_deleted=0) order by item_level, item_order";
            Query q = em_slave.createNativeQuery(query, MenteItem.class);
            q.setParameter(1, name);
            q.setParameter(2, companyId);
            return q.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<MenteItem> getAllLevels(int companyId) {
        List<MenteItem> cachedAllLevelList = payloadFactory.getInstance(companyId).get(Payload.Alias.MAINTE.name(), 
                String.format("%s_%d", KEYS.MAINTE_ALL_LEVELS_.name(), companyId), 
                List.class);
        if(cachedAllLevelList == null || cachedAllLevelList.isEmpty()){
            cachedAllLevelList = setReloadCachedAllLevelList(companyId);
        }
        return cachedAllLevelList;
    }

    @Override
    public int removeItemNotInList(int companyId, String parentName, List<Integer> importedIdList) {
        int rowOfUpdated = 0;
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            tx_slave = beginTransaction(em_slave);
            
            rowOfUpdated = em_slave.createQuery("UPDATE MenteItem m SET m.itemDeleted = 1 WHERE m.company.companyId = :companyId AND m.itemName = :parentName AND m.itemId NOT IN :importedList")
                .setParameter("parentName", parentName)
                .setParameter("companyId", companyId)
                .setParameter("importedList", importedIdList)
                .executeUpdate();
            
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return rowOfUpdated;
    }

    @Override
    public int removeAll(int companyId, String itemName) {
        int rowOfUpdated = 0;
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            tx_slave = beginTransaction(em_slave);
            
            rowOfUpdated = em_slave.createQuery("UPDATE MenteItem m SET m.itemDeleted = 1 WHERE m.company.companyId = :companyId AND m.itemName = :itemName")
                .setParameter("itemName", itemName)
                .setParameter("companyId", companyId)
                .executeUpdate();
            
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return rowOfUpdated;
    }

    @Override
    public List<MenteItem> setReloadCachedAllLevelList(int comId) {
        List<MenteItem> cachedAllLevelList = new ArrayList<>();
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(comId);
            String query = "SELECT m FROM MenteItem m WHERE m.itemDeleted = 0 and m.company.companyId = :companyId ORDER By m.itemLevel ASC, m.itemOrder ASC, m.itemId ASC";
            Query q = em_slave.createQuery(query);
            q.setParameter("companyId", comId);
            q.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
            cachedAllLevelList =  q.getResultList();

            //push to cache
            payloadFactory.getInstance(comId).set(Payload.Alias.MAINTE.name(), 
                String.format("%s_%d", KEYS.MAINTE_ALL_LEVELS_.name(), comId), 
                cachedAllLevelList,
                List.class
            );

            return cachedAllLevelList;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return cachedAllLevelList;
    }
}