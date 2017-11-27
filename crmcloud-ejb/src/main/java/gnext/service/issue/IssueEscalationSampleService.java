/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue;

import gnext.bean.issue.EscalationSample;
import gnext.service.EntityService;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface IssueEscalationSampleService extends EntityService<EscalationSample> {
    public EscalationSample getEscalationSampleById(int sampleId, int comId);
    public EscalationSample getEscalationSampleByTypeIdAndTargetId(int typeId, Integer targetId, int comId, String locale);
}
