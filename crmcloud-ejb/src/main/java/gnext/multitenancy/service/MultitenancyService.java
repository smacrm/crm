/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.multitenancy.service;

import gnext.bean.Company;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.MultipleMemberGroupRel;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface MultitenancyService extends java.io.Serializable {
    public void updateCompanyOnSlaveDB(final Company company) throws Exception;
    
    public void createGroupOnSlaveDB(final Group groupOnMaster) throws Exception;
    public void updateGroupOnSlaveDB(final Group groupOnMaster) throws Exception;
    public Group deleteGroupOnSlaveDB(int companySlaveId, Group g) throws Exception;
    
    public void createMemberOnSlaveDB(final Member memberOnMaster, List<MultipleMemberGroupRel> multipleMemberGroupRels) throws Exception;
    public void updateMemberOnSlaveDB(final Member memberOnMaster, List<MultipleMemberGroupRel> multipleMemberGroupRels) throws Exception;
    public Member deleteMemberOnSlaveDB(int companySlaveId, Member member) throws Exception;
    
    public void updateMemberOnGroup(int companyId, Member member, Integer[] memberGroupRelIds) throws Exception;
    public void markDeletedMemberOnCompanyGroup(Integer companySlaveId, Integer companyGroupId, Integer memberId, boolean isDeleted) throws Exception;
    
    public List<Group> findAllGroupUnderSlave(int companyId) throws Exception;
    public List<MultipleMemberGroupRel> findMultipleMemberGroupRelByMemberIdOnSlave(int companyId, int memberId) throws Exception;

    public Member findMemberOnSlaveById(int companyId, int memberId) throws Exception;
    public List<Member> findAllMemberOnSlave(int companySlaveId);
    public List<Integer> findAllMemberIdsOnSlave(int companyId);
    public List<Integer> findAllMemberAllowLoginComapnyGroup(int companySlaveId) throws Exception;
    public List<Integer> findAllMemberIdsOnGroupByIds(List<Integer> memberIds, int companyGroupId);
    public List<Member> findMemberOnSlaveByGroupId(int companyId, int groupId);
    
    public void copyMemberToCompanyGroup(Member memberOnMaster, Integer companyGroupId, Integer groupOnCompanyGroupId) throws Exception;
}
