package gnext.service.issue.impl;

import gnext.bean.Member;
import gnext.bean.issue.IssueRelated;
import gnext.bean.issue.IssueTodo;
import gnext.service.impl.AbstractService;
import gnext.service.issue.IssueTodoService;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import javax.persistence.EntityTransaction;

/**
 *
 * @author daind
 */
@Stateless
public class IssueTodoServiceImpl extends AbstractService<IssueTodo> implements IssueTodoService {
    private final Logger logger = LoggerFactory.getLogger(IssueTodoServiceImpl.class);

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public IssueTodoServiceImpl() { super(IssueTodo.class); }

    @Override
    public List<IssueTodo> getTodoByIssueId(int issueId) {
        if(issueId <= 0) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query query = em_slave.createNamedQuery(
                    "IssueTodo.findByIssueId"
                    , IssueRelated.class
                    ).setParameter("todoIssueId", issueId
                    ).setParameter("todoDeleted", 0);
            if(query == null) return null;
            return (List<IssueTodo>) query.getResultList();
        } catch(Exception e) {
            logger.error(e.getMessage());
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public IssueTodo getTodoIdAndIssueId(int issueId, int todoId) {
        if(todoId <= 0 || issueId <= 0) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query query = em_slave.createNamedQuery(
                    "IssueTodo.findByTodoId"
                    , IssueTodo.class
                    ).setParameter("todoId", todoId
                    ).setParameter("todoDeleted", 0);
            if(query == null) return null;
            return (IssueTodo) query.getSingleResult();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public int updateStatusByTodoId(int todoId, short status) {
        if(todoId <= 0) return 0;
        int rowOfUpdated = 0;
        EntityManager em_slave = null;
        EntityTransaction tx = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx = beginTransaction(em_slave);
            
            Query q = em_slave.createNamedQuery("IssueTodo.updateStatusByTodoId");
            q.setParameter("todoStatus", status);
            q.setParameter("todoId", todoId);
            rowOfUpdated = q.executeUpdate();
            commitAndCloseTransaction(tx);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return rowOfUpdated;
    }

    @Override
    public List<IssueTodo> getTodoListByUser(Member user) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.set(Calendar.HOUR, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.AM_PM, Calendar.AM);

            Date today = c.getTime();

            c.add(Calendar.DATE, 1);
            Date tomorow = c.getTime();

            Query q = em_slave.createNamedQuery("IssueTodo.findTodoListByUserId");
            q.setParameter("companyId", user.getGroup().getCompany().getCompanyId());
            q.setParameter("memberId", user.getMemberId());
            q.setParameter("groupId", user.getGroup().getGroupId());
            q.setParameter("yesterday", today);
            q.setParameter("todayStart", today);
            q.setParameter("todayEnd", tomorow);

            return q.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
}
