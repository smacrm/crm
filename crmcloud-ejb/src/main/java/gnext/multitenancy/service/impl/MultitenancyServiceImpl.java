/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.multitenancy.service.impl;

import gnext.bean.Company;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.MultipleMemberGroupRel;
import gnext.bean.MultipleMemberGroupRelPK;
import gnext.bean.role.SystemUseAuthRel;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.multitenancy.service.MultitenancyService;
import gnext.service.UnionCompanyRelService;
import gnext.utils.Console;
import gnext.utils.JPAUtils;
import gnext.utils.MapObjectUtil;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Stateless
public class MultitenancyServiceImpl implements MultitenancyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultitenancyServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @EJB private UnionCompanyRelService unionCompanyRelService;
    
    @Override public void updateCompanyOnSlaveDB(final Company company) throws Exception {
        
    }
    
    @Override public void createGroupOnSlaveDB(Group groupOnMaster) throws Exception {
        if(groupOnMaster == null || groupOnMaster.getGroupId() == null)
            throw new IllegalArgumentException("id's groups is null.");
        
        if(isLoginedAdminCompany(groupOnMaster.getCompanyId())) return;
        
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(groupOnMaster.getCompanyId(), em_slave);
            
            if(em_slave == null) return;
            if(em_slave.find(Group.class, groupOnMaster.getGroupId()) != null) return;
            
            // bắt đầu quá trình đồng bộ dữ liệu xuống Slave DB.
            Group groupOnSlave = MapObjectUtil.convert(groupOnMaster);
            groupOnSlave.setManualId(groupOnMaster.getManualId());
            Group groupOnSlaveSaved = JPAUtils.create(groupOnSlave, em_slave, true);
            
            if(groupOnSlaveSaved.getGroupId() == null) throw new Exception("Can not save the new Group to Slave.");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    @Override public void updateGroupOnSlaveDB(final Group groupOnMaster) throws Exception {
        if(isLoginedAdminCompany(groupOnMaster.getCompanyId())) return;
        
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(groupOnMaster.getCompanyId(), em_slave);
            
            if(em_slave == null) return;
            if(em_slave.find(Group.class, groupOnMaster.getGroupId()) == null) return;
            
            // đồng bộ group xuống slavedb.
            Group groupOnSlave = MapObjectUtil.convert(groupOnMaster);
            groupOnSlave.setManualId(groupOnMaster.getManualId());
            groupOnSlave.setGroupId(groupOnMaster.getGroupId());
            JPAUtils.edit(groupOnSlave, em_slave, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    @Override public Group deleteGroupOnSlaveDB(int companySlaveId, Group g) throws Exception {
        EntityManager em_master = null;
        EntityManager em_slave = null;
        EntityTransaction tx = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx = em_master.getTransaction(); tx.begin();
            
            g.setGroupDeleted((short) 1);
            JPAUtils.edit(g, em_master, false);
            
            if(!isLoginedAdminCompany(companySlaveId)) {
                em_slave = JPAUtils.getSlaveEntityManager(companySlaveId);
                Group groupOnSlave = em_slave.find(Group.class, g.getGroupId());
                if(groupOnSlave != null) {
                    groupOnSlave.setGroupDeleted((short) 1);
                    JPAUtils.edit(groupOnSlave, em_slave, true);
                }
            }
            
            tx.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            tx.rollback();
            throw e;
        } finally {
            JPAUtils.release(em_master, true);
            JPAUtils.release(em_slave, true);
        }
        return g;
    }
    
    @Override public void createMemberOnSlaveDB(Member memberOnMaster, List<MultipleMemberGroupRel> multipleMemberGroupRels) throws Exception {
        if(isLoginedAdminCompany(memberOnMaster.getGroup().getCompanyId())) return;
        if(isLoginedSuperAdminMember(memberOnMaster.getMemberId())) return;
        
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(memberOnMaster.getGroup().getCompanyId(), em_slave);
            if(em_slave == null) return;
            if(em_slave.find(Member.class, memberOnMaster.getMemberId()) != null) return;
            
            // đồng bộ member xuống slavedb.
            Member memberOnSlave = MapObjectUtil.convert(memberOnMaster, true);
            memberOnSlave.setManualId(memberOnMaster.getMemberId());
            Member memberOnSlaveSaved = JPAUtils.create(memberOnSlave, em_slave, true);
            if(memberOnSlaveSaved.getMemberId() == null) throw new Exception("Can not save the new Member to Slave.");
            
            // đồng bộ crm_multiple_member_group_rel xuống slavedb.
            String sql = "DELETE FROM MultipleMemberGroupRel c WHERE c.crmMultipleMemberGroupRelPK.memberId = :memberId";
            JPAUtils.executeDeleteOrUpdateQuery(em_slave, em_slave.createQuery(sql, MultipleMemberGroupRel.class).setParameter("memberId", memberOnMaster.getMemberId()));
            
            for(MultipleMemberGroupRel mmgr : multipleMemberGroupRels) {
                MultipleMemberGroupRelPK mmgrpk = mmgr.getMultipleMemberGroupRelPK();
                MultipleMemberGroupRel mmgrSaving = new MultipleMemberGroupRel(mmgrpk.getMemberId(), mmgrpk.getGroupId());
                JPAUtils.create(mmgrSaving, em_slave, true);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    @Override public void updateMemberOnSlaveDB(final Member memberOnMaster, List<MultipleMemberGroupRel> multipleMemberGroupRels) throws Exception {
        if(isLoginedAdminCompany(memberOnMaster.getGroup().getCompanyId())) return;
        if(isLoginedSuperAdminMember(memberOnMaster.getMemberId())) return;
        
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(memberOnMaster.getGroup().getCompanyId(), em_slave);
            if(em_slave == null) return;
            if(em_slave.find(Member.class, memberOnMaster.getMemberId()) == null) return;
            
            // đồng bộ member xuống slavedb.
            Member memberOnSlave = MapObjectUtil.convert(memberOnMaster, true);
            memberOnSlave.setMemberId(memberOnMaster.getMemberId());
            JPAUtils.edit(memberOnSlave, em_slave, true);
            
            String sql = "DELETE FROM MultipleMemberGroupRel c WHERE c.crmMultipleMemberGroupRelPK.memberId = :memberId";
            JPAUtils.executeDeleteOrUpdateQuery(em_slave, em_slave.createQuery(sql, MultipleMemberGroupRel.class).setParameter("memberId", memberOnMaster.getMemberId()));
            
            for(MultipleMemberGroupRel mmgr : multipleMemberGroupRels) {
                MultipleMemberGroupRelPK mmgrpk = mmgr.getMultipleMemberGroupRelPK();
                MultipleMemberGroupRel mmgrSaving = new MultipleMemberGroupRel(mmgrpk.getMemberId(), mmgrpk.getGroupId());
                JPAUtils.create(mmgrSaving, em_slave, true);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    @Override public Member deleteMemberOnSlaveDB(int companySlaveId, Member member) throws Exception {
        EntityManager em_master = null;
        EntityManager em_slave = null;
        EntityTransaction tx = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx = em_master.getTransaction(); tx.begin();
            
            member.setMemberDeleted((short) 1);
            JPAUtils.edit(member, em_master, false);
            
            if(!isLoginedAdminCompany(companySlaveId)) {
                em_slave = JPAUtils.getSlaveEntityManager(companySlaveId);
                Member memberOnSlave = em_slave.find(Member.class, member.getMemberId());
                if(memberOnSlave != null) {
                    memberOnSlave.setMemberDeleted((short) 1);
                    JPAUtils.edit(memberOnSlave, em_slave, true);
                }
                
                // tìm tất cả các công ty group và xóa member nếu tồn tại.
                List<Integer> companyGroupIds = unionCompanyRelService.findAllCompanyGroupIds(companySlaveId);
                if(companyGroupIds != null) {
                    for(Integer companyGroupId : companyGroupIds) {
                        if(companyGroupId != null && companyGroupId == companySlaveId) continue;
                        deleteMemberOnCompanyGroup(member.getMemberId(), companyGroupId);
                    }
                }
            }
            
            tx.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            tx.rollback();
            throw e;
        } finally {
            JPAUtils.release(em_master, true);
            JPAUtils.release(em_slave, true);
        }
        return member;
    }
    private void deleteMemberOnCompanyGroup(int memberId, int companyGroupId) {
        EntityManager em_group = null;
        try {
            em_group = JPAUtils.getSlaveEntityManager(companyGroupId);
            if(em_group == null) return;
            
            Member memberOnCompanyGroup = em_group.find(Member.class, memberId);
            if(memberOnCompanyGroup == null) return;
            
            String sql = "update crm_member set member_deleted=? where member_id=?";
            JPAUtils.executeDeleteOrUpdateQuery(em_group, em_group.createNativeQuery(sql).setParameter(1, (short) 1).setParameter(2, memberId));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_group, true);
        }
    }

    @Override public List<Group> findAllGroupUnderSlave(int companyId)  throws Exception {
        EntityManager em = null;
        try {
            if(isLoginedAdminCompany(companyId)) {
                em = masterEntityManager.getEntityManager();
            } else {
                em = JPAUtils.getSlaveEntityManager(companyId);
            }
            if(em == null) return new ArrayList<>();
            
            String sql = "SELECT g FROM Group g WHERE (g.groupDeleted is null OR g.groupDeleted=0) AND g.companyId=:companyId";
            Query query = JPAUtils.buildJQLQuery(em, sql).setParameter("companyId", companyId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em, true);
        }
    }
    
    @Override public Member findMemberOnSlaveById(int companyId, int memberId) throws Exception {
        EntityManager em = null;
        try {
            if(isLoginedAdminCompany(companyId)) {
                em = masterEntityManager.getEntityManager();
            } else {
                em = JPAUtils.getSlaveEntityManager(companyId);
            }
            if(em == null) return null;
            
            Member memberOnGroup = em.find(Member.class, memberId);
            if(memberOnGroup == null) return null;
            if(memberOnGroup.getMemberDeleted() != null && memberOnGroup.getMemberDeleted() == 1) return null;
            return memberOnGroup;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em, true);
        }
    }
    @Override public List<Member> findMemberOnSlaveByGroupId(int companyId, int groupId) {
        EntityManager em = null;
        try {
            if(isLoginedAdminCompany(companyId)) {
                em = masterEntityManager.getEntityManager();
            } else {
                em = JPAUtils.getSlaveEntityManager(companyId);
            }
            if(em == null) return new ArrayList<>();
            
            String sql = "SELECT m FROM Member m WHERE m.groupId=:groupId  AND (m.memberDeleted is null or m.memberDeleted=0)";
            Query query = em.createQuery(sql, Member.class).setParameter("groupId", groupId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em, true);
        }
        return new ArrayList<>();
    }
    @Override public List<Member> findAllMemberOnSlave(int companySlaveId) {
        EntityManager em = null;
        try {
            if(isLoginedAdminCompany(companySlaveId)) {
                em = masterEntityManager.getEntityManager();
            } else {
                em = JPAUtils.getSlaveEntityManager(companySlaveId);
            }
            if(em == null) return new ArrayList<>();
            
            String sql = "SELECT m FROM Member m WHERE"
                    + " m.groupId IN (SELECT g.groupId FROM Group g WHERE g.companyId=:companyId AND (g.groupDeleted is null or g.groupDeleted = 0))"
                    + " AND (m.memberDeleted is null or m.memberDeleted=0)";
            Query query = em.createQuery(sql, Member.class).setParameter("companyId", companySlaveId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em, true);
        }
        return new ArrayList<>();
    }
    @Override public List<Integer> findAllMemberIdsOnSlave(int companySlaveId) {
        EntityManager em = null;
        try {
            if(isLoginedAdminCompany(companySlaveId)) {
                em = masterEntityManager.getEntityManager();
            } else {
                em = JPAUtils.getSlaveEntityManager(companySlaveId);
            }
            if(em == null) return new ArrayList<>();
            
            String sql = "SELECT m FROM Member m WHERE m.group.company.companyId=:companyId AND (m.memberDeleted is null or m.memberDeleted=0)";
            Query query = em.createQuery(sql).setParameter("companyId", companySlaveId);
            List<Member> members = query.getResultList();
            if(members == null || members.isEmpty()) return new ArrayList<>();
            
            List<Integer> re = new ArrayList<>();
            for(Member member : members) { re.add(member.getMemberId()); }
            return re;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em, true);
        }
        return new ArrayList<>();
    }
    @Override public List<Integer> findAllMemberAllowLoginComapnyGroup(int companySlaveId) throws Exception {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companySlaveId);
            String sql = "select * from crm_member where (member_deleted is null or member_deleted=0) and member_global_flag=1 and group_id in (select group_id from crm_group where company_id=?)";
            Query q = em_slave.createNativeQuery(sql, Member.class);
            q.setParameter(1, companySlaveId);
            List<Member> members = q.getResultList();
            if(members == null) return new ArrayList<>();
            List<Integer> memberIds = new ArrayList<>();
            for(Member m : members) {
                memberIds.add(m.getMemberId());
            }
            return memberIds;
        } catch(Exception ex){
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    @Override public List<Integer> findAllMemberIdsOnGroupByIds(List<Integer> memberIds, int companyGroupId) {
        if(memberIds == null || memberIds.isEmpty()) return new ArrayList<>();
        
        EntityManager em = null;
        try {
            if(isLoginedAdminCompany(companyGroupId)) {
                em = masterEntityManager.getEntityManager();
            } else {
                em = JPAUtils.getSlaveEntityManager(companyGroupId);
            }
            if(em == null) return new ArrayList<>();
            
            String sql = "SELECT m FROM Member m WHERE m.group.company.companyId=:companyId AND m.memberId IN :memberIds";
            Query query = em.createQuery(sql).setParameter("companyId", companyGroupId).setParameter("memberIds", memberIds);
            List<Member> members = query.getResultList();
            if(members == null || members.isEmpty()) return new ArrayList<>();
            
            List<Integer> re = new ArrayList<>();
            for(Member member : members) { re.add(member.getMemberId()); }
            return re;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em, true);
        }
        return new ArrayList<>();
    }
    
    @Override public List<MultipleMemberGroupRel> findMultipleMemberGroupRelByMemberIdOnSlave(int companyId, int memberId) throws Exception {
        EntityManager em = null;
        try {
            if(isLoginedAdminCompany(companyId)) {
                em = masterEntityManager.getEntityManager();
            } else {
                em = JPAUtils.getSlaveEntityManager(companyId);
            }
            if(em == null) return new ArrayList<>();
            
            String sql = "SELECT c FROM MultipleMemberGroupRel c WHERE c.crmMultipleMemberGroupRelPK.memberId = :memberId ";
            Query query = em.createQuery(sql, MultipleMemberGroupRel.class).setParameter("memberId", memberId);
            List<MultipleMemberGroupRel> results = query.getResultList();
            if(results != null && !results.isEmpty()) return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em, true);
        }
        return new ArrayList<>();
    }
    
    @Override public void updateMemberOnGroup(int companyId, Member member, Integer[] memberGroupRelIds) throws Exception {
        if(isLoginedAdminCompany(companyId)) return;
        
        EntityManager em_group = null;
        EntityTransaction tx_slave = null;
        try {
            em_group = JPAUtils.getSlaveEntityManager(companyId);
            if(em_group == null) return;
            
            tx_slave = em_group.getTransaction();
            tx_slave.begin();
            
            // cập nhật thông tin member.
            JPAUtils.edit(member, em_group, false);
            
            // cập nhật danh sách phòng ban được phép quản lí.
            String sql = "DELETE FROM MultipleMemberGroupRel c WHERE c.crmMultipleMemberGroupRelPK.memberId = :memberId";
            Query query = em_group.createQuery(sql, MultipleMemberGroupRel.class).setParameter("memberId", member.getMemberId());
            query.executeUpdate();
            JPAUtils.flush(em_group);
            
            if(memberGroupRelIds != null && memberGroupRelIds.length > 0) {
                for (Integer memberGroupRelId : memberGroupRelIds) {
                    MultipleMemberGroupRel multipleMemberGroupRel = new MultipleMemberGroupRel(member.getMemberId(), memberGroupRelId);
                    JPAUtils.create(multipleMemberGroupRel, em_group, false);
                }
            }
            
            tx_slave.commit();
        } catch (Exception e) {
            if(tx_slave != null) tx_slave.rollback();
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_group, true);
        }
    }
    @Override public void markDeletedMemberOnCompanyGroup(Integer companySlaveId, Integer companyGroupId, Integer memberId, boolean isDeleted) throws Exception {
        EntityManager em_group = null;
        EntityManager em_slave= null;
        EntityManager em_master = null;
        
        // trạng thái của member.
        String sql = "update crm_member set member_deleted=? where member_id=?";
        // xóa role của group trên công ty group.
        String cleanRoleSql = "delete from crm_system_use_auth_rel where company_id=? and group_member_flag=? and group_member_id=?";
        String sqlFindMemberOnGroup = "select * from crm_member where group_id=? and member_id != ? and (member_deleted is null or member_deleted=0)";
        try {
            em_group = JPAUtils.getSlaveEntityManager(companyGroupId);
            em_master = masterEntityManager.getEntityManager();
            em_slave = JPAUtils.getSlaveEntityManager(companySlaveId);
            
            // nếu member không tồn tại dưới công ty group thì bỏ qua.
            Member memberOnCompanyGroup = em_group.find(Member.class, memberId); if(memberOnCompanyGroup == null) return;
            int groupOnComapnyGroupId = memberOnCompanyGroup.getGroupId();
            
            // lấy member trên master.
            Member memberOnMaster = em_master.find(Member.class, memberId);
            int groupOnMasterGroupId = memberOnMaster.getGroupId();
            
            // lấy member trên slave.
            Member memberOnSlave = em_slave.find(Member.class, memberId);
            int groupOnSlaveGroupId = memberOnSlave.getGroupId();
            
            // xóa role của member theo group tại công ty group.
            // trường hợp member ở công ty group thuộc group có nhiều members khác thì không được xóa quyền của group trên công ty group đó.
//            int groupOnComapnyGroupId = memberOnCompanyGroup.getGroupId();
            
            // trường hợp member đang ở trên công ty group.
            if(groupOnMasterGroupId != groupOnComapnyGroupId) {
                Query query = em_group.createNativeQuery(sqlFindMemberOnGroup, Member.class);
                query.setParameter(1, groupOnComapnyGroupId);
                query.setParameter(2, memberId);
                List<Member> members = query.getResultList();
                if(members == null || members.isEmpty()) {
                    // FIXME-DAIND: tạm thời sẽ không xóa quyền của Role, tránh trường hợp 1 số member thuộc Group mất quyền.
//                    JPAUtils.executeDeleteOrUpdateQuery(em_master, em_master.createNativeQuery(cleanRoleSql)
//                            .setParameter(1, companyGroupId).setParameter(2, SystemUseAuthRel.GROUP_FLAG).setParameter(3, groupOnComapnyGroupId));
                }

                // xóa role của member tại công ty group.
                JPAUtils.executeDeleteOrUpdateQuery(em_master, em_master.createNativeQuery(cleanRoleSql)
                        .setParameter(1, companyGroupId).setParameter(2, SystemUseAuthRel.MEMBER_FLAG).setParameter(3, memberId));

                // cập nhật trạng thái là deleted của member tại công ty group.
                short deleted = isDeleted ? (short)1 : (short)0;
                JPAUtils.executeDeleteOrUpdateQuery(em_group, em_group.createNativeQuery(sql).setParameter(1, deleted).setParameter(2, memberId));
            } else { // trường hợp member đang ở trên công ty slave.
                
                Query query = em_slave.createNativeQuery(sqlFindMemberOnGroup, Member.class);
                query.setParameter(1, groupOnSlaveGroupId);
                query.setParameter(2, memberId);
                List<Member> members = query.getResultList();
                if(members == null || members.isEmpty()) {
                    // FIXME-DAIND: tạm thời sẽ không xóa quyền của Role, tránh trường hợp 1 số member thuộc Group mất quyền.
//                    JPAUtils.executeDeleteOrUpdateQuery(em_master, em_master.createNativeQuery(cleanRoleSql)
//                            .setParameter(1, companySlaveId).setParameter(2, SystemUseAuthRel.GROUP_FLAG).setParameter(3, groupOnSlaveGroupId));
                }
                
                // xóa role của member tại công ty group.
                JPAUtils.executeDeleteOrUpdateQuery(em_master, em_master.createNativeQuery(cleanRoleSql)
                        .setParameter(1, companySlaveId).setParameter(2, SystemUseAuthRel.MEMBER_FLAG).setParameter(3, memberId));
                
                // cập nhật trạng thái là deleted của member tại công ty group.
                short deleted = isDeleted ? (short)1 : (short)0;
                JPAUtils.executeDeleteOrUpdateQuery(em_slave, em_slave.createNativeQuery(sql).setParameter(1, deleted).setParameter(2, memberId));
            }
            
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_group, true);
            JPAUtils.release(em_master, true);
            JPAUtils.release(em_slave, true);
        }
    }
    @Override public void copyMemberToCompanyGroup(Member memberOnMaster, Integer companyGroupId, Integer groupOnCompanyGroupId) throws Exception {
        
        EntityManager em_group = null;
        try {
            em_group = JPAUtils.getSlaveEntityManager(companyGroupId);
            if(em_group == null) return;
            
            Member memberOnCompanyGroup = em_group.find(Member.class, memberOnMaster.getMemberId());
            if(memberOnCompanyGroup != null) { // trường hợp trên công ty group đã có member rồi thì update dữ liệu.
                Member memberOnGroupEditing = MapObjectUtil.convert(memberOnMaster, false);
                memberOnGroupEditing.setGroupId(groupOnCompanyGroupId);
                memberOnGroupEditing.setMemberId(memberOnMaster.getMemberId());
                memberOnGroupEditing.setMemberDeleted((short) 0);
                JPAUtils.edit(memberOnGroupEditing, em_group, true);
            } else { // clone dữ liệu xuống công ty group.
                Member newMemberOnCompanyGroup = MapObjectUtil.convert(memberOnMaster, false);
                newMemberOnCompanyGroup.setGroupId(groupOnCompanyGroupId);
                newMemberOnCompanyGroup.setManualId(memberOnMaster.getMemberId());
                newMemberOnCompanyGroup.setMemberDeleted((short) 0);
                JPAUtils.create(newMemberOnCompanyGroup, em_group, true);
            }
        } catch (Exception e) {
            Console.log(e);
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            JPAUtils.release(em_group, true);
        }
    }

    private boolean isLoginedAdminCompany(int companyId) {
        return companyId == Company.MASTER_COMPANY_ID;
    }
    private boolean isLoginedSuperAdminMember(int memberId) {
        return memberId == Member.SUPER_ADMIN_MEMBER_ID;
    }
}
