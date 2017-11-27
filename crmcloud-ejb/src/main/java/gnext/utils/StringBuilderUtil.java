package gnext.utils;

import gnext.utils.InterfaceUtil.ARRAY_STRING_ICON;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.MAIL_TYPE;
import gnext.utils.InterfaceUtil.TARGET;
import static gnext.utils.InterfaceUtil.UN_DELETED;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author HUONG
 */
public class StringBuilderUtil {
    private static String DB_SCHEMA = StringUtils.EMPTY;

    /** フィールド名によって、プルダウンレベルやテーブル名を取得
     * @param field：フィールド名
     * @param companyId：会社ID
     * @param product：商品関連「true、false」
     * @return StringBuilder
     */
    public static StringBuilder selectMaxLevelField(String field,int companyId, boolean product) {
        if(StringUtils.isBlank(field) || companyId <= 0) return null;
        StringBuilder sel = new StringBuilder();
        sel.append(" SELECT ");
        sel.append(" MAX(i.`item_level`) ");
        if(product && field.equals(COLS.PRODUCT)) {
            sel.append(" + (SELECT IF(COUNT(products_id)>0,1,0) FROM ").append(DB_SCHEMA).append("crm_products) ");
        }
        sel.append(" FROM ").append(DB_SCHEMA).append("`crm_mente_item` i ");
        sel.append(" WHERE i.`item_deleted`= 0 ");
        sel.append(" AND i.`company_id`=").append(companyId);
        sel.append(" AND i.`item_name`='").append(field).append("' ");
        return sel;
    }


    /** プルダウンDATAを取得
     * @param field：フィールド名
     * @param companyId：会社ID
     * @param language：言語
     * @return SELECT StringBuilder
     */
    public static StringBuilder selectDataByField(String field,int companyId, String language) {
        if(StringUtils.isBlank(field)
                || companyId <= 0 || StringUtils.isBlank(language)) return null;
        StringBuilder sel = new StringBuilder();
        sel.append(" SELECT ");
        sel.append(" o.`item_data` as label ");
        sel.append(" ,i.`item_id` as value ");
        sel.append(" FROM ").append(DB_SCHEMA).append("`crm_mente_item` i ");
        sel.append(" LEFT JOIN ").append(DB_SCHEMA).append("`crm_mente_option_data_value` o ");
        sel.append(" ON o.`item_id`=i.`item_id` AND o.`item_language`='").append(language).append("' ");
        sel.append(" WHERE i.`item_deleted`= 0 ");
        sel.append(" AND i.`company_id`=").append(companyId);
        sel.append(" AND i.`item_level`= 1 ");
        sel.append(" AND i.`item_name`='").append(field).append("' ");
        return sel;
    }

    /** プルダウン階層DATAを取得
     * @param companyId：会社ID
     * @param language：言語
     * @param level：階層
     * @param parentId：上階層ID
     * @param product：商品データを取得Flag
     * @return SELECT StringBuilder
     */
    public static StringBuilder selectDataByParentId(int companyId, String language, int level, int parentId, boolean product) {
        if(companyId <= 0 || StringUtils.isBlank(language) || level <= 0) return null;
        StringBuilder sel = new StringBuilder();
        sel.append(" SELECT ");
        if(product) {
            sel.append(" i.`products_name` as label ");
            sel.append(" ,i.`products_id` as value ");
            sel.append(" FROM ").append(DB_SCHEMA).append("`crm_products` i ");
            sel.append(" WHERE i.`products_is_deleted`= 0 ");
            sel.append(" AND i.`company_id`=").append(companyId);
            sel.append(" AND i.`products_is_public`=0 ");
            if(parentId > 0) {
                sel.append(" AND i.`products_category_small_id`=").append(parentId);
            }
        } else {
            sel.append(" o.`item_data` as label ");
            sel.append(" ,i.`item_id` as value ");
            sel.append(" FROM ").append(DB_SCHEMA).append("`crm_mente_item` i ");
            sel.append(" LEFT JOIN ").append(DB_SCHEMA).append("`crm_mente_option_data_value` o ");
            sel.append(" ON o.`item_id`=i.`item_id` AND o.`item_language`='").append(language).append("' ");
            sel.append(" WHERE i.`item_deleted`= 0 ");
            sel.append(" AND i.`company_id`=").append(companyId);
            sel.append(" AND i.`item_level`=").append(level);
            if(parentId > 0) {
                sel.append(" AND i.`item_parent_id`=").append(parentId);
            }
        }
        return sel;
    }

    public static StringBuilder selectProductByName(String name, int companyId, String language, int max, Integer flag, String inKey) {
        if(companyId <= 0 || StringUtils.isBlank(name) || StringUtils.isBlank(language) || StringUtils.isBlank(inKey)) return null;
        StringBuilder sel = new StringBuilder();
        switch(inKey) {
            case COLS.PRODUCT:
                StringBuilder valueProduct = new StringBuilder();
                StringBuilder labelProduct = new StringBuilder();
                labelProduct.append(" CONCAT( ");
                valueProduct.append(" CONCAT( ");
                for(int i=1; i<=max; i++) {
                    valueProduct.append(String.format("o%d.`item_id`", i)).append(",'").append(ARRAY_STRING_ICON.VALUE).append("',");
                    labelProduct.append(String.format("o%d.`item_data`", i)).append(",'").append(ARRAY_STRING_ICON.LABEL).append("',");
                }
                valueProduct.append(" c.`products_id`) as content_value ");
                labelProduct.append(" c.`products_name`) as content_label ");

                sel.append(" SELECT ");
                sel.append(" c.`products_name` as label ");
                sel.append(" ,c.`products_id` as value ");
                sel.append(" ,").append(valueProduct);
                sel.append(",").append(labelProduct);
                sel.append(" FROM ").append(DB_SCHEMA).append("`crm_products` c ");
                for(int i=max; i>0; i--) {
                    if(i == max) {
                        sel.append(String.format(" INNER JOIN %s`crm_mente_item` i%d ON i%d.`item_id`=c.`products_category_small_id` AND i%d.`item_level`=%d ", DB_SCHEMA, i, i, i, i));
                        sel.append(String.format(" LEFT JOIN %s`crm_mente_option_data_value` o%d ON o%d.`item_id`=i%d.`item_id` AND o%d.item_language='%s' ", DB_SCHEMA, i, i, i, i, language));
                    } else {
                        sel.append(String.format(" INNER JOIN %s`crm_mente_item` i%d ON i%d.item_id=i%d.item_parent_id AND i%d.item_level=%d ", DB_SCHEMA, i, i, i+1, i, i));
                        sel.append(String.format(" LEFT JOIN %s`crm_mente_option_data_value` o%d ON o%d.`item_id`=i%d.`item_id` AND o%d.item_language='%s' ", DB_SCHEMA, i, i, i, i, language));
                    }
                }
                sel.append(" WHERE c.`products_is_deleted`=0 ");
                sel.append(" AND c.`products_is_public`=0 ");
                sel.append(" AND c.`company_id`=").append(companyId);
                sel.append(" AND c.`products_name` LIKE '%").append(name).append("%' ");
                if(null == flag || flag == 0) {
                    sel.append(" AND c.`products_public_start_day` <= now()");
                    sel.append(" AND c.`products_public_end_day` >= now()");
                } else switch (flag) {
                    case 1:
                        sel.append(" AND c.`products_public_start_day` > now()");
                        break;
                    case 2:
                        sel.append(" AND c.`products_public_end_day` < now()");
                        break;
                    default:
                        break;
                }
                break;
            case COLS.PROPOSAL:
                StringBuilder valueProposal = new StringBuilder();
                StringBuilder labelProposal = new StringBuilder();
                labelProposal.append(" CONCAT( ");
                valueProposal.append(" CONCAT( ");
                sel.append(" SELECT ");
                for(int i=1; i<=max; i++) {
                    if(i == max) {
                        sel.append(String.format(" o%d.`item_data` as label ", i));
                        sel.append(String.format(" ,o%d.`item_id` as value ", i));
                        valueProposal.append(String.format("o%d.`item_id`", i));
                        labelProposal.append(String.format("o%d.`item_data`", i));
                    } else {
                        valueProposal.append(String.format("o%d.`item_id`", i)).append(",'").append(ARRAY_STRING_ICON.VALUE).append("',");
                        labelProposal.append(String.format("o%d.`item_data`", i)).append(",'").append(ARRAY_STRING_ICON.LABEL).append("',");
                    }
                }
                valueProposal.append(" ) as content_value ");
                labelProposal.append(" ) as content_label ");

                sel.append(" ,").append(valueProposal);
                sel.append(",").append(labelProposal);
                for(int i=max; i>0; i--) {
                    if(i == max) {
                        sel.append(String.format(" FROM %s`crm_mente_item` i%d ", DB_SCHEMA, i));
                        sel.append(String.format(" INNER JOIN %s`crm_mente_option_data_value` o%d ON o%d.`item_id`=i%d.`item_id` AND o%d.item_language='%s' ", DB_SCHEMA, i, i, i, i, language));
                    } else {
                        sel.append(String.format(" INNER JOIN %s`crm_mente_item` i%d ON i%d.item_id=i%d.item_parent_id AND i%d.item_level=%d ", DB_SCHEMA, i, i, i+1, i, i));
                        sel.append(String.format(" LEFT JOIN %s`crm_mente_option_data_value` o%d ON o%d.`item_id`=i%d.`item_id` AND o%d.item_language='%s' ", DB_SCHEMA, i, i, i, i, language));
                    }
                }
                sel.append(" WHERE i3.`item_level`=").append(max);
                sel.append(" AND i3.`company_id`=").append(companyId);
                sel.append(" AND o3.`item_data` LIKE '%").append(name).append("%' ");
                break;
            default:
                break;
        }

        return sel;
    }


    /**
     * 「電話番号、メール」どっちかが同じであれば取得する
     * @param issueId：受付情報ID
     * @param telMobileMails：「TEL、MOBILE、MAIL」リスト例「08000001111,09011112222,taro01@gnext.co.jp...」
     * @param custCode
     * @return StringBuilder
     */
    public static StringBuilder selectHistoryCustomers(int issueId, int companyId, String telMobileMails, String custCode) {
        StringBuilder sel = new StringBuilder();
        sel.append(" SELECT DISTINCT ");
        sel.append(" i.* ");
        sel.append(" FROM ").append(DB_SCHEMA).append("crm_issue i ");
        sel.append(" INNER JOIN ").append(DB_SCHEMA).append("crm_issue_cust_rel cr ON cr.issue_id = i.issue_id ");
        sel.append(" INNER JOIN ").append(DB_SCHEMA).append("crm_customer c ON c.cust_id = cr.cust_id ");
        sel.append(" INNER JOIN ").append(DB_SCHEMA).append("crm_cust_target_info cir ON cir.cust_id = cr.cust_id ");
        sel.append(" WHERE i.issue_deleted=").append(UN_DELETED);
        sel.append(" AND c.cust_deleted=").append(UN_DELETED);
        sel.append(" AND i.company_id=").append(companyId);
        if(issueId > 0) {
            sel.append(" AND i.issue_id<>").append(issueId);
        }
        _BuildSelectFromCustCodeOrTelMobileMails(sel, telMobileMails, custCode);
        return sel;
    }

    public static StringBuilder selectSpecialCustomers(String telMobileMails, int companyId, String custCode) {
        StringBuilder sel = new StringBuilder();
        sel.append(" SELECT DISTINCT ");
        sel.append(" c.* ");
        sel.append(" FROM ").append(DB_SCHEMA).append("crm_customer c ");
        sel.append(" INNER JOIN ").append(DB_SCHEMA).append("crm_cust_target_info cir ON cir.cust_id = c.cust_id ");
        sel.append(" AND c.company_id=").append(companyId);
        sel.append(" WHERE c.cust_deleted=").append(UN_DELETED);
        sel.append(" AND c.company_id=").append(companyId);
        sel.append(" AND c.cust_special_id IS NOT NULL AND c.cust_special_id <> 0 ");
        _BuildSelectFromCustCodeOrTelMobileMails(sel, telMobileMails, custCode);
        return sel;
    }
    
    private static void _BuildSelectFromCustCodeOrTelMobileMails(StringBuilder sel, String telMobileMails, String custCode){
        sel.append(" AND (");
         if (!StringUtils.isBlank(custCode)) {
            if (custCode.indexOf(",") > 0) {
                sel.append(" c.cust_code IN  (").append(custCode).append(")");
            } else {
                sel.append(" c.cust_code = ").append(custCode);
            }
        }
        if(!StringUtils.isBlank(telMobileMails) && !StringUtils.isBlank(custCode)){
            sel.append(" OR ");
        } 
        if (!StringUtils.isBlank(telMobileMails)) {
            if (telMobileMails.indexOf(",") > 0) {
                sel.append(" cir.cust_target_data IN (").append(telMobileMails).append(") ");
            } else {
                sel.append(" cir.cust_target_data=").append(telMobileMails);
            }
        }
        sel.append(")");
    }

    public static StringBuilder deleteCustRelByCustId(List<Integer> custIds) {
        if(custIds == null || custIds.size() <= 0) return null;
        StringBuilder sel = new StringBuilder();
        sel.append(" DELETE FROM ").append(DB_SCHEMA).append("crm_issue_cust_rel WHERE cust_id IN(").append(StringUtils.join(custIds, ",")).append(") ");
        return sel;
    }

    /**
     * 送信メール一覧取得
     * 条件：
     * 1、送信Flag：1
     * 2、依頼メール用Flag：1
     * 3、メールサーバ又はアカウント削除Flag：0
     * @param comId：会社ID
     * @param support：「依頼、対応」
     * @return StringBuilder
     */
    public static StringBuilder getSendAccountList(int comId, boolean support) {
        if(comId <= 0) return null;
        StringBuilder sel = new StringBuilder();
        sel.append(" SELECT m FROM MailAccount m ");
        sel.append(" WHERE m.company.companyId = ").append(comId);
        sel.append(" AND m.mailServer.serverDeleted = ").append(UN_DELETED);
        sel.append(" AND m.accountIsDeleted = ").append(UN_DELETED);
        sel.append(" AND m.accountSendFlag = 1 ");
        if(support) {
            sel.append(" AND m.accountSupport = 1 ");
        } else {
            sel.append(" AND m.acountRequest = 1 ");
        }
        return sel;
    }

    /**
     * メンバーメールアドレス1つを取得
     * @param userId：メンバーID
     * @return StringBuilder
     */
    public static StringBuilder getUserMailFristByUserId(int userId) {
        if(userId <= 0) return null;
        StringBuilder sel = new StringBuilder();
        sel.append(" SELECT t.company_target_data FROM crm_company_target_info t ");
        sel.append(" INNER JOIN crm_member u on u.member_id = t.company_target_id AND t.company_target = ").append(MAIL_TYPE.USER);
        sel.append(" WHERE t.company_flag_type = ").append(TARGET.MAIL);
        sel.append(" AND u.member_deleted = ").append(UN_DELETED);
        sel.append(" AND t.company_target_id = ").append(userId);
        sel.append(" AND t.company_target_data != '' AND t.company_target_data IS NOT NULL LIMIT 0,1 ");
        return sel;
    }

    /**
     * メールアドレスがあるメンバーリストを取得
     * @param groupId
     * @param userId
     * @return 
     */
    public static StringBuilder getUserMailList(int groupId, int userId) {
        if(groupId <= 0) return null;
        StringBuilder sel = new StringBuilder();
        sel.append(" SELECT u.* FROM crm_member u ");
        sel.append(" INNER JOIN crm_company_target_info t on t.company_target_id = u.member_id AND t.company_target = ").append(MAIL_TYPE.USER);
        sel.append(" WHERE u.group_id = ").append(groupId);
        sel.append(" AND u.member_deleted = ").append(UN_DELETED);
        sel.append(" AND t.company_flag_type = ").append(TARGET.MAIL);
        if(userId > 0) {
            sel.append(" AND u.member_id <> ").append(userId);            
        }
        return sel;
    }

    /**
     * Tìm kiếm tất cả các Issue liên quan tới mail.
     * @param mailaddress
     * @param maildataid
     * @param companyid
     * @return 
     */
    public static StringBuilder getListIssueRelated(String mailaddress, int maildataid, int companyid) {
        StringBuilder sel = new StringBuilder();
        sel.append(" select b.issue_id from crm_cust_target_info a inner join crm_issue_cust_rel b on a.cust_id = b.cust_id ");
        sel.append(" where a.cust_flag_type = ").append(InterfaceUtil.TARGET.MAIL);
        sel.append(" and a.cust_target_data = '").append(mailaddress).append("'  ");
        sel.append(" and a.company_id=").append(companyid).append(" ");
        sel.append(" and b.issue_id != ( ");
        sel.append(" SELECT IFNULL(c.mail_data_issue_id, -1) FROM crm_mail_data c  ");
        sel.append(" where c.mail_data_id = ").append(maildataid).append(" limit 1) ");
        return sel;
    }

    public static StringBuilder getCountIssueSame(int companyId, List<Integer> months, Integer issueId) {
        StringBuilder sel = new StringBuilder();
        if(companyId <= 0 || months.isEmpty() || issueId == null || issueId <= 0) return sel;
        int idx = 0;
        for(Integer c:months) {
            if(c == null || c <= 0) continue;
            if(idx > 0) sel.append(" union all ");
//            sel.append(" select ");
//            sel.append(" count(i").append(c).append(".issue_id) as count_").append(c).append(", group_concat(i").append(c).append(".issue_id) as detail");
//            sel.append(" from ").append(DB_SCHEMA).append("crm_mente_item_value ov").append(c);
//            sel.append(" inner join ").append(DB_SCHEMA).append("crm_issue i").append(c).append(" on i").append(c).append(".issue_id=ov").append(c).append(".issue_id ");
//            sel.append(" and i").append(c).append(".company_id=ov").append(c).append(".company_id ");
//            sel.append(" left join ").append(DB_SCHEMA).append("crm_mente_item it").append(c).append(" on it").append(c).append(".item_id=ov").append(c).append(".mente_issue_field_value ");
//            sel.append(" and it").append(c).append(".item_level=3 ");
//            sel.append(" and it").append(c).append(".item_risk_sensor=1 ");
//            sel.append(" and it").append(c).append(".item_name='").append(COLS.PROPOSAL).append("' ");
//            sel.append(" left join ").append(DB_SCHEMA).append("crm_products p").append(c).append(" on p").append(c).append(".products_id=ov").append(c).append(".mente_issue_field_value ");
//            sel.append(" where ov").append(c).append(".mente_issue_field_level=4 ");
//            sel.append(" and ov").append(c).append(".mente_issue_item_name='").append(COLS.PRODUCT).append("' ");
//            sel.append(" and i").append(c).append(".issue_receive_date>=DATE_SUB(NOW(), INTERVAL ").append(c).append(" MONTH) ");
//            sel.append(" and i").append(c).append(".issue_id<>").append(issueId);
//            sel.append(" and i").append(c).append(".company_id=").append(companyId);
            
            sel.append(" select ");
            sel.append("     count(a.issue_id) AS count_1, ");
            sel.append("     group_concat(a.issue_id) AS detail ");
            sel.append(" from crm_mente_item_value a  ");
            sel.append(" INNER JOIN crm_issue_product b on a.issue_id = b.issue_id ");
            sel.append(" INNER JOIN crm_issue c ON a.issue_id = c.issue_id ");
            sel.append(" where  ");
            sel.append(" a.mente_issue_field_value =  ");
            sel.append(" ( ");
            sel.append("         select t2.item_id from crm_mente_item_value t1  ");
            sel.append("         INNER JOIN crm_mente_item t2 ON t2.item_id = t1.mente_issue_field_value ");
            sel.append("         where t1.issue_id = ").append(issueId);
            sel.append("                 and t1.mente_issue_field_level = 3  ");
            sel.append("                 and t2.item_risk_sensor = 1  ");
            sel.append("                 and t2.item_name = 'issue_proposal_id' ");
            sel.append(" ) ");
            sel.append(" and b.product_id =  ");
            sel.append(" ( ");
            sel.append("         select product_id from crm_issue_product t1 where t1.issue_id = ").append(issueId);
            sel.append(" ) ");
            sel.append(" and a.issue_id != ").append(issueId);
            sel.append(" AND c.issue_receive_date >= DATE_SUB(NOW(), INTERVAL ").append(c).append(" MONTH) ");
            sel.append(" AND c.company_id = ").append(companyId);
            idx++;
        }
        return sel;
    }
}
