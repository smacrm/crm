/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.impl;

import gnext.bean.DatabaseServer;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.DatabaseServerService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Stateless
public class DatabaseServerServiceImpl implements DatabaseServerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseServerServiceImpl.class);
    private static final long serialVersionUID = 3486464874449277583L;
    @Inject ProxyMasterEntityManager masterEntityManager;
    
    @Override
    public DatabaseServer findById(final int databaseServerId) {
        EntityManager em_master = null;
        try {
            String sql = "select * from crm_database_server where database_server_id=?";

            em_master = masterEntityManager.getEntityManager();
            Query query = em_master.createNativeQuery(sql, DatabaseServer.class);
            query.setParameter(1, databaseServerId);
            
            return (DatabaseServer) query.getSingleResult();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }
    
    @Override
    public DatabaseServer findOneDatabaseServer(Integer companyId) {
        EntityManager em_master = null;
        try {
            String sql = "select * from crm_database_server where database_server_id in ("
                    + " select dsr.database_server_id from crm_database_server_company_rel dsr "
                    + " inner join crm_company cc on cc.company_id=dsr.company_id "
                    + " where cc.company_id=? and cc.company_deleted=0 "
                    + " )";

            em_master = masterEntityManager.getEntityManager();
            Query query = em_master.createNativeQuery(sql, DatabaseServer.class);
            query.setParameter(1, companyId);
            
            List<DatabaseServer> databaseServers = query.getResultList();
            if(databaseServers != null && !databaseServers.isEmpty()) return databaseServers.get(0);
            
            return null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    @Override
    public List<DatabaseServer> findAll() {
        EntityManager em_master = null;
        try {
            String sql = "select * from crm_database_server";

            em_master = masterEntityManager.getEntityManager();
            Query query = em_master.createNativeQuery(sql, DatabaseServer.class);
            
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    @Override
    public DatabaseServer create(DatabaseServer bean) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            return JPAUtils.create(bean, em_master, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return bean;
    }

    @Override
    public DatabaseServer edit(DatabaseServer bean) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            return JPAUtils.edit(bean, em_master, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return bean;
    }
}
