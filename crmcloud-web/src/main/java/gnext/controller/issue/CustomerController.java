package gnext.controller.issue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import gnext.bean.Company;
import gnext.bean.Prefecture;
import gnext.bean.ZipCode;
import gnext.bean.issue.CustDataSpecial;
import gnext.bean.issue.CustTargetInfo;
import gnext.bean.issue.Customer;
import gnext.bean.issue.Issue;
import gnext.bean.project.DynamicColumn;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.model.authority.UserModel;
import gnext.controller.issue.bean.CusSpecialLazyList;
import gnext.exporter.Export;
import gnext.exporter.excel.CustomerSpecialExportXsl;
import gnext.importer.Import;
import gnext.importer.excel.CustomerImport;
import gnext.model.search.SearchField;
import gnext.model.search.SearchFilter;
import gnext.model.search.SearchGroup;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.PrefectureService;
import gnext.service.ZipCodeService;
import gnext.service.issue.CustomerService;
import gnext.service.issue.IssueCustomerService;
import gnext.service.mente.MenteService;
import gnext.util.DateUtil;
import gnext.util.HTTPResReqUtil;
import gnext.util.InterfaceUtil.HTML;
import gnext.util.IssueUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.SelectUtil;
import gnext.util.StatusUtil;
import gnext.util.StringUtil;
import gnext.utils.InterfaceUtil.COMPANY_TYPE;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIInput;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xu ly cho TH khach hang dac biet
 * @author gnextadmin
 */
@ManagedBean(eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.ISSUE)
public class CustomerController extends AbstractController {
    private static final long serialVersionUID = 447445203977775874L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    @ManagedProperty(value = "#{issueController}") @Getter @Setter private IssueController issueController;
    @ManagedProperty(value = "#{ais}") @Getter @Setter private AdvanceIssueSearchController ais;
    @ManagedProperty(value = "#{layout}") @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{projectController}") @Getter @Setter private ProjectController projectController;
    
    @EJB @Getter @Setter private MenteService menteService;
    @EJB private IssueCustomerService issueCustomerService;
    @EJB private PrefectureService prefectureService;
    @EJB @Getter @Setter private CustomerService customerService;
    @EJB private ZipCodeService zipCodeService;
    
//    private Customer customer;
    private final Map<Integer, Map<Integer, Customer>> customerHolder = new HashMap<>();
    
    @Getter @Setter private boolean renderScriptHiddenSearchPanel;
    @Getter @Setter private Integer companyLoginedId;
    @Getter @Setter private LazyDataModel<Map<String, String>> specialCustomers;
    
    // các tham số cho quick-search.
    @Getter @Setter private int searchType = 0;
    @Getter @Setter private String keyqord;
    @Getter @Setter private String operator = "or";
    
    // tham số cho normal search.
    @Getter @Setter private String query;
    
    @Getter @Setter private List<SelectItem> prefecturesSelectItems;
    private void fillPrefecturesSelectItems() {
        String language = UserModel.getLogined().getLanguage();
        List<Prefecture> prefectures = prefectureService.findCities(language);
        prefecturesSelectItems = new ArrayList<>();
        for(Prefecture p : prefectures)
            prefecturesSelectItems.add(new SelectItem(p, p.getPrefectureName()));
    }
    
    @PostConstruct
    public void init(){
        fillPrefecturesSelectItems();
        if(this.companyLoginedId == null)
            this.companyLoginedId = UserModel.getLogined().getCompanyId();
        searchType = 0; // mặc định tìm kiếm theo mặc định(không dùng quicksearch).
    }

    /**
     * Được xử dụng trong datatable.xhtml.
     * URL được mã hóa đẩy lên address sau đó được dùng lại để tránh lỗi
     * khi sử dụng multiple-tabs.
     * @param custId
     * @return
     * @throws UnsupportedEncodingException 
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public String outcome(Integer custId) throws UnsupportedEncodingException {
        return "/index.xhtml?custId="+custId+"&s="+layout.getCurrentEaseyEncrypt()+"&faces-redirect=true";
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    @Override
    public void create(ActionEvent event) {
        try {
            String custId = HTTPResReqUtil.getRequestParameter("custId");
            
            Customer cust = null;
            if(NumberUtils.isDigits(custId) && Integer.parseInt(custId) > 0) {
                cust = this.issueCustomerService.find(Integer.valueOf(custId));
            } else {
                cust = new Customer();
                cust.setCompany(UserModel.getLogined().getCompany());
                cust.setCustDeleted(StatusUtil.EXISTS);
                cust.setCreatorId(UserModel.getLogined().getUserId());
                cust.setCreatedTime(DateUtil.now());
                cust.setUpdatedId(UserModel.getLogined().getUserId());
                cust.setUpdatedTime(DateUtil.now());
                if(NumberUtils.isDigits(custId)) {
                    cust.setCustId(Integer.parseInt(custId));
                }
            }
            
            // khởi tạo các meta-data cho customer nếu công ty là khách hàng đặc biệt.
            if(isSpecialCompanyFlag()) {
                cust.initSpecicalData();
            }
            
            this.setCustomer(cust);
        } catch(NumberFormatException e) {
            LOGGER.error("CustomerController.create()", e.getMessage());
        }
    }

    @SecureMethod(SecureMethod.Method.CREATE)
    public void openTab(ActionEvent event) {
        this.create(event);
        layout.setCenter("/modules/customer/special/create.xhtml");
    }
    
    public boolean isSpecialCompanyFlag() {
        Short businessFlag = UserModel.getLogined().getCompany().getCompanyBusinessFlag();
        if(businessFlag != null && businessFlag == COMPANY_TYPE.CUSTOMER) return true;
        return false;
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    public void insert() {
        try {
            /** 申出分類チェック */
            Object[] para = new Object[1];
            if(!_DoValidateCustomer(para, false)){
                return;
            }
            
            this.getCustomer().removeInvalidData();
            this.setCustomer(this.issueCustomerService.createCustomer(this.getCustomer()));
            mergeToIssue();
            
            // đóng dialog nếu đang mở dialog.
            try {
                RequestContext context = RequestContext.getCurrentInstance();
                context.closeDialog(0);                
            } catch (Exception e) { }
            
        } catch(Exception e) {
            JsfUtil.addErrorMessage(e.getMessage());
            LOGGER.error("CustomerController.insert()", e.getMessage());
        }
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    public void insertSpecialCust(int typeOfScreen) {
        try {
            /** 申出分類チェック */
            Object[] para = new Object[1];
            if(!_DoValidateCustomer(para, true)){
                return;
            }
            
            this.manualPrefectureCustSpecial();
            
            // làm mịn các dữ liệu (meta-data).
            this.getCustomer().smoothCustDataSpecInvalid();
            this.getCustomer().removeInvalidData();
            
            Customer custSaved = this.issueCustomerService.createCustomer(this.getCustomer());
            this.setCustomer(custSaved);
            mergeToIssue();
            
            if(typeOfScreen == 0) { // on dialog.
                try {
                    RequestContext context = RequestContext.getCurrentInstance();
                    context.closeDialog(0);
                } catch (Exception e) { }
                onInsertSuccess();
            } else {
                unHoldCustomer();
                projectController.goback();
                JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.create.success", custSaved.getCustFirstKana()));
            }
        } catch(Exception e) {
            JsfUtil.addErrorMessage(e.getMessage());
            LOGGER.error("CustomerController.insert()", e.getMessage());
        }
    }

    private void manualPrefectureCustSpecial() {
        for (Iterator<CustDataSpecial> iterator = this.getCustomer().getCustDataSpecials().iterator(); iterator.hasNext();) {
            CustDataSpecial custDataSpecial = iterator.next();
            if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_ADDRESS) {
                if(!StringUtils.isEmpty(custDataSpecial.getCustData2())) { // nếu có chọn mã thành phố.
                    Integer prefectureId = Integer.parseInt(custDataSpecial.getCustData2());
                    Prefecture prefecture = prefectureService.find(prefectureId);
                    
                    // tên thành phố sẽ được sử dụng trong quick-search.
                    if(prefecture != null) custDataSpecial.setCustCityNameData2(prefecture.getPrefectureName());
                }
                
                // trường hợp khách hàng chưa có mã thành phố lấy mã đầu tiên trong bộ địa chỉ của khách hàng.
                if (this.getCustomer().getCustCity() == null && !StringUtils.isEmpty(custDataSpecial.getCustData2())) {
                    Integer prefectureId = Integer.parseInt(custDataSpecial.getCustData2());
                    Prefecture prefecture = prefectureService.find(prefectureId);
                    if(prefecture != null) this.getCustomer().setCustCity(prefecture);
                }
            }
        }
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    @Override
    public void edit(ActionEvent event) {
        try {
            String custId = HTTPResReqUtil.getRequestParameter("custId");
            
            if(!NumberUtils.isDigits(custId)) return;
            if(Integer.parseInt(custId) < 0) return;
            
            this.setCustomer(this.issueCustomerService.find(Integer.valueOf(custId)));
            this.getCustomer().initSpecicalData();
        } catch(NumberFormatException e) {
            JsfUtil.addErrorMessage(e.getMessage());
            LOGGER.error("CustomerController.edit()", e.getMessage());
        }
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    @Override
    public void update(ActionEvent event) {
        try {
            if(this.getCustomer() == null || this.getCustomer().getCustId() == null) return;
            Object[] para = new Object[1];
            if(!_DoValidateCustomer(para, false)){
                return;
            }
            this.getCustomer().removeInvalidData();
            this.issueCustomerService.updateCustomer(this.getCustomer());
            mergeToIssue();
            
            // đóng dialog nếu đang mở dialog.
            try {
                RequestContext context = RequestContext.getCurrentInstance();
                context.closeDialog("UPDATE");
            } catch (Exception e) { }
            
        } catch(Exception e) {
            JsfUtil.addErrorMessage(e.getMessage());
            LOGGER.error("CustomerController.update()", e.getMessage());
        }
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void updateSpecialCust(int status, int typeOfScreen) {
        try {
            if(this.getCustomer() == null || this.getCustomer().getCustId() == null) return;
            Object[] para = new Object[1];
            if(!_DoValidateCustomer(para, true)){
                return;
            }
            
            this.getCustomer().setCustStatus(status); // trạng thái của khách hàng.
            this.manualPrefectureCustSpecial();
            this.getCustomer().smoothCustDataSpecInvalid();
            
            this.getCustomer().removeInvalidData();
            Customer custEdited = this.issueCustomerService.updateCustomer(this.getCustomer());
            mergeToIssue();
            
            if(typeOfScreen == 0) { // on dialog.
                try {
                    RequestContext context = RequestContext.getCurrentInstance();
                    context.closeDialog("UPDATE");
                } catch (Exception e) { }
                onEditSucess();
            } else {
                unHoldCustomer();
                projectController.goback();
                JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.delete.success", custEdited.getCustFirstKana()));
            }
        } catch(Exception e) {
            JsfUtil.addErrorMessage(e.getMessage());
            LOGGER.error("CustomerController.update()", e.getMessage());
        }
    }
    
    @SecureMethod(SecureMethod.Method.DELETE)
    @Override
    public void delete(ActionEvent event) {
        try {
            Customer cust = this.getCustomer();
            cust.removeInvalidData();
            
            this.issueCustomerService.removeCustomer(cust);
            this.issueController.getIssue().getHistorySpecials().remove(cust);
            
            // đóng dialog.
            try {
                RequestContext context = RequestContext.getCurrentInstance();
                context.closeDialog("DELETE");
            } catch (Exception e) { }
        } catch(Exception e) {
            JsfUtil.addErrorMessage(e.getMessage());
            LOGGER.error("CustomerController.delete()", e.getMessage());
        }
    }
    
    public void onEditSucess() {
        String custFirstKana = StringUtils.EMPTY;
        
        if(isSpecialCompanyFlag()) {
            List<CustDataSpecial> custDataSpecials = this.getCustomer().getCustDataSpecials();
            for(CustDataSpecial custDataSpecial : custDataSpecials) {
                if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_NAME) {
                    custFirstKana = custDataSpecial.getCustData1();
                    if(!StringUtils.isEmpty(custFirstKana)) break;
                }
            }
        } else {
            custFirstKana = this.getCustomer().getCustFirstKana();
        }
        
        JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.delete.success", custFirstKana));
    }
    public void onInsertSuccess() {
        String custFirstKana = StringUtils.EMPTY;
        
        if(isSpecialCompanyFlag()) {
            List<CustDataSpecial> custDataSpecials = this.getCustomer().getCustDataSpecials();
            for(CustDataSpecial custDataSpecial : custDataSpecials) {
                if(custDataSpecial.getCustType() == CustDataSpecial.CUST_SPECIAL_TYPE_NAME) {
                    custFirstKana = custDataSpecial.getCustData1();
                    if(!StringUtils.isEmpty(custFirstKana)) break;
                }
            }
        } else {
            custFirstKana = this.getCustomer().getCustFirstKana();
        }
        
        JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.create.success", custFirstKana));
    }
    
    public String getStatusOfSpecialCustomer(Integer custStatus) {
        if(custStatus == null) return StringUtils.EMPTY;
        if(custStatus == 1) return JsfUtil.getResource().message(companyLoginedId, ResourceUtil.BUNDLE_MSG ,"label.correction_application");
        if(custStatus == 2) return JsfUtil.getResource().message(companyLoginedId, ResourceUtil.BUNDLE_MSG ,"label.apply_deletion");
        if(custStatus == 3) return JsfUtil.getResource().message(companyLoginedId, ResourceUtil.BUNDLE_MSG ,"label.pending");
        if(custStatus == 4) return JsfUtil.getResource().message(companyLoginedId, ResourceUtil.BUNDLE_MSG ,"label.approval");
        if(custStatus == 5) return JsfUtil.getResource().message(companyLoginedId, ResourceUtil.BUNDLE_MSG ,"label.not_show");
        return StringUtils.EMPTY;
    }
    
    /**
     * Chuyen khach hang dang biet sang Tab customer cua phan issue
     * @param cust 
     * @param index 
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void moveToIssue(Customer cust, Integer index){
        Issue issue = issueController.getIssue();
        if(issue != null){
            if(!issue.getCustomerList().isEmpty()){
                issue.getCustomerList().set(index, cust);
            }else{
                issue.getCustomerList().add(cust);
            }
        }
    }
    private void mergeToIssue(){
        Issue issue = issueController.getIssue();
        if(issue != null){
            for(Customer c : issue.getCustomerList()){
                if(c.isSameCustTarget(this.getCustomer())){
                    this.issueController.getIssue().getHistorySpecials().add(this.getCustomer());
                    break;
                }
            }
        }
    }
    
    public void closeOpenDialog(boolean save) {
        Object[] para = new Object[1];
        para[0] = JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_ISSUE_NAME, "label.tab_customer_special", para);
        JsfUtil.getResource().putMessager(JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, save?"label.issue.add":"label.issue.update", para));
    }

    /** 顧客情報タプを追加
     * 「TEL、FAX、MAIL」を追加
     * @param flag
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void addTarget(Integer flag) {
        CustTargetInfo target = IssueUtil.createCustTargetInfo(this.getCustomer(), flag.shortValue(), null);
        getCustomer().getCustTargetInfoList().add(target);
    }
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void addSpecicalData(int type) {
        this.getCustomer().addNewCustDataSpecial(type);
    }
    
    /** 「TEL、FAX、MAIL」を削除
     * @param target 
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void removeTarget(CustTargetInfo target) {
        this.getCustomer().getCustTargetInfoList().remove(target);
    }
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void removeSpecicalData(int type, int custDataSpecialId) {
        this.getCustomer().removeCustDataSpecial(type, custDataSpecialId);
    }
    
    public List<CustDataSpecial> getByType(int type) {
        try {
            List<CustDataSpecial> custDataSpecials = new ArrayList<>();
            List<CustDataSpecial> mainCustDataSpecials = this.getCustomer().getCustDataSpecials();
            for(CustDataSpecial custDataSpecial : mainCustDataSpecials) {
                if(custDataSpecial.getCustType() == type)
                    custDataSpecials.add(custDataSpecial);
            }

            return custDataSpecials;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }
    
    private String getBaseQuery() {
        String sql_cust_special = "cust_special_id > 0";

        Integer cid = getCurrentCompanyId();
        if(cid == null) return sql_cust_special + " and 1=1";
        
        String base_sql = "company_id=" + cid + " and " + sql_cust_special + " and 1=1 ";
        return base_sql;
    }
    
    private void theNewsCustomerOnThreeDay() {
        Calendar now = Calendar.getInstance();
        Calendar piorThreeDay = Calendar.getInstance();
        
        piorThreeDay.add(Calendar.DAY_OF_WEEK, -3);
        now.add(Calendar.DAY_OF_WEEK, 1);
        
        SearchField cdlt = new SearchField();
        cdlt.setName("cust_created_time");//cust_created_time 
        cdlt.setOperator("<=");
        cdlt.setCondition("and");
        cdlt.setValue(DateUtil.getDateToString(now.getTime(), "yyyy-MM-dd"));
        
//        SearchField cdgt = new SearchField();
//        cdgt.setName("cust_created_time");
//        cdgt.setOperator(">=");
//        cdgt.setCondition("");
//        cdgt.setValue(DateUtil.getDateToString(piorThreeDay.getTime(), "yyyy-MM-dd"));
        
        List<SearchField> fields = new ArrayList<>();
        fields.add(cdlt);
//        fields.add(cdgt);
        
        SearchGroup group = new SearchGroup();
        group.setFilters(fields);
        group.setOperator("");
        List<SearchGroup> groups = new ArrayList<>();
        groups.add(group);
        
        this.searchDataJson = new Gson().toJson(groups);
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    @Override public void show(ActionEvent event) {}
    
    /**
     * Quyen show tab lich su khach hang
     * @param event 
     */
    @SecureMethod(SecureMethod.Method.CUSTOMER_HISTORY)
    public void history(ActionEvent event) {}
    
    /**
     * Quyen show tab khach hang dac biet
     * @param event 
     */
    @SecureMethod(SecureMethod.Method.CUSTOMER_SPECIAL)
    public void special(ActionEvent event) {}
    
    @SecureMethod(value=SecureMethod.Method.SEARCH) 
    public void reload() {}
    
    @Override
    protected void doSearch(SearchFilter filter) {
        // ban đầu vào màn hình danh sách kiểu tìm kiếm là normal.
        this.renderScriptHiddenSearchPanel = false;
        this.searchType = 0;
        this.keyqord = StringUtils.EMPTY;

        query = getBaseQuery() + " AND ";
        String advanceSearch = !StringUtils.isEmpty(filter.getQuery()) ? filter.getQuery() : "1=1";
        query = query + advanceSearch;
    }
    
    @SecureMethod(value=SecureMethod.Method.SEARCH)
    public void quickSearch() {
        this.renderScriptHiddenSearchPanel = true;
        this.searchType = 1;
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        theNewsCustomerOnThreeDay();
        search();
        specialCustomers = new CusSpecialLazyList(this);
        this.layout.setCenter("/modules/customer/special/index.xhtml");
        this.renderScriptHiddenSearchPanel = true;
    }
    
    @SecureMethod(SecureMethod.Method.DOWNLOAD)
    public void export(String fileName) {
        try {
            List<Map<String, String>> results = customerService.find(0, companyLoginedId, null, null, null, null, getQuery(),
                getSearchType(), getKeyqord(), getOperator(), UserModel.getLogined().getLanguage());
            Export exporter = new CustomerSpecialExportXsl(results, fileName);
            exporter.execute();
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
        }
    }
    
    public static final String[] CUSTOMER_DYNAMIC_FIELD_ADDITION = {"cust_created_time", "cust_updated_time", "cust_creator_name", "cust_updated_name"};
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public String getCustomerSpecialFieldJson() {
        JsonArray json = new JsonArray();
        
        // các field chính.
        List<DynamicColumn> custCols = SelectUtil.getCustomerColumns();

        // các field bổ sung.
        List<DynamicColumn> custDynamicColumnAddition = new ArrayList<>();
        for(String dynamicField : CUSTOMER_DYNAMIC_FIELD_ADDITION){
            custDynamicColumnAddition.add(new DynamicColumn(dynamicField,
                        JsfUtil.getResource().message(companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME ,"label." + dynamicField)));
        }
        custCols.addAll(custDynamicColumnAddition);
        
        custCols.forEach((t) -> { json.add(ais.getFieldJson(t)); });
        return new GsonBuilder().disableHtmlEscaping().create().toJson(json);
    }
    
    public void onBlurCustomerPost(AjaxBehaviorEvent event){
        UIInput input = (UIInput) event.getSource();
        if(input == null || input.getValue() == null) return;
        
        String zipCode = input.getValue().toString();
        if(StringUtils.isEmpty(zipCode)) return;

        String locale = UserModel.getLogined().getLanguage();
        ZipCode zc = zipCodeService.findByZipCode(zipCode, locale);
        if(zc == null || zc.getCodeId() == null) return;
        String cityKaneJii = zc.getCityKannji();
        List<Prefecture> listPrefecture = prefectureService.findCities(locale, cityKaneJii);
        if (!listPrefecture.isEmpty()) {
            this.getCustomer().setCustCity(listPrefecture.get(0));
        }
        if(StringUtils.isEmpty(this.getCustomer().getCustAddress())){
            this.getCustomer().setCustAddress(String.format("%s%s", zc.getAddressKannji(), zc.getDistrictKanj()));
        }
        if(StringUtils.isEmpty(this.getCustomer().getCustAddressKana())){
            this.getCustomer().setCustAddressKana(String.format("%s%s%s", zc.getCityKana(), zc.getAddressKana(), zc.getDistrictKana()));
        }
    }
    
    public void onBlurSpecialCustomerPost(AjaxBehaviorEvent event){
        UIInput input = (UIInput) event.getSource();
        if(input == null || input.getValue() == null) return;
        
        String szIndex = String.valueOf(input.getAttributes().get("input_post_index"));
        if(StringUtils.isEmpty(szIndex)) return;
        if(!NumberUtils.isNumber(szIndex)) return;
        Integer index = Integer.parseInt(szIndex);
        
        String zipCode = input.getValue().toString();
        if(StringUtils.isEmpty(zipCode)) return;

        String locale = UserModel.getLogined().getLanguage();
        ZipCode zc = zipCodeService.findByZipCode(zipCode, locale);
        if(zc == null || zc.getCodeId() == null) return;        
        Integer cusCityId=null;
        String address=null;
        String addressKana=null;
        String cityKaneJii = zc.getCityKannji();
        List<Prefecture> listPrefecture = prefectureService.findCities(locale, cityKaneJii);
        if (!listPrefecture.isEmpty()) {
            cusCityId = listPrefecture.get(0).getPrefectureId();
        }
        address = String.format("%s%s", zc.getAddressKannji(), zc.getDistrictKanj());
        addressKana = String.format("%s%s%s", zc.getCityKana(), zc.getAddressKana(), zc.getDistrictKana());
        List<CustDataSpecial> custDataSpecials = getByType(2);
        if(custDataSpecials != null) {
            for(int i=0; i< custDataSpecials.size(); i++) {
                if(i == index) {
                    if(cusCityId != null) custDataSpecials.get(i).setCustData2(String.valueOf(cusCityId));
                    if(address != null) custDataSpecials.get(i).setCustData3(String.valueOf(address));
                    if(addressKana != null) custDataSpecials.get(i).setCustData4(String.valueOf(addressKana));
                }
            }
        }
    }
    
    private boolean _DoValidateCustomer(Object[] para, boolean isCustSpec) {
        Boolean flag = true;
        
        if (this.getCustomer().getCustCooperationId() == null) {
            para[0] = JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.cust_cooperation_name", para);
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", para));
            flag = false;
        }
        
        if (StringUtils.isEmpty(this.getCustomer().getCustCode())) {
            para[0] = JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.cust_code", para);
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", para));
            flag = false;
        }
        
        if (this.getCustomer().getCustSpecialId() == null) {
            para[0] = JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_CUSTOMER_NAME, "label.cust_special_name", para);
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", para));
            flag = false;
        }
        /**
         * 漢字名チェック
         */
        if(!isCustSpec) {
            if (StringUtils.isBlank(this.getCustomer().getCustFirstHira())) {
                para[0] = JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.name_hira", para);
                JsfUtil.addErrorMessage(JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", para));
                flag = false;
            }
        } else {
            List<CustDataSpecial> custDataSpecials = getByType(1);
            if(custDataSpecials != null) {
                for(CustDataSpecial custDataSpecial : custDataSpecials) {
                    String nameKana = custDataSpecial.getCustData1();
                    if(StringUtils.isEmpty(nameKana)) {
                        para[0] = JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.name_hira", para);
                        JsfUtil.addErrorMessage(JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", para));
                        flag = false;
                    }
                }
            }
        }
        /**
         * カナ名チェック
         */
        if(!isCustSpec) {
            if (StringUtils.isBlank(this.getCustomer().getCustFirstKana())) {
                para[0] = JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.name_kana", para);
                JsfUtil.addErrorMessage(JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", para));
                flag = false;
            }
        } else {
            List<CustDataSpecial> custDataSpecials = getByType(1);
            if(custDataSpecials != null) {
                for(CustDataSpecial custDataSpecial : custDataSpecials) {
                    String nameKana = custDataSpecial.getCustData3();
                    if(StringUtils.isEmpty(nameKana)) {
                        para[0] = JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.name_kana", para);
                JsfUtil.addErrorMessage(JsfUtil.getResource().message(this.companyLoginedId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", para));
                flag = false;
                    }
                }
            }
        }
        
        return flag;
    }
    
    @Setter @Getter private UploadedFile uploadedFile;
    public void fileUploadListener(FileUploadEvent event) { this.uploadedFile = event.getFile(); }
    
    @SecureMethod(value = SecureMethod.Method.UPLOAD)
    public void importData() throws IOException{
        if(this.uploadedFile == null){
            JsfUtil.addErrorMessage("エラーがあるので、完了ができない。");
            return;
        }
        try {
            String language = UserModel.getLogined().getLanguage();
            Company companyLogined = UserModel.getLogined().getCompany();
            int loginId = UserModel.getLogined().getUserId();

            Import i = new CustomerImport(prefectureService, customerService, menteService, loginId, companyLogined, language, this.uploadedFile.getFileName());
            i.execute(this.uploadedFile.getInputstream());
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(companyLoginedId, ResourceUtil.BUNDLE_SYSTEM, "label.system.label.success"));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            
            String error = e.getMessage();
            if(error.contains("ERROR_CUST_IMPORT")) {
                JsfUtil.addErrorMessage(error.split(":")[1]);
            } else {
                JsfUtil.addErrorMessage(JsfUtil.getResource().message(companyLoginedId, ResourceUtil.BUNDLE_SYSTEM, "label.system.label.error"));
            }
        }
    }
    
    public void handleCloseDialog(CloseEvent event) {
        uploadedFile = null;
    }

    public String breakDataToNewLine(String value) {
        if(StringUtil.isNullOrEmpty(value)
                || "null".equalsIgnoreCase(value)) return "";
        if(!value.contains(HTML.CONCAT_BR)) return value;
        return value.replaceAll(HTML.CONCAT_BR, "\n");
    }
    
    public String breakDataToNewLine(Map<String, String> row, String col, String type) {
        if(row == null || row.isEmpty()) return "";
        if(StringUtils.isEmpty(col)) return  "";
        return breakDataToNewLine(row.get(col));
    }


    private Integer custIdViewParamInDialog;

    public Integer getCustIdViewParamInDialog() {
        return custIdViewParamInDialog;
    }

    public void setCustIdViewParamInDialog(Integer custIdViewParamInDialog) {
        this.custIdViewParamInDialog = custIdViewParamInDialog;
    }
    
    public Customer getCustomer() {
        String custId = HTTPResReqUtil.getRequestParameter("custId");
        if(StringUtils.isEmpty(custId)) return new Customer();//throw new IllegalArgumentException("The cusId parameter is does not exsits.");
        
        Integer icustId = NumberUtils.toInt(custId);
        if(icustId == null) return new Customer();//throw new IllegalArgumentException("The cusId parameter is does not exsits.");
        
        if(!customerHolder.containsKey(companyLoginedId)) return new Customer();//throw new IllegalArgumentException("The customer holder is not have company " + companyLoginedId);
        
        return customerHolder.get(companyLoginedId).get(icustId);
    }
    public void setCustomer(Customer p_customer) {
        Integer maskCustId = p_customer.getCustId();
        
        if(maskCustId == null) {
            maskCustId = 0 - (customerHolder.size() + 1);
            p_customer.setCustId(maskCustId);
        }
        
        if(customerHolder.containsKey(companyLoginedId)) {
            Map<Integer, Customer> m = customerHolder.get(companyLoginedId);
            m.put(maskCustId, p_customer);
        } else {
            Map<Integer, Customer> m = new HashMap();
            m.put(maskCustId, p_customer);
            customerHolder.put(companyLoginedId, m);
        }
//        this.customer = p_customer;
//        custIdViewParamInDialog = maskCustId;
    }

    public void unHoldCustomer() {
        try {
            Customer currentCust = getCustomer();
            if(currentCust == null) return;
            if(currentCust.getCustId() == null) return;
            
            customerHolder.get(companyLoginedId).remove(currentCust.getCustId());
        }catch(Exception e){
            //Unhold issue error exception, do nothing
        }
    }
    
    public void unHoldAllCustomer() {
        try {
            customerHolder.get(companyLoginedId).clear();
        } catch (Exception e) {
            //Unhold issue error exception, do nothing
        }
    }
}
