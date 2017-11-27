/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail.impl;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import gnext.bean.mail.MailServer;
import gnext.service.impl.AbstractService;
import gnext.service.mail.MailServerService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;

/**
 *
 * @author hungpham
 */
@Stateless
public class MailServerServiceImpl extends AbstractService<MailServer> implements MailServerService {
    private static final long serialVersionUID = -8350844236075858792L;
    private final Logger LOGGER = LoggerFactory.getLogger(MailServerServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public MailServerServiceImpl() { super(MailServer.class); }

    @Override
    public List<MailServer> search(int companyId, Boolean serverDeleted) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "select ms from MailServer ms where ms.company.companyId=:companyId";
            if(serverDeleted != null) {
                if(serverDeleted) sql = sql + " and ms.serverDeleted = 1 ";
                else sql = sql + " and (ms.serverDeleted is null or ms.serverDeleted = 0) ";
            }
            Query query = em_slave.createQuery(sql);
            query.setParameter("companyId", companyId);
            List<MailServer> results = query.getResultList();
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    private static final String Q_MAILSERVER = "select ms from MailServer ms";
    @Override
    public List<MailServer> find(int first, int pageSize, String sortField, String sortOrder, String where) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = Q_MAILSERVER + " where 1=1 and " + where;
            Query query = em_slave.createQuery(sql);
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            List<MailServer> results = query.getResultList();
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    private static final String Q_TOTAL_MAILSERVER = "SELECT count(ms.serverId) FROM MailServer ms";
    @Override
    public int total(final String where) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = Q_TOTAL_MAILSERVER + " where 1=1 and " + where;
            Query query = em_slave.createQuery(sql);
            Long total = (Long) query.getSingleResult();
            return total.intValue();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return 0;
    }

    @Override
    public MailServer search(int companyId, String serverName) {
        if(StringUtils.isBlank(serverName)) return null;
        EntityManager em_slave = null;
        try{
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String query = "SELECT ms FROM MailServer ms WHERE ms.serverName=:serverName AND ms.company.companyId=:companyId";
            List<MailServer> servers = em_slave.createQuery(query, MailServer.class).setParameter("serverName", serverName).setParameter("companyId", companyId).getResultList();
            if(servers != null && !servers.isEmpty()) return servers.get(0);
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
}
