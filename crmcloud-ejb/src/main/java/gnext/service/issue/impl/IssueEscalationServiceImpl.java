/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue.impl;

import gnext.bean.attachment.Attachment;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.bean.issue.Escalation;
import gnext.service.impl.AbstractService;
import gnext.service.issue.IssueEscalationService;
import java.util.ArrayList;
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
public class IssueEscalationServiceImpl extends AbstractService<Escalation> implements IssueEscalationService {
    private final Logger logger = LoggerFactory.getLogger(IssueEscalationServiceImpl.class);

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public IssueEscalationServiceImpl() { super(Escalation.class); }

    public void deleteEscalation(Escalation esc) {
        if(esc == null || esc.getEscalationId() == null) return;
    }

    @Override
    public Escalation findEscalationByIssueId(int issueId, int userId, Short isSave, Short type) {
        if(issueId <= 0 || userId <= 0 || type <= 0) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Query q = em_slave.createNamedQuery(
                    "Escalation.findEscalationByIssueId"
                    , Escalation.class
                    ).setParameter("escalationIssueId", issueId
                    ).setParameter("escalationMemberId", userId
                    ).setParameter("escalationIsSaved", isSave
                    ).setParameter("escalationSendType", type.intValue()
                    ).setParameter("escalationIsDeleted", 0).setMaxResults(1);
            if(q != null) return (Escalation) q.getSingleResult();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<Escalation> findEscalationListByIssueId(final int issueId){
        if(issueId <= 0) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            List<Escalation> lists = new ArrayList<>();
            Query q = em_slave.createNamedQuery(
                    "Escalation.findEscalationListByIssueId"
                    , Escalation.class
                    ).setParameter("escalationIssueId", issueId
                    ).setParameter("escalationIsSaved", 1
                    ).setParameter("escalationIsDeleted", 0);
            if(q != null) {
                lists = q.getResultList();
                for(Escalation esc:lists) {
                    if(esc == null || esc.getEscalationId() == null) continue;
                    try {
                        Query attach = em_slave.createNamedQuery(
                                "Attachment.findAttachmentByEscalationId"
                                , Attachment.class
                                ).setParameter("attachmentDeleted", 0
                                ).setParameter("attachmentTargetType", AttachmentTargetType.ISSUE.getId()
                                ).setParameter("attachmentTargetId", esc.getEscalationId());
                        if(attach != null) {
                            if(esc.getAttachs() == null) esc.setAttachs(new ArrayList<>());
                            esc.setAttachs(attach.getResultList());
                        }
                    } catch(Exception e) {
                        logger.info(e.getMessage(), e);
                    }
                }
            }
            return lists;
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
}
