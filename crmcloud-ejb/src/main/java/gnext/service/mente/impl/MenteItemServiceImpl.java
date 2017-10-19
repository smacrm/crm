/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mente.impl;

import gnext.bean.mente.MenteItem;
import gnext.service.impl.AbstractService;
import gnext.service.mente.MenteItemService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;

/**
 *
 * @author tungdt
 */
@Stateless
public class MenteItemServiceImpl extends AbstractService<MenteItem> implements MenteItemService{
    private static final long serialVersionUID = -7702112545287251225L;
    private final Logger LOGGER = LoggerFactory.getLogger(MenteItemServiceImpl.class);

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public MenteItemServiceImpl() { super(MenteItem.class); }
    
    @Override
    public List<MenteItem> findByMenteOptionValue(String language, String itemData, String itemName,Integer companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String sql = "SELECT m_item FROM MenteItem m_item INNER JOIN m_item.langs m_option ON m_item.itemId = m_option.menteOptionDataValuePK.itemId WHERE m_item.itemName = :itemName AND m_item.itemDeleted = :itemDeleted ";
            
            Integer level = 0;
            if(itemName.startsWith("issue_proposal_id") || itemName.startsWith("issue_product_id")){
                level = NumberUtils.toInt(itemName.substring(itemName.lastIndexOf("_") + 1));
            }
            if(level > 0) sql += " AND m_item.itemLevel = :level";
            sql += " AND m_option.menteOptionDataValuePK.itemLanguage = :language AND m_option.itemData = :itemData AND m_item.company.companyId = :companyId";
            
            Query q = em_slave.createQuery(sql,MenteItem.class);
            if(level > 0){
                q.setParameter("level", level);
                itemName = itemName.substring(0, itemName.lastIndexOf("_"));
            }
            q.setParameter("itemName", itemName
            ).setParameter("itemDeleted", false
            ).setParameter("language", language
            ).setParameter("itemData", itemData
            ).setParameter("companyId", companyId);
            
            List<MenteItem> result = q.getResultList();
            if(result != null & ! result.isEmpty()) return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
}
