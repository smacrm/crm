/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role;

import gnext.bean.role.SystemUseAuthRel;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;
import javax.persistence.EntityManager;

/**
 *
 * @author daind
 */
@Local
public interface SystemUseAuthRelService extends EntityService<SystemUseAuthRel> {
    public List<SystemUseAuthRel> findByRoleFlag(int companyId, int groupMemberId, short groupMemberFlag, Short hidden);
    public SystemUseAuthRel find(int companyId, int groupMemberId, short groupMemberFlag, int roleId);
    
    public List<SystemUseAuthRel> find( int groupMemberId, short groupMemberFlag);
    public List<SystemUseAuthRel> findByFlag(int roleId, short groupMemberFlag);
    
    public List<SystemUseAuthRel> find(int companyId, int roleId);
    
    public void delete(SystemUseAuthRel systemUseAuthRel, EntityManager em_master) throws Exception;
}
