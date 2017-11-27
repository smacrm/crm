package gnext.service.issue.impl;

import gnext.bean.issue.IssueLampGlobal;
import gnext.bean.issue.IssueLampGlobalPK;
import gnext.bean.issue.IssueLamp;
import gnext.service.impl.AbstractService;
import gnext.service.issue.IssueLampService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

/**
 *
 * @author tungdt
 */
@Stateless
public class IssueLampServiceImpl extends AbstractService<IssueLamp> implements IssueLampService {
    private static final Logger logger = LoggerFactory.getLogger(IssueLampServiceImpl.class);
    private static final long serialVersionUID = -1865650597541706590L;

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public IssueLampServiceImpl() { super(IssueLamp.class); }

    @Override
    public List<IssueLamp> findIssueLamps(Integer companyId) {
        if(companyId <= 0) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String sql = "SELECT c FROM IssueLamp c WHERE c.company.companyId = :companyId";
            Query q = em_slave.createQuery(sql);
            q.setParameter("companyId", companyId);
            q.setHint(QueryHints.READ_ONLY, HintValues.FALSE);
            return q.getResultList();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public void insert(IssueLamp issueLamp) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            IssueLamp saved = JPAUtils.create(issueLamp, em_slave, false);
            IssueLampGlobal issueLampGlobal = new IssueLampGlobal(new IssueLampGlobalPK(saved.getLampId(), saved.getLocale()));
            issueLampGlobal.setItemName(issueLamp.getItemName());
            List<IssueLampGlobal> listIssueLampGlobal = new ArrayList<>();
            listIssueLampGlobal.add(issueLampGlobal);
            saved.setIssueLampsGlobal(listIssueLampGlobal);
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public void update(IssueLamp issueLamp) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            for(IssueLampGlobal il:issueLamp.getIssueLampsGlobal()) {
                if(il == null || !il.getCrmIssueLampGlobalPK().getItemLang().equals(issueLamp.getLocale())) continue;
                il.setItemName(issueLamp.getItemName());
                break;
            }
            JPAUtils.edit(issueLamp, em_slave, false);
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public IssueLamp checkSameLampColor(IssueLamp lamp) {
        if(lamp == null) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Query q = em_slave.createNamedQuery("IssueLamp.findSameLampColor");
            q.setParameter("companyId", lamp.getCompany().getCompanyId());
            q.setParameter("lampId", lamp.getLampId()==null?0:lamp.getLampId());
            q.setParameter("lampColor", lamp.getLampColor());
            
            return (IssueLamp) q.getSingleResult();
        } catch(NoResultException e) {
            logger.info(e.getMessage());
            return null;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
}
