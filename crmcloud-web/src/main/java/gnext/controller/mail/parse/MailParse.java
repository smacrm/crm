package gnext.controller.mail.parse;

import gnext.bean.issue.Issue;
import java.util.Map;

/**
 *
 * @author daind
 */
public interface MailParse {
    public void parse(Issue issue, Map<String, String> mappings, Map<String, String> params) throws Exception;
}
