/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.customize;

import gnext.bean.customize.AutoFormItemGlobal;
import gnext.service.*;
import gnext.bean.customize.AutoFormPageTabDivItemRel;
import gnext.bean.customize.AutoFormTab;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author hungpd
 */
@Local
public interface AutoFormPageTabDivItemService extends EntityService<AutoFormPageTabDivItemRel> {
    public List<AutoFormPageTabDivItemRel> findRelList(int pageId, int pageType, int refId);
    
    /**
     * Ham xu li tra ve danh sach cac TAB cung voi cac ITEMS cho moi TAB.
     * @param companyId cong ty dang nhap.
     * @param itemLang ngon ngu nguoi dung chon.
     * @return 
     */
    public Map<AutoFormTab, List<AutoFormItemGlobal>> getCustomizeList(int companyId, String itemLang);
    
    /**
     * Ham tra ve danh sach cac itemID trong 1 cong ty va 1 page
     * @param companyId
     * @param pageId
     * @return 
     */
    public List<Integer> listDynamicItems(int companyId, int pageId);
}
