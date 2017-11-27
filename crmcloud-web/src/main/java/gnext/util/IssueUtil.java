/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import gnext.bean.Prefecture;
import gnext.bean.issue.CustTargetInfo;
import gnext.bean.issue.Customer;
import gnext.bean.issue.Issue;
import gnext.bean.mente.MenteItem;
import gnext.model.authority.UserModel;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.FIELD_TYPE;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 * @author HUONG
 */
public class IssueUtil {
    
    public static final String CUST_FIRST_HIRA = "cust_first_hira";
    public static final String CUST_FIRST_KANA = "cust_first_kana";

    public interface IS_FIELD {
        public static enum ISSUE_SELECT {
            issue_receive_name
            ,issue_status_name
            ,issue_public_name
            ,issue_authorizer_name
            ,issue_receive_person_name
            ,issue_receive_person_group_name
            ,issue_proposal_name
//            ,issue_proposal_name_2
//            ,issue_proposal_name_3
            ,issue_keyword_name
            ,issue_product_name
//            ,issue_product_name_2
//            ,issue_product_name_3
            ,issue_creator_name
            ,issue_updated_name
        }
        public static enum ISSUE_DATE {
            issue_receive_date
            ,issue_closed_date
            ,issue_created_time
            ,issue_updated_time
        }

        public static enum CUSTOMER_SELECT {
            cust_cooperation_name
            ,cust_special_name
            ,cust_sex_name
            ,cust_age_name
            ,cust_city
        }
        public static enum CUSTOMER_DATE {
        }
    }
    
    public interface ALLOW_SEARCH_COL {
        public static enum ISSUE{
            issue_view_code ,
            issue_receive_name,
            issue_receive_date,
            issue_closed_date,
            issue_product_memo,
            issue_content_ask,
            issue_authorizer_name,
            issue_receive_person_name,
            issue_receive_person_group_name,
            issue_status_name,
            issue_public_name,
            issue_creator_name,
            issue_created_time,
            issue_updated_name,
            issue_updated_time,
            issue_proposal_name_1,
            issue_proposal_name_2,
            issue_proposal_name_3,
            issue_keyword_name,
            issue_product_name_1,
            issue_product_name_2,
            issue_product_name_3
        };
        
        public static enum CUSTOMER {
            cust_code,
            cust_cooperation_name,
            cust_special_name,
            cust_name_hira,
            cust_name_kana,
            cust_sex_name,
            cust_age_name,
            cust_post,
            cust_city,
            cust_address,
            cust_address_kana,
            cust_memo,
            cust_mail,
            cust_tel,
            cust_mobile
        }
        
        public static enum REQUIRED {
            issue_status_name,
            issue_public_name,
            issue_proposal_name_1,
            issue_receive_date,
            issue_created_time,
            issue_creator_name,
            issue_receive_person_name
        }
    }
    
    public static class COL_TYPE {
        public static enum PERSON {
            issue_authorizer_name,
            issue_receive_person_name,
            issue_creator_name,
            issue_updated_name,
            cust_creator_name,
            cust_updated_name,
        }
        
        public static enum GROUP {
            issue_receive_person_group_name,
        }
        
        public static enum MAINTENANCE {
            issue_status_name,
            issue_receive_name,
            issue_public_name,
            issue_proposal_name_1,
            issue_proposal_name_2,
            issue_proposal_name_3,
            issue_keyword_name,
            issue_product_name_1,
            issue_product_name_2,
            issue_product_name_3,
            cust_cooperation_name,
            cust_sex_name,
            cust_age_name,
        }
        
        public static enum CITY {
            cust_city,
        }
        
        
        public static String getType(String key){
            for(PERSON i : PERSON.values()){
                if(i.name().equals(key)) return "PERSON";
            }
            for(GROUP i : GROUP.values()){
                if(i.name().equals(key)) return "GROUP";
            }
            for(MAINTENANCE i : MAINTENANCE.values()){
                if(i.name().equals(key)) return "MAINTENANCE";
            }
            for(CITY i : CITY.values()){
                if(i.name().equals(key)) return "CITY";
            }
            return null;
        }
        
        public static Boolean isExists(String key){
            for(PERSON i : PERSON.values()){
                if(i.name().equals(key)) return true;
            }
            for(GROUP i : GROUP.values()){
                if(i.name().equals(key)) return true;
            }
            for(MAINTENANCE i : MAINTENANCE.values()){
                if(i.name().equals(key)) return true;
            }
            for(CITY i : CITY.values()){
                if(i.name().equals(key)) return true;
            }
            return false;
        }
    }

    /**
     * 受付情報に顧客情報を追加
     * @param issue：受付情報
     * @param select：プルダウン内容リスト
     * @param prefectures：都道府県リスト
     */
    public static void addCustomer(Issue issue, Map<String, List<SelectItem>> select, List<Prefecture> prefectures) {
        if(issue == null) return;
        List<Customer> custs = issue.getCustomerList();
        if(custs == null || custs.size() <= 0) return;
        List<SelectItem> telSessions = select.get(COLS.SESSION);
        for(Customer cust:custs) {
            if(cust == null) continue;
            if(IssueUtil.chkAddCustomer(cust)) {

                /** 受電番号リストを追加 */
                if(cust.getTel() != null && cust.getTel().size() > 0) {
                    IssueUtil.convertTargetList(cust, cust.getTel(), telSessions);
                }
                /** 連絡先番号リストを追加 */
                if(cust.getMobile()!= null && cust.getMobile().size() > 0) {
                    IssueUtil.convertTargetList(cust, cust.getMobile(), telSessions);
                }
                /** メールリストを追加 */
                if(cust.getMail()!= null && cust.getMail().size() > 0) {
                    IssueUtil.convertTargetList(cust, cust.getMail(), null);
                }
            } else {
                if(cust.getCustId() == null) continue;
                issue.getDelCustomerList().add(cust.getCustId());
            }
        }
    }

    /**
     * Check and remove invalid data on issue before flush to db
     * @param issue 
     */
    public static void removeInvalidData(Issue issue){
        // Check Customer, remove if not have any information
        for (Iterator<Customer> iterator = issue.getCustomerList().iterator(); iterator.hasNext();) {
            Customer o = iterator.next();
            if(!o.isValid()){
                iterator.remove();
            }
            else{
                if(o.getCustId() == null){
                    o.setCreatorId(UserModel.getLogined().getUserId());
                    o.setCreatedTime(new Date());
                }else{
                    o.setUpdatedId(UserModel.getLogined().getUserId());
                    o.setUpdatedTime(new Date());
                }
                //Check valid target info then remove if not valid
                o.removeInvalidData();
            }
        }
    }

    /**
     * 顧客のターゲットオブジェクトを作成、例「TEL、MOBILE、MAIL」
     * @param cust：顧客オブジェクト
     * @param tagetFlagType：ターゲットタイプ
     * @param data
     * @return ターゲットオブジェクト
     */
    public static CustTargetInfo createCustTargetInfo(Customer cust, Short tagetFlagType, String data) {
        CustTargetInfo target = new CustTargetInfo();
        target.setCustFlagType(tagetFlagType);
        target.setCompany(UserModel.getLogined().getCompany());
        target.setCustomer(cust);
        if(!StringUtils.isEmpty(data)) target.setCustTargetData(data);
        return target;
    }

    /** 
     * 「TEL、MOBILE、MAIL」リストフォームから顧客情報に追加
     * @param cust：顧客情報オブジェクト
     * @param targets：「TEL、MOBILE、MAIL」リスト 
     * @param items：リスト 
     */
    public static void convertTargetList(Customer cust, List<CustTargetInfo> targets, List<SelectItem> items) {
        if(cust == null || targets == null || targets.size() <= 0) return;
        targets.stream().filter((target)
                -> !(target == null || StringUtils.isBlank(target.getCustTargetData())))
                .forEachOrdered((target) -> {
                /** 電話番号区分 */
                //target.setCustTargetClassName(SelectUtil.getLabelById(items, target.getCustTargetClass()));
                cust.getCustTargetInfoList().add(target);
        });
    }

    /**
     * Lay ve danh sach value cua CustTargetInfo, phan cach nhau bang dau ,
     * @param issue
     * @return 
     */
    public static String getListTargets(Issue issue) {
        if(issue == null
                || issue.getCustomerList() == null
                || issue.getCustomerList().get(0).getCustTargetInfoList() == null) return null;
        String val = StringUtils.EMPTY;
        for(Customer cust : issue.getCustomerList()) {
            if(cust == null) continue;
            List<CustTargetInfo> targets = cust.getCustTargetInfoList();
            for(CustTargetInfo target:targets) {
                if(target == null || StringUtils.isBlank(target.getCustTargetData())) continue;
                if(StringUtils.isBlank(val)) {
                    val = "'" + target.getCustTargetData() + "'";
                } else {
                    val += ",'" + target.getCustTargetData() + "'";
                }
            }
        }
        return val;
    }
    
    /**
     * Lay ve danh sach value cua CustCode, phan cach nhau bang dau ,
     * @param issue
     * @return 
     */
    public static String getCustCode(Issue issue) {
        if(issue == null
                || issue.getCustomerList() == null
                || issue.getCustomerList().get(0).getCustCode() == null) return null;
        StringBuilder val = new StringBuilder();
        String prefix = "";
        for(Customer cust : issue.getCustomerList()) {
            if(cust == null || cust.getCustCode() == null || StringUtils.isBlank(cust.getCustCode())) continue;
            
            val.append(prefix);
            prefix = ",";
            val.append("'").append(cust.getCustCode()).append("'");
        }
        return val.toString();
    }

    /**
     * カスタマイズ項目、「value、label」を取得
     * @param pageId：
     * @return Map<String, String[]>
     */
    public static Map<String, String[]> getCustomizes(String pageId) {
        if(!NumberUtils.isDigits(pageId)) return null;
        Map<String, String[]> requestParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap();
        if(requestParams == null) return null;
        Map<String, String[]> customizeItems = new HashMap<>();
        for (Map.Entry<String, String[]> field : requestParams.entrySet()) {
            String key = field.getKey();
            if(!StringUtils.isBlank(key)
                    && key.startsWith(StringUtil.FIELD_NAME_PREFIX)
                    && (key.indexOf("[") <= 0 || key.indexOf("]") <= 0)) {
                String[] values = field.getValue();
                String[] arrayValue = null;
                if(!StringUtils.isBlank(values[0]) && values[0].startsWith("[")) {
                    arrayValue = values[0].replace("[", "").replace("]", "").split(",");
                }
                if(arrayValue != null && arrayValue.length >0) {
                    customizeItems.put(key,arrayValue);
                } else {
                    customizeItems.put(key,values);
                }
            }
        }
        return customizeItems;
    }

    /**
     * 顧客情報リセット
     * @param cust：顧客情報
     */
    public static void resertCustomer(Customer cust) {
        cust.setCustCooperationId(null);
        cust.setCustLastHira(null);
        cust.setCustFirstHira(null);
        cust.setCustLastKana(null);
        cust.setCustFirstKana(null);
        cust.setCustAgeId(null);
        cust.setCustSexId(null);
        cust.setCustPost(null);
        cust.setCustCity(null);
        cust.setCustAddress(null);
        cust.setCustAddressKana(null);
        cust.getCustTargetInfoList().clear();
        cust.setCustMemo(null);
        cust.setCustCode(null);
        cust.setCustSpecialId(null);
    }

    /**
     * 顧客リストが追加する際、以下の条件で判断
     * @param cust：顧客情報
     * @return 「true、false」
     */
    public static boolean chkAddCustomer(Customer cust) {
        /** 申出者種別 */
        if(cust.getCustCooperationId() != null) return true;
        /** 氏名(カナ) */
        if(!StringUtils.isBlank(cust.getCustLastKana()) || !StringUtils.isBlank(cust.getCustFirstKana())) return true;
        /** 氏名(漢字) */
        if(!StringUtils.isBlank(cust.getCustLastHira()) || !StringUtils.isBlank(cust.getCustFirstHira())) return true;
        /** 郵便番号 */
        if(!StringUtils.isBlank(cust.getCustPost())) return true;
        /** 都道府県 */
        if(cust.getCustCity() != null) return true;
        /** 住所 */
        if(!StringUtils.isBlank(cust.getCustAddress())) return true;
        /** 住所（カナ） */
        if(!StringUtils.isBlank(cust.getCustAddressKana())) return true;
        /** 備考 */
        if(!StringUtils.isBlank(cust.getCustMemo())) return true;
        /** 受電番号 */
        if(!StringUtils.isBlank(cust.getTel().get(0).getCustTargetData())) return true;
        /** 連絡先番号 */
        if(!StringUtils.isBlank(cust.getMobile().get(0).getCustTargetData())) return true;
        /** ﾒｰﾙｱﾄﾞﾚｽ */
        return !StringUtils.isBlank(cust.getMail().get(0).getCustTargetData());
    }

    public static int getCustomerFieldType(String field) {
        if(StringUtils.isEmpty(field)) return FIELD_TYPE.TEXT;
        for(IS_FIELD.CUSTOMER_SELECT t : IS_FIELD.CUSTOMER_SELECT.values()){
            if(!field.equals(t.name())) continue;
            return FIELD_TYPE.SELECT;
        }
        return FIELD_TYPE.TEXT;
    }

    public static int getIssueFieldType(String field) {
        if(StringUtils.isEmpty(field)) return FIELD_TYPE.TEXT;
        for(IS_FIELD.ISSUE_SELECT t : IS_FIELD.ISSUE_SELECT.values()){
            if(!field.equals(t.name())) continue;
            return FIELD_TYPE.SELECT;
        }
        for(IS_FIELD.ISSUE_DATE d : IS_FIELD.ISSUE_DATE.values()){
            if(!field.equals(d.name())) continue;
            return FIELD_TYPE.DATE;
        }
        return FIELD_TYPE.TEXT;
    }

    public static List<MenteItem> getMenteListByNameAndLevel(List<MenteItem> inList, String name, int level) {
        if(inList == null || inList.size() <= 0 || StringUtils.isEmpty(name) || level <= 0) return new ArrayList<>();
        List<MenteItem> list = new ArrayList<>();
        for(MenteItem item:inList) {
            if(item == null
                    || !name.equals(item.getItemName())
                    || level != item.getItemLevel()) continue;
            list.add(item);
        }
        return list;
    }
}
