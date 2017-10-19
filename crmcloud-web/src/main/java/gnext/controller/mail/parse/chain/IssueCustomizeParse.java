package gnext.controller.mail.parse.chain;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import gnext.bean.issue.Customer;
import gnext.controller.mail.parse.MailParse;
import gnext.bean.issue.Issue;
import gnext.controller.ServiceResolved;
import gnext.model.authority.UserModel;
import gnext.rest.customize.bean.KeyValueBean;
import gnext.service.customize.AutoFormItemService;
import gnext.util.JsfUtil;
import gnext.utils.InterfaceUtil.FIELDS;
import java.util.List;
import java.util.Map;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
* Customize Data
* 
* @author hungpd
* @since 2017/04
*/
public class IssueCustomizeParse implements MailParse {
    UserModel isLogin;
    public IssueCustomizeParse() {
        isLogin = UserModel.getLogined();
    }

    private AutoFormItemService autoFormItemService;
    @Override
    public void parse(Issue issue, Map<String, String> mappings, Map<String, String> params) throws Exception {
        if(issue == null) return;
        if(mappings == null || mappings.isEmpty()) return;
        
        ServiceResolved serviceResolved = JsfUtil.getManagedBean("serviceResolved", ServiceResolved.class);
        this.autoFormItemService = serviceResolved.getAutoFormItemService();
        
        // phân tích dữ liệu đối với customer name.
        remainingCustomizeData(mappings, issue);
        
    }
    
    
    public void remainingCustomizeData(Map<String, String> remaining, Issue issue){
        remaining.forEach((k, v) -> {
            if(k.startsWith(FIELDS.DYNAMIC)){
                String strItemId = k.replace(FIELDS.DYNAMIC, "").trim();
                Integer itemId = 0;
                if(NumberUtils.isNumber(strItemId)) itemId = Integer.parseInt(strItemId);
                if(itemId > 0 && v != null && !StringUtils.isEmpty(v)){
                    
                    issue.getCustomizeDataMapping().put(itemId, getItemValueKeyByLabel(itemId, v));
                }
            }
        });
    }
    
    private String getItemValueKeyByLabel(Integer itemId, String label){
        if(isLogin == null) return StringUtils.EMPTY;
        SelectItem si = autoFormItemService.getItemGlobal(isLogin.getCompanyId(), itemId, "ja");
        if(si != null){
            try{
                List<LinkedTreeMap> defaultValues = new Gson().fromJson(si.getLabel(), List.class);
                for(LinkedTreeMap kv : defaultValues){
                    if(kv.get("label").equals(label)){
                        return kv.get("value").toString();
                    }
                }
            }catch(JsonSyntaxException e){}
        }
        return label;
    }
}
