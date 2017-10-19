/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role;

import gnext.bean.role.RolePageMethodRel;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author hungpham
 */
@Local
public interface RolePageMethodService extends EntityService<RolePageMethodRel>{
    public List<RolePageMethodRel> findByRoleId(Integer roleId);
    public List<RolePageMethodRel> findByRoleListId(List<Integer> roleListId);
}
