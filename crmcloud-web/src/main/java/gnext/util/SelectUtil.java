package gnext.util;

import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.customize.AutoFormItemGlobal;
import gnext.bean.customize.AutoFormPageTabDivItemRel;
import gnext.bean.project.DynamicColumn;
import gnext.bean.mente.MenteItem;
import gnext.bean.mente.Products;
import gnext.controller.ServiceResolved;
import gnext.model.authority.UserModel;
import gnext.util.IssueUtil.ALLOW_SEARCH_COL;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.COMPANY_TYPE;
import gnext.utils.InterfaceUtil.CUST_STATUS;
import gnext.utils.InterfaceUtil.FIELDS;
import gnext.utils.InterfaceUtil.FIELD_TYPE;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

/**
 *
 * @author gnextadmin
 */
@SuppressWarnings("StaticNonFinalUsedInInitialization")
public class SelectUtil {
    private static final List<SelectItem> BUSINESS = new ArrayList<>();
    private static final List<SelectItem> CUST_STATUS_LIST = new ArrayList<>();

    private static final String[] viewMemoTextarea = {
            "issue_content_ask"
            ,"issue_product_memo"
            ,"cust_memo"
    };
    public static String[] getViewMemoTextarea() {
        return viewMemoTextarea;
    }
    private static final String[] viewMemoCustSpecialTextarea = {
            "cust_name_hira"
            ,"cust_name_kana"
            ,"cust_post"
            ,"cust_city"
            ,"cust_address"
            ,"cust_address_kana"
            ,"cust_tel"
            ,"cust_mobile"
            ,"cust_mail"
            ,"cust_memo"
            ,"cust_history_memo"
            ,"cust_history_last_update"
    };
    public static String[] getViewMemoCustSpecialTextarea() {
        return viewMemoCustSpecialTextarea;
    }

    public static String[] dateTimeType = { "_time", "_date", "_datetime" };
    private static List<String> dateTimes = new ArrayList<>();
    public static String[] getDateTimes() {
        return dateTimes.toArray(new String[dateTimes.size()]);
    }

    static {
        BUSINESS.add(new SelectItem(COMPANY_TYPE.OPPORTUNITY, JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.company_flag_opportunity")));
        BUSINESS.add(new SelectItem(COMPANY_TYPE.STORE, JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.company_flag_store")));
        BUSINESS.add(new SelectItem(COMPANY_TYPE.CUSTOMER, JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.company_flag_customer")));

        CUST_STATUS_LIST.add(new SelectItem(CUST_STATUS.ADD_APPLICATION, JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.new_application")));
        CUST_STATUS_LIST.add(new SelectItem(CUST_STATUS.CORREC_APPLICATION, JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.correction_application")));
        CUST_STATUS_LIST.add(new SelectItem(CUST_STATUS.DELETE_APPLICATION, JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.apply_deletion")));
        CUST_STATUS_LIST.add(new SelectItem(CUST_STATUS.PENDING_APPLICATION, JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.pending")));
        CUST_STATUS_LIST.add(new SelectItem(CUST_STATUS.APPROVAL_APPLICATION, JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.approval")));
        CUST_STATUS_LIST.add(new SelectItem(CUST_STATUS.NOTSHOW_APPLICATION, JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.not_show")));        
    }

    public static List<SelectItem> getBussiness() {
        return BUSINESS;
    }
 
    public static List<SelectItem> getCustStatusList() {
        return CUST_STATUS_LIST;
    }
    
    public static List<DynamicColumn> getViewDynamicColumns(List<AutoFormPageTabDivItemRel> customizes) {
        List<DynamicColumn> cols = new ArrayList<>();
        if(UserModel.getLogined() == null) return cols;
        Integer companyId = UserModel.getLogined().getCompanyId();
        String lang = UserModel.getLogined().getLanguage();

        if(UserModel.getLogined().getCompanyBusinessFlag() != COMPANY_TYPE.CUSTOMER) {
            cols.add(new DynamicColumn(String.valueOf(-(cols.size() + 1)), JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.search.tile.issue")));
            for(ALLOW_SEARCH_COL.ISSUE en :  ALLOW_SEARCH_COL.ISSUE.values()){
                DynamicColumn dc = new DynamicColumn(en.name(), JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME ,"label." + en.name()));
                try{
                    dc.setType(IssueUtil.getIssueFieldType(en.name()));
                    if(ALLOW_SEARCH_COL.REQUIRED.valueOf(en.name()) != null){
                        dc.setRequired(true);
                    }

                }catch(IllegalArgumentException e){
                }
                cols.add(dc);
            }
        }
        
        cols.add(new DynamicColumn(String.valueOf(-(cols.size() + 1)), JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.search.tile.cust")));
        cols.addAll(getCustomerColumns());
//        for(ALLOW_SEARCH_COL.CUSTOMER en :  ALLOW_SEARCH_COL.CUSTOMER.values()){
//            cols.add(new DynamicColumn(en.name(), ResourceUtil.message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME ,"label." + en.name())));
//        }
//        if(companyType == COMPANY_TYPE.CUSTOMER) {
//            cols.add(new DynamicColumn("cust_history_memo", ResourceUtil.message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME ,"label.cust_maker")));
//            cols.add(new DynamicColumn("cust_history_last_update", ResourceUtil.message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME ,"label.last_updated")));
//        }
 
        if(UserModel.getLogined().getCompanyBusinessFlag() != COMPANY_TYPE.CUSTOMER) {
            /** カスタマイズリストカラムを追加 */
            if(customizes != null && customizes.size() > 0) {
                cols.add(new DynamicColumn(String.valueOf(-(cols.size() + 1)), JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME, "label.search.tile.customize")));
                List<Integer> addedTabList = new ArrayList<>();
                customizes.forEach((item) -> {
                    Map<String, Object> obj = item.getItem().getItemName();

                    // Add tab name to column list
                    if( !addedTabList.contains(item.getTab().getTabId()) ){
                        String tabName = item.getTab().getTabName();
                        DynamicColumn dc = new DynamicColumn(String.valueOf(-(cols.size() + 1)), "--- " + tabName + " ---");
                        dc.setTabId(item.getTab().getTabId());
                        cols.add(dc);
                        addedTabList.add(item.getTab().getTabId());
                    }

                    String label = StringUtils.defaultString(String.valueOf(obj.get(lang)), gnext.utils.InterfaceUtil.FIELD_NAME_PREFIX+item.getItem().getItemId());
                    DynamicColumn dc = new DynamicColumn(FIELDS.DYNAMIC + item.getItem().getItemId(), label);
                    dc.setType(item.getItem().getItemType());
                    dc.setTabId(item.getTab().getTabId());
                    cols.add(dc);
                });
            }
        }
        return cols;
    }
    
    public static List<DynamicColumn> getCustomerColumns() {
        List<DynamicColumn> cols = new ArrayList<>();
        if(UserModel.getLogined() == null) return cols;
        Integer companyId = UserModel.getLogined().getCompanyId();
        if(UserModel.getLogined().getCompanyBusinessFlag() == COMPANY_TYPE.CUSTOMER) {
            cols.add(new DynamicColumn("cust_status", JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME ,"label.cust_status"), FIELD_TYPE.TEXT));
        }
        for(ALLOW_SEARCH_COL.CUSTOMER en :  ALLOW_SEARCH_COL.CUSTOMER.values()){
            cols.add(new DynamicColumn(
                    en.name()
                    , JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME ,"label." + en.name())
                    , IssueUtil.getCustomerFieldType(en.name())));
        }
        if(UserModel.getLogined().getCompanyBusinessFlag() == COMPANY_TYPE.CUSTOMER) {
            cols.add(new DynamicColumn("cust_history_memo", JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME ,"label.cust_history_memo"), FIELD_TYPE.TEXT));
            cols.add(new DynamicColumn("cust_history_last_update", JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME ,"label.cust_history_last_update"), FIELD_TYPE.DATE));
        }
        return cols;
    }
    
    public static List<SelectItem> getArrayRIFieldExplodes() {
        List<SelectItem> items = new ArrayList<>();
        if(UserModel.getLogined() == null) return items;
        Integer companyId = UserModel.getLogined().getCompanyId();
        for(ALLOW_SEARCH_COL.ISSUE en :  ALLOW_SEARCH_COL.ISSUE.values()){
            items.add(new SelectItem(en.name(), JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_ISSUE_NAME ,"label." + en.name())));
        }
        return items;
    }
    
    public static List<SelectItem> getArrayCIFieldExplodes() {
        List<SelectItem> cols = new ArrayList<>();
        if(UserModel.getLogined() == null) return cols;
        Integer companyId = UserModel.getLogined().getCompanyId();
        if(UserModel.getLogined().getCompanyBusinessFlag() == COMPANY_TYPE.CUSTOMER) {
            cols.add(new SelectItem("cust_status", JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME ,"label.cust_status")));
        }
        for(ALLOW_SEARCH_COL.CUSTOMER en :  ALLOW_SEARCH_COL.CUSTOMER.values()){
            cols.add(new SelectItem(en.name(), JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME ,"label." + en.name())));
        }
        if(UserModel.getLogined().getCompanyBusinessFlag() == COMPANY_TYPE.CUSTOMER) {
            cols.add(new SelectItem("cust_history_memo", JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME ,"label.cust_maker")));
            cols.add(new SelectItem("cust_history_last_update", JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_CUSTOMER_NAME ,"label.last_updated")));
        }
        return cols;
    }
    

    /**
     * 会社の全て部署やメンバーを取得
     * @param command：コマンド
     * @param groups：部署リスト
     * @return MenuModel
     */
    public static MenuModel getSelectGroupMember(String command, String updateComponent, List<Group> groups) {
        MenuModel model = new DefaultMenuModel();
        if(groups == null) return null;
        for(Group group: groups){
            if((group.getGroupDeleted() != null && group.getGroupDeleted() == 1) || group.getParent() != null) continue; // detect deleted group
            DefaultSubMenu groupMenu = new DefaultSubMenu(group.getGroupName(), "fa fa-group");
            fetchGroupMemberSubMenu(groupMenu, group, command, updateComponent);
            model.addElement(groupMenu);
            
        }
        return model;
    }
    
    private static void fetchGroupMemberSubMenu(DefaultSubMenu mGroupParent, Group group, String command, String updateComponent){
        //add group member
        group.getMembers().forEach((m) -> {
//            if(m.getHidden() == null || m.getHidden() == 0) {
                DefaultMenuItem child = new DefaultMenuItem(m.getMemberNameFull(), "fa fa-user");
                child.setProcess("@this");
                if(!StringUtils.isEmpty(command)) child.setCommand(command);
                child.setOnclick("$($(this).closest('div.ui-menu').attr('ref')).text('"+m.getMemberNameFull()+"'); $($(this).closest('div.ui-menu').attr('target')).val('"+m.getMemberId()+"')");
                child.setRel(m.getMemberId().toString());
                mGroupParent.addElement(child);
//            }
        });
        group.getChilds().forEach((g) -> {
            DefaultSubMenu groupMenu = new DefaultSubMenu(g.getGroupName(), "fa fa-group");
            mGroupParent.addElement(groupMenu);
            fetchGroupMemberSubMenu(groupMenu, g, command, updateComponent);
        });
    }

    /** 
     * 「会社ID、言語コード」で各リストを取得
     * @param selects：MAPリスト
     * @param dataSource：MAPのキーリスト
     * @param language：言語コード「ja、en、vn、。。。」
     */
    public static void addSelectItems(Map<String, List<SelectItem>> selects, List<MenteItem> dataSource, String language) {
        dataSource.forEach((item) -> {
            String code = item.getItemName();
            String label = item.getItemViewData(language);
            if(!selects.containsKey(code)) selects.put(code, new ArrayList<>());
            selects.get(code).add(new SelectItem(item, label));
        });
    }
    
    /** 
     * 「会社ID、言語コード」で各リストを取得
     * @param selects：MAPリスト
     * @param code
     * @param dataSource：MAPのキーリスト
     */
    public static void addProductSelectItems(Map<String, List<SelectItem>> selects, String code, List<Products> dataSource) {
        dataSource.forEach((item) -> {
            if(!selects.containsKey(code)) selects.put(code, new ArrayList<>());
            selects.get(code).add(new SelectItem(item, item.getProductsName()));
        });
    }
    
    /**
     * 「会社ID」又は「会社ID、部署ID」でメンバーリストを取得
     * @param selects：MAPリスト
     * @param field：MAPのキー
     */
    public static void addMemberGroupSelectItems(Map<String, List<SelectItem>> selects, String field) {
        ServiceResolved serviceResolved = JsfUtil.getManagedBean("serviceResolved", ServiceResolved.class);
        switch(field){
            case COLS.GROUP:
                List<Group> groupList = serviceResolved.getGroupService().findByCompanyId(UserModel.getLogined().getCompanyId(), (short)0);
                selects.put(field, new ArrayList<>());
                groupList.forEach((group) -> {
                    selects.get(field).add(new SelectItem(group, group.getGroupName()));
                });
                break;
            case COLS.USER:
                List<Member> memberListInCurrentGroup = serviceResolved.getMemberService().findByGroupId(UserModel.getLogined().getGroupId());
                selects.put(field, new ArrayList<>());
                memberListInCurrentGroup.forEach((member) -> {
                    selects.get(field).add(new SelectItem(member, member.getMemberNameFull()));
                });
                break;
        }
    }

    /**
     * DefaultMenuItem設定
     * @param itemName：Item表示名
     * @return DefaultMenuItem
     */
    private static DefaultMenuItem setChildItem(String itemName) {
        DefaultMenuItem menuItem = new DefaultMenuItem(nullStringToEmpty(itemName));
        menuItem.setAjax(true);
        return menuItem;
    }

    /**
     * リストの中にIDから値を取得
     * @param items：リスト
     * @param id：ID
     * @return String
     */
    public static String getLabelById(List<SelectItem> items, Integer id) {
        String val =  StringUtils.EMPTY;
        if(id == null) return val;
        if(id <= 0 || items == null || items.isEmpty()) return val;
        for(SelectItem item:items) {
            if(item == null || !Objects.equals(id, item.getValue())) continue;
            return nullStringToEmpty(String.valueOf(item.getLabel()));
        }
        return val;
    }

    public static Integer getIdByLabel(List<SelectItem> items, String label) {
        Integer val = null;
        if(StringUtils.isBlank(label) || items == null || items.isEmpty()) return val;
        for(SelectItem item:items) {
            if(item == null || !Objects.equals(label, item.getLabel())) continue;
            return NumberUtils.isDigits(String.valueOf(item.getValue()))?(Integer)item.getValue():null;
        }
        return val;
    }

    public static String nullStringToEmpty(String val) {
        if(StringUtils.isBlank(val)
                || "null".equals(val)
                || "NULL".equals(val)) return StringUtils.EMPTY;
        return val;
    }

    public static List<SelectItem> getMailListBySelectItem(List<SelectItem> items) {
        if(items == null || items.isEmpty()) return null;
        List<SelectItem> mails = new ArrayList<>();
        for(SelectItem item:items) {
            if(item == null || StringUtils.isBlank(SelectUtil.nullStringToEmpty(item.getDescription()))) continue;
            mails.add(item);
        }
        return mails;
    }

    public static MenteItem getMenteById(List<MenteItem> items, Integer val) {
        if(val == null || items == null || items.isEmpty()) return null;
        for(MenteItem item:items) {
            if(item == null || !Objects.equals(val, item.getItemId())) continue;
            return item;
        }
        return null;
    }

    public static MenteItem getMenteByName(List<MenteItem> items, String val) {
        if(StringUtils.isEmpty(val) || items == null || items.isEmpty()) return null;
        for(MenteItem item:items) {
            if(item == null || val.equals(item.getItemName())) continue;
            return item;
        }
        return null;
    }

    public static DynamicColumn getDynamicColumn(List<DynamicColumn> list,String id) {
        if(list == null || StringUtils.isEmpty(id)) return null;
        for(DynamicColumn dc:list) {
            if(dc == null || !id.equals(dc.getId())) continue;
            return dc;
        }
        return null;
    }

    public static AutoFormItemGlobal getAutoFormItemById(List<AutoFormItemGlobal> items, Integer val) {
        if(val == null || items == null || items.isEmpty()) return null;
        for(AutoFormItemGlobal item:items) {
            if(item == null || !Objects.equals(val, item.getAutoFormItem().getItemId())) continue;
            return item;
        }
        return null;
    }
}
