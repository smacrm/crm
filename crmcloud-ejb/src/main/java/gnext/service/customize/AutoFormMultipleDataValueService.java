/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.customize;

import gnext.service.*;
import gnext.bean.customize.AutoFormMultipleDataValue;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author hungpd
 */
@Local
public interface AutoFormMultipleDataValueService extends EntityService<AutoFormMultipleDataValue> {
    public boolean isExists(AutoFormMultipleDataValue entity);
    public Map<Integer, String> findItemData(int pageId, int refId, int companyId);

    public Integer removeNoDataItemExcludes(int companyId, int pageId, int pageType, int targetId, List<Integer> itemIdList);
}
