/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue;

import gnext.bean.issue.Escalation;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 * 
 * @author gnextadmin
 */
@Local
public interface IssueEscalationService extends EntityService<Escalation> {
    public Escalation findEscalationByIssueId(final int issueId, final int userId, Short isSave, final Short type);
    public List<Escalation> findEscalationListByIssueId(final int issueId);
}
