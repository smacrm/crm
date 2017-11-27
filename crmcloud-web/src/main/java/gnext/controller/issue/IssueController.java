package gnext.controller.issue;

import com.google.gson.Gson;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.issue.CustTargetInfo;
import gnext.bean.issue.Issue;
import gnext.bean.Prefecture;
import gnext.bean.ZipCode;
import gnext.bean.automail.SimpleAutoMail;
import gnext.bean.mente.MenteItem;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.controller.customize.RenderController;
import gnext.bean.issue.Customer;
import gnext.bean.issue.Escalation;
import gnext.bean.issue.IssueAttachment;
import gnext.bean.issue.IssueInfo;
import gnext.bean.issue.IssueRelated;
import gnext.bean.mente.Products;
import gnext.bean.project.DynamicColumn;
import gnext.bean.softphone.Twilio;
import gnext.controller.common.LocaleController;
import gnext.controller.common.LoginController;
import gnext.model.DialogObject;
import gnext.model.authority.UserModel;
import gnext.model.customize.Tab;
import gnext.model.search.SearchFilter;
import gnext.multitenancy.service.MultitenancyService;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.GroupService;
import gnext.service.MemberService;
import gnext.service.PrefectureService;
import gnext.service.config.ConfigService;
import gnext.service.issue.IssueEscalationService;
import gnext.service.issue.IssueRelatedService;
import gnext.service.issue.IssueService;
import gnext.rest.iteply.bean.TelCustomer;
import gnext.service.ZipCodeService;
import gnext.service.automail.AutoMailService;
import gnext.service.issue.IssueAttachmentService;
import gnext.service.issue.IssueCustomerService;
import gnext.service.issue.IssueEscalationSampleService;
import gnext.service.issue.IssueStatusHistoryService;
import gnext.service.label.LabelService;
import gnext.service.mail.MailAccountService;
import gnext.service.mente.MenteService;
import gnext.service.mente.ProductService;
import gnext.service.softphone.TwilioService;
import gnext.util.DateUtil;
import gnext.util.HTTPClient;
import gnext.util.HTTPResReqUtil;
import gnext.util.IssueUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.SelectUtil;
import gnext.util.StatusUtil;
import gnext.util.StringUtil;
import gnext.util.WebFileUtil;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.COMPANY_TYPE;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.primefaces.context.RequestContext;
import org.primefaces.event.MenuActionEvent;
import org.primefaces.event.TabCloseEvent;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author HUONG
 */
@ManagedBean(name = "issueController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.ISSUE)
public class IssueController extends AbstractController {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(IssueController.class);

    @ManagedProperty(value = "#{layout}") @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{renderController}") @Getter @Setter private RenderController renderController;
    @ManagedProperty(value = "#{projectController}") @Getter @Setter private ProjectController projectController;
    @ManagedProperty(value = "#{localeController}") @Getter @Setter private LocaleController localeController;
    @ManagedProperty(value = "#{loginController}") @Getter @Setter private LoginController loginController;
    @ManagedProperty(value = "#{ais}") @Getter @Setter private AdvanceIssueSearchController ais;
    @ManagedProperty(value = "#{issueGlobalStatus}") @Getter @Setter private IssueGlobalStatus issueGlobalStatus;

    @EJB private MultitenancyService multitenancyService;
    @EJB private PrefectureService prefectureService;
    @EJB private ConfigService configService;
    @EJB private IssueRelatedService issueRelatedService;
    @EJB private IssueEscalationService issueEscalationService;
    @EJB private ZipCodeService zipCodeService;

    @EJB @Getter @Setter private IssueService issueService;
    @EJB @Getter @Setter private MenteService menteService;
    @EJB @Getter @Setter private ProductService productService;
    @EJB @Getter @Setter private MemberService memberService;
    @EJB @Getter @Setter private GroupService groupService;
    @EJB @Getter @Setter private TwilioService twilioService;
    @EJB @Getter @Setter private IssueAttachmentService issueAttachService;
    @EJB @Getter @Setter private IssueCustomerService issueCustomerService;
    @EJB private IssueStatusHistoryService issueStatusHistoryService;
    @EJB private AutoMailService autoMailService;
    @EJB private MailAccountService mailAccountService;
    @EJB private IssueEscalationSampleService issueEscalationSampleService;
    @EJB private LabelService labelService;

    @Getter @Setter private List<Prefecture> prefectures;
    @Getter @Setter private List<Products> products;
    
    @Setter private Integer pageId;
    @Getter @Setter private String backURL;

    @Getter @Setter private Map<String, List<SelectItem>> select;
    @Getter @Setter private List<String> fields;
    @Getter @Setter private Map<String, MenuModel> selects;
    @Getter @Setter private Map<Integer, Issue> issueMapper = new HashMap<>();
    @Getter private Integer lastIssuerInsertedId = 0;

    @Getter @Setter private Map<String, Integer> issueIndexMap = new HashMap<>();
    @Getter @Setter private Integer issueIdx;
    @Getter @Setter private boolean issueCopy;
    @Getter @Setter private String issueViewRelatedCode;
    @Getter @Setter private String issueRelatedComment;
    @Getter @Setter private int companyId;
    
    // Using for elastic index
    List<String> avaiableColumns = new ArrayList<>();
    @Getter @Setter private List<Issue> riskSensorList;
    @Getter @Setter private Collection<Tab> customizeForm; // Luu du lieu Customize Form data (Layout)
    
    @Getter @Setter private Integer customerViewIndex = 0;
    @Getter @Setter private Integer infoViewIndex = 0;
    
    private Integer previousStatusId;
    
    @Getter final private List<String> callbackIssueUpdated = new ArrayList<>();
    @Getter final private List<String> callbackIssueCreated = new ArrayList<>();
    
    public Issue getIssue(){
        Integer issueId = NumberUtils.toInt(HTTPResReqUtil.getRequestParameter("id"), 0);
        return getIssue(issueId);
    }
    
    public Issue getIssue(int issueId){
        if(!issueMapper.containsKey(issueId)){
            issueMapper.put(issueId, new Issue(issueId));
        }
        return issueMapper.get(issueId);
    }

    public Integer getPageId() {
        if(this.pageId == null){
            this.pageId = this.issueService.getCustomizePageId("IssueController", getCompanyId());
        }
        return pageId;
    }
    
    @PostConstruct
    public void init() {
        this.setCompanyId(UserModel.getLogined().getCompanyId());
        List<DynamicColumn> availableDynamicColumns =  this.projectController.getListAllVisibleColumn();
        availableDynamicColumns.forEach((dc) -> {
            avaiableColumns.add(dc.getId());
        });
        
        /** !! Note !!
        * Thuc hien load context data lan dau ten khi tao issue
        * @hungpd 2017/05/19
        */
        loadContextData();
    }
    
    /**
     * Tao du lieu mau, danh sach cac dropdown value cho issue
     */
    public void loadContextData() {
        try {
            startMeasure("loadContextData");
            String idx = getParameter("issueIdx");
            String issueId = getParameter("id");
            if("notNext".equals(idx)) {
                this.setIssueIdx(null);
            } else if(NumberUtils.isDigits(idx)) {
                this.setIssueIdx(Integer.valueOf(idx));
                issueIndexMap.put(issueId, issueIdx);
            }
            
            loadPrefectureAndZipcodeContext();
            loadAllMemberContext();
            loadMenteContext();
            loadUserGroupContext();
            
            stopMeasure("loadContextData"); 
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    public void loadPrefectureAndZipcodeContext() {
        String language = UserModel.getLogined().getLanguage();
        this.prefectures = prefectureService.findCities(language);
        this.products = productService.getAllProducts(this.companyId);
    }
    
    public void loadAllMemberContext() throws Exception {
        if(this.selects != null) this.selects.clear();
        else this.selects = new HashMap<>();
        
        /** Lấy danh sách groups từ tenancy */
        List<Group> groupList = multitenancyService.findAllGroupUnderSlave(UserModel.getLogined().getCompanyId());
        
        /** 作成者 */
        MenuModel creatorPerson = SelectUtil.getSelectGroupMember("#{issueController.setCreatorPerson}", "label_issue_creator_id", groupList);
        if(creatorPerson != null) this.selects.put(COLS.CREATOR_ID, creatorPerson);
        
        /** 受付者 */
        MenuModel receivePerson = SelectUtil.getSelectGroupMember("#{issueController.setReceivePerson}", "label_issue_receive_person_id", groupList);
        if(receivePerson != null) this.selects.put(COLS.PERSON_ID, receivePerson);
        
        /** メンバーリスト */
        MenuModel members = SelectUtil.getSelectGroupMember(COLS.USERS, "", groupList);
        if(members != null) this.selects.put(COLS.USERS, members);
    }
    
    public void loadMenteContext() throws Exception  {
        String language = UserModel.getLogined().getLanguage();
        
        if(this.select != null) this.select.clear();
        else this.select = new HashMap<>();
        
        // Tải danh sách mente cho products.
        SelectUtil.addProductSelectItems(this.select, COLS.PRODUCTS, this.products);
        
        /** メンテがリストを追加 */
        List<MenteItem> allMenteItemList = this.menteService.getAllLevels(this.companyId);
        SelectUtil.addSelectItems(this.select, allMenteItemList, language);
    }
    
    public void loadUserGroupContext(){
        /** グループプルダウンがリストを追加 */
        SelectUtil.addMemberGroupSelectItems(this.select, COLS.GROUP);
        /** メンバープルダウンがリストを追加 */
        SelectUtil.addMemberGroupSelectItems(this.select, COLS.USER);
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void setCreatorPerson(MenuActionEvent e){
        String strMemberId = e.getMenuItem().getRel();
        if(NumberUtils.isNumber(strMemberId)){
            this.getIssue().setCreatorId(getMemberFromGroupList(NumberUtils.toInt(strMemberId), UserModel.getLogined().getCompany().getGroupList()));
        }
    }
    
    private Member getMemberFromGroupList(Integer memberId, List<Group> groups){
        for(Group g : groups){
            for(Member m : g.getMembers()){
                if(m.getMemberId().equals(memberId)) return m;
            }
            Member m = getMemberFromGroupList(memberId, g.getChilds());
            if(m != null) return m;
        }
        return null;
    }
    
    /**
     * Kiem tra user dang dang nhap co quyen view issue hay khong
     * @param issue
     * @return 
     */
    public boolean hasViewRole(Issue issue){
        Integer currentLoggedInMemberId = UserModel.getLogined().getUserId();
        Integer memberId = issue.getCreatorId().getMemberId();
        if(issue.getCreatorId().getMemberManagerFlag() == 1){
            Integer currentLoggedInGroupId = UserModel.getLogined().getGroupId();
            Integer createdGroupId = issue.getCreatorId().getGroup().getGroupId();
            return currentLoggedInMemberId.equals(memberId) || currentLoggedInGroupId.equals(createdGroupId);
        }else{
            return currentLoggedInMemberId.equals(memberId);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void setReceivePerson(MenuActionEvent e){
        String strMemberId = e.getMenuItem().getRel();
        if(NumberUtils.isNumber(strMemberId)){
            this.getIssue().setIssueReceivePerson(getMemberFromGroupList(NumberUtils.toInt(strMemberId), UserModel.getLogined().getCompany().getGroupList()));
        }
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    public void show() {
        String issueId = getParameter("id");
        if(!NumberUtils.isDigits(issueId)) return;
        String _issueIdx = getParameter("issueIdx");
        if(NumberUtils.isDigits(_issueIdx)) this.setIssueIdx(Integer.valueOf(_issueIdx));
        show(Integer.valueOf(issueId));
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    public void show(Integer issueId) {
        try {
            if(!loadIssue(issueId)) return;
            /** TODOタプ表示設定 */
            String tabId = getParameter("issueActiveTab");
            if(!StringUtils.isBlank(tabId)) this.getIssue().setIssueActiveTab(tabId);
            layout.setCenter("/modules/issue/show.xhtml");
            
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public String outcome(Integer issueId) throws UnsupportedEncodingException{
        return "/index.xhtml?id="+issueId+"&s="+layout.getCurrentEaseyEncrypt()+"&faces-redirect=true";
    }
    
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public String outcome(Integer issueId, String params) throws UnsupportedEncodingException{
        return "/index.xhtml?id="+issueId+"&s="+layout.getCurrentEaseyEncrypt()+"&faces-redirect=true" + params;
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    public String skip(String flag) throws UnsupportedEncodingException {
        if(this.ais == null) {
            return null;
        }
        Map<String, Object> obj;
        Integer idx = NumberUtils.toInt(getParameter("issueIdx"), 0);
        this.setIssueIdx(idx);
        obj = this.ais.getIssues().get(idx);
        if(obj != null){
            if(!NumberUtils.isDigits(String.valueOf(obj.get("issue_id")))) return null;
            Integer nextId = Integer.valueOf(String.valueOf(obj.get("issue_id")));
            //this.setIssueIdx(idx);
            if("show".equals(flag)) {
                show(nextId);
            } else if("edit".equals(flag)) {
                edit(nextId);
            }
            return outcome(nextId);
        }
        return null;
    }

    @SecureMethod(SecureMethod.Method.UPDATE)
    public String updateSkip() throws UnsupportedEncodingException {
        if(update()){
            return skip("edit");
        }
        return "";
    }
    
    /**
     * Load customize data for edit or create new issue
     */
    private void loadCustomizeData(){
        startMeasure("loadCustomizeData");
        if(this.getIssue() != null){
            if(this.getIssue().getIssueId() == null || this.getIssue().getIssueId() == 0){ //Truong hop tao moi issue
                this.setCustomizeForm(renderController.tabsWithData(this.getPageId(), 2, 0, new HashMap<>()));
            }else if(this.getIssue().getIssueId() == -1){ //Truong hop du lieu tu email hoac duplicate issue
                this.setCustomizeForm(renderController.tabsWithData(this.getPageId(), 2, 0, this.getIssue().getCustomizeDataMapping()));
            }else{ //Truong hop edit issue dang ton tai
                this.setCustomizeForm(renderController.tabs(this.getPageId(), 2, this.getIssue().getIssueId()));
            }
        }
        stopMeasure("loadCustomizeData");
    }
    
    /**
     * Load customize with issue id
     * @param issueId 
     */
    private void loadCustomizeData(Integer issueId){
        startMeasure("loadCustomizeData");
        if(issueId == null || issueId == 0){
            this.setCustomizeForm(renderController.tabsWithData(this.getPageId(), 2, 0, new HashMap<>()));
        }else{
            this.setCustomizeForm(renderController.tabs(this.getPageId(), 2, issueId));
        }
        stopMeasure("loadCustomizeData");
    }

    @SecureMethod(SecureMethod.Method.CREATE)
    @Override
    public void create(ActionEvent event) {
        try {
            /** プルダウンリスト再初期 */
            loadContextData();
            loadNewIssue();
            loadCustomizeData();
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        layout.setCenter("/modules/issue/create.xhtml");
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    public void duplicate(final Issue issue) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException{
        Issue _issue = new Issue();
        BeanUtils.copyProperties(_issue, issue);
        Integer oldIssueId = issue.getIssueId();
        issueMapper.put(-1, _issue);
        
        _issue.setIssueId(null);
        _issue.setDuplicateId(oldIssueId);
        _issue.setIssueCode(DateUtil.getIssueCodeNow());
        _issue.setIssueViewCode("");
        
        // clear data list
        _issue.getRelatedCases().clear();
        _issue.getIssueTodoList().clear();
        _issue.getEscalationList().clear();
        _issue.getIssueAttachmentList().clear();
        
        // reset meta data
        _issue.setIssueAuthorizerId(null);
        _issue.setIssueReceivePerson(UserModel.getLogined().getMember());
        _issue.setIssueReceiveDate(DateUtil.now());
        
        _issue.setCreatorId(UserModel.getLogined().getMember());
        _issue.setCreatedTime(DateUtil.now());
        
        _issue.setUpdatedId(UserModel.getLogined().getMember());
        _issue.setUpdatedTime(DateUtil.now());
        
        List<SelectItem> publics = this.select.get(COLS.PUBLIC);
        if(publics != null && !publics.isEmpty() && NumberUtils.isDigits(publics.get(0).getValue().toString())) {
            _issue.setIssuePublicId((MenteItem) publics.get(0).getValue());
        }
        _issue.getEscalations().clear();
        _issue.getHistorySpecials().clear();
        _issue.getHistoryCustomers().clear();
        
        //reset customer view index
        this.customerViewIndex = 0;
        this.infoViewIndex = 0;
        
        //load customize data
        //JsfUtil.executeClientScript("reloadCustomerList()");
        this.setCustomizeForm(renderController.tabs(this.getPageId(), 2, oldIssueId));
        layout.setCenter("/modules/issue/create.xhtml");
    }

    @SecureMethod(SecureMethod.Method.CREATE)
    public void createIssueEmail(Issue issue) {
        try {
            if(issue == null) return;
            issue.setSource("manual");
            
            this.issueMapper.put(-1, issue);
            issue.setIssueId(-1);
            loadContextData();
            loadCustomizeData();
            reLoadCustomers(issue);
//            addCountRiskSensor();
            
        } catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
        layout.setCenter("/modules/issue/create.xhtml");
    }

    /**
     * Thuc hien luu du lieu vao DB (CREATE)
     * @param notify 
     * @return  
     */
    @SecureMethod(SecureMethod.Method.CREATE)
    public boolean insert(boolean notify) {
        return insert(this.getIssue(), notify);
    }
    /**
     * Thuc hien luu du lieu vao DB (CREATE)
     * @param issue
     * @param notify 
     * @return  
     */
    @SecureMethod(SecureMethod.Method.CREATE)
    public boolean insert(Issue issue, boolean notify) {
        try {
            if(chkIssueExists(issue, SecureMethod.Method.CREATE.toString())) return false;
            
            /** 受付情報作成 */
            issue.setIssueCode(DateUtil.getIssueCodeNow());
            issue.setIssueDeleted(StatusUtil.UN_DELETED);

            IssueUtil.removeInvalidData(issue);
            
            /** Add issue attachment */
            Twilio twilio = null;
            if("manual".equals(issue.getSource())){
                if(!StringUtils.isEmpty(issue.getTwilioCallSid()) && (twilio = twilioService.getByCallId(issue.getTwilioCallSid())) != null){
                    IssueAttachment issueAttachment = new IssueAttachment();
                    issueAttachment.setCategory(null);
                    issueAttachment.setIssue(issue);
                    issueAttachment.setTwilio(twilio);
                    issueAttachment.setShareFlag(false);
                    issueAttachment.setExpireFlag(false);
                    issueAttachment.setAttachmentDeleted(StatusUtil.UN_DELETED);
                    issueAttachment.setCreator(UserModel.getLogined().getMember());
                    issueAttachment.setCreatedTime(DateUtil.now());
                    issueAttachment.setUpdatedId(UserModel.getLogined().getUserId());
                    issueAttachment.setUpdatedTime(DateUtil.now());
                    issue.getIssueAttachmentList().add(issueAttachment);
                }
            }
            /** 受付情報に追加 */
            issue = this.issueService.createIssue(issue, avaiableColumns, Arrays.asList(localeController.getAvailableLocales()));
            
            // Saving customize for temporary
            saveCusomizeDataTmp(issue);
            
            /** 受付カスタマイズ情報に追加 */
            if(issue.getIssueId() != null && issue.getIssueId() > 0) {
                this.renderController.saveFormData(this.getPageId(), 2, issue.getIssueId(), issue.getCustomizeDataMapping());
            }
            
            /**
             * Update Twilio save reference
             */
            if("manual".equals(issue.getSource()) && twilio != null){
                twilio.setIssue(issue);
                twilioService.edit(twilio);
            }
            
            /** 受付情報を保存成功した場合 */
            if("manual".equals(issue.getSource())){ // Truong hop event == null la TH bulk insert
                JsfUtil.getResource().alertMsgInfo("label.page.issue", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.add", ResourceUtil.BUNDLE_ISSUE_NAME);
                
                issueMapper.put(issue.getIssueId(), issue);
                //remove saved id
                if("-1".equals(getParameter("id"))){
                    issueMapper.remove(-1);
                }else if("0".equals(getParameter("id"))){
                    issueMapper.remove(0);
                }
                lastIssuerInsertedId = issue.getIssueId();
                
                issueStatusHistoryService.push(issue, issue.getIssueStatusId().getItemId(), issue.getIssueStatusId().getItemId(), UserModel.getLogined().getMember());
                
                //reload customize data
                loadCustomizeData(lastIssuerInsertedId);
                layout.setCenter("/modules/issue/show.xhtml");
            }
            
            // Process callback actions
            executeCallback(issue, callbackIssueCreated, notify);
            
            return true;
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
            if(notify) { // Truong hop event == null la TH bulk insert
                JsfUtil.getResource().alertMsg("label.page.issue", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.not.add", ResourceUtil.BUNDLE_ISSUE_NAME);
            }
        }
        return false;
    }
    
    private void saveCusomizeDataTmp(Issue issue){
        Map<String, String[]> requestParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap();
        Pattern r = Pattern.compile(StringUtil.FIELD_ID_PATTERN);
        for (Map.Entry<String, String[]> field : requestParams.entrySet()) {
            String key = field.getKey();
            String[] values = field.getValue();
            Matcher m = r.matcher(key);
            if(m.find() && !isCustomizeValueEmpty(values)){
                String id = m.group(1);
                String value = getCustomizeFieldValue(values);
                if(NumberUtils.isNumber(id)){
                    Integer itemId = Integer.parseInt(id);
                    issue.getCustomizeDataMapping().put(itemId, value);
                }
            }
        }
    }
    
    private boolean isCustomizeValueEmpty(String[] values){
        return values.length == 1 && StringUtils.isEmpty(values[0]);
    }

    private String getCustomizeFieldValue(String[] values){
        if(values.length == 1) return values[0];
        return new Gson().toJson(values);
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void edit() {
        String issueId = getParameter("id");
        if(!NumberUtils.isDigits(issueId)) {
           issueId = String.valueOf(this.getIssue().getIssueId());
        }
        if(!NumberUtils.isDigits(issueId)) return;
        
        String _issueIdx = getParameter("issueIdx");
        if(NumberUtils.isDigits(_issueIdx)) this.setIssueIdx(Integer.valueOf(_issueIdx));
        
        edit(NumberUtils.toInt(issueId));
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void edit(Integer issueId) {
        try {
            startMeasure("editIssue");
            if(issueId < -1) return;
            
            // Kiem tra trang thai, neu issue dang bi hold boi nguoi khac -> chuyen sang man hinh view
            if(issueGlobalStatus.isEditing(companyId, issueId) && !issueGlobalStatus.isEditingByMember(companyId, issueId, UserModel.getLogined().getUserId())){
                show(issueId);
                return;
            }

            try {
                if(!loadIssue(issueId)) {
                    return;
                }
                /** TODOタプ表示設定 */
                String tabId = getParameter("issueActiveTab");
                if(!StringUtils.isBlank(tabId) && this.getIssue().getIssueActiveTab() == null) {
                    this.getIssue().setIssueActiveTab(tabId);
                }
            } catch(NumberFormatException e) {
                LOGGER.error(e.getMessage());
            }
            stopMeasure("editIssue");
            holdIssue(issueId);
            layout.setCenter("/modules/issue/edit.xhtml");
            
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    /**
     * Set issue status to global hold
     * @param issueId 
     */
    public void holdIssue(Integer issueId){
        issueGlobalStatus.putEditing(companyId, issueId, UserModel.getLogined().getMember());
    }
    
    /**
     * remove issue from global status hold
     */
    public void unHoldIssue(){
        try{
            issueGlobalStatus.popEditing(companyId, this.getIssue().getIssueId());
        }catch(Exception e){
            //Unhold issue error exception, do nothing
        }
    }

    /**
     * Thuc hien luu du lieu vao DB (UPDATE)
     * @return  
     */
    @SecureMethod(SecureMethod.Method.UPDATE)
    public boolean update() {
        return update(true);
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public boolean update(boolean notify) {
        return update(this.getIssue(), notify);
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public boolean update(Issue issue, boolean notify) {
        try {
            
            if(chkIssueExists(issue, SecureMethod.Method.UPDATE.toString())) return false;
            
            /** 受付情報に更新 */
            IssueUtil.removeInvalidData(issue);
            if("manual".equals(issue.getSource())){
                issue.setUpdatedId(UserModel.getLogined().getMember());
                issue.setUpdatedTime(DateUtil.now());
            }
            issue =this.issueService.editIssue(issue, avaiableColumns, Arrays.asList(localeController.getAvailableLocales()), UserModel.getLogined().getLanguage());
            
            /**
             * Saving customize for temporary
             */
            saveCusomizeDataTmp(issue);
            
            /** 受付カスタマイズ情報に更新 */
            if(issue.getIssueId() != null && issue.getIssueId() > 0) {
                this.renderController.saveFormData(this.getPageId(), 2, issue.getIssueId(), issue.getCustomizeDataMapping());
            }
            
            if("manual".equals(issue.getSource())){
                /** insert history by issue status */
                if(issue.getStatusHistory().isEmpty()){
                    issueStatusHistoryService.push(issue, issue.getIssueStatusId().getItemId(), issue.getIssueStatusId().getItemId(), UserModel.getLogined().getMember());
                }else{
                    if(previousStatusId != null && !previousStatusId.equals(issue.getIssueStatusId().getItemId())){
                        issueStatusHistoryService.push(issue, previousStatusId, issue.getIssueStatusId().getItemId(), UserModel.getLogined().getMember());
                    }
                }

                reLoadCustomers(issue);
                
                //reload customize data
                loadCustomizeData();
                
                layout.setCenter("/modules/issue/show.xhtml");
                
                unHoldIssue();
            }
            
            // Process callback actions
            executeCallback(issue, callbackIssueUpdated, notify);
            
            if(notify){
                JsfUtil.getResource().alertMsgInfo("label.page.issue", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.update", ResourceUtil.BUNDLE_ISSUE_NAME);
            }
            
//            Integer issueIndex = issueIndexMap.get(String.valueOf(issue.getIssueId()));
//            if(issueIndex != null){
//                this.setIssueIdx(issueIndex);
//            }
            
            return true;
        } catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }
    
    private void executeCallback(Issue issue, List<String> callbacks, boolean notify){
        callbacks.forEach((command) -> {
            try{
                command = StringUtils.replace(command, "{ISSUE_ID}", issue.getIssueId().toString());
                System.out.println("Execute callback: " + command);
                JsfUtil.executeJsfCommand(command);
            }catch(Exception e){
                LOGGER.error(e.getMessage(), e);
            }
        });
        callbacks.clear();
        
        if(notify){ // neu tao email bang cach thong thuong, khong phai import, thi thuc hien gui mail
            try{
                // Xu ly gui mail tu dong cho issue
                List<SimpleAutoMail> data = autoMailService.findRequiredSendWithIssue(UserModel.getLogined().getCompanyId(), issue.getIssueId());
                data.forEach((item) -> {
                    gnext.controller.mail.AutoMailService.run(item, configService, mailAccountService, autoMailService, issueService, issueEscalationSampleService, labelService);
                });
            }catch(Exception e){
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @SecureMethod(SecureMethod.Method.DELETE)
    @Override
    public void delete(ActionEvent event) {
        try {
            IssueUtil.removeInvalidData(this.getIssue());
            this.issueService.remove(this.getIssue());
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.delete.success", this.getIssue().getIssueViewCode()));
        } catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
        projectController.loadProjectList();
        layout.setCenter("/modules/project/issue_list.xhtml");
    }

    @SecureMethod(SecureMethod.Method.PRINT)
    public void print(ActionEvent event) {
//        try {
//            if(chkIssueExists(SecureMethod.Method.PRINT.name())) return;
//        } catch(NumberFormatException e) {
//            logger.error(e.getMessage());
//        }
    }

    public void call() {
        try {
            if(this.getIssue() == null || this.getIssue().getIssueId() == null) return;
            String url = HTTPResReqUtil.getHostContext();
            HTTPClient client = new HTTPClient(url);
            TelCustomer customer = new TelCustomer();
            client.call(customer);
        } catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void backTo(ActionEvent event) {
        String back = null;
        try {
            back = HTTPResReqUtil.getRequestParameter("backURL");
            if(StringUtils.isBlank(back)) {
                back = "/modules/project/list.xhtml";
            }
        } catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
        layout.setCenter(back);
    }

    //AjaxBehaviorEvent event
    public void onComplete() {
        layout.setCenter("/modules/project/list.xhtml");
    }

    /**
     * 関連受付情報を追加
     */
    @SecureMethod(value=SecureMethod.Method.RELATED)
    public void saveRelatedCases() {
        try {
            if(this.getIssue() == null || this.getIssue().getIssueId() == null) {
                return;
            }
            if(StringUtils.isBlank(this.getIssueViewRelatedCode())) {
                JsfUtil.getResource().alertMsg("label.issue_code", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }
            if(issueViewRelatedCode.equals(String.valueOf(this.getIssue().getIssueViewCode()))){
                JsfUtil.getResource().alertMsg("label.issue_code", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.repeat.code", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }
            Issue isExists = this.issueService.findByIssueViewCode(this.companyId, this.getIssueViewRelatedCode());
            if(isExists == null || isExists.getIssueId() == null) {
                JsfUtil.getResource().alertMsg("label.issue_code", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.not.exist", ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }

            IssueRelated rel = this.issueRelatedService.getRelatedIdAndIssueId(this.getIssue().getIssueId(), isExists.getIssueId());
            if(rel == null || rel.getRelatedId() <= 0) {
                IssueRelated related = new IssueRelated();
                related.setIssueId(this.getIssue());
                related.setIssueRelatedId(isExists);
                related.setIssueRelatedComment(this.issueRelatedComment);
                related.setCreatorId(UserModel.getLogined().getUserId());
                related.setCreatedTime(DateUtil.now());
                related.setUpdatedId(UserModel.getLogined().getUserId());
                related.setUpdatedTime(DateUtil.now());
                related = this.issueRelatedService.create(related);

                this.getIssue().getRelatedCases().add(related);
                JsfUtil.addSuccessMessage(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MSG, "msg.action.create.success", JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MSG, "label.tab_related_cases")));
            } else {
                rel.setIssueRelatedComment(this.issueRelatedComment);
                rel.setUpdatedId(UserModel.getLogined().getUserId());
                rel.setUpdatedTime(DateUtil.now()); 
                this.issueRelatedService.edit(rel);

                for(int i = 0; i < this.getIssue().getRelatedCases().size(); i++){
                    if (this.getIssue().getRelatedCases().get(i).getRelatedId().equals(rel.getRelatedId())) {
                        this.getIssue().getRelatedCases().set(i, rel);
                        break;
                    }
                }

                JsfUtil.addSuccessMessage(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MSG, "msg.action.update.success", JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MSG, "label.tab_related_cases")));
            }

            RequestContext context = RequestContext.getCurrentInstance();
            context.closeDialog(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 関連受付情報を削除
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void deleteRelatedCases() {
        try {
            String ids = getParameter("relatedId");
            if(!NumberUtils.isDigits(ids) || this.getIssue().getRelatedCases() == null) return;
            try {
                for(IssueRelated related:this.getIssue().getRelatedCases()) {
                    if(!related.getRelatedId().equals(Integer.valueOf(ids))) continue;
                    this.issueRelatedService.remove(related);
                    this.getIssue().getRelatedCases().remove(related);
                    break;
                }
            } catch(NumberFormatException e) {
                LOGGER.error("IssueController.deleteRelatedCases()", e.getMessage());
                return ;
            }
            JsfUtil.getResource().alertMsgInfo("label.tab_related_cases", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.delete", ResourceUtil.BUNDLE_ISSUE_NAME);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    
    @Setter @Getter private Map<Long, SelectItem> proposalDialogSelected = new HashMap<>();
    
    /**
     * @author hungpham
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void openProposalProductDialog(){
        startMeasure("openProposalProductDialog");
        String val = HTTPResReqUtil.getRequestParameter("openDialogURL");
        String top = HTTPResReqUtil.getRequestParameter("top");
        String width = HTTPResReqUtil.getRequestParameter("width");
        String height = HTTPResReqUtil.getRequestParameter("height");
        String tabId = HTTPResReqUtil.getRequestParameter("tabId");
        infoViewIndex = NumberUtils.toInt(tabId, 0);
        //reset temp data
        proposalDialogSelected.clear();
        
        String filterKey = val.indexOf("proposal") > 0 ? COLS.PROPOSAL : COLS.PRODUCT;
        
        this.getIssue().getIssueInfoList().get(NumberUtils.toInt(tabId, 0)).getMenteItem().forEach((item) -> {
            if(item.getItemName().equals(filterKey)){
                proposalDialogSelected.put(item.getItemLevel().longValue(), new SelectItem(item, item.getItemViewData(localeController.getLocale())));
            }
        });
        if(filterKey.equals(COLS.PRODUCT)){
            this.getIssue().getIssueInfoList().get(NumberUtils.toInt(tabId, 0)).getProductList().forEach((item) -> {
               proposalDialogSelected.put(4L, new SelectItem(item, item.getProductsName())); //level 4
            });
        }
        
        // open dialog
        if(!StringUtils.isBlank(val)){
            Map<String,Object> options = new HashMap<>();
            options.put("draggable", true);
            options.put("resizable", false);
            if(NumberUtils.isDigits(top)) {
                options.put("top", top);
            }
            if(NumberUtils.isDigits(width)) {
                options.put("contentWidth", width);
            }
            if(NumberUtils.isDigits(height)) {
                options.put("contentHeight", height);
            }
            options.put("includeViewParams", true);
            DialogObject.openDialog(val, options);
        }
        stopMeasure("openProposalProductDialog");
    }
    
    /**
     * @author hungpham
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void openRelateCaseDialog(){
        startMeasure("openRelateCaseDialog");
        String val = HTTPResReqUtil.getRequestParameter("openDialogURL");
        String top = HTTPResReqUtil.getRequestParameter("top");
        String width = HTTPResReqUtil.getRequestParameter("width");
        String height = HTTPResReqUtil.getRequestParameter("height");
        
        //reset temp data
        issueViewRelatedCode = null;
        issueRelatedComment = null;
        
        // open dialog
        if(!StringUtils.isBlank(val)){
            Map<String,Object> options = new HashMap<>();
            options.put("draggable", true);
            options.put("resizable", false);
            if(NumberUtils.isDigits(top)) {
                options.put("top", top);
            }
            if(NumberUtils.isDigits(width)) {
                options.put("contentWidth", width);
            }
            if(NumberUtils.isDigits(height)) {
                options.put("contentHeight", height);
            }
            options.put("includeViewParams", true);
            DialogObject.openDialog(val, options);
        }
        stopMeasure("openRelateCaseDialog");
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onLinkSelect(SelectItem src, Long level) {
        proposalDialogSelected.put(level, src);
        for (long i = level+1; i <= 10; i++) {
            try{
                proposalDialogSelected.remove(i);
            }catch(Exception e){}
        }
        this.getIssue().getIssueInfoList().get(infoViewIndex).getProductList().clear();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onLinkDoubleSelect(String inKey) {
        for (Iterator<MenteItem> iterator = this.getIssue().getIssueInfoList().get(infoViewIndex).getMenteItem().iterator(); iterator.hasNext();) {
            MenteItem o = iterator.next();
            if (o.getItemName().equals(inKey)) {
                iterator.remove();
            }
        }
        if(inKey.equals(COLS.PRODUCT)) this.getIssue().getIssueInfoList().get(infoViewIndex).getProductList().clear();
        proposalDialogSelected.forEach((idx, item) -> {
            if(item.getValue() instanceof MenteItem){
                this.getIssue().getIssueInfoList().get(infoViewIndex).getMenteItem().add((MenteItem) item.getValue());
            }else if(item.getValue() instanceof Products){
                this.getIssue().getIssueInfoList().get(infoViewIndex).getProductList().add((Products)item.getValue());
            }
        });
        RequestContext context = RequestContext.getCurrentInstance();
        context.closeDialog(0);
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)        
    public void deleteSelectName(String inKey, Integer tabIndex) {
        if(StringUtils.isBlank(inKey)) return;
        for (Iterator<MenteItem> iterator = this.getIssue().getIssueInfoList().get(tabIndex).getMenteItem().iterator(); iterator.hasNext();) {
            MenteItem o = iterator.next();
            if (o.getItemName().equals(inKey)) {
                iterator.remove();
            }
        }
        if(inKey.equals(COLS.PRODUCT)) this.getIssue().getIssueInfoList().get(tabIndex).getProductList().clear();
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void deleteAuthorizedUser(){
        this.getIssue().setIssueAuthorizerId(null);
    }

    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean loadIssue(int issueId) {
        startMeasure("loadIssue");
        Issue issue = this.issueService.findByIssueId(issueId, UserModel.getLogined().getLanguage());
        if(issue == null) {
            JsfUtil.getResource().alertMsgInfo("label.issue_code", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.not.exist", ResourceUtil.BUNDLE_ISSUE_NAME);
            return Boolean.FALSE;
        } else {
            if(!UserModel.getLogined().getCompanyCustomerMode()) {
                if(UserModel.getLogined().isCompanyStoreMode()
                        && !issue.getCustomerList().isEmpty()
                        && issue.getCustomerList().size() > 1) {
                    List<Customer> custs = new ArrayList<>();
                    custs.add(issue.getCustomerList().get(0));
                    issue.setCustomerList(custs);
                }
                if(UserModel.getLogined().getCompanyBusinessFlag() == COMPANY_TYPE.OPPORTUNITY
                        && !issue.getIssueInfoList().isEmpty()
                        && issue.getIssueInfoList().size() > 1) {
                    List<IssueInfo> infos = new ArrayList<>();
                    infos.add(issue.getIssueInfoList().get(0));
                    issue.setIssueInfoList(infos);
                }
            }
            this.issueMapper.put(issue.getIssueId(), issue);
            try{
                previousStatusId = issue.getIssueStatusId().getItemId();
            }catch(NullPointerException npe){
                previousStatusId = 0;
            }
            /** プルダウンリスト再初期 */
            loadContextData();
            loadCustomizeData(issueId);
            reLoadCustomers(issue);
            addCountRiskSensor(issueId);
        }
        stopMeasure("loadIssue");
        return Boolean.TRUE;
    }
    
    /** 受付情報を初期化 */
    private void loadNewIssue() {
        startMeasure("createIssue");
        FacesContext.getCurrentInstance().getViewRoot().getViewMap().clear();
        this.issueMapper.remove(0);
//        this.getIssue().setIssueContentAsk("");
        this.getIssue().setIssueCustActiveTab("#tabViewIssue:tabViewIssueBase");
        this.getIssue().setIssueCustActiveTab("#tabViewCustomer:cust_1");
        this.getIssue().setIssueReceiveDate(DateUtil.now());
        this.getIssue().setCompany(UserModel.getLogined().getCompany());
        this.getIssue().setIssueReceivePerson(UserModel.getLogined().getMember());
        this.getIssue().setCreatorId(UserModel.getLogined().getMember());
        this.getIssue().setCreatedTime(DateUtil.now());
        this.getIssue().setUpdatedId(UserModel.getLogined().getMember());
        this.getIssue().setUpdatedTime(DateUtil.now());
        List<SelectItem> publics = this.select.get(COLS.PUBLIC);
        if(publics != null && !publics.isEmpty() && NumberUtils.isDigits(publics.get(0).getValue().toString())) {
            this.getIssue().setIssuePublicId((MenteItem) publics.get(0).getValue());
        }
        this.getIssue().getHistorySpecials().clear();
        this.getIssue().getHistoryCustomers().clear();
        
        //reset customer view index
        this.customerViewIndex = 0;
        this.infoViewIndex = 0;
        
        //reset customize form
        this.setCustomizeForm(new ArrayList<>());
        
        stopMeasure("createIssue");
    }

    /** 受付情報が存在するか確認
     * flag：「1、2」1は新規、2は編集
     */
    private boolean chkIssueExists(Issue issue, String flag) {
        boolean error = false;
        String msg = null;
        Object[] para = new Object[1];
        para[0] = JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue_code", para);
//        if(!NumberUtils.isDigits(String.valueOf(this.getIssue().getIssueId()))){
//            if("manual".equals(this.getIssue().getSource())){
//                JsfUtil.addErrorMessage(ResourceUtil.message(this.companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.not.exist", para));
//            }
//            error = true;
//        } else {
//            if(SecureMethod.Method.CREATE.toString().equals(flag)) {
//                if("manual".equals(this.getIssue().getSource())){
//                    JsfUtil.addErrorMessage(ResourceUtil.message(this.companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.created", para));
//                }
//                error = true;
//            }
//            if(SecureMethod.Method.UPDATE.toString().equals(flag)
//                    && this.getIssue().getIssueId() == null
//                    || this.getIssue().getIssueId() <= 0) {
//                if("manual".equals(this.getIssue().getSource())){
//                    JsfUtil.addErrorMessage(ResourceUtil.message(this.companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.update", para));
//                }
//                error = true;
//            }
//        }
        
        if((SecureMethod.Method.UPDATE.toString().equals(flag) || SecureMethod.Method.DELETE.toString().equals(flag) || SecureMethod.Method.PRINT.toString().equals(flag)) && !(issue.getIssueId() != null && issue.getIssueId() >= -1)) {
            if("manual".equals(issue.getSource())){
                JsfUtil.addErrorMessage(JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.not.exist", para));
            }
            error = true;
        }
        
        if(issue.getIssueReceiveDate() == null){
            if("manual".equals(issue.getSource())){
                JsfUtil.getResource().alertMsg("label.issue_receive_date", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
            }
            error = true;
        }

        if(issue.getIssuePublicId() == null){
            if("manual".equals(issue.getSource())){
                JsfUtil.getResource().alertMsg("label.issue_public_name", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
            }
            error = true;
        }
        
        /** 申出分類チェック */
        boolean hasProposal = false;
        if(UserModel.getLogined().isCompanyStoreMode()) {
            hasProposal = true;
        } else {
            for(IssueInfo info : issue.getIssueInfoList()){
                for(MenteItem item : info.getMenteItem()){
                    if(item.getItemName().equals(COLS.PROPOSAL) && item.getItemLevel() == 1){
                        hasProposal = true;
                        break;
                    }
                }
                if(hasProposal) {
                    break;
                }
            }
        }

        if(!hasProposal) {
            if("manual".equals(issue.getSource())){
                JsfUtil.getResource().alertMsg("label.issue_proposal_name", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
            }
            error = true;
        }

        return error;
    }
    
    /** 顧客情報タプを追加
     * 「TEL、FAX、MAIL」を追加
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void addCustomer() {
        final Customer cust = new Customer();
        cust.setCompany(UserModel.getLogined().getCompany());
        cust.setCustDeleted(StatusUtil.EXISTS);
        cust.setCreatorId(UserModel.getLogined().getUserId());
        cust.setCreatedTime(DateUtil.now());
        cust.setUpdatedId(UserModel.getLogined().getUserId());
        cust.setUpdatedTime(DateUtil.now());

        /** 「TEL、MOBILE、MAIL」リストを初期化 */
        this.getIssue().getCustomerList().add(cust);

        JsfUtil.getResource().alertMsgInfo("label.page.customer", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.add", ResourceUtil.BUNDLE_ISSUE_NAME);
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void addInfoTab() {
        final IssueInfo info = new IssueInfo();
        info.setCompany(UserModel.getLogined().getCompany());
        info.setInfoDeleted(StatusUtil.EXISTS);
        info.setCreatorId(UserModel.getLogined().getUserId());
        info.setCreatedTime(DateUtil.now());
        info.setUpdatedId(UserModel.getLogined().getUserId());
        info.setUpdatedTime(DateUtil.now());

        /** 「TEL、MOBILE、MAIL」リストを初期化 */
        this.getIssue().getIssueInfoList().add(info);

        JsfUtil.getResource().alertMsgInfo("label.page.info", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.add", ResourceUtil.BUNDLE_ISSUE_NAME);
    }
    
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void removeInfoTab(String tabIdx){
        try {
            for (Iterator<IssueInfo> iterator = this.getIssue().getIssueInfoList().iterator(); iterator.hasNext();) {
                IssueInfo o = iterator.next();
                if (o.getTabIdx().equals(tabIdx)) {
                    iterator.remove();
                    if(infoViewIndex >= this.getIssue().getIssueInfoList().size()){
                        infoViewIndex = this.getIssue().getIssueInfoList().size() - 1;
                    }
                    // TODO ?? 
                    // issueCustomerService.remove(o); // intermediate deleted
                }
            }
            JsfUtil.getResource().alertMsgInfo("label.page.customer", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.delete", ResourceUtil.BUNDLE_ISSUE_NAME);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    
    /** 顧客情報タプを追加
     * 「TEL、FAX、MAIL」を追加
     * @param customer
     * @param flag
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void addTarget(Customer customer, Integer flag) {
        CustTargetInfo target = IssueUtil.createCustTargetInfo(customer, flag.shortValue(), null);
        customer.getCustTargetInfoList().add(target);
        reloadCustomer(this.getIssue());
    }

    /** 「TEL、FAX、MAIL」を削除
     * @param customer
     * @param target 
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void removeTarget(Customer customer, CustTargetInfo target) {
        customer.getCustTargetInfoList().remove(target);
        reloadCustomer(this.getIssue());
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void resertCustomer(Customer customer) {
        IssueUtil.resertCustomer(customer);
        reloadCustomer(this.getIssue());
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onBlurPost(Customer cust) {
        String zipCode = cust.getCustPost();
        if(StringUtils.isEmpty(zipCode)) return;
        String locale = UserModel.getLogined().getLanguage();
        ZipCode zc = zipCodeService.findByZipCode(zipCode, locale);
        if(zc == null || zc.getCodeId() == null) return;
        
        String cityKaneJii = zc.getCityKannji();
        List<Prefecture> listPrefecture = prefectureService.findCities(locale, cityKaneJii);
        if(!listPrefecture.isEmpty()) cust.setCustCity(listPrefecture.get(0));
        cust.setCustAddress(
                StringUtil.getStringNullToEmpty(zc.getAddressKannji()) +
                StringUtil.getStringNullToEmpty(zc.getDistrictKanj()));
        cust.setCustAddressKana(
                StringUtil.getStringNullToEmpty(zc.getCityKana()) +
                StringUtil.getStringNullToEmpty(zc.getAddressKana()) +
                StringUtil.getStringNullToEmpty(zc.getDistrictKana()));
    }
    
    /**
     * 顧客タプ「×」ボタンを押したとき処理
     * @param event：TabCloseEvent
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void onTabClose(TabCloseEvent event) {
        String tab = event.getTab().getId();
        if(StringUtils.isBlank(tab)) return;
        String[] tabIdx = tab.split("_");
        if(tabIdx == null || tabIdx.length != 2) return;
        String iIdx = tabIdx[1];
        this.removeCustomer(iIdx);
    }
    
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void removeCustomer(String tabIdx){
        try {
            for (Iterator<Customer> iterator = this.getIssue().getCustomerList().iterator(); iterator.hasNext();) {
                Customer o = iterator.next();
                if (o.getTabIdx().equals(tabIdx)) {
                    iterator.remove();
                    if(customerViewIndex >= this.getIssue().getCustomerList().size()){
                        customerViewIndex = this.getIssue().getCustomerList().size() - 1;
                    }
                    issueCustomerService.remove(o); // intermediate deleted
                }
            }
            JsfUtil.getResource().alertMsgInfo("label.page.customer", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.delete", ResourceUtil.BUNDLE_ISSUE_NAME);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    /**
     * Copy danh sach khach hang tu issue khac
     * @param issueOther 
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void copyCustomerFrom(Issue issueOther){
        this.getIssue().setCustomerList(issueOther.getCustomerList());
    }

    /** 作成者を選択された時処理 */
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void getLoginUserName() {
        this.getIssue().setIssueAuthorizerId(UserModel.getLogined().getMember());
    }

    /**
     * Speech API機能利用、店舗検索アプリへデータ転送
     */
//    public void issueSreachDB() {
//        if(StringUtils.isBlank(this.getIssue().getIssueContentAsk())) return;
//        String url = this.configService.get("SHOP_DB");
//        if(StringUtils.isBlank(url)) {
//            JsfUtil.getResource().putErrors(
//                    this.companyId
//                    , ResourceUtil.BUNDLE_MSG
//                    , ResourceUtil.SEVERITY_INFO
//                    , "店舗検索アプリへのURLが設定されていません。"
//                    , false);
//            return;
//        }
//        String[] params = {"", ""};
//        String[] paramVals = {"", ""};
//        HTTPClient client = new HTTPClient(url, params, paramVals);
//        JsonObject gson = client.post();
//        if(gson != null) {
//            JsfUtil.getResource().putErrors(
//                    this.companyId
//                    , ResourceUtil.BUNDLE_MSG
//                    , ResourceUtil.SEVERITY_INFO
//                    , "店舗検索アプリへ送信を完了しました。"
//                    , false);
//        }
//    }

    /**
     * Them danh sach Risk Sensor
     */
    private void addCountRiskSensor(int issueId) {
        startMeasure("addCountIssueSame");
        if(this.getIssue() == null || this.getIssue().getIssueId() == null) return;
        List<Integer> months = new ArrayList<>();
        months.add(1);
        months.add(3);
        List<Object> counts = this.issueService.findByCountIssueSame(this.companyId, months, issueId);
        if(counts.isEmpty() || counts.size() < 2) return;
        Object[] val = (Object[]) counts.get(0);
        this.getIssue().setRiskSensorOneMonth(String.valueOf(val[0]));
        if(val[1] != null){
            Arrays.asList(StringUtils.split(String.valueOf(val[1]), ",")).forEach((item) -> {
                if(NumberUtils.isNumber(item)){
                    this.getIssue().getRiskSensorOneMonthList().add(NumberUtils.toInt(item));
                }
            });
        }
        
        val = (Object[]) counts.get(1);
        this.getIssue().setRiskSensorThreeMonth(String.valueOf(val[0]));
        if(val[1] != null){
            Arrays.asList(StringUtils.split(String.valueOf(val[1]), ",")).forEach((item) -> {
                if(NumberUtils.isNumber(item)){
                    this.getIssue().getRiskSensorThreeMonthList().add(NumberUtils.toInt(item));
                }
            });
        }
        
        stopMeasure("addCountIssueSame");
    }

    /** 顧客情報のIDSから名前に変更 */
    private void reLoadCustomers(Issue issue) {
        startMeasure("reLoadCustomers");
        if(StringUtils.isBlank(this.getIssue().getIssueActiveTab())) {
            this.getIssue().setIssueActiveTab("#tabViewIssue:tabViewIssueBase");
        }
        // Reset customer view index
        this.customerViewIndex = 0;
        this.infoViewIndex = 0;
        
        reloadCustomer(issue);
        reloadEscalations(issue.getIssueId() != null ? issue.getIssueId() : 0);
        stopMeasure("reLoadCustomers");
    }
    
    /** 特殊顧客履歴リストを取得
     * @param issue */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void reloadCustomer(){
        this.reloadCustomer(this.getIssue());
    }
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void reloadCustomer(Issue issue){
        startMeasure("reloadCustomer");
        /** 顧客履歴リストを取得 */
        Integer issueId = issue.getIssueId() == null ? 0 : issue.getIssueId();
        this.getIssue(issueId).setHistoryCustomers(this.issueService.getIssueHistoryCustomers( issueId, companyId, IssueUtil.getListTargets(issue), IssueUtil.getCustCode(issue)));
        this.getIssue(issueId).setHistorySpecials(this.issueService.getSpecialCustomers(IssueUtil.getListTargets(issue), companyId, IssueUtil.getCustCode(issue)));
        
        stopMeasure("reloadCustomer");
    }

    
    /** Escalationリストを取得
     * @param issueId */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void reloadEscalations(){
        this.reloadEscalations(getIssue().getIssueId());
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void reloadEscalations(Integer issueId) {
        startMeasure("reloadEscalations");
        this.getIssue(issueId).setEscalations(this.issueEscalationService.findEscalationListByIssueId(issueId));
        if(this.getIssue().getEscalations() != null) {
            for(Escalation esc : this.getIssue().getEscalations()) {
                if(esc == null
                   || (esc.getEscalationSendType() != 1 && esc.getEscalationSendType() != 4)
                   || (!NumberUtils.isDigits(esc.getEscalationTo()) || !NumberUtils.isDigits(esc.getEscalationCc()))) continue;
                if(esc.getEscalationSendType() == 1) {
                    MenteItem item = this.menteService.find(esc.getEscalationSendType());
                    if(item != null) esc.setLabel1(item.getItemViewData(UserModel.getLogined().getLanguage()));
                } else if(esc.getEscalationSendType() == 4) {
                    if(NumberUtils.isDigits(esc.getEscalationTo())) {
                        MenteItem item = this.menteService.find(Integer.valueOf(esc.getEscalationTo()));
                        if(item != null) esc.setLabel1(item.getItemViewData(UserModel.getLogined().getLanguage()));
                    }
                    if(NumberUtils.isDigits(esc.getEscalationCc())) {
                        MenteItem item = this.menteService.find(Integer.valueOf(esc.getEscalationCc()));
                        if(item != null) esc.setLabel2(item.getItemViewData(UserModel.getLogined().getLanguage()));
                    }
                }
            }
        }
        stopMeasure("reloadEscalations");
    }
    
//    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
//    public String getIssueLampList(){
//        List<IssueLamp> list = UserModel.getLogined().getCompany().getIssueLampList();
//        String language =  UserModel.getLogined().getLanguage();
//        if(StringUtils.isBlank(language)) return null;
//        Map<Integer, List<Map<String, String>>> json = new HashMap<>();
//        for(IssueLamp item : list){
//            Map<String, String> parent = new HashMap<>();
//            parent.put("lampDates", String.valueOf(item.getLampDates()));
//            parent.put("lampColor", item.getLampColor());
//            List<IssueLampGlobal> globals = item.getIssueLampsGlobal();
//            for(IssueLampGlobal global:globals) {
//                if(global == null || !language.equals(global.getCrmIssueLampGlobalPK().getItemLang())) continue;
//                parent.put("lampName", global.getItemName());
//                break;
//            }
//            if(StringUtils.isBlank(parent.get("lampName"))) parent.put("lampName", StringUtils.EMPTY);
//            List<Map<String, String>> lampList = new ArrayList<>();//json.getOrDefault(item.getLampProposalId(), new ArrayList());
//            lampList.add(parent);
//            json.put(item.getLampProposalId(), lampList);
//        }
//        return new Gson().toJson(json);
//    }
    
    @SecureMethod(value = SecureMethod.Method.UPLOAD)
    public void upload(){}
    
    @SecureMethod(value = SecureMethod.Method.DOWNLOAD)
    public void dowload() throws IOException{
        if(this.projectController.getProject() == null || this.ais.getIssues() == null) return;
        String name = this.projectController.getProject().getListName();
        if(com.ocpsoft.pretty.faces.util.StringUtils.isBlank(name)) return;
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(name);
        
        final int[] rownum = {0};
        final int[] headercellnum = {0};
        final Row headerRow = sheet.createRow(rownum[0]++);
        for(SelectItem item:this.projectController.getLiveSearchVisibleColumns()) {
            Cell cell = headerRow.createCell(headercellnum[0]++);
            cell.setCellValue(SelectUtil.nullStringToEmpty(item.getLabel()));
        }
        List<Map<String, Object>> issues = this.ais.getIssues();
        for (Map<String, Object> map : issues) {
            final Row row = sheet.createRow(rownum[0]++);
            final int[] cellnum = {0};
            for(SelectItem item:this.projectController.getLiveSearchVisibleColumns()) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if(item == null || item.getValue() == null || !item.getValue().equals(entry.getKey())) continue;
                    Cell cell = row.createCell(cellnum[0]++);
                    cell.setCellValue(SelectUtil.nullStringToEmpty(String.valueOf(entry.getValue())));
                    break;
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.PATTERN_CSV_EXPORT_DATE);
        WebFileUtil.forceDownload(String.format("%s_%s.xls", name, sdf.format(new Date())), wb);
    }

    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void showRiskSensor(List<Integer> issueList){
        riskSensorList = issueService.findByIssueIdList(issueList);
    }
}
