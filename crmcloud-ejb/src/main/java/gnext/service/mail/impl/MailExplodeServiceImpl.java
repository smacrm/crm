/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail.impl;

import gnext.bean.mail.MailExplode;
import gnext.service.impl.AbstractService;
import gnext.service.mail.MailExplodeService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;

/**
 *
 * @author daind
 */
@Stateless
public class MailExplodeServiceImpl extends AbstractService<MailExplode> implements MailExplodeService {
    private static final long serialVersionUID = 2850758935863983208L;
    private final Logger LOGGER = LoggerFactory.getLogger(MailExplodeServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public MailExplodeServiceImpl() { super(MailExplode.class); }

    @Override
    public List<MailExplode> search(Integer companyId, short mailExplodeDeleted) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "SELECT me FROM MailExplode me WHERE me.company.companyId=:companyId AND me.mailExplodeDeleted=:mailExplodeDeleted ORDER BY me.mailExplodeOrder DESC";
            Query query = em_slave.createQuery(sql);
            
            query.setParameter("companyId", companyId);
            query.setParameter("mailExplodeDeleted", mailExplodeDeleted);
            
            List<MailExplode> results = query.getResultList();
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public MailExplode search(Integer companyId, String mailExplodeTitle) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String query = "SELECT me FROM MailExplode me WHERE me.mailExplodeTitle=:mailExplodeTitle AND me.company.companyId=:companyId";
            List<MailExplode> mailExplodes = em_slave.createQuery(query).setParameter("mailExplodeTitle", mailExplodeTitle).setParameter("companyId", companyId).getResultList();
            if(mailExplodes != null && !mailExplodes.isEmpty() ) return mailExplodes.get(0);
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public Integer getMaxOrder(Integer companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "SELECT MAX(me.mailExplodeOrder) FROM MailExplode me WHERE me.company.companyId = :companyId";
            Integer maxOrder = em_slave.createQuery(sql,Integer.class).setParameter("companyId", companyId).getSingleResult();
            return maxOrder;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return 0;
    }
}
