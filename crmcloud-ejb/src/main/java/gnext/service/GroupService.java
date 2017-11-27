/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service;

import gnext.bean.Group;
import gnext.bean.Member;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface GroupService extends EntityService<Group> {
    public List<Group> find(int first, int pageSize, String sortField, String sortOrder, String where);
    public int total(final String where);
    
    public List<Group> findByTarget(final int companyId, String target);
    public List<Group> findByCompanyId(final int companyId);
    public List<Group> findByCompanyId(final int companyId, short isDeleted);
    public List<Group> findByGroupName(String groupName, int companyId );
    public List<Group> findGroupTree(Integer companyId, List<Group> disableGroup);
    public List<Group> findByGroupParentId(final int groupParentId);
    public List<Group> findByGroupIsExists(int companyId);
    
    public Group findByGroupId(final int groupId);
    
    public Group insert(Group g, Member m, Integer[] roleIds) throws Exception;
    public Group update(Group g, Member m, Integer[] roleIds) throws Exception;
    
    public List<Group> findRootGroup(Integer currentCompanyId);
}
