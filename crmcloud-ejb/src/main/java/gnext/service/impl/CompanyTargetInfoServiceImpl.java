/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.impl;

import gnext.bean.CompanyTargetInfo;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.CompanyTargetInfoService;
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
public class CompanyTargetInfoServiceImpl extends AbstractService<CompanyTargetInfo> implements CompanyTargetInfoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyTargetInfoServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public CompanyTargetInfoServiceImpl() { super(CompanyTargetInfo.class); }

    @Override
    public List<CompanyTargetInfo> find(int companyTarget, int companyTargetId, int companyFlagType, short companyTargetDeleted) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String query = "SELECT c FROM CompanyTargetInfo c"
                    + " WHERE c.crmCompanyTargetInfoPK.companyTarget = :companyTarget"
                    + " AND c.crmCompanyTargetInfoPK.companyTargetId = :companyTargetId"
                    + " AND c.crmCompanyTargetInfoPK.companyFlagType = :companyFlagType"
                    + " AND c.companyTargetDeleted = :companyTargetDeleted";
            Query q = em_master.createQuery(query, CompanyTargetInfo.class)
                    .setParameter("companyTarget", companyTarget)
                    .setParameter("companyTargetId", companyTargetId)
                    .setParameter("companyFlagType", companyFlagType)
                    .setParameter("companyTargetDeleted", companyTargetDeleted);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<CompanyTargetInfo> find(int companyTarget, int companyTargetId, short companyTargetDeleted) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String query = "SELECT c FROM CompanyTargetInfo c"
                    + " WHERE c.crmCompanyTargetInfoPK.companyTarget = :companyTarget"
                    + " AND c.crmCompanyTargetInfoPK.companyTargetId = :companyTargetId"
                    + " AND c.companyTargetDeleted = :companyTargetDeleted";
            Query q = em_master.createQuery(query, CompanyTargetInfo.class)
                    .setParameter("companyTarget", companyTarget)
                    .setParameter("companyTargetId", companyTargetId)
                    .setParameter("companyTargetDeleted", companyTargetDeleted);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
}
