package gnext.service.attachment.impl;

import gnext.bean.attachment.Attachment;
import gnext.multitenancy.TenantHolder;
import gnext.service.attachment.AttachmentService;
import gnext.service.impl.AbstractService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityTransaction;

/**
 *
 * @author daind
 */
@Stateless
public class AttachmentServiceImpl extends AbstractService<Attachment> implements AttachmentService {
    private static final long serialVersionUID = -2097651689079917004L;
    private final Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public AttachmentServiceImpl() { super(Attachment.class); }
    
    @Override
    public Attachment search(Integer companyId, Integer attchmentId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String sql = "SELECT * FROM crm_attachment WHERE attachment_id = ? AND company_id = ?";
            Query q = em_slave.createNativeQuery(sql, Attachment.class);
            q.setParameter(1, attchmentId);
            q.setParameter(2, companyId);
            List<Attachment> results = q.getResultList();
            if(results != null && !results.isEmpty()) return results.get(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<Attachment> search(Integer companyId, Integer attachmentTargetType, Integer attachmentTargetId, Short attachmentDeleted) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String sws = null;
            if(attachmentDeleted == null || attachmentDeleted == 0) sws = "(attachment_deleted = 0 OR attachment_deleted IS NULL)";
            else sws = "(attachment_deleted = 1)";
            String sql = "SELECT * FROM crm_attachment WHERE attachment_target_type = ? AND attachment_target_id = ? AND company_id = ? AND "
                    + sws + " ORDER BY created_time DESC";
            Query q = em_slave.createNativeQuery(sql, Attachment.class);
            q.setParameter(1, attachmentTargetType);
            q.setParameter(2, attachmentTargetId);
            q.setParameter(3, companyId);
            return q.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public int deleteAttachmentByEscalationId(Integer escId) {
        if (escId == null || escId <= 0) return 0;
        int rowOfUpdated = 0;
        EntityManager em_slave = null;
        EntityTransaction tx = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx = beginTransaction(em_slave);
            
            String sql = " delete from crm_attachment where attachment_target_id=? ";
            Query q = em_slave.createNativeQuery(sql);
            q.setParameter(1, escId);
            rowOfUpdated = q.executeUpdate();
            commitAndCloseTransaction(tx);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            rollbackAndCloseTransaction(tx);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return rowOfUpdated;
    }
    
    @Override
    public void deleteAttachment(int attachmentId) {
        EntityManager em_slave = null;
        EntityTransaction tx = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx = beginTransaction(em_slave);
            
            String sql = " delete from crm_attachment where attachment_id = ?";
            Query q = em_slave.createNativeQuery(sql);
            q.setParameter(1, attachmentId);
            q.executeUpdate();
            commitAndCloseTransaction(tx);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
}
