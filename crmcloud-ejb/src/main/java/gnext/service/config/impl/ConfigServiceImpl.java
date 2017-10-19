package gnext.service.config.impl;

import gnext.bean.Company;
import gnext.bean.config.Config;
import gnext.caching.Payload;
import gnext.caching.PayloadFactory;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.CompanyService;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import gnext.service.config.ConfigService;
import gnext.service.impl.AbstractService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;
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
public class ConfigServiceImpl extends AbstractService<Config> implements ConfigService {
    private static final long serialVersionUID = -2345179603534698541L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceImpl.class);
    
    @EJB private CompanyService companyServiceImpl;
    @Inject private PayloadFactory payloadFactory;
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public ConfigServiceImpl() { super(Config.class); }

    private String getCachedKey(Config item){
        String code = item.getConfigKey();
        return code;
    }
    
    @Override
    public void reloadCache() {
        List<Company> coms = companyServiceImpl.findAllCompanyIsExist();
        coms.forEach((company) -> {
            if(company.getCompanyDeleted() == 0){ // Undeleted company
                Integer companyId = company.getCompanyId();
                reloadCache(companyId);
            }
        });
    }
    
    @Override
    public void reloadCache(Integer companyId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Config c WHERE c.configDeleted = 0";
            Query q = em_master.createQuery(sql);
            List<Config> results = q.getResultList();
            if(results == null || results.isEmpty()) return;
            
            Payload payload = payloadFactory.getInstance(PayloadFactory.DEFAULT_PIPE);
            if(payload == null) return;
            
            results.forEach((item) -> {
                payload.set(Payload.Alias.CONFIG.name(), getCachedKey(item), item.getConfigValue(), String.class);
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
    }
    
    @Override
    public Config create(Config item) throws Exception {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            payloadFactory.getInstance(PayloadFactory.DEFAULT_PIPE).set(Payload.Alias.CONFIG.name(), getCachedKey(item), item.getConfigValue(), String.class);
            return JPAUtils.create(item, em_master, true); 
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    @Override
    public Config edit(Config item) throws Exception {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            if(item.getConfigDeleted() == 1){
                payloadFactory.getInstance(PayloadFactory.DEFAULT_PIPE).remove(Payload.Alias.CONFIG.name(), getCachedKey(item));
            }else{
                payloadFactory.getInstance(PayloadFactory.DEFAULT_PIPE).set(Payload.Alias.CONFIG.name(), getCachedKey(item), item.getConfigValue(), String.class);
            }
            return JPAUtils.edit(item, em_master, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    @Override
    public void remove(Config item) throws Exception {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            item.setConfigDeleted((short) 1);
            JPAUtils.edit(item, em_master, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
    }
    
    @Override
    public List<Config> search(String query) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            StringBuilder nq = new StringBuilder("SELECT o FROM Config o WHERE 1=1");
            if (!StringUtils.isEmpty(query)) nq.append(" AND ").append(query);
            nq.append(" ORDER BY o.configGroup ASC, o.configKey ASC");

            Query q = em_master.createQuery(nq.toString());
        return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    @Override
    public String get(String key) {
//        Integer companyId = tenantHolder.getCompanyId();
//        if(companyId <= 0) {
//            companyId = PayloadFactory.DEFAULT_PIPE;
//        }
        
        String cachedValue = payloadFactory.getInstance(PayloadFactory.DEFAULT_PIPE).get(Payload.Alias.CONFIG.name(), key, String.class);
        if( !StringUtils.isEmpty(cachedValue) ) return cachedValue;
        
        // Trong TH khong co du lieu, tra ve null
        return null;
    }

    @Override
    public Integer getInt(String key) {
        String value = get(key);
        return NumberUtils.toInt(value, 0);
    }
    
    @Override
    public Boolean getBoolean(String key) {
        String value = get(key);
        if( !StringUtils.isEmpty(value) && StringUtils.isNumeric(value)) return Boolean.valueOf(value.trim());
        return true;
    }
}
