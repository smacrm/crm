package gnext.service.impl;

import gnext.bean.ZipCode;
import gnext.multitenancy.TenantHolder;
import gnext.service.ZipCodeService;
import gnext.utils.JPAUtils;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Stateless
public class ZipCodeServiceImpl extends AbstractService<ZipCode> implements ZipCodeService {
    private static final long serialVersionUID = 1930303831906343334L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipCodeServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public ZipCodeServiceImpl() { super(ZipCode.class); }
    
    @Override
    public ZipCode findByZipCode(String zipCode, String locale) {
        if(StringUtils.isEmpty(locale)) return new ZipCode();
        EntityManager em_slave = null;
        try {
            em_slave = getEntityManager();
            Query query = em_slave.createNamedQuery("ZipCode.findByLocaleCode", ZipCode.class).setParameter("zipCode", zipCode).setParameter("localeCode", locale);
            List<ZipCode> list = query.getResultList();
            if(list == null || list.size() <= 0) return new ZipCode();
            ZipCode zc = list.get(0);
            if("ja".equals(zc.getDistrictKanj())
                && "以下に掲載がない場合".equals(zc.getDistrictKanj())) {
                zc.setDistrictKanj(null);
            }
            return zc;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ZipCode();
    }
}
