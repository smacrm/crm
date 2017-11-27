package gnext.service.issue;

import gnext.bean.issue.IssueAttachment;
import gnext.bean.softphone.Twilio;
import gnext.service.EntityService;
import java.util.Date;
import java.util.List;
import javax.ejb.Local;

/**
 * 
 * @author hungpham
 */
@Local
public interface IssueAttachmentService extends EntityService<IssueAttachment> {
    public List<IssueAttachment> getAttachmetListByIssue(final int issueId);

    public List<Twilio> search(String idSearch, Integer categoryIdSearch, Date fromDateSearch, Date toDateSearch, String creatorIdSearch, String callerPhoneSearch, Integer companyId);
}
