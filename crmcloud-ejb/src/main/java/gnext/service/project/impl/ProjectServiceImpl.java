package gnext.service.project.impl;

import gnext.bean.Member;
import gnext.bean.issue.Issue;
import gnext.service.elastic.SearchService;
import gnext.bean.elastic.Document;
import gnext.bean.project.ProjectCustColumnWidth;
import gnext.service.impl.*;
import gnext.bean.project.ProjectCustSearch;
import gnext.dbutils.util.StringUtil;
import gnext.interceptors.annotation.enums.Module;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHits;
import gnext.service.project.ProjectService;
import gnext.utils.InterfaceUtil.COMPANY_TYPE;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 *
 * @author hungpd
 */
@Stateless
public class ProjectServiceImpl extends AbstractService<ProjectCustSearch> implements ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private static final long serialVersionUID = 8782493008532663725L;
    @EJB private SearchService searchServiceImpl;

    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public ProjectServiceImpl() { super(ProjectCustSearch.class); }

    @Override
    public void remove(ProjectCustSearch t) throws Exception {
        t.setListDeleted((short)1);
        super.edit(t);
    }

    @Override
    public List<ProjectCustSearch> findSearchAvaiableList(int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StringBuilder nq = new StringBuilder();
            if( companyId == 0 ){
                nq.append("SELECT o FROM ProjectCustSearch o WHERE o.listDeleted = 0 AND o.listType = 3");
            } else {
                nq.append("SELECT o FROM ProjectCustSearch o WHERE o.listDeleted = 0 AND o.listType = 3 AND o.company.companyId = :companyId");
            }

            Query q = em_slave.createQuery(nq.toString());
            if(companyId != 0) q.setParameter("companyId", companyId);
            
            return q.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<ProjectCustSearch> findAllAvaiable(int companyId, Member member) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StringBuilder nq = new StringBuilder();
            Query q = em_slave.createNamedQuery("ProjectCustSearch.findByMember");
            q.setParameter("companyId", companyId);
            q.setParameter("memberId", member.getMemberId());
            q.setParameter("groupId", member.getGroup().getGroupId());
            q.setHint(QueryHints.READ_ONLY, HintValues.TRUE);

            return q.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<ProjectCustSearch> search(int companyId, String query) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            Query q = null;
            StringBuilder nq = new StringBuilder();
            if( companyId == 0 ){
                nq.append("SELECT o FROM ProjectCustSearch o WHERE 1=1");
            }else{
                nq.append("SELECT o FROM ProjectCustSearch o WHERE o.listDeleted = 0 AND o.company.companyId = :companyId");
            }
            if( !StringUtils.isEmpty(query) ){
                nq.append(" AND ").append(query);
            }

            q = em_slave.createQuery(nq.toString());
            if( companyId != 0 ){
                q.setParameter("companyId", companyId);
            }

            return q.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<Map<String, Object>> advanceSearch(short comFlag, int companyId, int memberId, boolean quickSearch, String query, List<String> condFields, String keyword, String keywordCondition, List<SelectItem> visibleColumns, Date from, Date to, String lang){
        return advanceSearch(comFlag, 0, companyId, memberId, quickSearch, query, condFields, keyword, keywordCondition, visibleColumns, from, to, lang);
    }
    
    @Override
    public List<Map<String, Object>> advanceSearch(short comFlag, int issueId, int companyId, int memberId, boolean quickSearch, String query, List<String> condFields, String keyword, String keywordCondition, List<SelectItem> visibleColumns, Date from, Date to, String lang){
        if(visibleColumns == null || visibleColumns.size() <= 0) return new ArrayList<>();
        
        List<String> columns = new ArrayList<>();
        List<SelectItem> items = (List) ((ArrayList)visibleColumns).clone();
        for(SelectItem item : items) {
            if(item == null) continue;
            
            if("issue_proposal_level_id".equals(item.getValue())) continue;
            if("issue_id".equals(item.getValue())) continue;
            if("issue_receive_date".equals(item.getValue())) continue;
            
            columns.add(String.valueOf(item.getValue()));
        }
        
        if(comFlag != COMPANY_TYPE.CUSTOMER) { // không phải là khách hàng đặc biệt.
            if( !columns.contains("issue_proposal_level_id") ){
                columns.add(0, "issue_proposal_level_id");
            }
            if( !columns.contains("issue_id") ){
                columns.add(1, "issue_id");
            }

            if( !columns.contains("issue_receive_date") ){
                columns.add(2, "issue_receive_date");
            }
        } else {
            if( !columns.contains("cust_id") ){
                columns.add(0, "cust_id");
            }            
        }
        
        if(!columns.contains("lamp_color")){
            columns.add("lamp_color");
        }

        if(!StringUtils.isEmpty(keyword)){
            keyword = keyword.trim().replaceAll(" +", " ");
            keyword = keyword.trim().replaceAll("　+", " ");
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        if(quickSearch){ //using elastic search   
            SearchHits hits;
            if(comFlag == COMPANY_TYPE.CUSTOMER) {
                hits = searchServiceImpl.search(Module.CUSTOMER, query, keyword, columns);
            } else {
                hits = searchServiceImpl.search(Module.ISSUE, query, keyword, columns);
            }
            if(hits != null){
                hits.forEach((hit) -> {
                    results.add(hit.getSource());
                });
            }
        }else{ //using DB search
            if(comFlag == COMPANY_TYPE.CUSTOMER) {
                searchCustomer(results, columns, issueId, companyId, memberId, query, condFields, keyword, keywordCondition, from, to, lang);
            } else {
                searchIssue(results, columns, issueId, companyId, memberId, query, condFields, keyword, keywordCondition, from, to, lang);
            }
        }
        return results;
    }
    
    private void searchCustomer(List<Map<String, Object>> results, List<String> columns, int issueId, int companyId, int memberId,
            String query, List<String> condFields, String keyword, String keywordCondition, Date from, Date to, String lang) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StoredProcedureQuery q = em_slave.createStoredProcedureQuery("proc_customer_search");
            q.registerStoredProcedureParameter("p_count", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_search_type", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_keywords", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_operator", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_company_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_display_columns", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_lang", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_sort_field", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_sort_order", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_limit", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_offset", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_where", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_from_date", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("p_to_date", String.class, ParameterMode.IN);
            
            int p_search_type = 0;
            if(!StringUtils.isEmpty(keyword) && !StringUtils.isEmpty(keywordCondition))
                p_search_type = 1;
            
            query = query + (StringUtils.isEmpty(query)?"":"and")+ " cust_special_id > 0 "; // luôn luôn tìm các khách hàng đặc biệt.
            
            q.setParameter("p_count", 0); // =0 -> lấy danh sách khách hàng đặc biệt, =1 -> lấy tổng số khách hàng đặc biệt tìm thấy.
            q.setParameter("p_search_type", p_search_type); // =0 -> tìm kiếm bình thường, =1 -> tìm kiếm theo quick-search.
            q.setParameter("p_keywords", keyword); // không tìm kiếm theo quick-search.
            q.setParameter("p_operator", keywordCondition); // không tìm kiếm theo quick-search.
            q.setParameter("p_company_id", companyId); // công ty đăng nhập.
            q.setParameter("p_display_columns", String.join(",", columns)); // danh sách columns.
            q.setParameter("p_lang", StringUtil.isEmpty(lang)?"":lang); // ngôn ngữ lựa chọn.
            q.setParameter("p_sort_field", null);   // không sort.
            q.setParameter("p_sort_order", "cust.created_time desc");   // không order.
            q.setParameter("p_limit", 0);           // không paging.
            q.setParameter("p_offset", 0);          // không paging.
            q.setParameter("p_where", query);        
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
//            String s1 = sdf.format(from);
//            String s2 = sdf.format(to);
            q.setParameter("p_from_date", from != null ? sdf.format(from) : "");
            q.setParameter("p_to_date", to != null ? sdf.format(to) : "");

            List<Object> qList = (List<Object>) q.getResultList();
            if(qList == null || qList.size() <= 0) return;
            
            qList.forEach((t) -> {
                Object[] value = (Object[]) t;
                Map<String, Object> item = new HashMap<>();
                int valIdx = 0;
                for(int i = 0; i< columns.size(); i++){
                    String columnName = columns.get(i);
                    try{
                        if(columnName.equals("lamp_color")) {
                            String[] lcn = StringUtils.isEmpty(String.valueOf(value[valIdx]))?null:String.valueOf(value[valIdx]).split("_");
                            if(lcn != null && lcn.length == 2) {
                                item.put(columnName, lcn[0]);
                                item.put("lamp_test_color", lcn[1]);
                            } else {
                                item.put(columnName, StringUtils.EMPTY);
                                item.put("lamp_test_color", StringUtils.EMPTY);
                            }
                        } else {
                            item.put(columnName, value[valIdx]);
                        }
                        valIdx++;
                    }catch(ArrayIndexOutOfBoundsException e){
                        item.put(columnName, "");
                    }
                }
                results.add(item);
            });
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    private void searchIssue(List<Map<String, Object>> results, List<String> columns, int issueId, int companyId, int memberId,
            String query, List<String> condFields, String keyword, String keywordCondition, Date from, Date to, String lang) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StoredProcedureQuery q = em_slave.createStoredProcedureQuery("proc_issue_search");
            if(q == null) return;
            
            q.registerStoredProcedureParameter("in_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("company_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("in_member_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("display_columns", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("select_conditions", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("keywords", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("keywordCondition", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("fromDate", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("toDate", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("lang", String.class, ParameterMode.IN);

            // remove duplicate key
            for (Iterator<String> iterator = condFields.iterator(); iterator.hasNext();) {
                String cf = iterator.next();
                if (columns.contains(cf)) {
                    iterator.remove();
                }
            }

            List<String> facedColumns = new ArrayList<>();
            facedColumns.addAll(columns);
            facedColumns.addAll(condFields);
            facedColumns.removeAll(Collections.singleton(null));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

            //add plus 1 day to date
            if(to != null){
                Calendar c = Calendar.getInstance();
                c.setTime(to);
                c.add(Calendar.DATE, 1);
                to = c.getTime();
            }

            q.setParameter("in_id", issueId);
            q.setParameter("company_id", companyId);
            q.setParameter("in_member_id", memberId);
            q.setParameter("display_columns", StringUtils.join(facedColumns, ","));
            q.setParameter("select_conditions", query);
            q.setParameter("keywords", keyword);
            q.setParameter("keywordCondition", keywordCondition);
            q.setParameter("fromDate", from != null ? sdf.format(from) : "");
            q.setParameter("toDate", to != null ? sdf.format(to) : "");
            q.setParameter("lang", lang);

            List<Object> qList = (List<Object>) q.getResultList();
            if(qList == null || qList.size() <= 0) return;
            
            qList.forEach((t) -> {
                Object[] value = (Object[]) t;
                Map<String, Object> item = new HashMap<>();
                int valIdx = 0;
                for(int i = 0; i< columns.size(); i++){
                    String columnName = columns.get(i);
                    if(!columnName.startsWith("hidden_") && (!columnName.endsWith("_id") || columnName.equals("issue_id") || columnName.equals("issue_proposal_level_id"))){
                        try{
                            if(columnName.equals("lamp_color")) {
                                String[] lcn = StringUtils.isEmpty(String.valueOf(value[valIdx]))?null:String.valueOf(value[valIdx]).split("_");
                                if(lcn != null && lcn.length == 2) {
                                    item.put(columnName, lcn[0]);
                                    item.put("lamp_test_color", lcn[1]);
                                } else {
                                    item.put(columnName, StringUtils.EMPTY);
                                    item.put("lamp_test_color", StringUtils.EMPTY);
                                }
                            } else {
                                item.put(columnName, value[valIdx]);
                            }
                            valIdx++;
                        }catch(ArrayIndexOutOfBoundsException e){
                            item.put(columnName, "");
                        }
                    }
                }
                results.add(item);
            });
        } catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    @Override
    public int reIndexAll() {
        EntityManager em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
        try {
            List<String> columns = new ArrayList<>();

            Query query;
            query = em_slave.createNativeQuery("DESC `crm_issue`");

            List<Object[]> staticColumns = query.getResultList();
            staticColumns.forEach((item) -> {
                columns.add(item[0].toString());
            });

            StringBuilder q = new StringBuilder("SELECT ");
            q.append(StringUtils.join(columns, ","));
            q.append(" FROM `crm_issue` WHERE `issue_deleted` = 0 LIMIT 10;"); //limit 10 documents for test

            query = em_slave.createNativeQuery(q.toString());
            List<Object[]> results = query.getResultList();

            List<Document> documents = new ArrayList<>();
            results.stream().map((t) -> {
                return t;
            }).forEach((row) -> {
                Map<String, Object> item = new HashMap<>();
                for(int i=0; i< row.length; i++){
                    item.put(columns.get(i), row[i]);
                }
                String docId = item.get("issue_id").toString();

                this.addDynamicColumnData(item, docId, em_slave);

                documents.add(new Document(docId, item));
            });

            int successCount = searchServiceImpl.bulkIndex(documents, Module.ISSUE);

            return successCount;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return 0;
    }
    
    @Override
    public boolean indexDocument(int issueId){
        EntityManager em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
        try {
            List<String> columns = new ArrayList<>();
            Query query;
            query = em_slave.createNativeQuery("DESC `crm_issue`");

            List<Object[]> staticColumns = query.getResultList();
            staticColumns.forEach((item) -> {
                columns.add(item[0].toString());
            });

            StringBuilder q = new StringBuilder("SELECT ");
            q.append(StringUtils.join(columns, ","));
            q.append(" FROM `crm_issue` WHERE `issue_id` = ? AND `issue_deleted` = 0;");

            query = em_slave.createNativeQuery(q.toString()).setParameter(1, issueId);
            List<Object[]> results = query.getResultList();

            List<Document> documents = new ArrayList<>();
            results.stream().map((t) -> {
                return t;
            }).forEach((row) -> {
                Map<String, Object> item = new HashMap<>();
                for(int i=0; i< row.length; i++){
                    item.put(columns.get(i), row[i]);
                }
                String docId = item.get("issue_id").toString();

                this.addDynamicColumnData(item, docId, em_slave);

                String rowId = searchServiceImpl.index(new Document(docId, item), Module.ISSUE);

            });

            searchServiceImpl.bulkIndex(documents, Module.ISSUE);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return false;
    }

    @Override
    public List<Map<String, String>> findIssueById(Integer issueId, Integer companyId, Integer memberId, List<String> columns){
        return getIssueByIdOrLocale(issueId, companyId, memberId, columns, null);
    }
 
    @Override
    public List<Map<String, String>> findIssueByIdAndLocale(Integer issueId, Integer companyId, Integer memberId, List<String> columns, String lang){
        return getIssueByIdOrLocale(issueId, companyId, memberId, columns, lang);
    }

    private List<Map<String, String>> getIssueByIdOrLocale(Integer issueId, Integer companyId, Integer memberId, List<String> columns, String lang) {
        EntityManager em_slave = null;
        try{
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            StoredProcedureQuery q = em_slave.createStoredProcedureQuery("proc_issue_get");
            q.registerStoredProcedureParameter("issue_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("company_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("member_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("display_columns", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("langs", String.class, ParameterMode.IN);
            
            q.setParameter("issue_id", issueId);
            q.setParameter("company_id", companyId);
            q.setParameter("member_id", memberId);
            q.setParameter("display_columns", StringUtils.join(columns, ","));
            if(StringUtils.isEmpty(lang)) {
                q.setParameter("langs", "");
            } else {
                q.setParameter("langs", lang);
            }
            List<Map<String, String>> results = new ArrayList<>();
            do{
                List<Object> qList = (List<Object>) q.getResultList();
                Map<String, String> item = new HashMap<>();
                if(qList.size() > 0){
                    for(int i = 0; i< columns.size(); i++){
                        List<Object> row = Arrays.asList((Object[])qList.get(0));
                        String col = columns.get(i);
                        String value = row.get(i) != null ? row.get(i).toString() : "";
                        item.put(col, value);
                    }
                }
                if(!item.isEmpty()){
                    results.add(item);
                }
            }while(q.hasMoreResults());
            return results;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public String persitIssueToElastic(Issue issue, List<String> columns, List<Locale> locales){
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            columns.removeAll(Arrays.asList("", null));
            Map<String, Object> item = new HashMap<>();
            String docId = String.format("%d_%s", issue.getCompany().getCompanyId(), issue.getIssueViewCode());
            List<String> langs = new ArrayList<>();
                
            if(locales != null){
                locales.forEach((locale) -> {
                    langs.add(locale.getLanguage());
                });
            }
            
            StoredProcedureQuery q = em_slave.createStoredProcedureQuery("proc_issue_get");
            q.registerStoredProcedureParameter("issue_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("company_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("member_id", Integer.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("display_columns", String.class, ParameterMode.IN);
            q.registerStoredProcedureParameter("langs", String.class, ParameterMode.IN);

            q.setParameter("issue_id", issue.getIssueId());
            q.setParameter("company_id", issue.getCompany().getCompanyId());
            q.setParameter("member_id", issue.getCreatorId().getMemberId());
            q.setParameter("display_columns", StringUtils.join(columns, ","));
            q.setParameter("langs", StringUtils.join(langs, ","));

            do{
                List<Object> qList = (List<Object>) q.getResultList();
                if(qList.size() > 0){
                    for(int i = 0; i< columns.size(); i++){
                        List<Object> row = Arrays.asList((Object[])qList.get(0));
                        String col = columns.get(i);
                        String value = row.get(i) != null ? row.get(i).toString() : "";
                        item.put(col, value);
                    }
                }
            }while(q.hasMoreResults());

            return searchServiceImpl.index(new Document(docId, item), Module.ISSUE);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
    
    private void addDynamicColumnData(Map<String, Object> source, String issueId, EntityManager em_slave){
        try {
            String subQueryStr = "select item_id, item_data from crm_multiple_data_value WHERE page_id = ? AND page_type = ? AND target_id = ?;";
            Query subQuery = em_slave.createNativeQuery(subQueryStr)
                    .setParameter(1, 2) //sample page id  = 2
                    .setParameter(2, 2) // page_type = 2 is static page
                    .setParameter(3, issueId);
            List<Object[]> subResults = subQuery.getResultList();
            subResults.stream().map((subRow) -> {
                return subRow;
            }).forEach((subRow) -> {
                source.put("dynamic_"+subRow[0], subRow[1]);
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    @Override
    public void saveColumnWidth(String column, int width, int companyId, int updateId) {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            ProjectCustColumnWidth w = new ProjectCustColumnWidth(column, width, companyId, updateId);
            w.setUpdatedTime(new Date());
            if(em_slave.find(ProjectCustColumnWidth.class, column) != null) {
                em_slave.merge(w);
            }else{
                em_slave.persist(w);
            }
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public List<ProjectCustColumnWidth> getColumnWidthList(int companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            List<ProjectCustColumnWidth> list =
                    em_slave.createQuery("SELECT o FROM ProjectCustColumnWidth o WHERE o.companyId = :companyId")
                    .setParameter("companyId", companyId).getResultList();
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
}
