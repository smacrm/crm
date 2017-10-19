/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gnext.bean.customize.AutoFormItemGlobal;
import gnext.bean.customize.AutoFormTab;
import gnext.bean.mail.MailExplode;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.controller.common.LocaleController;
import gnext.model.BaseModel;
import gnext.model.authority.UserModel;
import gnext.model.mail.MailExplodeModel;
import gnext.model.mail.items.MailExplodeItem;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.customize.AutoFormPageTabDivItemService;
import gnext.service.mail.MailExplodeService;
import gnext.util.DateUtil;
import gnext.util.InterfaceUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.SelectUtil;
import gnext.utils.InterfaceUtil.COMPANY_TYPE;
import gnext.utils.InterfaceUtil.FIELDS;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "mailExplodeController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.MAIL, require = true)
public class MailExplodeController extends AbstractController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailExplodeController.class);
    private static final long serialVersionUID = -596855748587482331L;

    @ManagedProperty(value = "#{layout}") @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{localeController}") @Getter @Setter private LocaleController localeController;

    @EJB private MailExplodeService mailExplodeService;
    @EJB private AutoFormPageTabDivItemService autoFormPageTabDivItemService;
    
    @Getter @Setter private MailExplodeModel model;
    @Getter @Setter private List<MailExplodeModel> models = new ArrayList<>();
    @Getter @Setter private List<SelectItem> fieldExplodes = new ArrayList<>();

    private String oldMailExplodeTitle;
    
    private boolean _checkDuplicateMailExplodeTitle(MailExplode mf){
        // case insert
        if(mf.getMailExplodeId() == null && mailExplodeService.search(getCurrentCompanyId(), mf.getMailExplodeTitle()) != null){
            return true;
        // case edit
        }else {
            if(!mf.getMailExplodeTitle().equals(oldMailExplodeTitle)){
                if(mailExplodeService.search(getCurrentCompanyId(), mf.getMailExplodeTitle()) != null){
                    return true;
                }
            }
        }
        return false;
    }
    
    private void _Load(Integer mailExplodeId) {
        _Load();
        for (MailExplodeModel model : models) {
            if (model.getMailExplode().getMailExplodeId().intValue() == mailExplodeId.intValue()) {
                model.updateItemsFromJson();
                this.model = model;
                break;
            }
        }
    }

    private void _Load() {
        this.model = null;
        models.clear();
        List<MailExplode> mailExplodes = mailExplodeService.search(getCurrentCompanyId(), (short) 0);
        for (int i = 0; i < mailExplodes.size(); i++) {
            MailExplodeModel tmp = new MailExplodeModel(mailExplodes.get(i));
            
            if (mailExplodes.get(i).getMailExplodeOrder() == null) tmp.setRowNum(0);
            else tmp.setRowNum(mailExplodes.get(i).getMailExplodeOrder());
            
            this.models.add(tmp);
        }

        fieldExplodes.clear();
        short comFlag = UserModel.getLogined().getCompanyBusinessFlag();
        if(comFlag != COMPANY_TYPE.CUSTOMER) {
            fieldExplodes.add(_CreateSelectGroup(
                    JsfUtil.getResource().message(
                            UserModel.getLogined().getCompanyId()
                            , ResourceUtil.BUNDLE_ISSUE_NAME
                            , "label.search.tile.issue", (Object) null)
                    , SelectUtil.getArrayRIFieldExplodes().toArray(new SelectItem[]{})));
        }
        fieldExplodes.add(_CreateSelectGroup(
                JsfUtil.getResource().message(
                        UserModel.getLogined().getCompanyId()
                        , ResourceUtil.BUNDLE_ISSUE_NAME
                        , "label.search.tile.cust", (Object) null)
                , SelectUtil.getArrayCIFieldExplodes().toArray(new SelectItem[]{})));
        if(comFlag != COMPANY_TYPE.CUSTOMER) {
            _CreateDynamicSelectGroup();
        }
        
        // save to file..
//        saveToFile();
    }
    
    private SelectItemGroup _CreateSelectGroup(String name, SelectItem[] items) {
        SelectItemGroup group = new SelectItemGroup(name);
        group.setSelectItems(items);
        return group;
    }

    private void _CreateDynamicSelectGroup() {
        // TODO: Hien tai, he thong chi support cho module ISSUE.
        // can sua neu ap dung them cho cac module khac.
        Map<AutoFormTab, List<AutoFormItemGlobal>> m = autoFormPageTabDivItemService.getCustomizeList(getCurrentCompanyId(), localeController.getLocale());
        
        LinkedList<SelectItem> sis = new LinkedList<>();
        for (Map.Entry<AutoFormTab, List<AutoFormItemGlobal>> entry : m.entrySet()) {
            SelectItem tabSel = new SelectItem(entry.getKey().getTabId(), "--- " + entry.getKey().getTabName() + "--- ");
            tabSel.setDisabled(true);
            sis.add(tabSel);
            for(AutoFormItemGlobal afig : entry.getValue()) {
                SelectItem itemSel = new SelectItem(FIELDS.DYNAMIC + afig.getAutoFormItemGlobalPK().getItemId(), afig.getItemName());
                sis.add(itemSel);
            }
        }
        
        fieldExplodes.add(_CreateSelectGroup(
                JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_ISSUE_NAME, "label.search.tile.customize", (Object) null)
                , sis.toArray(new SelectItem[sis.size()])));
    }

    @PostConstruct
    public void init() {
        _Load();
    }

    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        layout.setCenter("/modules/mail/explode/index.xhtml");
    }

    @Override
    protected List<? extends BaseModel> getUFDLModels() {
        return models;
    }

    @Override
    protected void afterReSort() {
        try {
            for (MailExplodeModel mem : models) {
                mem.getMailExplode().setMailExplodeOrder(mem.getRowNum());
                mailExplodeService.edit(mem.getMailExplode());
            }
            _Load();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    @SecureMethod(SecureMethod.Method.VIEW)
    public void show(ActionEvent event) {
        String action = getParameter("showType");
        if ("edit".equalsIgnoreCase(action)) {
            Integer mailExplodeId = Integer.parseInt(getParameter("mailExplodeId"));
            MailExplodeModel mem = find(mailExplodeId);
            mem.updateItemsFromJson();
            this.model = mem;
            this.oldMailExplodeTitle = mem.getMailExplode().getMailExplodeTitle();
        } else if ("create".equalsIgnoreCase(action)) {
            this.model = new MailExplodeModel(new MailExplode());
            this.model.checkEmptyItems();
            this.oldMailExplodeTitle = null;
        }
    }

    @Override
    @SecureMethod(SecureMethod.Method.DELETE)
    public void delete(ActionEvent event) {
        try {
            Integer mailExplodeId = Integer.parseInt(getParameter("mailExplodeId"));
            MailExplodeModel mem = find(mailExplodeId);

            MailExplode me = mem.getMailExplode();
            me.setMailExplodeDeleted((short) 1);
            mailExplodeService.edit(me);
            models.remove(mem);

            // Kiểm tra nếu Explode là đang được chọn thì cần reload lại Page.
            if (this.model != null
                    && this.model.getMailExplode() != null
                    && this.model.getMailExplode().getMailExplodeId() != null
                    && mailExplodeId.intValue() == this.model.getMailExplode().getMailExplodeId().intValue()) {
                _Load();
            }
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG, "msg.action.delete.success", me.getMailExplodeTitle()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void update(ActionEvent event) {
        try {
            MailExplode me = this.model.getMailExplode();
            if(_checkDuplicateMailExplodeTitle(me)){
                this.model.getMailExplode().setMailExplodeTitle(oldMailExplodeTitle);
                focusMailExplodeTitle(me.getMailExplodeTitle());
                return;
            }
            if(!_checkMailExplodeConditionFormat(model.getExplodeItems())){
                focusMailExplodeCondition(me.getMailExplodeConditions());
                return;
            }
            if(_checkDupplicateFirstCharCondition(this.model.getCondition())){
                focusMailExplodeCondition(me.getMailExplodeConditions());
                return;
            }
            me.setMailExplodeConditions(this.model.getCondition());
            me.setUpdatedId(UserModel.getLogined().getMember().getMemberId());
            me.setMailExplodeUpdatedDatetime(DateUtil.now());
            MailExplode savedMailExplode = mailExplodeService.edit(me);
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG,
                    "msg.action.update.success", savedMailExplode.getMailExplodeTitle()));
            _Load();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void focusMailExplodeTitle(String value){
        UIComponent component = JsfUtil.findComponent("txtMailExplodeTitle");
        if(component == null) return;
        UIInput uIInput = (UIInput) component;
        uIInput.setValid(false);
        uIInput.setValue(value);
        String title = uIInput.getAttributes().get("title").toString();
        JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.mailexplode.duplicate", title));
    }
    
    private void focusMailExplodeCondition(String value){
        UIComponent component = JsfUtil.findComponent("txtMailExplodeCondition");
        if(component == null) return;
        UIInput uIInput = (UIInput) component;
        uIInput.setValid(false);
        uIInput.setValue(value);
        String title = uIInput.getAttributes().get("title").toString();
        JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.mailexplode.format", title));
    }
    
    @Override
    @SecureMethod(SecureMethod.Method.CREATE)
    public void save(ActionEvent event) {
        try {
            MailExplode me = this.model.getMailExplode();
            if(_checkDuplicateMailExplodeTitle(me)){
                focusMailExplodeTitle(me.getMailExplodeTitle());
                return;
            }
            if(!_checkMailExplodeConditionFormat(model.getExplodeItems())){
                focusMailExplodeCondition(me.getMailExplodeConditions());
                return;
            }
            if(_checkDupplicateFirstCharCondition(this.model.getCondition())){
                focusMailExplodeCondition(me.getMailExplodeConditions());
                return;
            }
            me.setMailExplodeConditions(this.model.getCondition());
            me.setCreatorId(UserModel.getLogined().getMember().getMemberId());
            me.setMailExplodeCreatedDatetime(DateUtil.now());
            me.setMailExplodeDeleted((short) 0);
            me.setCompany(getCurrentCompany());
            // set mail order
            Integer maxOrder = mailExplodeService.getMaxOrder(getCurrentCompanyId());
            if (maxOrder == null) maxOrder = 0;
            me.setMailExplodeOrder(maxOrder + 1);

            MailExplode savedMailExplode = mailExplodeService.create(me);
            _Load();
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG,
                    "msg.action.create.success", savedMailExplode.getMailExplodeTitle()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @SecureMethod(SecureMethod.Method.COPY)
    public void copy(ActionEvent event) {
        try {
            Integer mailExplodeId = Integer.parseInt(getParameter("mailExplodeId"));
            MailExplode me = mailExplodeService.find(mailExplodeId);
            me.setMailExplodeTitle(me.getMailExplodeTitle() + " COPY");
            me.setMailExplodeId(null);
            mailExplodeService.create(me);
            _Load(me.getMailExplodeId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @SecureMethod(SecureMethod.Method.DELETE)
    public void removeItem(Integer itemId) {
        List<MailExplodeItem> items = model.getExplodeItems();
        for (MailExplodeItem item : items) {
            if (item.getId().intValue() == itemId.intValue()) {
                items.remove(item);
                break;
            }
        }
        this.model.checkEmptyItems();
    }

    @SecureMethod(SecureMethod.Method.CREATE)
    public void addItem() {
        MailExplodeItem item = new MailExplodeItem();
        item.setSecondChar(InterfaceUtil.EXPLODE_RULE.EOL);
        item.setId(model.getExplodeItems().size());
        model.addItem(item);
    }

    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private MailExplodeModel find(Integer mailExplodeId) {
        for (MailExplodeModel mem : models) {
            if (mem.getMailExplode().getMailExplodeId().intValue()
                    == mailExplodeId.intValue()) {
                return mem;
            }
        }

        return null;
    }

    public boolean showTipPage() {
        return this.model == null;
    }

    public boolean showBackButton() {
        return this.showAddPage() || this.showEditPage();
    }

    public boolean showAddPage() {
        return this.model != null
                && this.model.getMailExplode() != null
                && this.model.getMailExplode().getMailExplodeId() == null;
    }

    public boolean showEditPage() {
        return this.model != null
                && this.model.getMailExplode() != null
                && this.model.getMailExplode().getMailExplodeId() != null;
    }

    public String activeMenu(Integer mailExplodeId) {
        if (this.model != null
                && this.model.getMailExplode() != null
                && this.model.getMailExplode().getMailExplodeId() != null
                && mailExplodeId.intValue() == this.model.getMailExplode().getMailExplodeId().intValue()) {
            return "bold";
        }

        return "";
    }

    private boolean _checkMailExplodeConditionFormat(List<MailExplodeItem> explodeItems) {
        for (MailExplodeItem item : explodeItems) {
            String secondChar = item.getSecondChar().trim();
            item.setSecondChar(secondChar);
            if (InterfaceUtil.EXPLODE_RULE.EOF.equals(item.getSecondChar()) ||
                    InterfaceUtil.EXPLODE_RULE.EOL.equals(item.getSecondChar())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean _checkDupplicateFirstCharCondition(String mailExplodeCondition) {
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(mailExplodeCondition, JsonArray.class);
        int size = jsonArray.size();
        for (int i = 0; i < size; i++) {
            JsonObject jsonObjectSrc = jsonArray.get(i).getAsJsonObject();
            for (int j = i + 1; j < size; j++) {
                JsonObject jsonObject = jsonArray.get(j).getAsJsonObject();
                if (jsonObjectSrc.get("firstChar").equals(jsonObject.get("firstChar"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
