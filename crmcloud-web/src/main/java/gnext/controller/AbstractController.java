package gnext.controller;

import com.google.gson.Gson;
import gnext.bean.Company;
import gnext.controller.common.LocaleController;
import gnext.exporter.excel.PrimefaceExtExporter;
import gnext.model.BaseModel;
import gnext.model.authority.UserModel;
import gnext.model.search.SearchFilter;
import gnext.model.search.SearchGroup;
import gnext.resource.bundle.IssueBundle;
import gnext.resource.bundle.MailBundle;
import gnext.resource.bundle.MsgBundle;
import gnext.resource.bundle.ValidationBundle;
import gnext.security.annotation.SecureMethod;
import gnext.util.DateUtil;
import gnext.util.HTTPResReqUtil;
import gnext.util.JsfUtil;
import gnext.util.ModelUtil;
import gnext.util.Pagination;
import java.io.Serializable;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.datatable.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 *
 * @param <T> The type of the model.
 */
public abstract class AbstractController<T extends BaseModel> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);    

    @Getter @Setter protected String exportType = "XLS";
    @Getter @Setter protected String searchDataJson;
    
    /** Using if paging base on LazyDataModel class type, an example in group module */
    @Getter @Setter protected int currentRowNum;
    
    /** Using if paging base on user customize, an example in mailing list module */
    private Pagination<T> paginator;
    
    /** List bundle on system */
    protected MsgBundle msgBundle = new MsgBundle();
    protected ValidationBundle validationBundle = new ValidationBundle();
    protected MailBundle mailBundle = new MailBundle();
    protected IssueBundle issueBundle = new IssueBundle();
    
    protected Map<String, Long> measure = new HashMap<>(); //Su dung de do performance
    
    @SecureMethod(SecureMethod.Method.NONE)
    public void onDownloadSelectionChanged(ActionEvent event) {
        exportType = getParameter("exportType");
    }

    @SecureMethod(SecureMethod.Method.CREATE)
    public void create(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SecureMethod(SecureMethod.Method.UPDATE)
    public void edit(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SecureMethod(SecureMethod.Method.VIEW)
    public void show(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SecureMethod(SecureMethod.Method.CREATE)
    public void save(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SecureMethod(SecureMethod.Method.UPDATE)
    public void update(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SecureMethod(SecureMethod.Method.DELETE)
    public void delete(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SecureMethod(SecureMethod.Method.UPLOAD)
    public void upload(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SecureMethod(SecureMethod.Method.DOWNLOAD)
    public void download(String tblName, String fileName) {
        StringBuilder builderFileName = new StringBuilder();
        try {
            builderFileName.append(fileName).append("_").append(new SimpleDateFormat(DateUtil.PATTERN_CSV_EXPORT_DATE).format(DateUtil.now()));
            fileName = URLEncoder.encode(builderFileName.toString(), "utf-8");
            
            UIComponent component = JsfUtil.findComponent(tblName);
            DataTable table = (DataTable) component;
            
            if ("csv".equalsIgnoreCase(exportType)) {
//                 new CSVExporter().export(getFacesContext(), table, fileName, false, false, "utf-8", null, null);
                throw new UnsupportedOperationException("Not supported yet.");
            } else if ("xls".equalsIgnoreCase(exportType)) {
                new PrimefaceExtExporter().export(getFacesContext(), table, fileName, false, false, "utf-8", null, null, null);
            } else {
//                new PDFExporter().export(getFacesContext(), table, fileName, false, false, "utf-8", null, null);
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            getFacesContext().responseComplete();
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        } finally {
            builderFileName = null; // will release by GC.
        }
    }

    @SecureMethod(SecureMethod.Method.NONE)
    public void up(ActionEvent event) {
        int rowIndex = Integer.parseInt(getParameter("rowIndex"));
        ModelUtil.up(rowIndex, getUFDLModels());
        afterReSort();
    }

    @SecureMethod(SecureMethod.Method.NONE)
    public void first(ActionEvent event) {
        int rowIndex = Integer.parseInt(getParameter("rowIndex"));
        ModelUtil.first(rowIndex, getUFDLModels());
        afterReSort();
    }

    @SecureMethod(SecureMethod.Method.NONE)
    public void down(ActionEvent event) {
        int rowIndex = Integer.parseInt(getParameter("rowIndex"));
        ModelUtil.down(rowIndex, getUFDLModels());
        afterReSort();
    }

    @SecureMethod(SecureMethod.Method.NONE)
    public void last(ActionEvent event) {
        int rowIndex = Integer.parseInt(getParameter("rowIndex"));
        ModelUtil.last(rowIndex, getUFDLModels());
        afterReSort();
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    public void nextOne(ActionEvent event) {
        currentRowNum = currentRowNum + 1;
        afterPaging();
    }

    @SecureMethod(SecureMethod.Method.VIEW)
    public void previousOne(ActionEvent event) {
        currentRowNum = currentRowNum - 1;
        afterPaging();
    }

    @SecureMethod(SecureMethod.Method.NONE)
    public void nextPaging() {
        paginator.getNextPage();
        afterPaging();
    }

    @SecureMethod(SecureMethod.Method.NONE)
    public void previousPaging() {
        paginator.getPreviousPage();
        afterPaging();
    }

    @SecureMethod(SecureMethod.Method.SEARCH)
    public void search() {
        Gson gson = new Gson();
        SearchGroup[] groups = gson.fromJson(getSearchDataJson(), SearchGroup[].class);

        this.doSearch(new SearchFilter(Arrays.asList(groups)));
    }

    protected void doSearch(SearchFilter filter){
        throw new UnsupportedOperationException("doSearch Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Pagination<T> getPaginator() {
        if (paginator == null) {
            paginator = new Pagination<>();
        }
        return paginator;
    }

    public void setPaginator(Pagination<T> paginator) {
        this.paginator = paginator;
    }

    protected List<? extends BaseModel> getUFDLModels() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void afterReSort() {
        // need to implement this method if the controller have sort UFDL;
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void afterPaging() {
        // need to implement this method if the controller have customize paging;
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    protected String getParameter(final String param) {
        return HTTPResReqUtil.getRequestParameter(param);
    }

    public Company getCurrentCompany() {
        if(UserModel.getLogined() != null)
            return UserModel.getLogined().getCompany();
        
        return null;
    }

    public Integer getCurrentCompanyId() {
        if(getCurrentCompany() != null)
            return getCurrentCompany().getCompanyId();

        return null;
    }
    
    public String getLocale() {
        LocaleController lc = JsfUtil .getManagedBean(LocaleController.MANAGED_BEAN_NAME, LocaleController.class);
        return lc.getLocale();
    }
    
    public void startMeasure(String key){
        measure.put(key, System.currentTimeMillis());
    }
    
    public void stopMeasure(String key){
        Long start = measure.containsKey(key) ? measure.get(key) : System.currentTimeMillis();
        System.err.println(">>> MEASURE ["+key+"] " + (System.currentTimeMillis() - start));
        measure.remove(key);
    }
    
    protected void rediect(String page) throws Exception {
        getFacesContext().getExternalContext().redirect(page);
        FacesContext.getCurrentInstance().responseComplete();
    }
}
