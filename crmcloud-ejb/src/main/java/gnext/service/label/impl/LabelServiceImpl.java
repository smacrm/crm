package gnext.service.label.impl;

import gnext.bean.label.PropertyItemLabel;
import gnext.caching.Payload;
import gnext.caching.PayloadFactory;
import javax.ejb.Stateless;
import gnext.service.impl.AbstractService;
import gnext.service.label.LabelService;
import gnext.utils.InterfaceUtil.FIELDS;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.service.CompanyService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import javax.ejb.EJB;
import javax.persistence.EntityManager;

/**
 *
 * @author hungpham
 */
@Stateless
public class LabelServiceImpl extends AbstractService<PropertyItemLabel> implements LabelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelServiceImpl.class);
    private static final long serialVersionUID = 6026832516856634815L;

    @EJB private CompanyService companyServiceImpl;
    
    @Inject private PayloadFactory payloadFactory;
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public LabelServiceImpl() { super(PropertyItemLabel.class); }
    
    private String getCachedKey(PropertyItemLabel item){
        Integer companyId = tenantHolder.getCompanyId();
        String code = item.getPk().getItemCode();
        String key = String.format("%d_%s", companyId, code);
        return key;
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
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            List<PropertyItemLabel> labelList = findByCompany(companyId);
            if(labelList == null || labelList.isEmpty()) return;
            
            Payload payload = payloadFactory.getInstance(companyId);
            if(payload == null) return;
            
            labelList.forEach((item) -> {
                payload.set(Payload.Alias.LABEL.name(), getCachedKey(item), item.getLabelName(), String.class);
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public PropertyItemLabel create(PropertyItemLabel t) throws Exception {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            payloadFactory.getInstance(tenantHolder.getCompanyId()).set(Payload.Alias.LABEL.name(), getCachedKey(t), t.getLabelName(), String.class);
            return JPAUtils.create(t, em_slave, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public PropertyItemLabel edit(PropertyItemLabel t) throws Exception {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            payloadFactory.getInstance(tenantHolder.getCompanyId()).set(Payload.Alias.LABEL.name(), getCachedKey(t), t.getLabelName(), String.class);
            return JPAUtils.edit(t, em_slave, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    @Override
    public void remove(PropertyItemLabel t) throws Exception {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            payloadFactory.getInstance(tenantHolder.getCompanyId()).remove(Payload.Alias.LABEL.name(), getCachedKey(t));
            JPAUtils.remove(t, em_slave, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    @Override
    public List<PropertyItemLabel> findByCompany(int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            Query q = em_slave.createQuery("SELECT o FROM PropertyItemLabel o WHERE o.pk.companyId = :companyId", PropertyItemLabel.class).setParameter("companyId", companyId);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public String findByKey(String bundleName, String key, String locale, int companyId){
        if(StringUtils.isBlank(bundleName) || StringUtils.isBlank(key) || StringUtils.isBlank(locale) || companyId <= 0) return null;
        
        try {
            String result = null; 
            if(key.contains("_") && key.startsWith(FIELDS.DYNAMIC) || key.startsWith(FIELDS.LABEL_DYNAMIC)) {
                String[] ids = key.split("_");
                if(ids == null || ids.length < 2  || !NumberUtils.isDigits(ids[ids.length -1])) return null;
                
                Integer id = Integer.valueOf(ids[ids.length -1]);
                String cachedKey = String.format("%d_%d_%s", companyId, id, locale);
                String cachedValue = payloadFactory.getInstance(companyId).get(Payload.Alias.DYNAMIC_lABEL.name(), cachedKey, String.class);
                result = StringUtils.isEmpty(cachedValue) ? StringUtils.EMPTY : cachedValue;
            } else {
                String itemCode = String.format("resource.%s.%s.%s", locale, bundleName, key);
                String cachedKey = String.format("%d_%s", companyId, itemCode);
                String cachedValue = payloadFactory.getInstance(companyId).get(Payload.Alias.LABEL.name(), cachedKey, String.class);
                result = StringUtils.isEmpty(cachedValue) ? StringUtils.EMPTY : cachedValue;
            }
            return result;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        
        return null;
    }
}
