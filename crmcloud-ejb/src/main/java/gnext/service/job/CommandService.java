/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.job;

import gnext.bean.job.Command;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface CommandService extends EntityService<Command> {
    
    public List<Command> findByCompanyId(Integer companyId);
    public Command findById(Integer commandId, Integer companyId);
}
