/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role.impl;

import gnext.bean.customize.AutoFormPageTab;
import gnext.bean.role.Page;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.multitenancy.TenantHolder;
import gnext.service.impl.AbstractService;
import gnext.service.role.PageService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 */
@Stateless
public class PageServiceImpl extends AbstractService<Page> implements PageService {
    private static final long serialVersionUID = -1064009236517650244L;
    private final Logger LOGGER = LoggerFactory.getLogger(PageServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public PageServiceImpl() { super(Page.class); }

    @Override
    public void remove(Page page) throws Exception {
        page.setPageDeleted((short)1);
        edit(page);
    }
    
    @Override
    public Integer getPageId(String pageName) {
        if(StringUtils.isEmpty(pageName)) return 0;
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = String.format("SELECT page_id FROM crm_page WHERE page_name = '%s' LIMIT 1", pageName);
            Query q = em_master.createNativeQuery(sql);
            
            Object pageId = q.getSingleResult();
            
            if(pageId != null) {
                return NumberUtils.toInt(pageId.toString(), 0);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return 0;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> findCorrelativeTheDynamicForm(Integer companyId) {
        if(companyId == null || companyId <= 0) return new ArrayList<>();
        
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            
            String sql = String.format("SELECT page_name FROM crm_page_tab WHERE company_id = %d AND page_type = %d", companyId, AutoFormPageTab.PAGE_TYPE_CORRELATIVE_DYNAMIC_FORM);
            Query q = em_slave.createNativeQuery(sql);
            
            List<Integer> usedList = new ArrayList<>();
            List<Object> results = q.getResultList();
            results.forEach((item) -> {
                if(NumberUtils.isNumber(item.toString())){
                    usedList.add(NumberUtils.toInt(item.toString()));
                }
            });
            
            return usedList;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
}
