/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.impl;

import gnext.bean.Company;
import gnext.bean.UnionCompanyRel;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.UnionCompanyRelService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Stateless
public class UnionCompanyRelServiceImpl extends AbstractService<UnionCompanyRel> implements UnionCompanyRelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnionCompanyRelServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public UnionCompanyRelServiceImpl() { super(UnionCompanyRel.class); }
    
    @Override
    public List<UnionCompanyRel> findByUnionKey(String companyUnionKey) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM UnionCompanyRel c WHERE c.crmUnionCompanyRelPK.companyUnionKey = :companyUnionKey";
            return em_master.createQuery(sql, UnionCompanyRel.class).setParameter("companyUnionKey", companyUnionKey).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<String> findAllUnionKey() {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT distinct ucr.crmUnionCompanyRelPK.companyUnionKey FROM UnionCompanyRel ucr";
            List<Object> ll = em_master.createQuery(sql).getResultList();

            List<String> companyUnionKeys = new ArrayList<>();
            for (Object obj : ll) companyUnionKeys.add((String) obj);

            return companyUnionKeys;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Integer> findAllCompanyGroupIds(String companyUnionKey) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM UnionCompanyRel c WHERE c.crmUnionCompanyRelPK.companyUnionKey = :companyUnionKey";
            List<UnionCompanyRel> unionCompanyRels = em_master.createQuery(sql, UnionCompanyRel.class).setParameter("companyUnionKey", companyUnionKey).getResultList();
            if(unionCompanyRels == null || unionCompanyRels.isEmpty()) return new ArrayList<>();
            
            List<Integer> companyGroupIds = new ArrayList<>();
            for(UnionCompanyRel unionCompanyRel : unionCompanyRels) {
                companyGroupIds.add(unionCompanyRel.getUnionCompanyRelPK().getCompanyId());
            }
            return companyGroupIds;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<Integer> findAllCompanyGroupIds(Integer currentCompanyId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            Company currentCompany = em_master.find(Company.class, currentCompanyId);
            String uKey = currentCompany.getCompanyUnionKey();
            if(StringUtils.isEmpty(uKey)) return new ArrayList<>();
            
            String sql = "SELECT c FROM UnionCompanyRel c WHERE c.crmUnionCompanyRelPK.companyUnionKey = :companyUnionKey";
            List<UnionCompanyRel> unionCompanyRels = em_master.createQuery(sql, UnionCompanyRel.class).setParameter("companyUnionKey", uKey).getResultList();
            if(unionCompanyRels == null || unionCompanyRels.isEmpty()) return new ArrayList<>();
            
            List<Integer> companyGroupIds = new ArrayList<>();
            for(UnionCompanyRel unionCompanyRel : unionCompanyRels) {
                companyGroupIds.add(unionCompanyRel.getUnionCompanyRelPK().getCompanyId());
            }
            return companyGroupIds;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
}
