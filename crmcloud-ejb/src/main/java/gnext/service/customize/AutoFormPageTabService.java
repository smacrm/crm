/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.customize;

import gnext.service.*;
import gnext.bean.customize.AutoFormPageTab;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author hungpd
 */
@Local
public interface AutoFormPageTabService extends EntityService<AutoFormPageTab> {
    /**
     * Tìm kiếm DYNAMIC-FORM theo công ty vào page-name(tham chiếu tới page_id trong bảng crm_page).
     * @param companyId
     * @param pageName
     * @return 
     */
    public List<AutoFormPageTab> search(Integer companyId, String pageName);
    
    /**
     * Tìm kiếm DYNAMIC-FORM theo công ty.
     * @param companyId
     * @return 
     */
    public List<AutoFormPageTab> search(int companyId);
    
    /**
     * Loai co cac field thua, chinh sua lai cac project, loai co cac field khong ton tai
     * @param pageId
     * @param companyId 
     */
    public void cleanUnusedItems(int pageId, int companyId);
}
