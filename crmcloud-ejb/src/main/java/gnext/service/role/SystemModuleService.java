/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role;

import gnext.bean.role.SystemModule;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author hungpham
 */
@Local
public interface SystemModuleService extends EntityService<SystemModule>{
    
    public SystemModule save(SystemModule module) throws Exception;
    public List<SystemModule> findAllAvailable();
    public List<SystemModule> findAvailableIn(List<Integer> includeList);
}
