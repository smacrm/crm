package gnext.controller.system;

import gnext.bean.Group;
import gnext.bean.automail.AutoMail;
import gnext.bean.mente.MenteItem;
import gnext.bean.mente.MenteOptionDataValue;
import gnext.bean.mente.Products;
import gnext.controller.ObserverLanguageController;
import gnext.controller.common.LayoutController;
import gnext.controller.common.LocaleController;
import gnext.bean.issue.EscalationSample;
import gnext.controller.issue.IssueController;
import gnext.controller.system.bean.AutoMailConfigBean;
import gnext.exporter.excel.CategoryExport;
import gnext.exporter.excel.ProductExport;
import gnext.exporter.excel.ProposalExportXls;
import gnext.exporter.excel.StaticColumnExport;
import gnext.importer.excel.MaintenanceExceptProductImport;
import gnext.importer.excel.ProductImport;
import gnext.importer.excel.ProposalImport;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.MemberService;
import gnext.service.automail.AutoMailService;
import gnext.service.issue.IssueEscalationSampleService;
import gnext.service.mente.MenteService;
import gnext.service.mente.ProductService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StringUtil;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.COMPANY_TYPE;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Nov 7, 2016
 */
@ManagedBean(name = "maintenanceController")
@ViewScoped
@SecurePage(module = SecurePage.Module.SYSTEM)
public class MaintenanceController implements Serializable, ObserverLanguageController {
    private static final long serialVersionUID = -3036914344019907460L;
    
    private final Logger logger = LoggerFactory.getLogger(MaintenanceController.class);

    @ManagedProperty(value = "#{layout}") @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{localeController}") @Getter @Setter private LocaleController localeController;
    @ManagedProperty(value = "#{issueController}") @Getter @Setter private IssueController issueController;
    
    @EJB @Getter private MenteService menteService;
    @EJB @Getter private ProductService productService;
    @EJB private IssueEscalationSampleService issueEscalationSampleService;
    @EJB private AutoMailService autoMailService;
    
    @Getter public int companyId;
    
    @Getter @Setter private List<String> dynamicColumnRoot = new ArrayList<>();
    @Getter @Setter private List dynamicColumns = new ArrayList();
    @Getter @Setter private List<Integer> dynamicColumnSelected = new ArrayList<>();
    @Getter @Setter private List<String> dynamicColumnHeader = new ArrayList<>();
    
    @Getter @Setter private List<String> staticColumnsData = new ArrayList<>();
    @Getter @Setter private List<MenteItem> staticColumnsDataLvl2 = new ArrayList<>();
    @Getter @Setter private String staticColumnsDataHeaderLvl2;
    
    @Getter @Setter private transient org.primefaces.component.tabview.TabView tabView=null;
    @Getter public  Integer tabIndex = null;
    
    private int currentDynamicIndex;
    @Getter @Setter private String currentDynamicRoot;
    @Getter @Setter private MenteItem currentDynamicParent;
    
    @Getter @Setter private String currentStaticParent;
    @Getter @Setter private Integer indexCurrentStaticParent;
    
    @Getter @Setter private String locale;
    @Getter @Setter private String selectedTab;
    @Getter @Setter private String escalationTargetId;
    
    private String exportFileName;
    @Setter @Getter private UploadedFile uploadedFile;
    
    @Setter @Getter private EscalationSample escalationSample = new EscalationSample();
    @Setter @Getter private List<ObserverLanguageController> lcs = new ArrayList<>();

    @Override
    public void onChangeLanguage() {
        if(COLS.PROJECT_PASS_DAYS.equals(this.currentStaticParent)) {
            if(lcs.isEmpty() || lcs.size() <= 0) return;
            for(ObserverLanguageController lc : lcs) {
                if(lc == null) continue;
                lc.onChangeLanguage();
            }
        }
        if(this.currentStaticParent != null) showStaticColumnLvl2(this.currentStaticParent);
    }
    
    @PostConstruct
    public void init(){
        tabIndex = 1;
        companyId = UserModel.getLogined().getCompanyId();
        if(locale == null) locale = localeController.getLocale();
        
        try {
            exportFileName = URLEncoder.encode("ロールリスト", "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
        }
       
        showStaticColumn();
        dynamicColumnRoot = menteService.getDynamicRoot();
        if(dynamicColumnRoot.size() > 0) showDynamicRoot(dynamicColumnRoot.get(0));
    }
    
    @SecureMethod(SecureMethod.Method.DOWNLOAD)
    public String getExportFileName() {
        return exportFileName;
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void showDynamicColumn(int index, MenteItem item){
        index = index + 1;
        currentDynamicParent = item;
        final List<MenteItem> childs = new ArrayList<>();
        item.getItemChilds().forEach((t) -> {
            if(t.getItemDeleted().equals(Boolean.FALSE)){
                childs.add(t);
            }
        });
        
        Collections.sort(childs, (o1, o2) -> {
            return o1.getItemOrder() - o2.getItemOrder();
        });
        
        if(dynamicColumns.size() <= index){
            dynamicColumns.add(index, childs);
            dynamicColumnHeader.add(index, item.getItemData());
            dynamicColumnSelected.add(index, item.getItemId());
        }else{
            dynamicColumns.set(index, childs);
            dynamicColumnHeader.set(index, item.getItemData());
            dynamicColumnSelected.set(index, item.getItemId());
            int numEls = dynamicColumns.size();
            for(int i=index+1; i< numEls; i++){
                int removeElm = dynamicColumns.size()-1;
                dynamicColumns.remove(removeElm);
                dynamicColumnHeader.remove(removeElm);
                dynamicColumnSelected.remove(removeElm);
            }
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void showProductCol(int index, MenteItem item){
        index = index + 1;
        currentDynamicParent = item;
        List<Products> childs = productService.getAllProducts(UserModel.getLogined().getCompanyId(), item.getItemId());
        Collections.sort(childs, (o1, o2) -> {
            return o1.getProductsOrder() - o2.getProductsOrder();
        });
        if(dynamicColumns.size() <= index){
            dynamicColumns.add(index, childs);
            dynamicColumnHeader.add(index, item.getItemData());
            dynamicColumnSelected.add(index, item.getItemId());
        }else{
            dynamicColumns.set(index, childs);
            dynamicColumnHeader.set(index, item.getItemData());
            dynamicColumnSelected.set(index, item.getItemId());
            for(int i=index+1; i< dynamicColumns.size(); i++){
                dynamicColumns.remove(index+1);
                dynamicColumnHeader.remove(index+1);
                dynamicColumnSelected.remove(index+1);
            }
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void onSPTabChange(TabChangeEvent event){   
        tabIndex = tabView.getChildren().indexOf(event.getTab())+1;
        this.setSelectedTab(event.getTab().getId());
        if("tabEscalationSample".equals(event.getTab().getId())) {
            if(UserModel.getLogined().getCompanyBusinessFlag() == COMPANY_TYPE.CUSTOMER) {
                currentStaticParent = COLS.PROJECT_PASS_DAYS;
            } else {
                currentStaticParent = COLS.MAIL_REQUEST;
            }
        } else {
            currentStaticParent = COLS.AGE;
        }
        if(!"tabGoodsExportXLS".equals(event.getTab().getId())) {
            showStaticColumnLvl2(currentStaticParent);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.UPDATE)
    public void onRowCancel(RowEditEvent event) {
        List checkList = new ArrayList();
        
        if(tabIndex == 2){
            checkList.addAll((List) dynamicColumns.get(currentDynamicIndex));
        }else{
            checkList.addAll(staticColumnsDataLvl2);
        }
        if( event.getObject() instanceof MenteItem && 
                ( 
                    ((MenteItem)event.getObject()).getItemId() == null || ((MenteItem)event.getObject()).getItemId() <= 0
                )
            ){
            checkList.remove((MenteItem)event.getObject());
        }
        staticColumnsDataLvl2 = checkList;
    }
    
    @SecureMethod(value = SecureMethod.Method.UPDATE)
    public void onRowEdit(RowEditEvent event) {
        try{
            if(event.getObject() instanceof MenteItem){
                MenteItem newObj = (MenteItem)event.getObject();
                MenteOptionDataValue lang = null;
                for (Iterator<MenteOptionDataValue> iter = newObj.getLangs().listIterator(); iter.hasNext(); ) {
                    MenteOptionDataValue it = iter.next();
                    if(it.getMenteOptionDataValuePK().getItemLanguage().equals(locale)){
                        lang = it;
                        iter.remove();
                        break;
                    }
                }
                if(lang != null){
                    lang.setUpdatedId(UserModel.getLogined().getUserId());
                    lang.setUpdatedTime(new Date());
                }else{
                    lang = new MenteOptionDataValue();
                    lang.setMenteItem(newObj);
                    lang.getMenteOptionDataValuePK().setItemLanguage(locale);
                    lang.setCompany(UserModel.getLogined().getCompany());
                    lang.setCreatorId(UserModel.getLogined().getUserId());
                    lang.setCreatedTime(new Date());
                }
                lang.setItemData(newObj.getItemData());

                if(newObj.getItemId() == null || newObj.getItemId() <= 0){
                    newObj.setItemId(null);
                    newObj.getLangs().add(lang);
                    menteService.create(newObj);
                    if( null != newObj.getItemParent()){
                        newObj.getItemParent().getItemChilds().add(newObj);
                    }
                }else{
                    lang.getMenteOptionDataValuePK().setItemId(newObj.getItemId() != null ? newObj.getItemId() : 0);
                    newObj.getLangs().add(lang);
                    newObj.setUpdatedTime(new Date());
                    newObj.setUpdatedId(UserModel.getLogined().getUserId());
                    menteService.edit(newObj);
                }
            }else if(event.getObject() instanceof Products){
                Products p = (Products) event.getObject();
                productService.saveProduct(p);
            }
            
            // làm mới lại dữ liệu trong IssueController.
            issueController.loadMenteContext();
            
            JsfUtil.addSuccessMessage("完了しました。");
        }catch(Exception e){
            JsfUtil.addErrorMessage("エラーがあるので、完了ができない。");
            logger.error(e.getMessage(), e);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.CREATE)
    public void addDynamicRow(int index){
        currentDynamicIndex = index;
        try{
            if(index == 3 && currentDynamicParent.getItemName().equals(COLS.PRODUCT)){
                Products newObj = new Products();
                newObj.setProductsCategorySmallId(currentDynamicParent.getItemId());
                newObj.setProductsIsDeleted(Boolean.FALSE);
                newObj.setCompanyId(UserModel.getLogined().getCompanyId());
                newObj.setProductsCreatorId(UserModel.getLogined().getUserId());
                newObj.setProductsCreatedDatetime(new Date());
                
                ((List)dynamicColumns.get(index)).add(newObj);
            }else{
                MenteItem newObj = new MenteItem();
                newObj.setItemName(currentDynamicRoot);
                newObj.setItemLevel(index+1);
                newObj.setCompany(UserModel.getLogined().getCompany());
                newObj.setItemParent(index == 0 ? null : ((List)dynamicColumns.get(index)).size() > 0 ? ((List<MenteItem>)dynamicColumns.get(index)).get(0).getItemParent() : currentDynamicParent);
                newObj.setCreatedTime(new Date());
                newObj.setCreatorId(UserModel.getLogined().getUserId());
                newObj.setItemDeleted(Boolean.FALSE);
                newObj.setItemOrder(((List)dynamicColumns.get(index)).size()+1);
                
                ((List)dynamicColumns.get(index)).add(newObj);
            }
            RequestContext.getCurrentInstance().execute("jQuery('.editable-"+index+":last span.ui-icon-pencil').trigger('click'); jQuery('.editable-"+index+"-input:last').focus();");
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void showDynamicRoot(String root){
        currentDynamicRoot = root;
        dynamicColumns.clear();
        dynamicColumnHeader.clear();
        dynamicColumnSelected.clear();
        dynamicColumns.add(menteService.getAllDynamicLevel(currentDynamicRoot, companyId));
        dynamicColumnHeader.add(JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG, "label."+root));
        dynamicColumnSelected.add(1);
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void addStaticRow(){
        MenteItem newObj = new MenteItem(UserModel.getLogined().getMember(), currentStaticParent, 1, null);
        newObj.setItemId(-(staticColumnsDataLvl2.size() + 1));
        newObj.setItemOrder(staticColumnsDataLvl2.size()+1);
//        newObj.setItemName(currentStaticParent);
//        newObj.setCompany(UserModel.getLogined().getCompany());
//        newObj.setItemLevel(1);
//        newObj.setCreatedTime(new Date());
//        newObj.setCreatorId(UserModel.getLogined().getUserId());
//        newObj.setItemDeleted(Boolean.FALSE);
//        newObj.setItemParent(null);
        
        staticColumnsDataLvl2.add(newObj);
        RequestContext.getCurrentInstance().execute("jQuery('.static-table .editable:last span.ui-icon-pencil').trigger('click'); jQuery('.static-table .editable-input:last').focus();");
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void showStaticColumn(){
        staticColumnsData = menteService.getAllStaticLevel(UserModel.getLogined().getCompanyBusinessFlag(), companyId);
        if(staticColumnsData.size() > 0) {
            if("tabEscalationSample".equals(this.selectedTab)) {
                if(UserModel.getLogined().getCompanyCustomerMode()) {
                    showStaticColumnLvl2(COLS.PROJECT_PASS_DAYS);
                } else {
                    showStaticColumnLvl2(COLS.MAIL_REQUEST);
                }
            } else {
                showStaticColumnLvl2(staticColumnsData.get(0));
            }
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void showStaticColumnLvl2(String parent){
        currentStaticParent = parent;
        indexCurrentStaticParent = staticColumnsData.indexOf(currentStaticParent);
        staticColumnsDataHeaderLvl2 = JsfUtil.getResource().message(ResourceUtil.BUNDLE_ISSUE_NAME, "label."+parent.replace("_id", "_name"));
        staticColumnsDataLvl2 = menteService.getAllStaticLevel(parent, companyId);
        if("tabEscalationSample".equals(this.selectedTab)) {
            Short typeId = StringUtil.getSampleTypeId(currentStaticParent);
            Integer targetId = null;
            if((COLS.SUPPORT_METHOD.equals(this.currentStaticParent)
                    || COLS.SIGNATURE.equals(this.currentStaticParent))
                    && this.staticColumnsDataLvl2 != null
                    && this.staticColumnsDataLvl2.size() > 0){
                targetId = staticColumnsDataLvl2.get(0).getItemId();
            }
            this.escalationSample =
                this.issueEscalationSampleService.getEscalationSampleByTypeIdAndTargetId(
                        typeId, targetId, UserModel.getLogined().getCompanyId(), this.locale);
            if(this.escalationSample == null) this.escalationSample = new EscalationSample();
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.DOWNLOAD)
    public void exportStaticData() throws Exception {
        new StaticColumnExport(staticColumnsDataHeaderLvl2, staticColumnsDataLvl2).execute();
    }
    
    /**
     * Import data for all tabs except product's data
     * @throws IOException 
     */
    @SecureMethod(value = SecureMethod.Method.UPLOAD)
    public void importData() throws Exception {
        if(this.uploadedFile == null){
            //upload failed
            JsfUtil.addErrorMessage("エラーがあるので、完了ができない。");
            return;
        }
        new MaintenanceExceptProductImport(this).execute(this.uploadedFile.getInputstream());
    }
    
    @SecureMethod(value = SecureMethod.Method.UPLOAD)
    public void importProposalData() throws Exception {
        if(this.uploadedFile == null){
            JsfUtil.addErrorMessage("エラーがあるので、完了ができない。");
            return;
        }
        new ProposalImport(this.locale, UserModel.getLogined().getCompany(), UserModel.getLogined().getMember(), menteService).execute(this.uploadedFile.getInputstream());
    }
    
    /**
     * Import data for product only
     * @throws IOException 
     */
    @SecureMethod(value = SecureMethod.Method.UPLOAD)
    public void importProductData() throws Exception {
        if(this.uploadedFile == null) {
            JsfUtil.addErrorMessage("エラーがあるので、完了ができない。");
            return;
        }
        new ProductImport(this.productService, this.companyId).execute(this.uploadedFile.getInputstream());
    }
    
    public void fileUploadListener(FileUploadEvent event) {
        this.uploadedFile = event.getFile();
    }
    
    public void handleCloseDialog(CloseEvent event) {
        uploadedFile = null;
    }
    
    @SecureMethod(value = SecureMethod.Method.DOWNLOAD)
    public void exportData(int type) throws Exception {
        switch(type){
            case 1:
                new ProductExport(this.locale, this.companyId, productService).execute();
                break;
            case 2:
                new CategoryExport(this.locale, this.companyId, menteService).execute();
                break;
            case 3:
                new ProposalExportXls(this.locale, this.companyId, menteService).execute();
                break;    
        }
    }
    
    @SecureMethod(SecureMethod.Method.DELETE)
    public void delete(MenteItem item){
        try{
            item.setUpdatedId(UserModel.getLogined().getUserId());
            item.setUpdatedTime(new Date());
            menteService.remove(item);
            if(!dynamicColumns.isEmpty()){
                Vector vector = (Vector)dynamicColumns.get(0);
                vector.remove(item);
            }
            JsfUtil.addSuccessMessage("削除しました。");
            showStaticColumnLvl2(currentStaticParent);
        }catch(Exception e){
            JsfUtil.addErrorMessage("エラーがあるので、完了ができない。");
            logger.error(e.getMessage(), e);
        }
    }
    
    @SecureMethod(SecureMethod.Method.DELETE)
    public void deleteProduct(Products item){
        try{
            productService.removeProduct(UserModel.getLogined().getCompanyId(), item.getProductsId());
            if(!dynamicColumns.isEmpty()){
                Vector vector = (Vector)dynamicColumns.get(0);
                vector.remove(item);
            }
            JsfUtil.addSuccessMessage("削除しました。");
            showStaticColumnLvl2(currentStaticParent);
        }catch(Exception e){
            JsfUtil.addErrorMessage("エラーがあるので、完了ができない。");
            logger.error(e.getMessage(), e);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void reload(){
        load();
        if(tabView != null) tabView.setActiveIndex(0);
    }
    
    @SecureMethod(value=SecureMethod.Method.INDEX, require = false)
    public void load(){
        init();
        layout.setCenter("/modules/system/mente/list.xhtml");
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void showSampleStaticColumnLvl2(String key){
        currentStaticParent = key;
        Integer targetId = null;
        if(COLS.MAIL_REQUEST.equals(key) || COLS.SIGNATURE.equals(key)) {
            this.staticColumnsDataHeaderLvl2 = JsfUtil.getResource().message(ResourceUtil.BUNDLE_ISSUE_NAME, "label." + key.replace("_id", "_name"));        
            this.staticColumnsDataLvl2 = menteService.getAllStaticLevel(key, companyId);
            if(this.staticColumnsDataLvl2 != null
                    && this.staticColumnsDataLvl2.size() > 0) {
                targetId = this.staticColumnsDataLvl2.get(0).getItemId();
                this.setEscalationTargetId(targetId.toString());
            }
        } else {
            this.staticColumnsDataLvl2 = new ArrayList<>();
            targetId = null;
            this.setEscalationTargetId(null);
        }
        Short typeId = StringUtil.getSampleTypeId(key);
        if(targetId != null) System.err.println(targetId);
        if(typeId != null && typeId > 0) {
            this.escalationSample =
                    this.issueEscalationSampleService.getEscalationSampleByTypeIdAndTargetId(
                            typeId, targetId, UserModel.getLogined().getCompanyId(), this.locale);
            if(this.escalationSample != null) this.setLocale(this.escalationSample.getSampleLang());
        }
        if(this.escalationSample == null) this.escalationSample = new EscalationSample();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void onSelectionChanged(AjaxBehaviorEvent event) {
        SelectOneMenu selectMenu = (SelectOneMenu) event.getSource();
        if(selectMenu == null) return;
        String id = String.valueOf(selectMenu.getValue());
        if(!NumberUtils.isDigits(id) || this.currentStaticParent == null) return;
        Short typeId = StringUtil.getSampleTypeId(this.currentStaticParent);
        this.escalationSample =
                this.issueEscalationSampleService.getEscalationSampleByTypeIdAndTargetId(
                        typeId, Integer.valueOf(id), UserModel.getLogined().getCompanyId(), this.locale);
        if(this.escalationSample == null) this.escalationSample = new EscalationSample();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void escalationSampleInsert() {
        if(this.currentStaticParent == null || !"tabEscalationSample".equals(this.selectedTab)) return;
        try {
            if(StringUtils.isBlank(this.locale)) {
                JsfUtil.getResource().alertMsg(
                        "label.language"
                        , ResourceUtil.BUNDLE_ISSUE_NAME
                        , "label.issue.not.selected"
                        , ResourceUtil.BUNDLE_ISSUE_NAME);
                return;
            }
            if(!chkForm()) return;
            Short typeId = StringUtil.getSampleTypeId(this.currentStaticParent);
            if(typeId == null) return;
            if(this.escalationSample != null && this.escalationSample.getSampleId() != null) {
                this.escalationSample.setSampleLang(this.locale);
                this.escalationSample.setUpdatedId(UserModel.getLogined().getUserId());
                this.escalationSample.setUpdatedTime(DateUtil.now());
                this.issueEscalationSampleService.edit(this.escalationSample);        
                JsfUtil.getResource().alertMsgInfo(
                        "label." + this.currentStaticParent.replace("_id", "_name")
                        , ResourceUtil.BUNDLE_ISSUE_NAME
                        , "label.issue.update"
                        , ResourceUtil.BUNDLE_ISSUE_NAME);
            } else {
                this.escalationSample.setSampleTypeId(Integer.valueOf(typeId));
                this.escalationSample.setSampleTargetId(
                        NumberUtils.isDigits(this.escalationTargetId)
                                ?Integer.valueOf(this.escalationTargetId)
                                :null);
                this.escalationSample.setSampleLang(this.locale);
                this.escalationSample.setCompanyId(UserModel.getLogined().getCompanyId());
                this.escalationSample.setCreatorId(UserModel.getLogined().getUserId());
                this.escalationSample.setCreatedTime(DateUtil.now());
                this.escalationSample.setUpdatedId(UserModel.getLogined().getUserId());
                this.escalationSample.setUpdatedTime(DateUtil.now());
                this.issueEscalationSampleService.create(this.escalationSample);
                
                String key = "label." + this.currentStaticParent.replace("_id", "_name");
                switch(currentStaticParent){
                    case COLS.MAIL_REQUEST://"mail_request_id"
                        key = "label.escalation_2";
                        break;
                    case COLS.SUPPORT_METHOD://"cust_support_method_id"
                        key = "label.escalation_4";
                        break;
                    case COLS.SIGNATURE://"mail_signature_id"
                        key = "label.mail_signature_name";
                        break;
                }
                JsfUtil.getResource().alertMsgInfo(
                        key
                        , ResourceUtil.BUNDLE_ISSUE_NAME
                        , "label.issue.save"
                        , ResourceUtil.BUNDLE_ISSUE_NAME);
            }
        }catch(Exception ex){
            logger.error("[MaintenanceController.escalationSampleInsert()]", ex);
        }
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require=false)
    public void escalationSampleDelete() {
        try {
            if(this.escalationSample == null || this.escalationSample.getSampleId() == null) {
                return;
            }
            this.issueEscalationSampleService.remove(this.escalationSample);
            JsfUtil.getResource().alertMsgInfo(
                "label." + this.currentStaticParent.replace("_id", "_name")
                , ResourceUtil.BUNDLE_ISSUE_NAME
                , "label.issue.delete"
                , ResourceUtil.BUNDLE_ISSUE_NAME);
        }catch(Exception ex){
            logger.error("[MaintenanceController.escalationSampleInsert()]", ex);
        }
    }

    private boolean chkForm() {
        if(this.currentStaticParent == null || this.currentStaticParent == null) return false;
        /** タイトルチェック */
        if(!COLS.SIGNATURE.equals(this.currentStaticParent)) {
            if(StringUtils.isBlank(this.escalationSample.getSampleSubject())) {
                JsfUtil.getResource().alertMsg("label.mail.subject", ResourceUtil.BUNDLE_MAIL_NAME, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
                return false;
            }
            if(this.escalationSample.getSampleSubject() != null
                    && this.escalationSample.getSampleSubject().length() > 100) {
                JsfUtil.getResource().alertMsgMaxLength("label.mail.subject", ResourceUtil.BUNDLE_MAIL_NAME, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 100);
                return false;
            }

        }
        /** Headerチェック */
        if(COLS.SUPPORT_METHOD.equals(this.currentStaticParent)){
            if(this.escalationSample.getSampleHeader() != null
                    && this.escalationSample.getSampleHeader().length() > 300) {
                JsfUtil.getResource().alertMsgMaxLength("label.mail.header", ResourceUtil.BUNDLE_MAIL_NAME, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 300);
                return false;
            }
        }
        /** 本文チェック */
        if(StringUtils.isBlank(this.escalationSample.getSampleBody())) {
            JsfUtil.getResource().alertMsg("label.mail.comment", ResourceUtil.BUNDLE_MAIL_NAME, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        if(this.escalationSample.getSampleBody() != null
                && this.escalationSample.getSampleBody().length() > 20000) {
            JsfUtil.getResource().alertMsgMaxLength("label.mail.comment", ResourceUtil.BUNDLE_MAIL_NAME, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 20000);
            return false;
        }
        /** Fotterチェック */
        if(COLS.SUPPORT_METHOD.equals(this.currentStaticParent)
                || COLS.MAIL_REQUEST.equals(this.currentStaticParent)){
            if(this.escalationSample.getSampleFotter() != null
                    && this.escalationSample.getSampleFotter().length() > 500) {
                JsfUtil.getResource().alertMsgMaxLength("label.mail.footer", ResourceUtil.BUNDLE_MAIL_NAME, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 500);
                return false;
            }
        }
        return true;
    }
    
    @Getter @Setter private MenteItem issueStatusConfigItem = new MenteItem();
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onChangeIssueStep(AjaxBehaviorEvent event) {
        SelectOneMenu selectMenu = (SelectOneMenu) event.getSource();
        if(selectMenu == null) return;
        Integer newStepId = NumberUtils.toInt(String.valueOf(selectMenu.getValue()), 0);
        issueStatusConfigItem.setIssueStatusStep(newStepId);
        configIssueStatus(issueStatusConfigItem);
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void configIssueStatus(MenteItem item){
        issueStatusConfigItem = item;
        
        // reload auto mail config 
        autoMailConfigList.clear();
        
        List<AutoMail> configList = autoMailService.findByStatus(item);
        configList.forEach((it) -> {
            AutoMailConfigBean bean = new AutoMailConfigBean(issueStatusConfigItem);
            bean.setBean(it);
            bean.setAutoSend(it.getAutoSend());
            bean.setDay(it.getDay());
            bean.setHours(it.getHour());
            bean.setMode(it.getMode());

            it.getMenteItemList().forEach((it2) -> {
                if("issue_proposal_id".equals(it2.getItemName())){
                    bean.getProposalList().add(it2);
                }else if("issue_product_id".equals(it2.getItemName())){
                    if(it2.getItemLevel() == 1){
                        bean.getProductList().add(it2);
                    }else if(it2.getItemLevel() == 2){
                        bean.getProductLv2List().add(it2);
                    }
                }
            });
            
            
            List<SelectItem> toList = new ArrayList<>();
            List<SelectItem> ccList = new ArrayList<>();
            
            it.getAutoMailMemberList().forEach((m) -> {
                String mail = this.memberService.getUserMailFristByUserId(m.getAutoMailMemberPK().getMemberId());
                if( !StringUtils.isEmpty(mail) ){
                    SelectItem selectItem = new SelectItem(m.getMember(), m.getMember().getMemberNameFull(), mail);
                    switch(m.getAutoMailMemberPK().getType()){
                        case "cc":
                            ccList.add(selectItem);
                            break;
                        case "to":
                            toList.add(selectItem);
                            break;
                    }
                }
            });
            
            bean.setToList(toList);
            bean.setCcList(ccList);
            
            autoMailConfigList.add(bean);
        });
        
        issueStatusConfigItem.setIssueStatusAutoMail(!configList.isEmpty());
        
        if(autoMailConfigList.isEmpty()){
            addNewAutoMailConfig();
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.UPDATE)
    public void saveConfigIssueStatus() throws Exception{
        if(issueStatusConfigItem != null && isConfigIssueStatusValid()){
            issueStatusConfigItem.setUpdatedTime(new Date());
            issueStatusConfigItem.setUpdatedId(UserModel.getLogined().getUserId());
            menteService.edit(issueStatusConfigItem);
            
            autoMailService.resetItemId(issueStatusConfigItem.getItemId());
            
            if(issueStatusConfigItem.getIssueStatusStep() == 2
                    || issueStatusConfigItem.getIssueStatusStep() == 5
                    || issueStatusConfigItem.getIssueStatusStep() == 6){
                // save auto mail config
                autoMailConfigList.forEach((item) -> {
                    AutoMail bean = item.getBean();
                    try {
                        autoMailService.create(bean);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                });
            }
            
            JsfUtil.addSuccessMessage("進捗状況が取込しました。");
            JsfUtil.executeClientScript("PF('issueStatusPanel').hide();");
        }
    }
    
    public boolean isConfigIssueStatusValid() throws Exception {
        boolean valid = true;
        if(issueStatusConfigItem.getIssueStatusAutoMail() && ( 
                issueStatusConfigItem.getIssueStatusStep() == 2
                || issueStatusConfigItem.getIssueStatusStep() == 5
                || issueStatusConfigItem.getIssueStatusStep() == 6 ) 
            ){
            
            // save auto mail config
            for(AutoMailConfigBean item: autoMailConfigList){
                if(item.getProposalList().isEmpty()){
                    JsfUtil.addErrorMessage("「申出分類」を入力してください。");
                    valid = false;
                }
                if(item.getToList().isEmpty() && item.getCcList().isEmpty()){
                    JsfUtil.addErrorMessage("「宛先」や「CC」などを入力してください。");
                    valid = false;
                }
            }
        }else{
            autoMailService.resetItemId(issueStatusConfigItem.getItemId());
            autoMailConfigList.clear();
        }
        return valid;
    }
    
    @Getter
    private List<AutoMailConfigBean> autoMailConfigList = new ArrayList<>();
    
    private AutoMailConfigBean currentAutoMailConfigBean = new AutoMailConfigBean();
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void addNewAutoMailConfig(){
        autoMailConfigList.add(new AutoMailConfigBean(issueStatusConfigItem));
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void removeAutoMailConfig(AutoMailConfigBean item){
        autoMailConfigList.remove(item);
    }
    
    @EJB private MemberService memberService;
    @Getter @Setter private DualListModel<SelectItem> autoMailMembers = new DualListModel(new ArrayList(), new ArrayList());
    private String autoMailMode;

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void setAutoMailMode(AutoMailConfigBean item, String autoMailMode) {
        this.currentAutoMailConfigBean = item;
        this.autoMailMode = autoMailMode;
        switch (autoMailMode) {
            case "to":
                this.autoMailMembers.setTarget(item.getToList());
                break;
            case "cc":
                this.autoMailMembers.setTarget(item.getCcList());
                break;
        }
        this.autoMailMembers.getSource().clear();
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public TreeNode getGroupTree(){
        List<Group> dataSource = UserModel.getLogined().getCompany().getGroupList();
        TreeNode root = new DefaultTreeNode();
        dataSource.forEach((group) -> {
            if(group.getParent() == null){
                TreeNode node = new DefaultTreeNode(group, root);
                node.setExpanded(true);
                if(!group.getChilds().isEmpty()){
                    this.addItemToGroupTree(node, group.getChilds());
                }
            }
        });
        return root;
    }
    
    private void addItemToGroupTree(TreeNode parent, List<Group> group){
        group.forEach((item) -> {
            TreeNode node = new DefaultTreeNode(item, parent);
            node.setExpanded(true);
            if(!item.getChilds().isEmpty()){
                this.addItemToGroupTree(node, item.getChilds());
            }
        });
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onChangeGroup(NodeSelectEvent event) {
        TreeNode node = event.getTreeNode();
        Group group = (Group)node.getData();
        
        List<SelectItem> newItems = new ArrayList<>();
        if(null != this.autoMailMode) switch (this.autoMailMode) {
            case "to":
                newItems = this.currentAutoMailConfigBean.getToList();
                break;
            case "cc":
                newItems = this.currentAutoMailConfigBean.getCcList();
                break;
        }
        this.autoMailMembers.setTarget(newItems);
        List<Integer> targetMemberId = new ArrayList<>();
        newItems.forEach((i) -> {
            targetMemberId.add(NumberUtils.toInt(i.getValue().toString()));
        });
        this.autoMailMembers.setSource(new ArrayList<>());
            memberService.findByGroupId(group.getGroupId()).forEach((m) -> {
            if(!targetMemberId.contains(m.getMemberId())){
                String mail = this.memberService.getUserMailFristByUserId(m.getMemberId());
                if( !StringUtils.isEmpty(mail) ){
                    this.autoMailMembers.getSource().add(new SelectItem(m, m.getMemberNameFull(), mail));
                }
            }
        });
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onTransferMember(TransferEvent event) {
        List<SelectItem> existsItems = new ArrayList();
        if(null != this.autoMailMode) switch (this.autoMailMode) {
            case "to":
                existsItems = this.currentAutoMailConfigBean.getToList();
                break;
            case "cc":
                existsItems = this.currentAutoMailConfigBean.getCcList();
                break;
        }
        for(Object item : event.getItems()) {
            if(item instanceof SelectItem){
                boolean exists = false;
                for (Iterator<SelectItem> iterator = existsItems.iterator(); iterator.hasNext();) {
                    SelectItem si = iterator.next();
                    if(si.getValue().equals(((SelectItem)item).getValue())){
                        exists = true;
                        iterator.remove();
                        break;
                    }
                }
                if(!exists){
                    existsItems.add((SelectItem)item);
                }
            }
       }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void updatePickList() {
        if(null != this.autoMailMode) switch (this.autoMailMode) {
            case "to":
                this.currentAutoMailConfigBean.setToList(this.autoMailMembers.getTarget());
                break;
            case "cc":
                this.currentAutoMailConfigBean.setCcList(this.autoMailMembers.getTarget());
                break;
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void removeMember(AutoMailConfigBean item, SelectItem item2, String type){
        switch (type) {
            case "to":
                item.getToList().remove(item2);
                break;
            case "cc":
                item.getCcList().remove(item2);
                break;
        }
    }
}
