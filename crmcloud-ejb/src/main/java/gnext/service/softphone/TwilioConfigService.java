package gnext.service.softphone;

import gnext.bean.softphone.TwilioConfig;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;


/**
 *
 * @author hungpham
 */
@Local
public interface TwilioConfigService extends EntityService<TwilioConfig>{
    public List<TwilioConfig> getByCompanyId(Integer companyId);
    public List<TwilioConfig> find(int first, int pageSize, String sortField, String sortOrder, String where);
    public TwilioConfig getByPhonenumber(String phoneNumber);
    public List<Double> getAllowMemberAdded(Integer companyId, Integer ignoreConfigId);
    public TwilioConfig getByUserId(Integer companyId, Integer memberId);
}
