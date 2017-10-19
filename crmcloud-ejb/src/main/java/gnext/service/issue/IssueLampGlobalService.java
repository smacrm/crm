/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue;

import gnext.bean.issue.IssueLampGlobal;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author tungdt
 */
@Local
public interface IssueLampGlobalService extends EntityService<IssueLampGlobal> {
    public IssueLampGlobal findByPK(Integer itemId, String locale);
    public List<IssueLampGlobal> findById(Integer itemId);
    public boolean checkExistIssueLampGlobal(Integer itemId);
}
