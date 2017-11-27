package gnext.service.automail;

import gnext.bean.automail.AutoMail;
import gnext.bean.automail.SimpleAutoMail;
import gnext.bean.mente.MenteItem;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author hungpd
 */
@Local
public interface AutoMailService extends EntityService<AutoMail> {
    public void resetItemId(Integer itemId) throws Exception;
    public List<AutoMail> findByStatus(MenteItem item);

    public List<SimpleAutoMail> findRequiredSendWithNoIssue(Integer companySlaveId);
    public List<SimpleAutoMail> findRequiredSendWithIssue(Integer companySlaveId, Integer requiredIssueId);
    public void pushHistory(Integer autoId, int issueId, Integer companyId, String historyData) throws Exception;
}
