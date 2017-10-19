package gnext.service.impl;

import gnext.dbutils.util.Console;
import gnext.service.EntityService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @param <T>
 */
public abstract class AbstractService<T> implements EntityService<T> {
    private static final long serialVersionUID = -5509521658787348995L;

    final private Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);
    
    private final Class<T> entityClass;
    public AbstractService(Class<T> entityClass) { this.entityClass = entityClass; }
    protected Class<T> getEntityClass() { return entityClass; }
    
    protected abstract EntityManager getEntityManager();
    
    /**
     * Tìm kiếm theo primary-key.
     * EntityManager lấy từ lớp implement sẽ được release sau khi được sử dụng.
     * @param id
     * @return 
     */
    @Override public T find(int id) {
        EntityManager em = this.getEntityManager();
        try {
            return em.find(entityClass, id);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em, true);
        }
        return null;
    }
    
    /**
     * Thêm mới dữ liệu cho entity.
     * EntityManager lấy từ lớp implement sẽ được release sau khi được sử dụng.
     * @param t
     * @return 
     */
    @Override public T create(T t) throws Exception {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);
        if(constraintViolations.size() > 0) {
            Iterator<ConstraintViolation<T>> iterator = constraintViolations.iterator();
            while(iterator.hasNext()) {
                ConstraintViolation<T> cv = iterator.next();
                LOGGER.error(cv.getRootBeanClass().getName()+"."+cv.getPropertyPath() + " " +cv.getMessage());
            }
        } else {
            EntityManager em = getEntityManager();
            EntityTransaction tx = beginTransaction(em);
            try {
                em.persist(t);
                commitAndCloseTransaction(tx);
            } catch (Exception e) {
                Console.error(e);
                rollbackAndCloseTransaction(tx);
                LOGGER.error(e.getMessage(), e);
            } finally {
                JPAUtils.release(em, true);
            }
            return t;
        }
        return t;
    }

    /**
     * Cập nhạt dữ liệu cho entity.
     * EntityManager lấy từ lớp implement sẽ được release sau khi được sử dụng.
     * @param t
     * @return 
     */
    @Override public T edit(T t) throws Exception {
        EntityManager em = getEntityManager();
        EntityTransaction tx = beginTransaction(em);
        try {
            t = em.merge(t);
            commitAndCloseTransaction(tx);
        } catch (Exception e) {
            rollbackAndCloseTransaction(tx);
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em, true);
        }
        return t;
    }

    /**
     * Xóa dữ liệu với entity.
     * EntityManager lấy từ lớp implement sẽ được release sau khi được sử dụng.
     * @param t
     */
    @Override public void remove(T t) throws Exception {
        EntityManager em = getEntityManager();
        EntityTransaction tx = beginTransaction(em);
        try {
            if (!em.contains(t)) t = em.merge(t);
            em.remove(t);
            commitAndCloseTransaction(tx);
        } catch (Exception e) {
            rollbackAndCloseTransaction(tx);
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em, true);
        }
    }

    /**
     * Tìm kiếm tất cả dữ liệu.
     * @return 
     */
    @Override public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(entityClass));
            return em.createQuery(cq).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em, true);
        }
        return new ArrayList<>();
    }
    
    /**
     * Khởi tạo một transactions.
     * @param entityManager
     * @return 
     */
    @Override public EntityTransaction beginTransaction(final EntityManager entityManager) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        return tx;
    }
    
    /**
     * Commit và Close transactions.
     * @param tx 
     */
    @Override public void commitAndCloseTransaction(EntityTransaction tx) {
        if(tx == null) return;
        
        try {
            tx.commit();
            tx = null;
        } catch (Exception e) {
            // does not nothing here :(
        }
    }
    
    /**
     * Rollback transactions.
     * @param tx 
     */
    @Override public void rollbackAndCloseTransaction(EntityTransaction tx) {
        if(tx == null) return;
        
        try {
            tx.rollback();
            tx = null;
        } catch (Exception e) {
            // does not nothing here :(
        }
    }
}
