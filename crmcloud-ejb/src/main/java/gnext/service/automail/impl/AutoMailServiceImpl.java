package gnext.service.automail.impl;

import gnext.bean.automail.AutoMail;
import gnext.bean.automail.AutoMailMember;
import gnext.bean.automail.AutoMailSentHistory;
import gnext.bean.automail.SimpleAutoMail;
import gnext.bean.mente.MenteItem;
import gnext.multitenancy.TenantHolder;
import gnext.service.MemberService;
import gnext.service.automail.AutoMailService;
import gnext.service.impl.AbstractService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author hungpham
 */
@Stateless
public class AutoMailServiceImpl extends AbstractService<AutoMail> implements AutoMailService {
    private static final long serialVersionUID = -4367962574381186765L;
    private final Logger LOGGER = LoggerFactory.getLogger(AutoMailServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    @EJB private MemberService memberService;
    
    public AutoMailServiceImpl() { super(AutoMail.class); }

    @Override
    public AutoMail create(AutoMail t) throws Exception{
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            List<AutoMailMember> members =  new ArrayList<>(t.getAutoMailMemberList());
            t.getAutoMailMemberList().clear();
            
            AutoMail createdItem = JPAUtils.create(t, em_slave, false);
            if(!members.isEmpty()){
                for(AutoMailMember m : members){
                    m.getAutoMailMemberPK().setAutoId(createdItem.getAutoConfigId());
                }
                createdItem.setAutoMailMemberList(members);

                createdItem = JPAUtils.edit(t, em_slave, false);
            }
            
            commitAndCloseTransaction(tx_slave);
            return createdItem;
        } catch (Exception e) {
             LOGGER.error(e.getMessage(), e);
             rollbackAndCloseTransaction(tx_slave);
             throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    @Override
    public void resetItemId(Integer itemId) throws Exception {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            // Xoa danh sach schedule dua theo menteitem va option
            Query q = em_slave.createQuery("DELETE FROM AutoMail a WHERE a.itemId.itemId = :itemId");
            q.setParameter("itemId", itemId);
            
            JPAUtils.executeDeleteOrUpdateQuery(em_slave, q);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public List<AutoMail> findByStatus(MenteItem item) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query q = em_slave.createQuery("SELECT a FROM AutoMail a WHERE a.itemId.itemId = :itemId AND a.optionId = :optionId ORDER BY a.autoConfigId ASC");
            q.setParameter("itemId", item.getItemId());
            q.setParameter("optionId", item.getIssueStatusStep());
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<SimpleAutoMail> findRequiredSendWithNoIssue(Integer companySlaveId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companySlaveId);
            
            String strQuery = "SELECT auto_config_id, issue_id, option_id, member_id, type, company_id, issue_view_code FROM view_auto_mail";
            Query q = em_slave.createNativeQuery(strQuery);
            List<Object> data = q.getResultList();

            final Map<Integer, SimpleAutoMail> list = new HashMap<>();
            data.forEach((o) -> {
                Object[] item = (Object[]) o;
                Integer autoId = NumberUtils.toInt(item[0].toString());
                Integer issueId = NumberUtils.toInt(item[1].toString());
                Integer optionId = NumberUtils.toInt(item[2].toString());
                Integer memberId = NumberUtils.toInt(item[3].toString());
                String typeId = item[4].toString();
                Integer companyId = NumberUtils.toInt(item[5].toString());
                String issueViewCode = item[6].toString();

                SimpleAutoMail bean = null;
                if(!list.containsKey(issueId)){
                    bean = new SimpleAutoMail();
                    bean.setAutoId(autoId);
                    bean.setIssueId(issueId);
                    bean.setIssueViewCode(issueViewCode);
                    bean.setCompanyId(companyId);
                    bean.setOptionId(optionId);
                    list.put(issueId, bean);
                }else{
                    bean = list.get(issueId);
                }
                String email = memberService.getUserMailFristByUserId(memberId);
                if(!StringUtils.isEmpty(email)){
                    if("to".equals(typeId)){
                        bean.getToList().add(email);
                        bean.getToIntList().add(memberId);
                    }else if("cc".equals(typeId)){
                        bean.getCcList().add(email);
                        bean.getCcIntList().add(memberId);
                    }
                }
            });
            return new ArrayList<>(list.values());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<SimpleAutoMail> findRequiredSendWithIssue(Integer companySlaveId, Integer requiredIssueId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companySlaveId);
            
            String strQuery = "SELECT auto_config_id, issue_id, option_id, member_id, type, company_id, issue_view_code FROM view_auto_mail WHERE issue_id = ?";
            Query q = em_slave.createNativeQuery(strQuery);
            q.setParameter(1, requiredIssueId);
            List<Object> data = q.getResultList();

            final Map<Integer, SimpleAutoMail> list = new HashMap<>();
            data.forEach((o) -> {
                Object[] item = (Object[]) o;
                Integer autoId = NumberUtils.toInt(item[0].toString());
                Integer issueId = NumberUtils.toInt(item[1].toString());
                Integer optionId = NumberUtils.toInt(item[2].toString());
                Integer memberId = NumberUtils.toInt(item[3].toString());
                String typeId = item[4].toString();
                Integer companyId = NumberUtils.toInt(item[5].toString());
                String issueViewCode = item[6].toString();

                SimpleAutoMail bean = null;
                if(!list.containsKey(issueId)){
                    bean = new SimpleAutoMail();
                    bean.setAutoId(autoId);
                    bean.setIssueId(issueId);
                    bean.setIssueViewCode(issueViewCode);
                    bean.setCompanyId(companyId);
                    bean.setOptionId(optionId);
                    list.put(issueId, bean);
                }else{
                    bean = list.get(issueId);
                }
                String email = memberService.getUserMailFristByUserId(memberId);
                if(!StringUtils.isEmpty(email)){
                    if("to".equals(typeId)){
                        bean.getToList().add(email);
                        bean.getToIntList().add(memberId);
                    }else if("cc".equals(typeId)){
                        bean.getCcList().add(email);
                        bean.getCcIntList().add(memberId);
                    }
                }
            });
            return new ArrayList<>(list.values());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public void pushHistory(Integer autoId, int issueId, Integer companyId, String hitoryData) throws Exception {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            
            AutoMailSentHistory history = new AutoMailSentHistory(autoId, issueId, new Date());
            history.setCompanyId(companyId);
            history.setHistoryData(hitoryData);
            
            JPAUtils.create(history, em_slave, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

}
