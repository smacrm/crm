/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue;

import gnext.bean.issue.IssueRelated;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface IssueRelatedService extends EntityService<IssueRelated> {
    public List<IssueRelated> getRelatedByIssueId(final int issueId);
    public IssueRelated getRelatedIdAndIssueId(final int relatedId, final int issueId);
}
