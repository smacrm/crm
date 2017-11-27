/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail.impl;

import gnext.bean.MailAccount;
import gnext.service.impl.AbstractService;
import gnext.service.mail.MailAccountService;
import gnext.utils.InterfaceUtil;
import gnext.utils.StringBuilderUtil;
import java.util.ArrayList;
import java.util.List;
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
public class MailAccountServiceImpl extends AbstractService<MailAccount> implements MailAccountService {
    private static final long serialVersionUID = 6865386830891682025L;
    private final Logger LOGGER = LoggerFactory.getLogger(MailAccountServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public MailAccountServiceImpl() { super(MailAccount.class); }
    
    private static final String Q_MAILACCOUNT = "select ma from MailAccount ma";

    @Override
    public List<MailAccount> find(int first, int pageSize, String sortField, String sortOrder, String where) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StringBuilder sql = new StringBuilder(Q_MAILACCOUNT);
            sql.append(" where 1=1 and ").append(where);
            Query query = JPAUtils.buildJQLQuery(em_slave, sql.toString());
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            List<MailAccount> results = query.getResultList();
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    private static final String Q_TOTAL_MAILACCOUNT = "SELECT count(ma.accountId) FROM MailAccount ma";

    @Override
    public int total(String where) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StringBuilder sql = new StringBuilder(Q_TOTAL_MAILACCOUNT);
            sql.append(" where 1=1 and ").append(where);
            Query query = JPAUtils.buildJQLQuery(em_slave, sql.toString());
            Long total = (Long) query.getSingleResult();
            return total.intValue();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return 0;
    }

    @Override
    public MailAccount search(int cid, String accountName) {
        if (StringUtils.isBlank(accountName)) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(cid);
            
            String query = "SELECT mc FROM MailAccount mc WHERE mc.accountName = :accountName AND mc.company.companyId = :companyId";
            List<MailAccount> mailAccounts = em_slave.createQuery(query, MailAccount.class).setParameter("accountName", accountName).setParameter("companyId", cid).getResultList();
            if (mailAccounts != null && !mailAccounts.isEmpty()) return mailAccounts.get(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<MailAccount> search(int cid) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(cid);
            
            String query = " SELECT mc FROM MailAccount mc WHERE mc.company.companyId = :companyId AND mc.accountIsDeleted = :accountIsDeleted ";
            List<MailAccount> mailAccounts = em_slave.createQuery(query, MailAccount.class).setParameter("companyId", cid).setParameter("accountIsDeleted", InterfaceUtil.UN_DELETED).getResultList();
            return mailAccounts;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<MailAccount> getSendAccountList(int comId) {
        List<MailAccount> list = new ArrayList<>();
        String query = String.valueOf(StringBuilderUtil.getSendAccountList(comId, true));
        if (comId <= 0 || StringUtils.isBlank(query)) return list;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(comId);
            
            Query q = em_slave.createQuery(query, MailAccount.class);
            if (q != null) list = q.getResultList();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return list;
    }

    @Override
    public Boolean isMaillAddressExist(String mailAddress, Integer companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            
            String sql = "SELECT EXISTS( SELECT * FROM crm_mail_account WHERE account_mail_address = #mailAddress AND company_id = #companyId ) AS IS_EXIST";
            Query query = em_slave.createNativeQuery(sql).setParameter("mailAddress", mailAddress).setParameter("companyId", companyId);
            Long result = (Long) query.getSingleResult();
            if (result == null || result == 0) return false;
            return true;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return false;
    }
}
