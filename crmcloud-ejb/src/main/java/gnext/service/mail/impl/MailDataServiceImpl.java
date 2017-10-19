package gnext.service.mail.impl;

import gnext.bean.Company;
import gnext.bean.MailAccount;
import gnext.bean.mail.MailData;
import gnext.bean.Member;
import gnext.bean.mail.MailExplode;
import gnext.bean.issue.Escalation;
import gnext.bean.issue.Issue;
import gnext.service.CompanyService;
import gnext.service.impl.AbstractService;
import gnext.service.issue.IssueService;
import gnext.service.mail.MailAccountService;
import gnext.service.mail.MailDataService;
import gnext.utils.InterfaceUtil.ISSUE_TYPE;
import gnext.utils.StringBuilderUtil;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
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
public class MailDataServiceImpl extends AbstractService<MailData> implements MailDataService {
    private static final long serialVersionUID = 6644255405051869743L;
    private final Logger LOGGER = LoggerFactory.getLogger(MailDataServiceImpl.class);

    @EJB private CompanyService companyService;
    @EJB private MailAccountService mailAccountService;
    @EJB private IssueService issueService;
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public MailDataServiceImpl() { super(MailData.class); }
    
    @Override
    public List<MailData> find(int companyId, Integer first, Integer pageSize, String sort, String where) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StringBuilder sql = new StringBuilder("SELECT * FROM crm_mail_data WHERE 1=1 AND ");
            sql.append(where).append(" ORDER BY ").append(sort);
            Query q = em_slave.createNativeQuery(sql.toString(), MailData.class);
            if (first != null && pageSize != null) {
                q.setFirstResult(first);
                q.setMaxResults(pageSize);
            }
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    @Override
    public int total(int companyId, final String where) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StringBuilder sql = new StringBuilder("SELECT count(mail_data_id) FROM crm_mail_data WHERE 1=1 AND ");
            sql.append(where);
            Query q = em_slave.createNativeQuery(sql.toString());
            Long total = (Long) q.getSingleResult();
            return total.intValue();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return 0;
    }
    
    @Override
    public MailData searchById(Integer companyId, Integer mailId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Company c = companyService.find(companyId);
            String sql = "SELECT * FROM {0} WHERE mail_data_id = ? AND company_id = ?";
            String tableName = MailData.getTableName(c);
            sql = MessageFormat.format(sql, tableName);
            Query q = em_slave.createNativeQuery(sql, MailData.class);
            q.setParameter(1, mailId);
            q.setParameter(2, companyId);
            List<MailData> results = q.getResultList();
            if(results != null && !results.isEmpty()) return results.get(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<MailData> searchByCompanyId(String folderCode, int companyId, Integer limit) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Company c = companyService.find(companyId);
            String sql = "SELECT * FROM {0} WHERE mail_data_folder_code = ? AND company_id = ?";
            String tableName = MailData.getTableName(c);
            sql = MessageFormat.format(sql, tableName);
            Query q = em_slave.createNativeQuery(sql, MailData.class);
            q.setParameter(1, folderCode);
            q.setParameter(2, companyId);
            if (limit != null) {
                q.setFirstResult(0);
                q.setMaxResults(limit);
            }
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<MailData> searchByAccountId(String folderCode, Integer accountId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            MailAccount account = mailAccountService.find(accountId);
            Company c = account.getCompany();
            String sql = "SELECT * FROM {0} WHERE mail_data_folder_code = ? AND mail_data_account_id = ? AND company_id = ?";
            String tableName = MailData.getTableName(c);
            sql = MessageFormat.format(sql, tableName);
            Query q = em_slave.createNativeQuery(sql, MailData.class);
            q.setParameter(1, folderCode);
            q.setParameter(2, accountId);
            q.setParameter(3, c.getCompanyId());
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<MailExplode> searchExplodes(final MailData md) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            if(md == null) return null;
            String sql = "SELECT me FROM MailExplode me WHERE me.mailExplodeId IN (SELECT mf.mailFilterMailExplodeId FROM MailFilter mf WHERE mf.mailFilterMoveFolderCode=:mailFilterMoveFolderCode)";
            Query q = em_slave.createQuery(sql);
            q.setParameter("mailFilterMoveFolderCode", md.getMailDataFolderCode());
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public void delete(Company c, List<Integer> mailIds) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            String mailDataTableName = MailData.getTableName(c);
            String sql = "DELETE FROM crm_attachment WHERE company_id = ? AND mail_data_id IN " + _BuildInQuery(mailIds);
            em_slave.createNativeQuery(sql) .setParameter(1, c.getCompanyId()) .executeUpdate();
            
            sql = "DELETE FROM {0} WHERE company_id = ? AND mail_data_id IN " + _BuildInQuery(mailIds);
            sql = MessageFormat.format(sql, mailDataTableName);
            em_slave.createNativeQuery(sql) .setParameter(1, c.getCompanyId()) .executeUpdate();
            
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
    public int moveToFolder(Company c, String toFolderCode, Collection<Integer> mailIds, EntityManager _em) throws Exception {
        int rowOfUpdated = 0;
        EntityTransaction tx = null;
        try {
            if(mailIds == null || mailIds.isEmpty()) return 0;
            if(_em == null){
                _em = JPAUtils.getSlaveEntityManager(tenantHolder);
                tx = beginTransaction(_em);
            }
            
            String sql = "UPDATE {0} SET mail_data_folder_code = {1} WHERE company_id = ? AND mail_data_id IN " + _BuildInQuery(mailIds);
            String mailDataTableName = MailData.getTableName(c);
            sql = MessageFormat.format(sql, mailDataTableName, toFolderCode);
            _em.createNativeQuery(sql).setParameter(1, c.getCompanyId()).executeUpdate();
            
            if(tx != null) commitAndCloseTransaction(tx);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if(tx != null) rollbackAndCloseTransaction(tx);
            throw e;
        } finally {
            if(tx != null) JPAUtils.release(_em, true);
        }
        return rowOfUpdated;
    }
    
    @Override
    public int markIsRed(Company c, List<Integer> mailIds) throws Exception {
        if(mailIds == null || mailIds.isEmpty()) return 0;
        int rowOfUpdated = 0;
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            String sql = "UPDATE {0} SET mail_data_is_read = 1 WHERE company_id = ? AND mail_data_id IN " + _BuildInQuery(mailIds);
            sql = MessageFormat.format(sql, MailData.getTableName(c));
            em_slave.createNativeQuery(sql).setParameter(1, c.getCompanyId()).executeUpdate();
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return rowOfUpdated;
    }

    private String _BuildInQuery(Collection<Integer> mailIds) {
        StringBuilder s = new StringBuilder();
        for (Integer mailId : mailIds) s.append(mailId).append(",");
        if (s.indexOf(",") > 0) s.deleteCharAt(s.lastIndexOf(","));
        return "(" + s.toString() + ")";
    }

    @Override
    public void checkoutIssue(Integer issueId, MailData mailData, Member member) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            Issue checkoutIssue = issueService.findByIssueId(issueId, null);
            mailData.setIssue(checkoutIssue);
            JPAUtils.edit(mailData, em_slave, false);
            
            Escalation escalation = new Escalation();
            escalation.setEscalationSendType(ISSUE_TYPE.EMAIL);
            escalation.setEscalationIssueId(checkoutIssue);
            escalation.setEscalationMemberId(member);
            escalation.setEscalationFromEmail(mailData.getMailDataFrom());
            escalation.setEscalationSendDateName(mailData.getMailDataTo());
            escalation.setEscalationTitle(mailData.getMailDataSubject());
            escalation.setEscalationBody(mailData.getMailDataBody());
            escalation.setEscalationSendFlag((short) 0);
            escalation.setEscalationSendDate(Calendar.getInstance().getTime());
            escalation.setEscalationRequestType((short) 0);
            escalation.setCompanyId(member.getGroup().getCompany().getCompanyId());
            escalation.setCreatorId(member);
            escalation.setEscalationIsDeleted((short) 0);
            JPAUtils.create(escalation, em_slave, false);
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public List<Issue> searchIssueRelated(final MailData mailData) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            if(mailData == null) return new ArrayList<>();
            String from = mailData.getMailDataFrom();
            if(StringUtils.isEmpty(from)) return new ArrayList<>();
            Set<String> smf = gnext.mailapi.util.MailUtil.getEmailAddress(from);
            if(smf == null || smf.isEmpty()) return new ArrayList<>();
            String sql = String.valueOf(StringBuilderUtil.getListIssueRelated(smf.iterator().next(), mailData.getMailDataId(), mailData.getCompany().getCompanyId()));
            if(sql == null) return new ArrayList<>();
            Query q = em_slave.createNativeQuery(sql);
            List<Object> ll = q.getResultList();
            if(ll == null || ll.isEmpty()) return new ArrayList<>();
            List<Issue> issues = new ArrayList<>();
            for (Object obj : ll) {
                int issueId = Integer.parseInt(String.valueOf(obj));
                issues.add(issueService.findByIssueId(issueId, null));
            }
            return issues;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public void changeMailPerson(MailData mailData, Company c) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            MailData mailDataSaved = JPAUtils.edit(mailData, em_slave, false);
            if (mailDataSaved.getMailPerson() != null && mailDataSaved.getMailPerson().getMailPersonTargetFolder() != null
                    && mailDataSaved.getMailPerson().getMailPersonIsRechangeFolder() != (short) 1) {
                String folderCode = String.valueOf(mailDataSaved.getMailPerson().getMailPersonTargetFolder());
                List<Integer> ids = new ArrayList<>(Arrays.asList(mailDataSaved.getMailDataId()));
                moveToFolder(c, folderCode, ids, em_slave);
            }
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
}
