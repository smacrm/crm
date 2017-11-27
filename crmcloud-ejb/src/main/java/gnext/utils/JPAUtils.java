/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.utils;

import gnext.bean.Company;
import gnext.multitenancy.ProxySlaveEntityManager;
import gnext.multitenancy.TenantHolder;
import gnext.service.impl.CompanyServiceImpl;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.jpa.metamodel.EntityTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public final class JPAUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyServiceImpl.class);
    
    public static void showDatabaseName(EntityManager em) {
        EntityType<Company> entity = em.getMetamodel().entity(Company.class);
        EntityTypeImpl entityTypeImpl = (EntityTypeImpl) entity;        
        ClassDescriptor descriptor =  entityTypeImpl.getDescriptor();
        
        String schema = descriptor.getDefaultTable().getTableQualifier();
        System.out.println("The current schema is " + schema);
    }
    
    /**
     * Xóa khỏi HEAP SPACE tất cả dữ liệu liên quan tới {@link EntityManager}.
     * @param em 
     * @param isClose 
     */
    public static void release(EntityManager em, boolean isClose) {
        if(em == null) return;
        try {
            clean(em);
            if(isClose) { em.close(); em = null; }
        } catch (Exception e) {  
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    /**
     * Xóa cache trong PERSISTEN CONTEXT.
     * @param em 
     */
    public static void clean(EntityManager em) {
        if(em == null) return;
        try {
            em.clear();
        } catch (Exception e) {  
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    /**
     * Trả về kết nối tới Slave theo tenant.Những connection trước sẽ được close và release khỏi bộ nhớ.
     * @param companyId
     * @param em_slave
     * @return 
     */
    public static EntityManager getSlaveEntityManager(int companyId, EntityManager em_slave) {
        release(em_slave, false);
        em_slave = getSlaveEntityManager(companyId);
        return em_slave;
    }
    
    /**
     * Trả về kết nối tới Slave theo tenant.
     * Những connection trước sẽ được close và release khỏi bộ nhớ.
     * @param tenantHolder
     * @param em_slave
     * @return 
     */
    public static EntityManager getSlaveEntityManager(TenantHolder tenantHolder, EntityManager em_slave) {
        int companyId = 0;
        if(tenantHolder != null) companyId = tenantHolder.getCompanyId();
        return getSlaveEntityManager(companyId, em_slave);
    }
    
    public static EntityManager getSlaveEntityManager(int companyId) {
        return new ProxySlaveEntityManager(companyId).getEntityManager();
    }
    public static EntityManager getSlaveEntityManager(TenantHolder tenantHolder) {
        int companyId = 0;
        if(tenantHolder != null) companyId = tenantHolder.getCompanyId();
        return getSlaveEntityManager(companyId);
    }
    
    /**
     * Hàm xử lí tạo entity trong một connection.
     * @param <T>
     * @param entity
     * @param em
     * @param iscreateTx xác định có mới mới 1 transaction hay không?
     * @return 
     * @throws java.lang.Exception 
     */
    public static <T> T create(T entity, EntityManager em, boolean iscreateTx) throws Exception {
        try {
            if(!iscreateTx) {
                em.persist(entity);
                em.flush();
            } else {
                boolean isJoinedToTransaction = em.isJoinedToTransaction();
                EntityTransaction transaction = null;
                try {
                    if(!isJoinedToTransaction) {
                        transaction = em.getTransaction();
                        transaction.begin();
                    }
                    
                    em.persist(entity);
                    
                    if(!isJoinedToTransaction)
                        transaction.commit();
                    else
                        em.flush();
                } catch (Exception e) {
                    if(!isJoinedToTransaction) {
                        transaction.rollback();
                        transaction = null;
                    }
                }
            }
            return entity;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Hàm xử lí chỉnh sửa entity trong một connection.
     * @param <T>
     * @param entity
     * @param em
     * @param iscreateTx xác định có mới mới 1 transaction hay không?
     * @return 
     */
    public static <T> T edit(T entity, EntityManager em, boolean iscreateTx) throws Exception {
        try {
            if(!iscreateTx) {
                entity = em.merge(entity);
                em.flush();
            } else {
                boolean isJoinedToTransaction = em.isJoinedToTransaction();
                EntityTransaction transaction = null;
                try {
                    if(!isJoinedToTransaction) {
                        transaction = em.getTransaction();
                        transaction.begin();
                    }
                    
                    entity = em.merge(entity);
                    
                    if(!isJoinedToTransaction)
                        transaction.commit();
                    else
                        em.flush();
                } catch (Exception e) {
                    if(!isJoinedToTransaction) {
                        transaction.rollback();
                        transaction = null;
                    }
                }
            }
            return entity;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Hàm xử xóa entity trong một connection.
     * @param <T>
     * @param entity
     * @param em
     * @param iscreateTx xác định có mới mới 1 transaction hay không?
     * @return 
     */
    public static <T> T remove(T entity, EntityManager em, boolean iscreateTx) throws Exception {
        try {
            if(!iscreateTx) {
                if (!em.contains(entity)) entity = em.merge(entity);
                em.remove(entity);
                em.flush();
            } else {
                boolean isJoinedToTransaction = em.isJoinedToTransaction();
                EntityTransaction transaction = null;
                try {
                    if(!isJoinedToTransaction) {
                        transaction = em.getTransaction();
                        transaction.begin();
                    }
                    
                    if (!em.contains(entity)) entity = em.merge(entity);
                    em.remove(entity);
                    
                    if(!isJoinedToTransaction)
                        transaction.commit();
                    else
                        em.flush();
                } catch (Exception e) {
                    if(!isJoinedToTransaction) {
                        transaction.rollback();
                        transaction = null;
                    }
                }
            }
            return entity;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Sử dụng khi muốn sync entity với database trong một transaction.
     * @param em 
     */
    public static void flush(EntityManager em) {
        em.flush();
    }
    
    /**
     * Trả về {@link Query} với chuỗi JQL.
     * @param em
     * @param sql
     * @return 
     */
    public static Query buildJQLQuery(EntityManager em, String sql) throws Exception {
        try {
            return em.createQuery(sql);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Execute lệnh xóa không close EntityManager.
     * @param em_slave
     * @param query
     * @return
     * @throws Exception 
     */
    public static int executeDeleteOrUpdateQuery(final EntityManager em_slave, final Query query) throws Exception {
        if(em_slave == null || query == null) return 0;
        
        int deletedCount = 0;
        
        boolean isJoinedToTransaction = em_slave.isJoinedToTransaction();
        EntityTransaction transaction = null;
        try {
            if(!isJoinedToTransaction) {
                transaction = em_slave.getTransaction();
                transaction.begin();
            }
            
            deletedCount = query.executeUpdate();
            
            if(!isJoinedToTransaction)
                transaction.commit();
            else
                em_slave.flush();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if(!isJoinedToTransaction) {
                transaction.rollback();
                transaction = null;
            }
        }
        
        return deletedCount;
    }

}
