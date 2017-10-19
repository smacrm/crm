/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role;

import gnext.bean.Member;
import gnext.bean.role.Role;
import gnext.service.EntityService;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import javax.persistence.EntityManager;

/**
 *
 * @author hungpham
 */
@Local
public interface RoleService extends EntityService<Role> {
    /**
     * Tìm kiếm role cùng query người dùng manual.
     * @param companyId
     * @param query
     * @return 
     */
    public List<Role> search(int companyId, String query);
    
    public void delete(Member memberLogined, Integer roleId);
    
    /**
     * Thực hiện xóa quan hệ ROLE với các MEMBER và GROUP.
     * @param roleId
     * @param type 
     *          <ul>
     *              <li>0: Xóa quan hệ và quyền của ROLE nhưng giữ lại ROLE trong DB.</li>
     *              <li>1: Xóa quan hệ và quyền của ROLE và xóa luôn ROLE khỏi DB.</li>
     *              <li>2: Xóa quan hệ với ROLE nhưng giữ lại quyền của ROLE và cập nhật trạng thái của ROLE là đã xóa.</li>
     *          </ul>
     * @param em them tham so nay vi service nay su dung o ca 2 tang controller va service.
     * @throws java.lang.Exception
     */
    public void removeRelation(Integer roleId, int type, EntityManager em) throws Exception;

    /**
     * Xử lí cập nhật hoặc thêm mới role trên masterdb.
     * @param memLogined
     * @param role
     * @param selectedGroup
     * @param selectedMember
     * @param pageActionIds
     * @throws Exception 
     */
    public void createOrUpdate(Member memLogined, Role role, List<String> selectedGroup, List<String> selectedMember, Map<Integer, List<Integer>> pageActionIds) throws Exception;
}
