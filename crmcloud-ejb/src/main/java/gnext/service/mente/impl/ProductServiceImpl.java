package gnext.service.mente.impl;

import gnext.bean.mente.Products;
import gnext.multitenancy.TenantHolder;
import gnext.service.impl.AbstractService;
import gnext.service.mente.ProductService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Jul 19, 2017
 */
@Stateless
public class ProductServiceImpl extends AbstractService<Products> implements ProductService {
    private static final long serialVersionUID = -1855794739211672598L;
    private final Logger logger = LoggerFactory.getLogger(MenteServiceImpl.class);

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public ProductServiceImpl() { super(Products.class); }
    
    @Override
    public int removeAllProductsExcept(int companyId, List<String> importedCodeList) {
        int rowOfUpdated = 0;
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            rowOfUpdated = em_slave.createQuery("UPDATE Products p SET p.productsIsDeleted = 1 WHERE p.companyId = :companyId AND p.productsCode NOT IN :importedList")
                    .setParameter("importedList", importedCodeList)
                    .setParameter("companyId", companyId)
                    .executeUpdate();
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return rowOfUpdated;
    }

    @Override
    public int removeProduct(int companyId, int productId) {
        int rowOfUpdated = 0;
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            rowOfUpdated = em_slave.createQuery("UPDATE Products p SET p.productsIsDeleted = 1 WHERE  p.companyId = :companyId AND p.productsId = :productId")
                    .setParameter("companyId", companyId)
                    .setParameter("productId", productId)
                    .executeUpdate();
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return rowOfUpdated;
    }

    @Override
    public List<Products> getAllProducts(int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            return em_slave.createQuery("SELECT p FROM Products p WHERE p.companyId = :companyId AND p.productsIsDeleted = 0")
                    .setParameter("companyId", companyId)
                    .getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Products> getAllProducts(int companyId, int smallProductId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            return em_slave.createQuery("SELECT p FROM Products p WHERE p.companyId = :companyId AND p.productsCategorySmallId = :smallProductId AND p.productsIsDeleted = 0")
                    .setParameter("companyId", companyId)
                    .setParameter("smallProductId", smallProductId)
                    .getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public Products getProductByCode(int companyId, String productsCode) {
        return getProductByCode(companyId, productsCode, Boolean.FALSE);
    }

    @Override
    public Products getProductByCode(int companyId, String productsCode, Boolean isSearchAll) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String strQuery = "SELECT p FROM Products p WHERE p.companyId = :companyId AND p.productsCode = :code";
            if (!isSearchAll) {
                strQuery += " AND p.productsIsDeleted = 0";
            }
            Object o = em_slave.createQuery(strQuery)
                    .setParameter("companyId", companyId)
                    .setParameter("code", productsCode)
                    .getSingleResult();
            return (Products) o;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public Products saveProduct(Products bean) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            if (bean.getProductsId() == null) {
                JPAUtils.create(bean, em_slave, false);
            } else {
                JPAUtils.edit(bean, em_slave, false);
            }
            commitAndCloseTransaction(tx_slave);
            return bean;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
}
