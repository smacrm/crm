/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue.impl;

import gnext.bean.issue.EscalationSample;
import gnext.service.impl.AbstractService;
import gnext.service.issue.IssueEscalationSampleService;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;

/**
 *
 * @author daind
 */
@Stateless
public class IssueEscalationSampleServiceImpl extends AbstractService<EscalationSample> implements IssueEscalationSampleService {
    private final Logger logger = LoggerFactory.getLogger(IssueEscalationSampleServiceImpl.class);

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public IssueEscalationSampleServiceImpl() { super(EscalationSample.class); }

    @Override
    public EscalationSample getEscalationSampleById(int sampleId, int comId) {
        if(sampleId <= 0 || comId <= 0) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String sql = "SELECT c FROM EscalationSample c  WHERE c.sampleId = :sampleId  AND c.companyId = :companyId";
            Query query = em_slave.createQuery(sql, EscalationSample.class).setParameter("sampleId", sampleId).setParameter("companyId", comId);
            if(query == null) return null;
            return (EscalationSample) query.getSingleResult();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public EscalationSample getEscalationSampleByTypeIdAndTargetId(int typeId, Integer targetId, int comId, String locale) {
        if(typeId <= 0 || comId <= 0 || StringUtils.isBlank(locale)) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(comId);
            EscalationSample esc;
            if(typeId > 0 && targetId != null && targetId > 0) {
                String sql = "SELECT c FROM EscalationSample c WHERE c.sampleTypeId = :sampleTypeId AND c.sampleTargetId = :sampleTargetId AND c.sampleLang = :sampleLang AND c.companyId = :companyId";
                Query query = em_slave.createQuery(sql, EscalationSample.class).setParameter("sampleTypeId", typeId).setParameter("sampleTargetId", targetId).setParameter("sampleLang", locale).setParameter("companyId", comId).setMaxResults(1);
                if(query == null) return null;
                esc = (EscalationSample) query.getSingleResult();
            } else {
                String sql = "SELECT c FROM EscalationSample c  WHERE c.sampleTypeId = :sampleTypeId  AND c.sampleLang = :sampleLang  AND c.companyId = :companyId";
                Query query = em_slave.createQuery(sql, EscalationSample.class).setParameter("sampleTypeId", typeId).setParameter("sampleLang", locale).setParameter("companyId", comId).setMaxResults(1);
                if(query == null) return null;
                esc = (EscalationSample) query.getSingleResult();                
            }
            return esc;
        } catch(Exception e) {
            logger.info(e.getMessage());
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
}
