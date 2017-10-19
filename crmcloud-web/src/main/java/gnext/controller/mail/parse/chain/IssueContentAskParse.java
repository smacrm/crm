package gnext.controller.mail.parse.chain;

import gnext.bean.issue.Customer;
import gnext.controller.mail.parse.MailParse;
import gnext.bean.issue.Issue;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public class IssueContentAskParse implements MailParse {

    @Override
    public void parse(Issue issue, Map<String, String> mappings, Map<String, String> params) throws Exception {
        if(issue == null) return;
        if(mappings == null || mappings.isEmpty()) return;
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> mapField : mappings.entrySet()) sb.append(mapField.getValue());
        
        String issueContentAsk = sb.toString();
        if(StringUtils.isEmpty(issueContentAsk)) return;

        if(!issue.getIssueInfoList().isEmpty()) {
            issueContentAsk = issue.getIssueInfoList().get(0).getIssueContentAsk() + issueContentAsk;
            issue.getIssueInfoList().get(0).setIssueContentAsk(issueContentAsk);
        }
//        issueContentAsk = issue.getIssueContentAsk() + issueContentAsk;
//        issue.setIssueContentAsk(issueContentAsk);
    }
}
