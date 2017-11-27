/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service;

import gnext.bean.UnionCompanyRel;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface UnionCompanyRelService extends EntityService<UnionCompanyRel> {
    public List<UnionCompanyRel> findByUnionKey(String companyUnionKey);
    public List<String> findAllUnionKey();
    public List<Integer> findAllCompanyGroupIds(String companyUnionKey);
    public List<Integer> findAllCompanyGroupIds(Integer currentCompanyId);
}
