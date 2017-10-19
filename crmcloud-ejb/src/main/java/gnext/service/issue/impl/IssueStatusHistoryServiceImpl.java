package gnext.service.issue.impl;

import gnext.bean.Member;
import gnext.bean.issue.Issue;
import gnext.bean.issue.IssueStatusHistory;
import gnext.multitenancy.TenantHolder;
import gnext.service.impl.AbstractService;
import gnext.service.issue.IssueStatusHistoryService;
import gnext.utils.JPAUtils;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 */
@Stateless
public class IssueStatusHistoryServiceImpl extends AbstractService<IssueStatusHistory> implements IssueStatusHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(IssueStatusHistoryServiceImpl.class);
    private static final long serialVersionUID = -1865650597541706590L;

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public IssueStatusHistoryServiceImpl() { super(IssueStatusHistory.class); }

    @Override
    public void push(Issue issue, Integer fromStatusId, Integer toStatusId, Member creator) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            IssueStatusHistory history = new IssueStatusHistory();
            history.setIssueId(issue);
            history.setFromStatusId(fromStatusId);
            history.setToStatusId(toStatusId);
            history.setCreatorId(creator);
            issue.getStatusHistory().add(JPAUtils.create(history, em_slave, false));
            
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

}
