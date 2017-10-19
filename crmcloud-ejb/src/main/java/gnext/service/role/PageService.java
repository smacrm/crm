/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role;

import gnext.bean.role.Page;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author hungpham
 */
@Local
public interface PageService extends EntityService<Page> {
    /**
     * Tra ve id cua 1 page theo page name
     * @param pageName
     * @return 
     */
    public Integer getPageId(String pageName);
    /**
     * Danh sách các PAGES đã liên quan tới DYNAMIC-FORM theo công ty.
     * @param companyId Công ty theo User đăng nhập.
     * @return danh sách Page ID.
     */
    public List<Integer> findCorrelativeTheDynamicForm(Integer companyId);
}
