/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.system;

import com.google.gson.Gson;
import gnext.interceptors.QuickSearch;
import gnext.interceptors.annotation.enums.Module;
import gnext.interceptors.items.MetaDataColumn;
import gnext.resource.bundle.ValidationBundle;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.quicksearch.QuickSearchService;
import gnext.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "quickSearchController")
@ViewScoped()
@SecurePage(module = SecurePage.Module.SYSTEM, require = false)
public class QuickSearchController implements Serializable {

    private static final long serialVersionUID = 1619551432992365697L;
    private final Logger LOGGER = LoggerFactory.getLogger(QuickSearchController.class);
    private static final int ROW_ON_PAGE = 10;
    
    @Getter @Setter private Map<String, LinkedList<String>> cols = new HashMap<>();
    @Getter @Setter private Map<String, List<LinkedList<String>>> vals = new HashMap<>();
    @Getter @Setter private Map<String, Integer> total = new HashMap<>();
    @EJB @Getter @Setter private QuickSearchService quickSearchService;
    
    /** a customize paging for each module */
    @Getter @Setter private Map<String, Integer> from = new HashMap<>();
    @Getter @Setter private Map<String, Integer> pagesize = new HashMap<>();
    public ValidationBundle vb = new ValidationBundle();
    
    /** the text of search key */
    @Getter @Setter private String searchKey;
    @Getter @Setter private Integer matchIssueId;
    
    @PostConstruct
    public void init() {
        _initPaging();
    }
    
    private void _initPaging() {
        from.clear(); pagesize.clear();
        for (Module module : Module.values()) {
            if(!module.isUse()) continue;
            from.put(module.getName(), 0);
            pagesize.put(module.getName(), ROW_ON_PAGE);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void next(String moduleName) {
        Integer f = from.get(moduleName);
        f = f + pagesize.get(moduleName);
        from.put(moduleName, f);
        _load();
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void previous(String moduleName) {
        Integer f = from.get(moduleName);
        f = f - pagesize.get(moduleName);
        from.put(moduleName, f);
        _load();
    }
    
    private void _load() {
        cols.clear(); vals.clear(); total.clear();
        for (Module module : Module.values()) {
            if(!module.isUse()) continue;
            String sql = "qs.quick_search_module = '" + module.getName() + "'";
            List<QuickSearch> quickSearchs = quickSearchService.find(from.get(module.getName()), pagesize.get(module.getName()), sql, this.searchKey);
            int t = quickSearchService.total(sql, this.searchKey);
            if(quickSearchs.isEmpty()) continue;
            total.put(module.getName(), t);
            for(QuickSearch qs : quickSearchs) {
                MetaDataColumn mdc = new Gson().fromJson(qs.getQuickSearchCols(), MetaDataColumn.class);
                if(!cols.containsKey(module.getName())) cols.put(module.getName(), mdc.getCols());
                List<LinkedList<String>> datas = null;
                if(!vals.containsKey(module.getName())) {
                    datas = new ArrayList<>();
                    vals.put(module.getName(), datas);
                } else {
                    datas = vals.get(module.getName());
                }
                datas.add(mdc.getVals());
            }
        } // end for::
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void viewQuickSearch() {
        RequestContext context = RequestContext.getCurrentInstance(); 
        if(StringUtils.isEmpty(this.searchKey)) {
           context.addCallbackParam("RE", "NG");
           JsfUtil.addErrorMessage(vb.getString("validator.required", "検索キー"));
           context.update("growl");
        } else {
            _load();
            context.addCallbackParam("RE", "OK");
        } // end if::
    }
}
