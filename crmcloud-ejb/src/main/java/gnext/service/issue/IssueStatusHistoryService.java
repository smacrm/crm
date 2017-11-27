package gnext.service.issue;

import gnext.bean.Member;
import gnext.bean.issue.Issue;
import gnext.bean.issue.IssueStatusHistory;
import gnext.service.EntityService;
import javax.ejb.Local;

/**
 *
 * @author hungpd
 */
@Local
public interface IssueStatusHistoryService extends EntityService<IssueStatusHistory>{
    public void push(Issue issue, Integer fromStatusId, Integer toStatusId, Member creator) throws Exception;
}
