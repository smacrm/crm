/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail.impl;

import gnext.bean.mail.MailPerson;
import gnext.service.impl.AbstractService;
import gnext.service.mail.MailPersonService;
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
import javax.persistence.EntityTransaction;

/**
 *
 * @author daind
 */
@Stateless
public class MailPersonServiceImpl extends AbstractService<MailPerson> implements MailPersonService {
    private static final long serialVersionUID = 5150215269872870018L;
    
    private final Logger LOGGER = LoggerFactory.getLogger(MailPersonServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public MailPersonServiceImpl() { super(MailPerson.class); }

    /**
     * Hàm trả về danh sách những member có thể xử lí issue.
     * @param cid company id đăng nhập.
     * @param deleted trạng thái của member.
     * @return
     */
    @Override
    public List<MailPerson> search(Integer cid, Short deleted) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "SELECT mp FROM MailPerson mp WHERE mp.company.companyId=:companyId ";
            if (deleted != null && deleted == 1) {
                sql = sql + " AND mp.mailPersonIsDeleted=1";
            } else {
                sql = sql + " AND (mp.mailPersonIsDeleted=0 OR mp.mailPersonIsDeleted IS NULL)";
            }
            sql = sql + " ORDER BY mp.mailPersonOrder DESC";
            Query query = em_slave.createQuery(sql);
            query.setParameter("companyId", cid);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public MailPerson search(Integer cid, Integer personId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "SELECT mp FROM MailPerson mp WHERE mp.company.companyId=:companyId AND mp.mailPersonId=:mailPersonId";
            Query query = em_slave.createQuery(sql);
            query.setParameter("companyId", cid);
            query.setParameter("mailPersonId", personId);
            List<MailPerson> mps = query.getResultList();
            if (mps != null && !mps.isEmpty()) return mps.get(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public void batchUpdate(List<MailPerson> memberSources, List<MailPerson> memberTarget) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            for(MailPerson mailPersonSource : memberSources) JPAUtils.edit(mailPersonSource, em_slave, false);
            for(MailPerson mailPersonTarget : memberTarget) JPAUtils.create(mailPersonTarget, em_slave, false);
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public MailPerson searchMailPersonByMemberId(Integer companyId, Integer memberId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "SELECT mp FROM MailPerson mp WHERE mp.company.companyId=:companyId AND mp.mailPersonInCharge.memberId = :memberId";
            Query query = em_slave.createQuery(sql, MailPerson.class)
                    .setParameter("companyId", companyId)
                    .setParameter("memberId", memberId);
            List<MailPerson> mps = query.getResultList();
            if (mps != null && !mps.isEmpty()) return mps.get(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
}
