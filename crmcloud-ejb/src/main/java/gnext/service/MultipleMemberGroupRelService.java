/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service;

import gnext.bean.MultipleMemberGroupRel;
import java.util.List;
import javax.ejb.Local;


/**
 *
 * @author tungdt
 */
@Local
public interface MultipleMemberGroupRelService extends EntityService<MultipleMemberGroupRel> {
    public List<MultipleMemberGroupRel> findByMemberId(Integer memberId);
}
