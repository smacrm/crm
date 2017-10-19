/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.issue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.Prefecture;
import gnext.bean.customize.AutoFormItemGlobal;
import gnext.controller.AbstractController;
import gnext.controller.ObserverLanguageController;
import gnext.controller.common.LayoutController;
import gnext.controller.common.LoginController;
import gnext.controller.system.MaintenanceController;
import gnext.bean.issue.IssueLamp;
import gnext.bean.issue.IssueLampGlobal;
import gnext.bean.mente.MenteItem;
import gnext.bean.project.DynamicColumn;
import gnext.model.authority.UserModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.GroupService;
import gnext.service.MemberService;
import gnext.service.PrefectureService;
import gnext.service.customize.AutoFormItemService;
import gnext.service.issue.IssueLampService;
import gnext.service.issue.IssueService;
import gnext.service.mente.MenteService;
import gnext.util.DateUtil;
import gnext.util.DateUtil.SYMBOL;
import gnext.util.HTTPResReqUtil;
import gnext.util.IssueUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.SelectUtil;
import gnext.util.StringUtil;
import gnext.utils.InterfaceUtil.COLS;
import static gnext.utils.InterfaceUtil.CUST_STATUS;
import gnext.utils.InterfaceUtil.FIELDS;
import gnext.utils.InterfaceUtil.FIELD_TYPE;
import gnext.utils.InterfaceUtil.SELECT_LEVEL;
import gnext.utils.LabelValue;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tungdt
 */
@ManagedBean(name = "issueLampController", eager = true)
@ViewScoped()
@SecurePage(module = SecurePage.Module.CUSTOMIZE, require = false)
public class IssueLampController extends AbstractController implements ObserverLanguageController {
    private static final long serialVersionUID = -719149308156211289L;
    private static final Logger logger = LoggerFactory.getLogger(IssueLampController.class);
    
    @ManagedProperty("#{maintenanceController}")
    @Getter @Setter private MaintenanceController maintenanceController;
    @ManagedProperty(value = "#{projectController}")
    @Getter @Setter private ProjectController projectController;

    @EJB private IssueLampService issueLampService;
    @EJB private MemberService memberService;
    @EJB private GroupService groupService;
    @EJB private MenteService menteService;
    @EJB private PrefectureService prefectureService;
    @EJB private IssueService issueService;
    @EJB private AutoFormItemService autoFormItemServiceImpl;

    @Getter @Setter private List<MenteItem> menteList = new ArrayList<>();
    @Getter @Setter private List<IssueLamp> issueLamps = new ArrayList<>();
    @Getter @Setter private List<Group> allGroupList = new ArrayList<>();
    @Getter @Setter private List<Member> allMemberList = new ArrayList<>();
    @Getter @Setter private List<Prefecture> prefectures = new ArrayList<>();
    @Getter @Setter private List<AutoFormItemGlobal> dynamicList =  new ArrayList<>();
    @Getter @Setter private List<DynamicColumn> availableColumns = new ArrayList<>();
    @Getter @Setter private List<SelectItem> proposalItems;

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @ManagedProperty(value = "#{loginController}")
    @Getter @Setter
    private LoginController loginController;

    @PostConstruct
    public void init() {
        load();
    }

    @Override
    public void search() {
        try {
            this.maintenanceController.setCurrentStaticParent("project_pass_days");
            load();
        } catch (Exception ex) {
            logger.error("[IssueLampController.search()]", ex);
        }
    }

    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addStaticRow() {
        IssueLamp newObj = new IssueLamp();
        newObj.setCompany(getCurrentCompany());
        newObj.setLampColor("ff0000");
        newObj.setLampColor("000000");
        newObj.setLocale(maintenanceController.getLocale());
        LabelValue lv = getLabelValue();
        setLabelValue(lv);
        newObj.getLampsItemList().add(lv);
        issueLamps.add(newObj);
        RequestContext.getCurrentInstance().execute("jQuery('.dataTableProjectPassDays .editable:last span.ui-icon-pencil').trigger('click'); jQuery('.dataTableProjectPassDays .editable-input:last').focus();");
    }

    @SecureMethod(value = SecureMethod.Method.UPDATE)
    public void onRowCancel(RowEditEvent event) {
        IssueLamp issueLamp = (IssueLamp) event.getObject();
        if (issueLamp.getLampId() == null) {
            issueLamps.remove(issueLamp);
        }
        reLoadLampList();
    }

    @SecureMethod(value = SecureMethod.Method.UPDATE)
    public void onRowEdit(RowEditEvent event) {
        try {
            IssueLamp issueLamp = (IssueLamp) event.getObject();
            if(issueLamp == null || checkForm(issueLamp)) return;
            String data = getCompanyList(issueLamp, issueLamp.getItemName());
            if(StringUtils.isEmpty(data)) return;
            if(StringUtils.isBlank(issueLamp.getLocale())) issueLamp.setLocale(maintenanceController.getLocale());
            issueLamp.setItemName(data);
            
            if (issueLamp.getLampId() == null) {
                issueLampService.insert(issueLamp);
            } else {
                issueLampService.update(issueLamp);
            }
            reLoadLampList();
//            loginController.getMember().getCompany().setIssueLampList(this.issueLamps);
        } catch (Exception e) {
//            this.issueLamps = issueLampService.findIssueLamps(getCurrentCompanyId());
            reLoadLampList();
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.system.error"));
            logger.error(e.getMessage(), e);
        }
    }

    public void delete(IssueLamp issueLamp) {
        try {
            if (issueLamp.getLampId() != null) {
                issueLampService.remove(issueLamp);
                this.issueLamps = issueLampService.findIssueLamps(getCurrentCompanyId());
//                addProposalName();
                loginController.getMember().getCompany().setIssueLampList(this.issueLamps);
            }else {
                this.issueLamps.remove(issueLamp);
            }   
            reLoadLampList();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(),
                    ResourceUtil.BUNDLE_MSG, "msg.system.error"));
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onChangeLanguage() {
//        issueLamps = issueLampService.findIssueLamps(getCurrentCompanyId());
        load();
    }

    public void load() {
        if (!this.maintenanceController.getLcs().contains(this)) {
            this.maintenanceController.getLcs().add(this);
        }
        if (this.maintenanceController.getLocale() == null) {
            this.maintenanceController.setLocale(UserModel.getLogined().getLanguage());
        }
        if (this.maintenanceController.getStaticColumnsDataLvl2() != null) {
            this.maintenanceController.setStaticColumnsDataLvl2(null);
        }
//        this.availableColumns = this.projectController.getListAllVisibleColumn();
        resetAvailableColumns();
        this.prefectures = this.prefectureService.findCities(this.maintenanceController.getLocale());
        this.allGroupList = this.groupService.findByGroupIsExists(getCurrentCompanyId());
        this.allMemberList = this.memberService.findByCompanyId(getCurrentCompanyId());
        this.menteList = this.menteService.getAllLevels(getCurrentCompanyId());
        this.dynamicList = this.autoFormItemServiceImpl.search(getCurrentCompanyId(), this.maintenanceController.getLocale());
        this.issueLamps = this.issueLampService.findIssueLamps(getCurrentCompanyId());
        if (this.issueLamps == null || this.issueLamps.size() <= 0) {
            this.issueLamps = new ArrayList<>();
        } else {
            reLoadLampList();
        }

        this.proposalItems = this.issueService.getList(COLS.PROPOSAL, UserModel.getLogined().getCompanyId(), this.maintenanceController.getLocale());
//        addProposalName();
//        if(this.issueLamps != null && this.issueLamps.size() > 0
//                && this.proposalItems != null && this.proposalItems.length > 0) {
//            for(IssueLamp lamp:this.issueLamps) {
//                if(lamp == null || lamp.getLampProposalId() == null) continue;
//                String val = SelectUtil.getLabelById(this.proposalItems, lamp.getLampProposalId());
//                if(val == null) val = StringUtils.EMPTY;
//                lamp.setLampProposalName(val);
//            }
//        }
    }

    public void addLampsItemList(IssueLamp lamp) {
        LabelValue lv = getLabelValue();
        setLabelValue(lv);
        lamp.getLampsItemList().add(lv);
    }

    @SuppressWarnings("element-type-mismatch")
    public void deleteLampsItemList(IssueLamp lamp, LabelValue item) {
        if(item == null || StringUtils.isEmpty(item.getLabel())) return;
        for(int i=0; i<lamp.getLampsItemList().size(); i++) {
            if(lamp.getLampsItemList().get(i).getLabel().equals(item.getLabel())) {
                lamp.getLampsItemList().remove(item);
                break;
            }
        }
    }

    public void onChangeLampsItemList(AjaxBehaviorEvent event) {
        String lampIdx = HTTPResReqUtil.getRequestParameter("lampIdx");
        String lampDataIdx = HTTPResReqUtil.getRequestParameter("lampDataIdx");
        if(!NumberUtils.isDigits(lampIdx) || !NumberUtils.isDigits(lampDataIdx)) return;
        SelectOneMenu change = (SelectOneMenu) event.getSource();
        String label = (String) change.getValue();
        if(this.issueLamps == null || this.issueLamps.size() <= Integer.valueOf(lampIdx)) return;
        List<LabelValue> items = this.issueLamps.get(Integer.valueOf(lampIdx)).getLampsItemList();
        if(items == null || items.size() <= Integer.valueOf(lampDataIdx)) return;
        LabelValue lv = items.get(Integer.valueOf(lampDataIdx));
        lv.setLabel(label);
        lv.setType(String.valueOf(getFieldType(label)));
        lv.setValue(null);
        setLabelValue(lv);
    }

    public void onDateViewChange(SelectEvent event) {
        String lampIdx = HTTPResReqUtil.getRequestParameter("lampIdx");
        String lampDataIdx = HTTPResReqUtil.getRequestParameter("lampDataIdx");
        String value = HTTPResReqUtil.getRequestParameter("value");
        if(!NumberUtils.isDigits(lampIdx) || !NumberUtils.isDigits(lampDataIdx)) return;
        SimpleDateFormat format = new SimpleDateFormat(DateUtil.getDate(this.maintenanceController.getLocale(), SYMBOL.SLASH));
        if(this.issueLamps == null || this.issueLamps.size() <= Integer.valueOf(lampIdx)) return;
        List<LabelValue> items = this.issueLamps.get(Integer.valueOf(lampIdx)).getLampsItemList();
        if(items == null || items.size() <= Integer.valueOf(lampDataIdx)) return;
        if("0".equals(value)) {
            items.get(Integer.valueOf(lampDataIdx)).setValue(format.format(event.getObject()));
        } else {
            items.get(Integer.valueOf(lampDataIdx)).setValueTo(format.format(event.getObject()));
        }
    }

    public void onLevelChange(AjaxBehaviorEvent event) {
        String lampIdx = HTTPResReqUtil.getRequestParameter("lampIdx");
        String lampDataIdx = HTTPResReqUtil.getRequestParameter("lampDataIdx");
        if(!NumberUtils.isDigits(lampIdx) || !NumberUtils.isDigits(lampDataIdx)) return;
        SelectOneMenu change = (SelectOneMenu) event.getSource();
        String level = (String) change.getValue();
        if(this.issueLamps == null || this.issueLamps.size() <= Integer.valueOf(lampIdx)) return;
        List<LabelValue> items = this.issueLamps.get(Integer.valueOf(lampIdx)).getLampsItemList();
        if(items == null || items.size() <= Integer.valueOf(lampDataIdx)) return;
        LabelValue lv = items.get(Integer.valueOf(lampDataIdx));
        lv.setLevel(level);
        lv.setValue(null);
       if(lv.getLabel().startsWith("issue_product_")
                || (StringUtils.isEmpty(lv.getDynamicKey()) && lv.getDynamicKey().startsWith("issue_product_"))) {
            String gl = lv.getLabel().startsWith(FIELDS.DYNAMIC)?lv.getDynamicKey():lv.getLabel();
            setProductList(lv, gl, null);
        } else if(lv.getLabel().startsWith("issue_proposal_")
                || (StringUtils.isEmpty(lv.getDynamicKey()) && lv.getDynamicKey().startsWith("issue_proposal_"))) {
            String gl = lv.getLabel().startsWith(FIELDS.DYNAMIC)?lv.getDynamicKey():lv.getLabel();
            setProposalList(lv, gl, null);
        }
    }

    public void onSelfChange(AjaxBehaviorEvent event) {
        String lampIdx = HTTPResReqUtil.getRequestParameter("lampIdx");
        String lampDataIdx = HTTPResReqUtil.getRequestParameter("lampDataIdx");
        if(!NumberUtils.isDigits(lampIdx) || !NumberUtils.isDigits(lampDataIdx)) return;
        SelectOneMenu change = (SelectOneMenu) event.getSource();
        String val = (String) change.getValue();
        if(!NumberUtils.isDigits(val) || this.issueLamps == null || this.issueLamps.size() <= Integer.valueOf(lampIdx)) return;
        List<LabelValue> items = this.issueLamps.get(Integer.valueOf(lampIdx)).getLampsItemList();
        if(items == null || items.size() <= Integer.valueOf(lampDataIdx)) return;
        LabelValue lv = items.get(Integer.valueOf(lampDataIdx));
        String label = lv.getLabel();
        if(label.equals("cust_city")) {
            setViewValuePrefectures(lv, val);
        } else if(label.equals("issue_receive_person_group_name") || COLS.GROUP.equals(lv.getDynamicKey())) {
            setViewValueGroup(lv, val);
        } else if(label.equals("issue_authorizer_name")
                    || label.equals("issue_receive_person_name")
                    || label.equals("issue_creator_name")
                    || label.equals("issue_updated_name")
                    || COLS.USER.equals(lv.getDynamicKey())) {
            setViewValueUser(lv, val);
        } else {
            MenteItem item = SelectUtil.getMenteById(lv.getItemMenteList(), Integer.valueOf(val));
            lv.setViewValue(item.getItemName());
        }
    }
 
    @SuppressWarnings("UnusedAssignment")
    public String getCompanyList(IssueLamp lamp, String name){
        if(StringUtils.isEmpty(name) || lamp == null || lamp.getLampsItemList().isEmpty()) return StringUtils.EMPTY;
        List<LabelValue> items = lamp.getLampsItemList();
        if(items == null || items.size() <= 0) {
            JsonObject jo = new JsonObject();
            jo.addProperty("name", name);
            jo.addProperty("order", 0);
            jo.add("object", new JsonObject());
            Gson gson = new Gson();
            return gson.toJson(jo);
        }
        JsonArray json = new JsonArray();
        JsonObject jo = new JsonObject();
        Integer order = 0;
        for(LabelValue t:items) {
            JsonObject joc = new JsonObject();
            joc.addProperty("label", t.getLabel());
            joc.addProperty("type", t.getType());
            joc.addProperty("level", NumberUtils.isDigits(t.getLevel())?t.getLevel():"0");
            String value = t.getValue();
            String viewValue = t.getViewValue();
            int type = Integer.valueOf(t.getType());
            if(type == FIELD_TYPE.DATE) {
                viewValue = String.format("%s%s%s", value, SYMBOL.FROMTO, t.getValueTo());
                if(t.getIsSearchPeriod()) {
                    value = StringUtil.getMysqlDateAddSup(t.getLampDatePeriodBeforeAfter(), t.getLampDateSearchPeriod());
                } else {
                    value = String.format("%s%s%s", value, SYMBOL.FROMTO, t.getValueTo());
                }
            }
            joc.addProperty("value", value);

            if(type == FIELD_TYPE.SELECT && (t.getLabel().startsWith("issue_proposal_") || t.getLabel().startsWith("issue_product_"))) {
                LabelValue nlv = t;
                if(t.getLabel().startsWith("issue_proposal_")) {
                    setProposalList(nlv, t.getLabel(), null);
                } else {
                    setProductList(nlv, t.getLabel(), null);
                }
                MenteItem mente = SelectUtil.getMenteById(nlv.getItemMenteList(), Integer.valueOf(t.getValue()));
                viewValue = mente.getItemViewData(this.maintenanceController.getLocale());
            } else if(StringUtils.isEmpty(viewValue)) {
                LabelValue nlv = t;
                setLabelValue(nlv);
                viewValue = nlv.getViewValue();
            }
            joc.addProperty("viewValue", viewValue);
            json.add(joc);
            order++;
        }
        lamp.setLampOrder(order);
        jo.addProperty("name", name);
        jo.add("object", json);
        
        Gson gson = new Gson();
        return gson.toJson(jo);
    }

//    private void addProposalName() {
//        if(this.issueLamps != null && this.issueLamps.size() > 0
//                && this.proposalItems != null && !this.proposalItems.isEmpty()) {
//            for(IssueLamp lamp:this.issueLamps) {
//                if(lamp == null || lamp.getLampProposalId() == null) continue;
//                lamp.setLampProposalName(
//                        (lamp.getLampProposalId() == 2)
//                        ?JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.condition.or", (Object) null)
//                        :JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.condition.and", (Object) null));
//            }
//        }
//    }

    private Integer getFieldType(String field) {
        if(StringUtils.isEmpty(field)) return FIELD_TYPE.TEXT;
        for(DynamicColumn d:this.availableColumns) {
            if(d == null || !field.equals(d.getId())) continue;
            return d.getType();
        }
        return FIELD_TYPE.TEXT;
    }

    private LabelValue getLabelValue() {
        LabelValue lv = new LabelValue(StringUtils.EMPTY, COLS.PROPOSAL.replace("_id", "_name"), String.valueOf(FIELD_TYPE.SELECT));
        if(UserModel.getLogined().getCompanyCustomerMode()) {
            lv = new LabelValue(String.valueOf(CUST_STATUS.ADD_APPLICATION), COLS.CUST_STATUS, String.valueOf(FIELD_TYPE.SELECT));
        }
        return lv;
    }

    private void setLabelValue(LabelValue lv) {
        if(lv == null || StringUtils.isEmpty(lv.getLabel())) return;
        String label = lv.getLabel();
        String val = lv.getValue();
        int type = NumberUtils.isDigits(lv.getType())?Integer.valueOf(lv.getType()):FIELD_TYPE.TEXT;
        if(!label.startsWith("issue_product_") && !label.startsWith("issue_proposal_")) {
            lv.setLevel("0");
        }
        if(type == FIELD_TYPE.DATE) {
            @SuppressWarnings("UnusedAssignment")
            String valFrom = new String(), valTo = new String();
            if(val.startsWith("-") || val.startsWith("+")) {
                lv.setIsSearchPeriod(true);
                lv.setLampDateSearchPeriod(Integer.valueOf(val.replace("-", "").replace("+", "")));
                String patten = DateUtil.getDate(this.maintenanceController.getLocale(), SYMBOL.SLASH);
                Integer addSup = Integer.valueOf(lv.getValue());
                Date date = DateUtil.getAddSupDate(addSup);
                if(addSup <= 0) {
                    lv.setLampDatePeriodBeforeAfter(false);
                    valFrom = DateUtil.getDateToString(date, patten);
                    valTo = DateUtil.getDateNowToString(this.maintenanceController.getLocale());
                    lv.setViewValue(String.format("%s%s%s", valFrom, SYMBOL.FROMTO, valTo));
                } else {
                    lv.setLampDatePeriodBeforeAfter(true);
                    valFrom = DateUtil.getDateNowToString(this.maintenanceController.getLocale());
                    valTo = DateUtil.getDateToString(date, patten);
                    lv.setViewValue(String.format("%s%s%s", valFrom, SYMBOL.FROMTO, valTo));
                }
            } else {
                String[] vals = val.split(SYMBOL.FROMTO);
                if(vals != null && vals.length == 2) {
                    valFrom = DateUtil.getDateYYYYMMDD(vals[0], this.maintenanceController.getLocale(), SYMBOL.SLASH);
                    valTo = DateUtil.getDateYYYYMMDD(vals[1], this.maintenanceController.getLocale(), SYMBOL.SLASH);
                    lv.setViewValue(String.format("%s%s%s", valFrom, SYMBOL.FROMTO, valTo));
                } else {
                    valFrom = DateUtil.getDateYYYYMMDD(vals[0], this.maintenanceController.getLocale(), SYMBOL.SLASH);
                    lv.setViewValue(String.format("%s%s", valFrom, SYMBOL.FROMTO));
                }
            }
            lv.setValue(valFrom);
            lv.setValueTo(valTo);
        }
        DynamicColumn dc = SelectUtil.getDynamicColumn(this.availableColumns, label);
        lv.setViewLabel(dc==null?StringUtils.EMPTY:dc.getName());
        if(!label.startsWith(FIELDS.DYNAMIC)
                && type != FIELD_TYPE.SELECT
                && type != FIELD_TYPE.RADIO_GROUP
                && type != FIELD_TYPE.CHECKBOX_GROUP) {
            if(type != FIELD_TYPE.DATE) lv.setViewValue(val);
            return;
        }
        lv.setDynamicKey(new String());
        List<MenteItem> list = new ArrayList<>();
        if(label.startsWith("issue_proposal_name")) {
            setProposalList(lv, label, val);
        } else if(label.startsWith("issue_product_name")) {
            setProductList(lv, label, val);
        } else if(label.equals("cust_city")) {
            setViewValuePrefectures(lv, val);
        } else if(label.equals("cust_status")) {
            setCustStatusList(lv, val);
        } else if(label.equals("issue_receive_person_group_name")) {
            setViewValueGroup(lv, val);
        } else if(label.equals("issue_authorizer_name")
                    || label.equals("issue_receive_person_name")
                    || label.equals("issue_creator_name")
                    || label.equals("issue_updated_name")) {
            setViewValueUser(lv, val);
        } else if(label.startsWith(FIELDS.DYNAMIC)) {
            if(type != FIELD_TYPE.DATE) lv.setViewValue(val);
            String[] ids = label.split(FIELDS.DYNAMIC);
            if(ids != null && ids.length >= 2
                    && (type == FIELD_TYPE.SELECT
                    || type == FIELD_TYPE.RADIO_GROUP
                    || type == FIELD_TYPE.CHECKBOX_GROUP)) {
                String id = ids[1];
                if(NumberUtils.isDigits(id)) {
                    AutoFormItemGlobal item = SelectUtil.getAutoFormItemById(this.dynamicList, Integer.valueOf(id));
                    if(item != null && !StringUtils.isEmpty(item.getItemDataDefault())) {
                        lv.setType(String.valueOf(item.getAutoFormItem().getItemType()));
                        Gson gson = new Gson();
                        JsonArray jsonArray = gson.fromJson(item.getItemDataDefault(), JsonArray.class);
                        boolean notAddList = false;
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JsonObject obj = (JsonObject) jsonArray.get(i);
                            String labelObj = obj.get("label").getAsString();
                            String valueObj = obj.get("value").getAsString();
                            if(StringUtils.isEmpty(valueObj) && StringUtils.isEmpty(labelObj)) continue;

//                            System.out.println(labelObj);
//                            System.out.println(valueObj);
                            switch (valueObj) {
                               case COLS.USER:
                                   lv.setDynamicKey(COLS.USER);
                                   setViewValueUser(lv, val);
                                   break;
                               case COLS.GROUP:
                                   lv.setDynamicKey(COLS.GROUP);
                                   setViewValueGroup(lv, val);
                                   break;
                               default:
                                   MenteItem itemObj = new MenteItem();
                                   if(StringUtils.isBlank(labelObj) && (valueObj.startsWith("issue_") || valueObj.startsWith("cust_"))) {
                                       notAddList = true;
                                       lv.setDynamicKey(valueObj);
                                       setLabelValueList(list, lv, valueObj, val);
                                   } else if(NumberUtils.isDigits(valueObj)) {
                                       itemObj.setItemId(Integer.valueOf(valueObj));
                                       itemObj.setItemName(labelObj);
                                       if(NumberUtils.isDigits(val) && valueObj.equals(val)) {
                                           lv.setViewValue(labelObj);
                                       }
                                       list.add(itemObj);
                                   }
                                   break;
                               }
                        }
                        if(!notAddList) lv.setItemMenteList(list);
                    }
                }
            }
        } else {
            setLabelValueList(list, lv, label, val);
        }
    }

    private void setViewValueGroup(LabelValue lv, String value) {
        if(this.allGroupList.isEmpty()) return;
        if(StringUtils.isEmpty(value))
            value = String.valueOf(this.allGroupList.get(0).getGroupId());
        for(Group g:this.allGroupList) {
            if(g == null || !Objects.equals(Integer.valueOf(value), g.getGroupId())) continue;
            lv.setViewValue(g.getGroupName());
            break;
        }
    }

    private void setViewValueUser(LabelValue lv, String value) {
        if(this.allMemberList.isEmpty()) return;
        if(StringUtils.isEmpty(value))
            value = String.valueOf(this.allMemberList.get(0).getMemberId());
        for(Member m:this.allMemberList) {
            if(m == null || !Objects.equals(Integer.valueOf(value), m.getMemberId())) continue;
            lv.setViewValue(String.format("%s%s"
                    , StringUtils.isBlank(m.getMemberNameFirst())?StringUtils.EMPTY:m.getMemberNameFirst()
                    , StringUtils.isBlank(m.getMemberNameFirst())?StringUtils.EMPTY:m.getMemberNameLast()));
            break;
        }
    }

    private void setViewValuePrefectures(LabelValue lv, String value) {
        if(this.prefectures.isEmpty()) return;
        if(StringUtils.isEmpty(value))
            value = String.valueOf(this.prefectures.get(0).getPrefectureCode());
        for(Prefecture p:this.prefectures) {
            if(p == null || !value.equals(p.getPrefectureCode())) continue;
            lv.setViewValue(p.getPrefectureName());
            break;
        }
    }

    private void setCustStatusList(LabelValue lv, String value) {
        List<SelectItem> items = SelectUtil.getCustStatusList();
        List<MenteItem> list =  new ArrayList<>();
        for(SelectItem item:items) {
            if(item == null || item.getValue() == null) continue;
            MenteItem mt = new MenteItem();
            mt.setItemId((Integer) item.getValue());
            mt.setItemName(item.getLabel());
            list.add(mt);
            if(NumberUtils.isDigits(value) && Objects.equals(Integer.valueOf(value), item.getValue())) {
                lv.setViewValue(item.getLabel());
            }
        }
        lv.setItemMenteList(list);
    }

    private void setLabelValueList(List<MenteItem> list, LabelValue lv, String label, String value) {
        if(lv == null || StringUtils.isEmpty(label)) return;
        String key = label.endsWith("_name")?label.replace("_name","_id"):label;
        if(!StringUtils.isEmpty(key))
            list = IssueUtil.getMenteListByNameAndLevel(this.menteList, key, SELECT_LEVEL.ONE);
        if(!list.isEmpty() && StringUtils.isEmpty(value)) {
            value = String.valueOf(list.get(0).getItemId());
        }
        if(NumberUtils.isDigits(value)) {
            MenteItem mente = SelectUtil.getMenteById(list, Integer.valueOf(value));
            if(mente != null)
                lv.setViewValue(mente.getItemViewData(this.maintenanceController.getLocale()));
        }
        lv.setItemMenteList(list);
    }

    private void setProposalList(LabelValue lv, String label, String value) {
        if(lv == null || StringUtils.isEmpty(label)) return;
        Integer level = lv.getLevel()==null?1:Integer.valueOf(lv.getLevel());
        List<MenteItem> list = IssueUtil.getMenteListByNameAndLevel(this.menteList, COLS.PROPOSAL, level);
        if(!list.isEmpty() && StringUtils.isEmpty(value)) {
            value = String.valueOf(list.get(0).getItemId());
        }
        if(NumberUtils.isDigits(value)) {
            MenteItem mente = SelectUtil.getMenteById(list, Integer.valueOf(value));
            if(mente != null)
                lv.setViewValue(mente.getItemViewTreeName(this.maintenanceController.getLocale()));
        }
        lv.setItemMenteList(list);
    }

    private void setProductList(LabelValue lv, String label, String value) {
        if(lv == null || StringUtils.isEmpty(label)) return;
        Integer level = lv.getLevel()==null?1:Integer.valueOf(lv.getLevel());
        List<MenteItem> list = IssueUtil.getMenteListByNameAndLevel(this.menteList, COLS.PRODUCT, level);
        if(!list.isEmpty() && StringUtils.isEmpty(value)) {
            value = String.valueOf(list.get(0).getItemId());
        }
        if(NumberUtils.isDigits(value)) {
            MenteItem mente = SelectUtil.getMenteById(list, Integer.valueOf(value));
            if(mente != null)
                lv.setViewValue(mente.getItemViewTreeName(this.maintenanceController.getLocale()));
        }
        lv.setItemMenteList(list);
    }

    private void resetAvailableColumns() {
        List<DynamicColumn> allField = this.projectController.getListAllVisibleColumn();
        if(allField == null || allField.size() <= 0) return;
        List<DynamicColumn> ava = new ArrayList<>();
        for(DynamicColumn dc:allField) {
            if(dc == null
                    || dc.getId().equals("issue_proposal_name_2")
                    || dc.getId().equals("issue_proposal_name_3")
                    || dc.getId().equals("issue_product_name_2")
                    || dc.getId().equals("issue_product_name_3")
                    || dc.getId().equals("issue_receive_person_group_name")) continue;
            if(dc.getId().startsWith("issue_proposal_name_1")) {
                dc.setId(COLS.PROPOSAL.replace("_id", "_name"));
                dc.setType(FIELD_TYPE.SELECT);
                dc.setName(JsfUtil.getResource().message(getCurrentCompanyId(), ResourceUtil.BUNDLE_ISSUE_NAME, "label." + COLS.PROPOSAL, (Object) null));
            } else if(dc.getId().startsWith("issue_product_name_1")) {
                dc.setId(COLS.PRODUCT.replace("_id", "_name"));                
                dc.setType(FIELD_TYPE.SELECT);
                dc.setName(JsfUtil.getResource().message(getCurrentCompanyId(), ResourceUtil.BUNDLE_ISSUE_NAME, "label." + COLS.PRODUCT, (Object) null));
            } else if(dc.getId().startsWith(COLS.CUST_STATUS)) {
                dc.setId(COLS.CUST_STATUS);                
                dc.setType(FIELD_TYPE.SELECT);
                dc.setName(JsfUtil.getResource().message(getCurrentCompanyId(), ResourceUtil.BUNDLE_CUSTOMER_NAME, "label." + COLS.CUST_STATUS, (Object) null));
            }
//            System.err.println(dc.getId());
            ava.add(dc);
        }
        this.setAvailableColumns(ava);
    }

    private void reLoadLampList() {
//        this.issueLamps = issueLampService.findIssueLamps(getCurrentCompanyId());
        loginController.getMember().getCompany().setIssueLampList(this.issueLamps);
        for(IssueLamp lamp:this.issueLamps) {
            if(lamp == null) continue;
            if(lamp.getLampProposalId()==null) lamp.setLampProposalId(1);//AND OR
            if(lamp.getIssueLampsGlobal().isEmpty() || !NumberUtils.isDigits(String.valueOf(lamp.getLampProposalId()))) {
                lamp.getLampsItemList().add(new LabelValue(COLS.PROPOSAL.replace("_id", "_name_1"), "", String.valueOf(FIELD_TYPE.SELECT)));
            } else {
                int conditions = lamp.getLampProposalId();
                lamp.setLampProposalName((conditions==2)
                        ?JsfUtil.getResource().message(getCurrentCompanyId(), ResourceUtil.BUNDLE_MSG, "label.condition.or", (Object) null)
                        :JsfUtil.getResource().message(getCurrentCompanyId(), ResourceUtil.BUNDLE_MSG, "label.condition.and", (Object) null));
                List<IssueLampGlobal> getList = lamp.getIssueLampsGlobal();
                for(IssueLampGlobal lampGlobal:getList) {
                    if(lampGlobal == null
                            || StringUtils.isEmpty(lampGlobal.getItemName())
                            || StringUtils.isEmpty(lampGlobal.getCrmIssueLampGlobalPK().getItemLang())
                            || !lampGlobal.getCrmIssueLampGlobalPK().getItemLang().equals(this.maintenanceController.getLocale())) continue;
                    Map<String, Object> obj = new Gson().fromJson(lampGlobal.getItemName(), Map.class);
                    if(obj == null ||
                            StringUtils.isEmpty(String.valueOf(obj.get("name"))) ||
                            StringUtils.isEmpty(String.valueOf(obj.get("object")))) continue;
                    List<LabelValue> lvR = new ArrayList<>();
                    lamp.setItemName(String.valueOf(obj.get("name")));
                    LabelValue[] objs = new Gson().fromJson(String.valueOf(obj.get("object")), LabelValue[].class);
                    if(obj == null) continue;
                    for(LabelValue lv:objs) {
                        if(lv == null) continue;
//                        lv.setType(String.valueOf(getFieldType(lv.getLabel())));
                        setLabelValue(lv);
                        lvR.add(lv);
                    }
                    lamp.setLampsItemList(lvR);
                }
            }
        }
    }

    private boolean checkForm(IssueLamp lamp) {
        boolean valError = false;
        for(LabelValue lv:lamp.getLampsItemList()) {
            if(lv == null || StringUtils.isEmpty(lv.getLabel())) continue;
            int type = NumberUtils.isDigits(lv.getType())?Integer.valueOf(lv.getType()):FIELD_TYPE.TEXT;
            if(!StringUtils.isEmpty(lv.getValue()) && type != FIELD_TYPE.DATE) continue;
            @SuppressWarnings("UnusedAssignment")
            String msgDynamic = StringUtils.EMPTY;
            DynamicColumn dv = SelectUtil.getDynamicColumn(this.availableColumns, lv.getLabel());
            if(dv!=null)msgDynamic = dv.getName();
            if(!StringUtils.isEmpty(lv.getValue()) && lv.getValue().length() > 100) {
                valError = valError|true;
                JsfUtil.getResource().alertMsgDynamicInfo(msgDynamic, ResourceUtil.SEVERITY_ERROR_NAME, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select");
            }
            if(type == FIELD_TYPE.DATE || type == FIELD_TYPE.SELECT || type == FIELD_TYPE.RADIO_GROUP || type == FIELD_TYPE.CHECKBOX_GROUP) {
                if(type == FIELD_TYPE.DATE) {
                    @SuppressWarnings("UnusedAssignment")
                    String valFrom = new String(), valTo = new String();
                    if(lv.getIsSearchPeriod()) {
                        String patten = DateUtil.getDate(this.maintenanceController.getLocale(), SYMBOL.SLASH);
                        Integer addSup = Integer.valueOf(StringUtil.getMysqlDateAddSup(lv.getLampDatePeriodBeforeAfter(), lv.getLampDateSearchPeriod()));
                        Date date = DateUtil.getAddSupDate(addSup);
                        if(addSup <= 0) {
                            valFrom = DateUtil.getDateToString(date, patten);
                            valTo = DateUtil.getDateNowToString(this.maintenanceController.getLocale());
                        } else {
                            valFrom = DateUtil.getDateNowToString(this.maintenanceController.getLocale());
                            valTo = DateUtil.getDateToString(date, patten);
                        }
                    } else {
                        valFrom = StringUtil.getStringNullToEmpty(lv.getValue());
                        valTo = StringUtil.getStringNullToEmpty(lv.getValueTo());
                    }

                    if(StringUtils.isEmpty(valFrom) && StringUtils.isEmpty(valTo)) {
                        valError = valError|true;
                        JsfUtil.getResource().alertMsgDynamicInfo(msgDynamic, ResourceUtil.SEVERITY_ERROR_NAME, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select");
                    } else {
                        lv.setValue(DateUtil.getStringYYYYMMDD(valFrom, this.maintenanceController.getLocale(), SYMBOL.SLASH));
                        lv.setValueTo(DateUtil.getStringYYYYMMDD(valTo, this.maintenanceController.getLocale(), SYMBOL.SLASH));
                        lv.setViewValue(String.format("%s%s%s", valFrom, SYMBOL.FROMTO, valTo));
                    }
                } else {
                    valError = valError|true;
                    JsfUtil.getResource().alertMsgDynamicInfo(msgDynamic, ResourceUtil.SEVERITY_ERROR_NAME, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_select");
                }
            } else {
                valError = true;
                JsfUtil.getResource().alertMsgDynamicInfo(msgDynamic, ResourceUtil.SEVERITY_ERROR_NAME, ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input");
            }
        }

        IssueLamp lampColor = this.issueLampService.checkSameLampColor(lamp);
        if(lampColor != null && lamp.getLampColor().equals(lampColor.getLampColor())) {
            JsfUtil.getResource().alertMsg("label.color", ResourceUtil.BUNDLE_MSG, "label.same.item", ResourceUtil.BUNDLE_ISSUE_NAME);
            valError = valError|true;
        }
        if(StringUtils.isBlank(lamp.getLampTextColor())) {
            lamp.setLampTextColor("000000");
//            JsfUtil.getResource().alertMsg("label.color", ResourceUtil.BUNDLE_MSG, "label.not_select", ResourceUtil.BUNDLE_ISSUE_NAME);
//            valError = valError|true;
        }

        if(StringUtils.isBlank(lamp.getItemName())) {
            JsfUtil.getResource().alertMsg("label.name", ResourceUtil.BUNDLE_MSG, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
            valError = valError|true;
        }
        if(lamp.getItemName() != null
                && lamp.getItemName().length() > 70) {
            JsfUtil.getResource().alertMsgMaxLength("label.name", ResourceUtil.BUNDLE_MSG, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 70);
            valError = valError|true;
        }
        if(valError) {
            for(LabelValue lv:lamp.getLampsItemList()) {
                int type = NumberUtils.isDigits(lv.getType())?Integer.valueOf(lv.getType()):FIELD_TYPE.TEXT;
                if(type != FIELD_TYPE.DATE || lv.getValue().contains("/")) continue;
                lv.setValue(DateUtil.getDateYYYYMMDD(lv.getValue(), this.maintenanceController.getLocale(), SYMBOL.SLASH));
                lv.setValueTo(DateUtil.getDateYYYYMMDD(lv.getValueTo(), this.maintenanceController.getLocale(), SYMBOL.SLASH));
            }
        }
        return valError;
    }

    public String getLampNameByColor(String color) {
        List<IssueLamp> list = loginController.getMember().getCompany().getIssueLampList();
        if(list.isEmpty()) return StringUtils.EMPTY;
        for(IssueLamp lamp:list) {
            if(lamp == null
                    || StringUtils.isEmpty(lamp.getLampColor())
                    || !color.equals(lamp.getLampColor())) continue;
            for(IssueLampGlobal global:lamp.getIssueLampsGlobal()) {
                if(global == null || !UserModel.getLogined().getLanguage().equals(global.getCrmIssueLampGlobalPK().getItemLang())) continue;
                Map<String, Object> obj = new Gson().fromJson(global.getItemName(), Map.class);
                if(obj == null) continue;
                return String.valueOf(obj.get("name"));
            }
        }
        return StringUtils.EMPTY;
    }

    public void lampChangeSearchPeriod() {
        String lampIdx = HTTPResReqUtil.getRequestParameter("lampIdx");
        String lampDataIdx = HTTPResReqUtil.getRequestParameter("lampDataIdx");
        if(!NumberUtils.isDigits(lampIdx) || !NumberUtils.isDigits(lampDataIdx)) return;
        if(this.issueLamps == null || this.issueLamps.size() <= Integer.valueOf(lampIdx)) return;
        List<LabelValue> items = this.issueLamps.get(Integer.valueOf(lampIdx)).getLampsItemList();
        if(items == null || items.size() <= Integer.valueOf(lampDataIdx)) return;
        LabelValue lv = items.get(Integer.valueOf(lampDataIdx));
        if(lv.getIsSearchPeriod()==null || !lv.getIsSearchPeriod()) {
            lv.setLampDatePeriodBeforeAfter(null);
        } else {
            if(lv.getLampDatePeriodBeforeAfter()==null) lv.setLampDatePeriodBeforeAfter(false);
            if(lv.getIsSearchPeriod()==null) lv.setLampDateSearchPeriod(3);
        }
    }
}
