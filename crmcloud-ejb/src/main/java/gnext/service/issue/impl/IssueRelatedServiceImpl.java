/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue.impl;

import gnext.bean.issue.IssueRelated;
import gnext.service.impl.AbstractService;
import gnext.service.issue.IssueRelatedService;
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
public class IssueRelatedServiceImpl extends AbstractService<IssueRelated> implements IssueRelatedService {
    private final Logger logger = LoggerFactory.getLogger(IssueRelatedServiceImpl.class);

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public IssueRelatedServiceImpl() { super(IssueRelated.class); }

    @Override
    public List<IssueRelated> getRelatedByIssueId(int issueId) {
        if(issueId <= 0) return null;
        
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query query = em_slave.createNamedQuery(
                    "IssueRelated.findRelatedByIssueId"
                    , IssueRelated.class
                    ).setParameter("issueId", issueId);
            if(query == null) return null;
            return (List<IssueRelated>) query.getResultList();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public IssueRelated getRelatedIdAndIssueId(int issueId, int relatedId) {
        if(relatedId <= 0 || issueId <= 0) return null;
        
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query query = em_slave.createNamedQuery(
                    "IssueRelated.findByRelatedIdIssueId"
                    , IssueRelated.class
                    ).setParameter("issueId", issueId
                    ).setParameter("issueRelatedId", relatedId);
            if(query == null) return null;
            return (IssueRelated) query.getSingleResult();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
}
