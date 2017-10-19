/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.customize;

import gnext.service.*;
import gnext.bean.customize.AutoFormItem;
import gnext.bean.customize.AutoFormItemGlobal;
import java.util.List;
import javax.ejb.Local;
import javax.faces.model.SelectItem;

/**
 *
 * @author hungpd
 */
@Local
public interface AutoFormItemService extends EntityService<AutoFormItem> {

    public void persitItemGlobal(List<AutoFormItemGlobal> itemGlobal);

    public int removeItemGlobal(AutoFormItem item);

    public void removeUnused(List<Integer> unusedItemList);
    
    public List<AutoFormItemGlobal> search(int com, String lang);

    public SelectItem getItemGlobal(int com, int itemId, String lang);
    
    // Caching
    public void reloadCache();
    public void reloadCache(Integer companyId);
    
}
