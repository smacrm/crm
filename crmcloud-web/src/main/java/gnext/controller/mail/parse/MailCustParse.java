package gnext.controller.mail.parse;

import gnext.bean.issue.Customer;
import java.util.Map;

/**
 *
 * @author daind
 */
public interface MailCustParse {
    public void parseCustomer(Customer cust, Map<String, String> mappings, Map<String, String> params) throws Exception;
}
