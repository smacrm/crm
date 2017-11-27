/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.impl;

import gnext.bean.Prefecture;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.PrefectureService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
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
public class PrefectureServiceImpl extends AbstractService<Prefecture> implements PrefectureService {
    private static final long serialVersionUID = 3830834010557884247L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PrefectureServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public PrefectureServiceImpl() { super(Prefecture.class); }
    
    @Override
    public List<Prefecture> findCities(String prefectureLocaleCode, String prefectureName) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            if(StringUtils.isBlank(prefectureLocaleCode) || StringUtils.isBlank(prefectureName)) return null;
            Query q = em_master.createQuery("SELECT NEW gnext.bean.Prefecture(p.prefectureId, p.prefectureCode, p.prefectureName) FROM Prefecture p WHERE p.prefectureLocaleCode = :prefectureLocaleCode AND p.prefectureName = :prefectureName AND p.prefectureIsDeleted = 0 ORDER BY p.prefectureOrder");
            q.setParameter("prefectureLocaleCode", prefectureLocaleCode);
            q.setParameter("prefectureName", prefectureName);

            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Prefecture> findCities(String prefectureLocaleCode) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            if(StringUtils.isBlank(prefectureLocaleCode)) return null;
            Query q = em_master.createQuery("SELECT NEW gnext.bean.Prefecture(p.prefectureId, p.prefectureCode, p.prefectureName) FROM Prefecture p WHERE p.prefectureLocaleCode = :prefectureLocaleCode AND p.prefectureIsDeleted = 0 ORDER BY p.prefectureOrder");
            q.setParameter("prefectureLocaleCode", prefectureLocaleCode);

            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    @Override
    public Prefecture findByPrefectureCode(String prefectureLocaleCode, String prefectureCode) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            Query q = em_master.createQuery("SELECT NEW gnext.bean.Prefecture(p.prefectureId, p.prefectureCode, p.prefectureName) FROM Prefecture p WHERE p.prefectureLocaleCode = :prefectureLocaleCode AND p.prefectureCode = :prefectureCode");
            q.setParameter("prefectureCode", prefectureCode);
            q.setParameter("prefectureLocaleCode", prefectureLocaleCode);
            return (Prefecture) q.getSingleResult();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    @Override
    public Prefecture findByPrefectureName(String prefectureLocaleCode, String prefectureName) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            Query q = em_master.createQuery("SELECT NEW gnext.bean.Prefecture(p.prefectureId, p.prefectureCode, p.prefectureName) FROM Prefecture p WHERE p.prefectureLocaleCode = :prefectureLocaleCode AND p.prefectureName = :prefectureName");
            q.setParameter("prefectureName", prefectureName);
            q.setParameter("prefectureLocaleCode", prefectureLocaleCode);
            
            return (Prefecture) q.getSingleResult();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    @Override
    public List<Prefecture> findAll() {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            Query q = em_master.createQuery("SELECT DISTINCT p FROM Prefecture p ORDER BY p.prefectureOrder");
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    
}
