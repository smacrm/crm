package gnext.interceptors;

import com.google.gson.Gson;
import gnext.bean.QuickSearchEntity;
import gnext.bean.elastic.Document;
import gnext.bean.issue.Issue;
import gnext.interceptors.annotation.QuickSearchAction;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.interceptors.annotation.QuickSearchField;
import gnext.interceptors.annotation.QuickSearchDb;
import gnext.interceptors.items.MetaDataColumn;
import gnext.interceptors.items.MetaDataContent;
import gnext.service.config.ConfigService;
import gnext.service.elastic.SearchService;
import gnext.service.project.ProjectService;
import gnext.service.quicksearch.QuickSearchService;
import gnext.utils.BigSmallString;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 *
 * @author daind
 */
public class QuickSearchDbInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuickSearchDbInterceptor.class);
    
    @Getter @Setter @EJB private QuickSearchService quickSearchService;
    @Getter @Setter @EJB private SearchService searchService;
    @Getter @Setter @EJB private ConfigService configService;
    @Getter @Setter @EJB private ProjectService projectService;
    
    private boolean useElasticSearch() {
        try {
            String state = configService.get("ELASTIC_ENABLE");
            if(state == null || state.isEmpty()) return false;
            return Boolean.valueOf(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Will intercept the ejb bean if it marked.
     * 
     * @param ctx
     * @return
     * @throws Exception 
     */
    @AroundInvoke
    public Object methodInterceptor(InvocationContext ctx) throws Exception {
        Object obj = null;
        try {
            obj = ctx.proceed();
            return obj;
        } catch (Exception e) {
            obj = null;
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            if(obj != null) _log(obj, ctx);
        }
    }
    
    private void _log(Object obj, InvocationContext ctx) {
        Method m = ctx.getMethod();
        QuickSearchAction qsa = m.getAnnotation(QuickSearchAction.class);
        if(qsa != null && obj.getClass().getAnnotation(QuickSearchDb.class) != null) {
            QuickSearchDb qsdb = obj.getClass().getAnnotation(QuickSearchDb.class);
            if(qsdb.disable()) return;
            _addQuickSearch(obj, qsa, qsdb);
        }
    }
    
    private void _addQuickSearch(final Object obj, final QuickSearchAction qsa, final QuickSearchDb asdb) {
        List<MetaDataContent> metadatas = new ArrayList();
        LinkedList<String> cols = new LinkedList<>();
        LinkedList<String> vals = new LinkedList<>();
        
        Field[] fields = obj.getClass().getDeclaredFields();
        for(Field f : fields) {
            if(f.getAnnotation(QuickSearchField.class) == null) continue;
            try {
                QuickSearchField qsf = f.getAnnotation(QuickSearchField.class);
                f.setAccessible(true);
                
                Object val = f.get(obj);
                if(val == null) continue;
                
                if(qsf.view()) {
                    cols.add(qsf.title());
                    vals.add(String.valueOf(val));
                }
                _buildContent(metadatas, val, qsf);
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
        
        try {
            if(obj instanceof QuickSearchEntity) {
                List<MetaDataContent> mdcs = ((QuickSearchEntity)obj).getMetadata();
                metadatas.addAll(mdcs);
            }
        } catch (Exception e) { }
        
        //hungpd
        String binary = null;
        try {
            binary = String.valueOf(MethodUtils.invokeMethod(obj, "getBinarySearchContent"));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
        //Add dynamic content to quick search
        if(obj instanceof Issue){
            Issue issue = (Issue)obj;
            List<String> columns = new ArrayList<>();
            issue.getCustomizeDataMapping().keySet().forEach((id) -> {
                columns.add("dynamic_" + id);
            });
            if(!columns.isEmpty()){
                List<Map<String, String>> results = projectService.findIssueById(issue.getIssueId(), issue.getCompany().getCompanyId(), issue.getCreatorId().getMemberId(), columns);
                StringBuilder strDynamicContent = new StringBuilder();
                results.forEach((item) -> {
                    item.forEach((col, val) -> {
                        strDynamicContent.append(val);
                    });
                });
                binary += BigSmallString.toSmall(strDynamicContent.toString());
            }
        }
        //}
        //end 20170313
        
        _saveQuickSearch(obj, qsa, asdb, metadatas, cols, vals, binary);
        
        if(!metadatas.isEmpty() && useElasticSearch()) _saveElasticSearch(obj, asdb, metadatas);
    }
    
    private void _buildContent(List<MetaDataContent> metadatas, Object data, QuickSearchField asf) {
        if(data instanceof String || data instanceof Integer) {
            metadatas.add(new MetaDataContent(asf.name(), String.valueOf(data)));
        } else {
            throw new UnsupportedOperationException("the QuickSearchField annotation just accept for String or Integer.");
        }        
    }
    
    private void _saveElasticSearch(Object obj, QuickSearchDb asdb, List<MetaDataContent> metadatas) {
        Integer id = getId(obj, asdb.fieldTargetId());
        if(id == null) return;
        
        Map<String, Object> data = new HashMap<>();
        for(MetaDataContent mdc : metadatas) data.put(mdc.getName(), mdc.getValue());
        searchService.index(new Document(id.toString(), data), asdb.module());
    }
    
    private void _saveQuickSearch(Object obj, QuickSearchAction qsa, QuickSearchDb asdb,
            List<MetaDataContent> metadatas, LinkedList<String> cols, LinkedList<String> vals, String binary) {
        try {
            String qsaAction = qsa.action();
            if(qsaAction == null || qsaAction.isEmpty()) return;
            
            Integer id = getId(obj, asdb.fieldTargetId());
            if(id == null) return;

            String metadata = metadatas.isEmpty() ? "" : new Gson().toJson(metadatas);
            String mdcols = cols.isEmpty() ? "": new Gson().toJson(new MetaDataColumn(cols, vals));
                
            if(StringUtils.isEmpty(metadata) && StringUtils.isEmpty(mdcols) && StringUtils.isEmpty(binary)){
                //DO NOTHING
            }else{
                if(qsaAction.equals(QuickSearchAction.UPDATE)) {
                    QuickSearch oldqs = quickSearchService.search(asdb.module().name(), id);
                    if(oldqs != null)
                        _updateQuickSearch(oldqs, asdb, binary, metadata, mdcols);
                    else
                        _createQuickSearch(id, asdb, binary, metadata, mdcols);
                } else if(qsaAction.equals(QuickSearchAction.CREATE)) {
                    _createQuickSearch(id, asdb, binary, metadata, mdcols);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    private void _createQuickSearch(Integer targetPK, QuickSearchDb asdb, String binary, String metadata, String mdcols) throws Exception {
        QuickSearch newqs = new QuickSearch();
        newqs.setQuickSearchModule(asdb.module().name());
        newqs.setQuickSearchBinary(binary);
        newqs.setQuickSearchContent(metadata);
        newqs.setQuickSearchCols(mdcols);
        newqs.setQuickSearchDeleted((short) 0);
        newqs.setQuickSearchTargetId(targetPK);
        quickSearchService.create(newqs);
    }
    
    private void _updateQuickSearch(QuickSearch qs, QuickSearchDb asdb, String binary, String metadata, String mdcols) throws Exception {
        qs.setQuickSearchBinary(binary);
        qs.setQuickSearchContent(metadata);
        qs.setQuickSearchModule(asdb.module().name());
        qs.setQuickSearchCols(mdcols);
        qs.setQuickSearchDeleted((short) 0);
        quickSearchService.edit(qs);
    }
    
    private Integer getId(final Object obj, final String fieldName) {
        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            for(Field f : fields) {
                if(f.getName().equals(fieldName)) {
                    f.setAccessible(true);
                    Object val = f.get(obj);
                    return Integer.parseInt(String.valueOf(val));
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        
        return null;
    }
}
