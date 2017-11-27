package gnext.controller.mail.parse;

import com.google.gson.Gson;
import gnext.bean.issue.Customer;
import gnext.bean.issue.Issue;
import gnext.bean.mail.MailExplode;
import gnext.controller.ServiceResolved;
import gnext.controller.mail.parse.chain.IssueCustParse;
import gnext.controller.mail.parse.chain.IssueInfoParse;
import gnext.controller.mail.parse.chain.IssueCustomizeParse;
import gnext.controller.mail.parse.chain.IssueFieldParse;
import gnext.controller.mail.parse.chain.IssueKeywordParse;
import gnext.controller.mail.parse.chain.MenteItemValueParse;
import gnext.model.authority.UserModel;
import gnext.model.mail.items.MailExplodeItem;
import gnext.service.issue.IssueCustomerService;
import gnext.service.issue.IssueService;
import gnext.service.project.ProjectService;
import static gnext.util.InterfaceUtil.EXPLODE_RULE.EOF;
import static gnext.util.InterfaceUtil.EXPLODE_RULE.EOL;
import gnext.util.JsfUtil;
import static gnext.util.StringUtil.string2utf8;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 *
 * @author daind
 */
public class StandardMailParse {
    UserModel isLogin;
    public StandardMailParse() {
        isLogin = UserModel.getLogined();
    }
    
    private final Logger LOGGER = LoggerFactory.getLogger(StandardMailParse.class);
    private final static String NEWLINE = "\r\n";
    
    // sử dụng chain cho việc sử dụng phân tích mail cơ bản.
    private final List<Class> parserList = Arrays.asList(
                IssueFieldParse.class, // IssueFieldParse must put at the first of all
                IssueInfoParse.class,
                IssueCustParse.class, 
                // IssueContentAskParse.class,  // Trong noi dung contentAsk hien tai, lay toan bo noi dung cac field append vao -> bi sai
                IssueCustomizeParse.class,
                IssueKeywordParse.class,
                MenteItemValueParse.class);
    
    private final List<Class> parserCustList = Arrays.asList(
                IssueCustParse.class, 
                MenteItemValueParse.class);

    public Issue parse(String content, List<MailExplode> explodes) throws Exception {
        ServiceResolved serviceResolved = JsfUtil.getManagedBean("serviceResolved", ServiceResolved.class);
        ProjectService projectService = serviceResolved.getProjectService();
        
        // gọi hàm phân cắt mail(chỉ dùng để debug-testing) sẽ comment lại sau khi hoàn thành.
//        parseMailContent2Issue("daind", content, null, true);
        
        return parse(mapping(content, explodes), new ArrayList<>(), projectService, new HashMap<>());
    }
    
    public Issue parse(final Map<String, String> fieldMappings, List<String> mappingConds, ProjectService projectService, final Map<String, String> params) throws Exception {
//        UserModel logined = UserModel.getLogined();
        Issue issue = null;
        if(isLogin == null) return issue;
        
        String issueViewCode = fieldMappings.get("issue_view_code");
        
        Integer[] matchingIssueId = {null};
        for(String strCond : mappingConds){
            List<String> conds = new ArrayList<>();
            for(String field : strCond.split(",")){
                String value = fieldMappings.containsKey(field.trim()) ?  fieldMappings.get(field.trim()) : null;
                if(!StringUtils.isEmpty(value)){
                    conds.add(field + " = '" + value + "'");
                }
            }
            List<SelectItem> items = new ArrayList<>();
            items.add(new SelectItem("issue_id", "issue_id"));
            List<Map<String, Object>> results = projectService.advanceSearch(
                    isLogin.getCompanyBusinessFlag(),
                    isLogin.getCompanyId(), 
                    isLogin.getUserId(), 
                    false, 
                    StringUtils.join(conds, " AND "), 
                    Arrays.asList(strCond.split(",")), 
                    "", "OR",
                    items, 
                    null, null,
                    isLogin.getLanguage());
            results.forEach((resultItemMap) -> {
                Object o = resultItemMap.get("issue_id");
                if(o != null && NumberUtils.isNumber(o.toString())){
                    matchingIssueId[0] = NumberUtils.toInt(o.toString(), 0);
                }
            });
            if(matchingIssueId[0] != null && matchingIssueId[0] > 0){
                break;
            }
        }
        
        ServiceResolved serviceResolved = JsfUtil.getManagedBean("serviceResolved", ServiceResolved.class);
        IssueService issueService = serviceResolved.getIssueService();
        if(matchingIssueId[0] != null && matchingIssueId[0] > 0){
            issue = issueService.findByIssueId(matchingIssueId[0], isLogin.getLanguage());
        }
        if(issue == null && !StringUtils.isEmpty(issueViewCode)){
            issue = issueService.findByIssueViewCode(isLogin.getCompanyId(), issueViewCode);
        }
        if(issue == null){
            issue = new Issue(isLogin.getMember());
        }
        issue.setSource("auto");
        for(Class _class : parserList){
            Object o = BeanUtils.instantiateClass(_class);
            if(o instanceof MailParse){
                MailParse parser = (MailParse) o;
                try {
                    parser.parse(issue, fieldMappings, params);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        
        return issue;
    }
    
    private Map<String, String> mapping(String content, List<MailExplode> explodes) {
        Map<String, String> mapFields = new HashMap<>();
        for(MailExplode me : explodes) {
            String condition = me.getMailExplodeConditions(); if(StringUtils.isEmpty(condition)) continue;
            MailExplodeItem[] items = new Gson().fromJson(condition, MailExplodeItem[].class);
            
            parseWithExplodeItem(mapFields, items, content);
        }
        
        return mapFields;
    }
    
    private void parseWithExplodeItem(Map<String, String> mapFields, MailExplodeItem[] items, String content) {
        if(items == null || items.length <= 0) return;
        if(StringUtils.isEmpty(content)) return;
        for(MailExplodeItem item : items) {
            String key = item.getFirstChar(); if(StringUtils.isEmpty(key)) continue;
            String operator = item.getSecondChar(); if(StringUtils.isEmpty(operator)) continue;
            String field = item.getFieldExplode(); if(StringUtils.isEmpty(field)) continue;

            List<String> otherKeys = null;
            if(EOF.equalsIgnoreCase(operator)) otherKeys = new ArrayList<>();
            else if(EOL.equalsIgnoreCase(operator)) otherKeys = MailExplodeItem.listOfKeys(items, key);
            if(otherKeys == null) continue;

            String val = parseMailContent2Issue(key, content, otherKeys, true);
            if(item.isTrimSpace()) val = StringUtils.strip(val);
            mapFields.put(field, val);
        }
    }
    
    public String parseMailContent2Issue(String key, String content, List<String> otherKeys, boolean multiline) {
        String content_utf8 = string2utf8(content);
        String key_utf8 = string2utf8(key);
        
        StringBuilder sz = new StringBuilder();
        boolean existsKey = false;
        try (Scanner scanner = new Scanner(content_utf8)) {
            String line = null;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                
                // nếu dòng không chứa key thì bỏ qua và tiếp tục dòng tiếp theo.
                if(existsKey || line.contains(key_utf8)) existsKey = true;
                if(!existsKey) continue;
                
                // nếu sử dụng multiline cần kiểm tra hết các keyword tiếp theo.
                if(multiline) {
                    for(String otherKey : otherKeys) {
                        String otherKey_utf8 = string2utf8(otherKey);
                        if(line.contains(otherKey_utf8)) return sz.toString();
                    }
                }
                
                // lấy text đúng của dòng, nếu dòng chứa key thì bỏ key đó đi.
                String val = line;
                if(line.contains(key_utf8))
                    val = line.substring(line.indexOf(key_utf8) + key_utf8.length(), line.length());
                if(val != null && !val.isEmpty()) sz.append(val).append(NEWLINE);
                
                // nếu không sử dụng multiline thì cần trả về ngay line này tới người dùng.
                if(!multiline) return val;
            }
        }
        
        return sz.toString();
    }

    public Customer parseCustomer(String content, List<MailExplode> explodes) throws Exception {
        ServiceResolved serviceResolved = JsfUtil.getManagedBean("serviceResolved", ServiceResolved.class);
        ProjectService projectService = serviceResolved.getProjectService();
        return parseCustomer(mapping(content, explodes), new ArrayList<>(), projectService, new HashMap<>());
    }

    public Customer parseCustomer(final Map<String, String> fieldMappings, List<String> mappingConds, ProjectService projectService, final Map<String, String> params) throws Exception {
//        UserModel logined = UserModel.getLogined();
        Customer cust = null;
        Integer[] matchingCustId = {null};
        for(String strCond : mappingConds){
            List<String> conds = new ArrayList<>();
            for(String field : strCond.split(",")){
                String value = fieldMappings.containsKey(field.trim()) ?  fieldMappings.get(field.trim()) : null;
                if(!StringUtils.isEmpty(value)){
                    conds.add(field + " = '" + value + "'");
                }
            }
            List<SelectItem> items = new ArrayList<>();
            items.add(new SelectItem("cust_id", "cust_id"));
            List<Map<String, Object>> results = projectService.advanceSearch(
                    isLogin.getCompanyBusinessFlag(),
                    isLogin.getCompanyId(), 
                    isLogin.getUserId(), 
                    false, 
                    StringUtils.join(conds, " AND "), 
                    Arrays.asList(strCond.split(",")), 
                    "", "OR",
                    items, 
                    null, null,
                    isLogin.getLanguage());
            results.forEach((resultItemMap) -> {
                Object o = resultItemMap.get("cust_id");
                if(o != null && NumberUtils.isNumber(o.toString())){
                    matchingCustId[0] = NumberUtils.toInt(o.toString(), 0);
                }
            });
            if(matchingCustId[0] != null && matchingCustId[0] > 0){
                break;
            }
        }
        
        ServiceResolved serviceResolved = JsfUtil.getManagedBean("serviceResolved", ServiceResolved.class);
        IssueCustomerService issueCustService = serviceResolved.getIssueCustomerService();
        if(matchingCustId[0] != null && matchingCustId[0] > 0){
            cust = issueCustService.find(matchingCustId[0]);
        }
        if(cust == null){
            cust = new Customer(isLogin.getMember(), null);
        }
//        cust.setSource("auto");
        for(Class _class : parserCustList){
            Object o = BeanUtils.instantiateClass(_class);
            if(o instanceof MailCustParse){
                MailCustParse parser = (MailCustParse) o;
                try {
                    parser.parseCustomer(cust, fieldMappings, params);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return cust;
    }
}
