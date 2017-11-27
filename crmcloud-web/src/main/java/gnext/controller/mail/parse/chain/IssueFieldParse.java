package gnext.controller.mail.parse.chain;

import gnext.bean.Member;
import gnext.bean.issue.Customer;
import gnext.controller.mail.parse.MailParse;
import gnext.bean.issue.Issue;
import gnext.controller.ServiceResolved;
import gnext.controller.mail.parse.MailCustParse;
import gnext.model.authority.UserModel;
import gnext.service.MemberService;
import gnext.util.ClassUtil;
import gnext.util.JsfUtil;
import java.util.Arrays;
import java.util.Map;
import org.springframework.util.StringUtils;

/**
 *
 * @author daind
 */
public class IssueFieldParse implements MailParse, MailCustParse {
    UserModel isLogin;
    public IssueFieldParse() {
        isLogin = UserModel.getLogined();
    }

    @Override
    public void parse(Issue issue, Map<String, String> mappings, Map<String, String> params) throws Exception {
        if(issue == null) return;
        if(mappings == null || mappings.isEmpty()) return;
        
        // sau khi có danh sách key và dữ liệu từ email => thiết lập giá trị cho các fields(chính) của ISSUE.
        ClassUtil.createObject(issue, mappings, isLogin.getLanguage());
        
        parseIdFields(issue, mappings);
    }

    @Override
    public void parseCustomer(Customer cust, Map<String, String> mappings, Map<String, String> params) throws Exception {
        if(cust == null) return;
        if(mappings == null || mappings.isEmpty()) return;
        ClassUtil.createObject(cust, mappings, isLogin.getLanguage());       
        parseIdCustomerFields(cust, mappings);
    }
    
    private void parseIdFields(final Issue issue, Map<String, String> mappings){
        ServiceResolved serviceResolved = JsfUtil.getManagedBean("serviceResolved", ServiceResolved.class);
        MemberService memberService = serviceResolved.getMemberService();
        Arrays.asList("issue_creator_id", "issue_updated_id", "issue_authorizer_id", "issue_receive_person_id").forEach((key) -> {
            String name = mappings.get(key);
            if(!StringUtils.isEmpty(name)){
                Member m = memberService.findByMemberName(name, isLogin.getCompanyId());
                if(m != null){
                    switch(key){
                        case "issue_creator_id":
                            issue.setCreatorId(m);
                            break;
                        case "issue_updated_id":
                            issue.setUpdatedId(m);
                            break;
                        case "issue_authorizer_id":
                            issue.setIssueAuthorizerId(m);
                            break;
                        case "issue_receive_person_id":
                            issue.setIssueReceivePerson(m);
                            break;
                    }
                }else{
                    switch(key){
                        case "issue_creator_id":
                            issue.setCreatorId(null);
                            break;
                        case "issue_updated_id":
                            issue.setUpdatedId(null);
                            break;
                        case "issue_authorizer_id":
                            issue.setIssueAuthorizerId(null);
                            break;
                        case "issue_receive_person_id":
                            issue.setIssueReceivePerson(null);
                            break;
                    }
                }
            }
        });
    }

    private void parseIdCustomerFields(final Customer cust, Map<String, String> mappings){
//        ServiceResolved serviceResolved = JsfUtil.getManagedBean("serviceResolved", ServiceResolved.class);
//        MemberService memberService = serviceResolved.getMemberService();
    }
}
