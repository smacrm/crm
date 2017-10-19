package gnext.service.issue.impl;

import gnext.bean.issue.IssueAttachment;
import gnext.bean.softphone.Twilio;
import gnext.service.impl.AbstractService;
import gnext.service.issue.IssueAttachmentService;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;
import javax.persistence.EntityManager;

/**
 * 
 * @author hungpham
 */
@Stateless
public class IssueAttachmentServiceImpl extends AbstractService<IssueAttachment> implements IssueAttachmentService {

    private static final long serialVersionUID = -1289560750309000320L;
    private final Logger logger = LoggerFactory.getLogger(IssueAttachmentServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public IssueAttachmentServiceImpl() { super(IssueAttachment.class); }

    @Override
    public List<IssueAttachment> getAttachmetListByIssue(int issueId) {
        if(issueId <= 0) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query query = em_slave.createQuery("SELECT a FROM IssueAttachment a WHERE a.issue.issueId = :issueId AND a.attachmentDeleted = :deleted ORDER BY a.createdTime DESC");
            query.setParameter("issueId", issueId).setParameter("deleted", 0);
            return query.getResultList();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<Twilio> search(String idSearch, Integer categoryIdSearch, Date fromDateSearch, Date toDateSearch, String creatorIdSearch, String callerPhoneSearch, Integer companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            StringBuilder strQuery = new StringBuilder("SELECT a FROM Twilio a WHERE a.company.companyId = :companyId");
            
            if(!StringUtils.isEmpty(idSearch) && NumberUtils.isNumber(idSearch)) strQuery.append(" AND a.twilioId = :idSearch");
            if(fromDateSearch != null) strQuery.append(" AND a.receivedTime >= :fromDateSearch");
            if(toDateSearch != null) strQuery.append(" AND a.receivedTime <= :toDateSearch");
            if(categoryIdSearch != null) strQuery.append(" AND a.type = :categoryIdSearch");
            if(creatorIdSearch != null) strQuery.append(" AND a.agentId = :creatorIdSearch");
            if(callerPhoneSearch != null) strQuery.append(" AND a.from LIKE :callerPhoneSearch");
            strQuery.append(" ORDER BY a.receivedTime DESC");
            
            Query query = em_slave.createQuery(strQuery.toString());
            
            query.setParameter("companyId", companyId);
            if(!StringUtils.isEmpty(idSearch) && NumberUtils.isNumber(idSearch)) query.setParameter("idSearch", Integer.parseInt(idSearch.trim()));
            if(fromDateSearch != null) query.setParameter("fromDateSearch", fromDateSearch);
            if(toDateSearch != null) query.setParameter("toDateSearch", toDateSearch);
            if(categoryIdSearch != null) query.setParameter("categoryIdSearch", categoryIdSearch);
            if(creatorIdSearch != null) query.setParameter("creatorIdSearch", creatorIdSearch);
            if(callerPhoneSearch != null) query.setParameter("callerPhoneSearch", "%"+callerPhoneSearch.trim()+"%");
            
            //search phone number;
            List<Twilio> results = query.getResultList();
            return results;
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
}
