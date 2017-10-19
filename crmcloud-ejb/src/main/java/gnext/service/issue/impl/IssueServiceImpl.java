package gnext.service.issue.impl;

import gnext.bean.mail.MailData;
import gnext.interceptors.QuickSearchDbInterceptor;
import gnext.interceptors.annotation.QuickSearchAction;
import gnext.bean.issue.Customer;
import gnext.bean.issue.Issue;
import gnext.bean.issue.IssueInfo;
import gnext.bean.issue.IssueLamp;
import gnext.bean.mente.MenteItem;
import gnext.service.config.ConfigService;
import gnext.service.impl.AbstractService;
import gnext.service.issue.CustomerService;
import gnext.service.issue.IssueService;
import gnext.service.project.ProjectService;
import gnext.utils.InterfaceUtil;
import gnext.utils.InterfaceUtil.SERVER_KEY;
import gnext.utils.StringBuilderUtil;
import gnext.utils.StringUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.sessions.CopyGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.service.role.PageService;
import gnext.utils.JPAUtils;
import java.util.Date;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

/**
 *
 * @author daind
 */
@Interceptors ({QuickSearchDbInterceptor.class})
@Stateless
public class IssueServiceImpl extends AbstractService<Issue> implements IssueService {

    private static final long serialVersionUID = -7614383657175588601L;
    
    private final Logger logger = LoggerFactory.getLogger(IssueServiceImpl.class);

    @EJB private ConfigService configServiceImpl;
    @EJB private ProjectService projectServiceImpl;
    @EJB private PageService pageServiceImpl;
    @EJB CustomerService customerService;
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }

    public IssueServiceImpl() { super(Issue.class); }

    @QuickSearchAction(action = QuickSearchAction.UPDATE)
    @Override
    public Issue edit(Issue t) throws Exception {
        return super.edit(t);
    }
    
    /**
     * Hàm xử lí trong transaction.
     * <b>CHÚ Ý</b>: Vì có gọi tới hàm getEntityManager() - em sẽ được clear trước khi trả về.
     * Do vậy, không được sử dụng lại hàm này trong khối transaction cần sử dụng
     * biên em để tránh lỗi Detach trong JPA.
     * @param issue
     * @param availableColumns
     * @param availableLocales
     * @return
     * @throws Exception 
     */
    @QuickSearchAction(action = QuickSearchAction.CREATE)
    @Override
    public Issue createIssue(Issue issue, List<String> availableColumns, List<Locale> availableLocales)
            throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            if(issue.getDuplicateId() != null){
                issue.setIssueId(issue.getDuplicateId());
                em_slave.detach(issue);
                issue.setDuplicateId(null);
                CopyGroup group = new CopyGroup();
                group.setShouldResetPrimaryKey( true );
                issue = (Issue)em_slave.unwrap( JpaEntityManager.class ).copy( issue, group );
            }
            List<Customer> customers = new ArrayList<>();
            customers.addAll(issue.getCustomerList());
            
            issue.getCustomerList().clear();
            
            List<IssueInfo> issueInfo = new ArrayList<>();
            issueInfo.addAll(issue.getIssueInfoList());
            
            issue.getIssueInfoList().clear();
            
            MenteItem issueReceiveId = issue.getIssueReceiveId();
            MenteItem issueStatusId = issue.getIssueStatusId();
            MenteItem issuePublicId = issue.getIssuePublicId();
            
//            TODO
//            List<MenteItem> menteItem = new ArrayList<>(issue.getMenteItem());
//            List<Products> products = new ArrayList<>(issue.getProductList());
            
            issue.setIssueReceiveId(null);
            issue.setIssueStatusId(null);
            issue.setIssuePublicId(null);
            
//            TODO
//            issue.getMenteItem().clear();
//            issue.getProductList().clear();
            
            JPAUtils.create(issue, em_slave, false);
            
            // !! Fix bug issue cannot create with exists customer
            issue.getCustomerList().addAll(customers);
            for (Iterator<Customer> iterator = issue.getCustomerList().iterator(); iterator.hasNext();) {
                Customer o = iterator.next();
                if(!o.isValid()){
                    iterator.remove();
                } else{
                    o.removeInvalidData(); //Check valid target info then remove if not valid
                }
            }
            
            issue.getIssueInfoList().addAll(issueInfo);
            for (Iterator<IssueInfo> iterator = issue.getIssueInfoList().iterator(); iterator.hasNext();) {
                IssueInfo o = iterator.next();
                if(!o.isValid()){
                    iterator.remove();
                }else{
                    o.setIssueId(issue);
                    o.setCreatedTime(new Date());
                    o.setCreatorId(issue.getCreatorId().getMemberId());
                }
            }
            
            issue.setIssueReceiveId(issueReceiveId);
            issue.setIssueStatusId(issueStatusId);
            issue.setIssuePublicId(issuePublicId);
            
//            TODO
//            issue.getMenteItem().addAll(menteItem);
//            issue.getProductList().addAll(products);
            
            JPAUtils.edit(issue, em_slave, false);

            createCallback(issue, availableColumns, availableLocales, em_slave);
            
            commitAndCloseTransaction(tx_slave);
            return issue;
        } catch (Exception e) {
             logger.error(e.getMessage(), e);
             rollbackAndCloseTransaction(tx_slave);
             throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    private void createCallback(Issue issue, List<String> availableColumns, List<Locale> availableLocales, EntityManager em_slave)
            throws Exception {
        // set issue_id cho mail_data
        MailData mailData = issue.getMailData();
        if(mailData != null) {
            mailData.setIssue(issue);
            JPAUtils.edit(mailData, em_slave, false);
        }

        /** 受付情報表示コードを更新 */
        if(StringUtils.isEmpty(issue.getIssueViewCode())){
            Object items = em_slave.createNativeQuery(
                    " call proc_issue_view_code(?,?,?) "
                    ).setParameter(1, issue.getIssueId()
                    ).setParameter(2, issue.getIssueCode()
                    ).setParameter(3, issue.getCompany().getCompanyId()).getSingleResult();
            if(items == null) throw new Exception("Can not retrieve the issue-code!");
            issue.setIssueViewCode(items.toString());
        }

        /** Elastic SearchへJSONオブジェクト送信 */
        if(this.configServiceImpl.get(SERVER_KEY.ELASTIC).equalsIgnoreCase("true")) {
            String rowId = this.projectServiceImpl.persitIssueToElastic(issue, availableColumns, availableLocales);
            /** Responeから「_id」を更新 */
            issue.setIssueRowId(rowId);
        }
    }

    /**
     * Hàm xử lí trong transaction.
     * <b>CHÚ Ý</b>: Vì có gọi tới hàm getEntityManager() - em sẽ được clear trước khi trả về.
     * Do vậy, không được sử dụng lại hàm này trong khối transaction cần sử dụng
     * biên em để tránh lỗi Detach trong JPA.
     * @param issue
     * @param availableColumns
     * @param availableLocales
     * @return
     * @throws Exception 
     */
    @QuickSearchAction(action = QuickSearchAction.UPDATE)
    @Override
    public Issue editIssue(Issue issue, List<String> availableColumns, List<Locale> availableLocales, String locale) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            JPAUtils.edit(issue, em_slave, false);
            
            List<Map<String, String>> lamps = this.projectServiceImpl.findIssueByIdAndLocale(
                    issue.getIssueId()
                    ,issue.getCompany().getCompanyId()
                    ,issue.getCreatorId().getMemberId()
                    ,StringUtil.getLampColors()
                    ,locale);
                if(!lamps.isEmpty()){
                    String[] vals = lamps.get(0).get("lamp_color").split("_");
                    if(vals != null) {
                        issue.setLampColor(vals[0]);
                        if(vals.length == 2) issue.setLampTextColor(vals[1]);
                    }
                }
            
            /** 受付情報表示コードを更新 */
            if(issue.getIssueViewCode() == null || StringUtils.isBlank(issue.getIssueViewCode())) {
                Object items = em_slave.createNativeQuery(
                        " call proc_issue_view_code(?,?,?) "
                        ).setParameter(1, issue.getIssueId()
                        ).setParameter(2, issue.getIssueCode()
                        ).setParameter(3, issue.getCompany().getCompanyId()).getSingleResult();
                if(items == null) throw new Exception("Can not retrieve the issue-code!");
                issue.setIssueViewCode(items.toString());
            }
            
            if(issue.getDelCustomerList() != null && issue.getDelCustomerList().size() > 0) {
                StringBuilder del = StringBuilderUtil.deleteCustRelByCustId(issue.getDelCustomerList());
                em_slave.createNativeQuery(del.toString()).executeUpdate();
            }
            if("true".equals(this.configServiceImpl.get(SERVER_KEY.ELASTIC))) {
                /** Elastic SearchへJSONオブジェクト送信 */
                String rowId = this.projectServiceImpl.persitIssueToElastic(issue, availableColumns, availableLocales);
                /** Responeから「_id」を更新 */
                issue.setIssueRowId(rowId);
            }
            commitAndCloseTransaction(tx_slave);
            return issue;
        } catch(Exception e) {
            logger.error(e.getMessage());
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @QuickSearchAction(action = QuickSearchAction.DELETE)
    @Override
    @SuppressWarnings("InfiniteRecursion")
    public void remove(Issue issue) throws Exception {
        issue.setIssueDeleted(InterfaceUtil.DELETED);
        super.edit(issue);
    }

    @Override
    public Issue findByIssueId(int issueId, String locale) {
        if(issueId <= 0) return null;
        
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            Query query = em_slave.createNamedQuery(
                    "Issue.findByIssueId"
                    , Issue.class
                    ).setParameter("issueId", issueId
                    ).setParameter("issueDeleted", 0);
            query.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
            Issue issue = (Issue) query.getSingleResult();
            if(issue != null && !StringUtils.isEmpty(locale)) {
                List<Map<String, String>> lamps = this.projectServiceImpl.findIssueByIdAndLocale(
                    issue.getIssueId()
                    ,issue.getCompany().getCompanyId()
                    ,issue.getCreatorId().getMemberId()
                    ,StringUtil.getLampColors()
                    ,locale);
                if(!lamps.isEmpty()){
                    String[] vals = lamps.get(0).get("lamp_color").split("_");
                    if(vals != null) {
                        issue.setLampColor(vals[0]);
                        if(vals.length == 2) issue.setLampTextColor(vals[1]);
                    }
                }
            }
            return issue;
        } catch(NoResultException e) {
            logger.info(e.getMessage());
            return null;
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
    
    @Override
    public List<Issue> findByIssueIdList(final List<Integer> issueIdList){
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            if(issueIdList.isEmpty()) return new ArrayList<>();
            Query query;
            query = em_slave.createNamedQuery(
                    "Issue.findByIssueIdList"
                    , Issue.class
                    ).setParameter("issueIdList", issueIdList
                    ).setParameter("issueDeleted", 0);
            query.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
            return query.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public Issue findByIssueViewCode(int companyId, String issueViewCode) {
        if(companyId <= 0 || StringUtils.isBlank(issueViewCode)) return null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            Query query = em_slave.createNamedQuery(
                    "Issue.findByIssueViewCode"
                    , Issue.class
                    ).setParameter("companyId", companyId
                    ).setParameter("issueViewCode", issueViewCode
                    ).setParameter("issueDeleted", 0);
            query.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
            return (Issue)query.getSingleResult();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<SelectItem> getList(String field, int companyId, String language) {
        if(StringUtils.isBlank(field) || companyId <= 0 || StringUtils.isBlank(language)) return null;
        StringBuilder sql = StringBuilderUtil.selectDataByField(field, companyId, language);
        if(sql == null || sql.length() <= 0) return null;
        
        @SuppressWarnings({"LocalVariableHidesMemberVariable", "UnusedAssignment"})
        List<SelectItem> arrayItems = null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            List<Object[]> items = em_slave.createNativeQuery(sql.toString()).getResultList();
            if(items == null || items.isEmpty()) return null;
            arrayItems = new ArrayList<>();
            for (Object[] item: items) {
                if(item == null) continue;
                SelectItem sel = new SelectItem(item[1], StringUtil.nullStringToEmpty(String.valueOf(item[0])));
                arrayItems.add(sel);
            }
            return arrayItems;
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<SelectItem> getListByLevel(int parentId, int companyId, String language, int level, boolean product) {
        if((parentId <= 0 && level > 1) || companyId <= 0 || StringUtils.isBlank(language) || level <= 0) return null;
        
        StringBuilder sql = StringBuilderUtil.selectDataByParentId(companyId, language, level, parentId, product);
        if(sql == null || sql.length() <= 0) return null;
        
        @SuppressWarnings({"LocalVariableHidesMemberVariable", "UnusedAssignment"})
        List<SelectItem> arrayItems = null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            List<Object[]> items = em_slave.createNativeQuery(sql.toString()).getResultList();
            if(items == null || items.isEmpty()) return null;
            arrayItems = new ArrayList<>();
            for (Object[] item: items) {
                if(item == null) continue;
                SelectItem sel = new SelectItem(item[1], StringUtil.nullStringToEmpty(String.valueOf(item[0])));
                arrayItems.add(sel);
            }
            return arrayItems;
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<SelectItem> getProductByName(String name, int companyId, String language, int level, Integer flag, String inKey) {
        if(level <= 0 || companyId <= 0 || StringUtils.isBlank(language) || StringUtils.isBlank(inKey)) return null;
        
        StringBuilder sql = StringBuilderUtil.selectProductByName(name, companyId, language, level, flag, inKey);
        if(sql == null || sql.length() <= 0) return null;
        
        @SuppressWarnings({"LocalVariableHidesMemberVariable", "UnusedAssignment"})
        List<SelectItem> arrayItems = null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            List<Object[]> items = em_slave.createNativeQuery(sql.toString()).getResultList();
            if(items == null || items.isEmpty()) return null;
            arrayItems = new ArrayList<>();
            int idx = 0;
            for (Object[] item: items) {
                if(item == null) continue;
                SelectItem sel = new SelectItem(item[1], StringUtil.nullStringToEmpty(String.valueOf(item[0])));
                if(item.length >= idx) {
                    sel.setDescription("{\"label\":\"" + item[3] + "\", \"value\":\"" + item[2] + "\"}");
                }
                arrayItems.add(sel);
                idx++;
            }
            return arrayItems;
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public String getMaxLevelField(String field, int companyId, boolean product) {
        if(StringUtils.isBlank(field) || companyId <= 0) return null;
        
        StringBuilder sql = StringBuilderUtil.selectMaxLevelField(field, companyId, product);
        if(sql == null || sql.length() <= 0) return null;
        
        @SuppressWarnings({"LocalVariableHidesMemberVariable", "UnusedAssignment"})
        String item = null;
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            Object items = em_slave.createNativeQuery(sql.toString()).getSingleResult();
            if(items == null) return null;
            item = items.toString();
            return item;
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public Integer getCustomizePageId(String pageName, int companyId) {
        Integer pageId = pageServiceImpl.getPageId(pageName);
        if(pageId == null) return 0;
        if(pageId == 0) return 0;
        
	EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            Query q = em_slave.createQuery("SELECT pt.pageId FROM AutoFormPageTab pt WHERE pt.company.companyId = :companyId AND pt.pageName = :pageName");
            
            Object customizePageId = q.setParameter("pageName", String.valueOf(pageId)).setParameter("companyId", companyId).getSingleResult();
            return NumberUtils.toInt(customizePageId.toString(), 0);
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }

        return 0;
    }

    @Override
    public List<Issue> getIssueHistoryCustomers(int issueId, int companyId, String telMobileMails, String custCode) {
        if(StringUtils.isBlank(telMobileMails) && StringUtils.isBlank(custCode)) return new ArrayList<>();
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            StringBuilder sql = StringBuilderUtil.selectHistoryCustomers(issueId, companyId, telMobileMails, custCode);
            List<Issue> issues = em_slave.createNativeQuery(sql.toString(), Issue.class).getResultList();
            return issues;
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Customer> getSpecialCustomers(String telMobileMails, int companyId, String custCode) {
        if(StringUtils.isBlank(telMobileMails) && StringUtils.isBlank(custCode)) return new ArrayList<>();
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            StringBuilder sql = StringBuilderUtil.selectSpecialCustomers(telMobileMails, companyId, custCode);
            return em_slave.createNativeQuery(sql.toString(), Customer.class).getResultList();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<IssueLamp> getIssueLamps(final int companyId) {
        if(companyId <= 0) return new ArrayList<>();
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            return em_slave.createNamedQuery("IssueLamp.findByCompanyId", IssueLamp.class).setParameter("companyId", companyId).getResultList();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Issue> findByIssueViewCodeLike(int companyId, String code) {
        if(companyId <= 0 || StringUtils.isBlank(code)) return new ArrayList<>();
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            return em_slave.createNamedQuery("Issue.findByIssueViewCodeLike", Issue.class
                    ).setParameter("companyId", companyId
                    ).setParameter("code", "%" + code + "%"
                    ).setParameter("issueDeleted", 0).getResultList();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Object> findByCountIssueSame(int companyId, List<Integer> months, Integer issueId) {
        if(companyId <= 0 || months.isEmpty() || issueId == null) return new ArrayList<>();
        
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            StringBuilder sql = StringBuilderUtil.getCountIssueSame(companyId, months, issueId);
            List<Object> counts = em_slave.createNativeQuery(sql.toString()).getResultList();
            if(counts == null) counts = new ArrayList<>();
            return counts;
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Issue> findByCompanyId(int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            return em_slave.createQuery("SELECT i FROM Issue i WHERE i.company.companyId = :companyId and i.issueDeleted = :issueDeleted") .setParameter("companyId", companyId) .setParameter("issueDeleted", InterfaceUtil.UN_DELETED) .getResultList();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Issue> findAll() {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            return em_slave.createQuery("SELECT i FROM Issue i WHERE i.issueDeleted = :issueDeleted") .setParameter("issueDeleted", InterfaceUtil.UN_DELETED) .getResultList();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
}
