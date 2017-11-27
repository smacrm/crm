package gnext.service.label;

import gnext.bean.label.PropertyItemLabel;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;


/**
 *
 * @author hungpham
 */
@Local
public interface LabelService extends EntityService<PropertyItemLabel>{
    public List<PropertyItemLabel> findByCompany(int companyId);
    public String findByKey(String bundleName, String key, String locale, int companyId);
    
    // Caching
    public void reloadCache();
    public void reloadCache(Integer companyId);
}
