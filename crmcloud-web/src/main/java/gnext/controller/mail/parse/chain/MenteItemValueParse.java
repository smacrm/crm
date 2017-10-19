/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail.parse.chain;

import gnext.bean.issue.Customer;
import gnext.bean.issue.Issue;
import gnext.bean.mente.MenteItem;
import gnext.bean.mente.MenteOptionDataValue;
import gnext.controller.ServiceResolved;
import gnext.controller.mail.parse.MailCustParse;
import gnext.controller.mail.parse.MailParse;
import gnext.model.authority.UserModel;
import gnext.service.mente.MenteItemService;
import gnext.util.JsfUtil;
import gnext.utils.InterfaceUtil.COLS;
import static gnext.utils.InterfaceUtil.EXISTS;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.StringUtils;

/**
 *
 * @author tungdt
 */
public class MenteItemValueParse implements MailParse, MailCustParse {
    UserModel isLogin;
    public MenteItemValueParse() {
        isLogin = UserModel.getLogined();
    }
    
    @Override
    public void parse(Issue issue, Map<String, String> mappings, Map<String, String> params) throws Exception {
        if (issue == null || isLogin == null) return;
        if (mappings == null) return;
        //InterfaceUtil.COLS.MENTE_COLS
        mappings.forEach((key, value) -> {
            String checkedKey = key;
            if(key.startsWith(COLS.PROPOSAL) || key.startsWith(COLS.PRODUCT)){
                checkedKey = checkedKey.substring(0, checkedKey.lastIndexOf("_"));
            }
            if(ArrayUtils.contains(COLS.MENTE_COLS, checkedKey) && !StringUtils.isEmpty(value)){
                boolean exists = doParseMenteItemValue(issue, isLogin.getLanguage(), value, key, isLogin.getCompanyId(), mappings);
                if(!exists){
                    MenteItem item = getMenteObject(key, value);
                    if(item.getItemLevel() > 1){
                        MenteItem parent = findParent(issue, item);
                        if(parent != null){
                            item.setItemParent(parent);
                            item.setItemOrder(parent.getItemChilds().size());
                            parent.getItemChilds().add(item);
                        }
                    }
                    processObject(issue, item, mappings);
                }
            }
        });
        // Khong hieu sao lai remove item co level > 1
//        for (Iterator<MenteItem> iterator = issue.getMenteItem().iterator(); iterator.hasNext();) {
//            MenteItem o = iterator.next();
//            if (o.getItemLevel() > 1) {
//                iterator.remove();
//            }
//        }
    }
    
    private MenteItem getMenteObject(String itemName, String itemData){
        Integer level = 0;
        if(itemName.startsWith("issue_proposal_id") || itemName.startsWith("issue_product_id")){
            level = NumberUtils.toInt(itemName.substring(itemName.lastIndexOf("_") + 1), level);
        }
        if(level > 0){
            itemName = itemName.substring(0, itemName.lastIndexOf("_"));
        }

        MenteItem item = new MenteItem(isLogin.getMember(), itemName, level, null);
        MenteOptionDataValue lang = new MenteOptionDataValue(isLogin.getLanguage(), itemData, item);
        lang.setCompany(isLogin.getCompany());
        lang.setCreatorId(isLogin.getUserId());
        lang.setCreatedTime(new Date());
        lang.setUpdatedId(isLogin.getUserId());
        lang.setUpdatedTime(new Date());
        lang.setMenteItem(item);
        
        item.getLangs().add(lang);        
        return item;
    }
    
    private MenteItem findParent(Issue issue, MenteItem child){
        for(MenteItem item : issue.getIssueInfoList().get(0).getMenteItem()){
            if(item.getItemName().equals(child.getItemName())
                    && (child.getItemLevel() - 1) >= 1
                    && item.getItemLevel() == child.getItemLevel() - 1
                    && item.getItemDeleted() == EXISTS){
                return item;
            }
        }
        return null;
    }
    
    private boolean doParseMenteItemValue(Issue issue,String lang, String itemData, String itemName, Integer companyId, Map<String, String> mappings) {
        ServiceResolved serviceResolved = JsfUtil.getManagedBean("serviceResolved", ServiceResolved.class);
        MenteItemService menteItemService = serviceResolved.getMenteItemService();
        List<MenteItem> menteItems = menteItemService.findByMenteOptionValue(lang, itemData, itemName, companyId);
        
        MenteItem menteItem = null;
        if(!menteItems.isEmpty()) menteItem = menteItems.get(0);
        if(menteItem == null) return false;
        if(issue != null) processObject(issue, menteItem, mappings);
        return true;
    }
    
    private void processObject(Issue issue, MenteItem item, Map<String, String> mappings){
        switch(item.getItemName()){
            case "issue_receive_id":
                issue.setIssueReceiveId(item);
                break;
            case "issue_status_id":
                issue.setIssueStatusId(item);
                break;
            case "issue_public_id":
                issue.setIssuePublicId(item);
                break;
            default:
                for (Iterator<MenteItem> iterator = issue.getIssueInfoList().get(0).getMenteItem().iterator(); iterator.hasNext();) {
                    MenteItem o = iterator.next();
                    if (o.getItemName().equals(item.getItemName()) && o.getItemLevel().equals(item.getItemLevel())) {
                        iterator.remove();
                    }
                }
                issue.getIssueInfoList().get(0).getMenteItem().add(item); 
        }
    }

    @Override
    public void parseCustomer(Customer cust, Map<String, String> mappings, Map<String, String> params) throws Exception {
        if (cust == null) return;
        if (mappings == null) return;
        mappings.forEach((key, value) -> {
            if(ArrayUtils.contains(COLS.MENTE_COLS, key) && !StringUtils.isEmpty(value)){
                boolean exists = doParseMenteItemValue(null, isLogin.getLanguage(), value, key, isLogin.getCompanyId(), mappings);
            }
        });
    }
}
