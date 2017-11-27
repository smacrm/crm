package gnext.service.impl;

import gnext.bean.CompanyTargetInfo;
import gnext.bean.CompanyTargetInfoPK;
import gnext.bean.Group;
import gnext.bean.Company;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import gnext.bean.Member;
import gnext.bean.MemberAuth;
import gnext.bean.MemberAuthPK;
import gnext.bean.MultipleMemberGroupRel;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.bean.role.Role;
import gnext.bean.role.RolePageMethodRel;
import gnext.bean.role.SystemModule;
import gnext.bean.role.SystemUseAuthRel;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.filetransfer.DeleteParameter;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.interceptors.annotation.QuickSearchAction;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.GroupService;
import gnext.service.MemberService;
import gnext.service.MultipleMemberGroupRelService;
import gnext.service.attachment.ServerService;
import gnext.utils.MapObjectUtil;
import gnext.utils.StringBuilderUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.multitenancy.service.MultitenancyService;
import gnext.service.attachment.AttachmentService;
import gnext.service.role.RolePageMethodService;
import gnext.service.role.RoleService;
import gnext.service.role.SystemUseAuthRelService;
import gnext.utils.JPAUtils;
import java.util.Map;
import javax.persistence.EntityTransaction;

/**
 *
 * @author hungpham
 */
@Stateless
public class MemberServiceImpl extends AbstractService<Member> implements MemberService {
    private static final long serialVersionUID = -5622590461070525727L;
    private final Logger LOGGER = LoggerFactory.getLogger(MemberServiceImpl.class);

    @Inject TenantHolder tenantHolder;
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    @EJB private AttachmentService attachmentService;
    @EJB private SystemUseAuthRelService systemUseAuthRelService;
    @EJB private MultipleMemberGroupRelService groupRelService;
    @EJB private GroupService groupService;
    @EJB private ServerService serverService;
    @EJB private MultitenancyService multitenancyService;
    @EJB private RolePageMethodService rolePageMethodService;
    @EJB private RoleService roleService;
    
    public MemberServiceImpl() { super(Member.class); }
    
    @Override
    public List<Member> findByGroupId(final int groupId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Member c WHERE c.group.groupId = :groupId";
            return em_master.createQuery(sql, Member.class).setParameter("groupId", groupId).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    @Override
    public Member findByMemberId(int memberId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Member c WHERE c.memberId = :memberId";
            return em_master.createQuery(sql, Member.class).setParameter("memberId", memberId).getSingleResult();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }
    
    @Override
    public Member findByMemberName(String memberFullName, Integer companyId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Member c WHERE c.group.company.companyId = :companyId AND CONCAT(c.memberNameFirst, c.memberNameLast) = :memberFullName";
            Query q = em_master.createQuery(sql, Member.class);
            q.setParameter("memberFullName", memberFullName);
            q.setParameter("companyId", companyId);
            q.setMaxResults(1);
            
            return (Member) q.getSingleResult();
        } catch (NoResultException nre) {
            LOGGER.error(nre.getMessage(), nre);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    /**
     * Xử lí cho trường hợp Member logined vào công ty group.
     * Hiện tại member đang thuộc group khác trên công ty group.
     * @param memberId
     * @param companyId
     * @return
     * @throws Exception 
     */
    public List<MemberAuth> getRoleListV2(int memberId, int companyId) throws Exception {
        EntityManager em_master = null;
        Query query = null;
        String sql = "select * from crm_system_use_auth_rel where company_id=? and group_member_flag=? and group_member_id=?";
        try {
            em_master = masterEntityManager.getEntityManager();
            Member memberOnMaster = em_master.find(Member.class, memberId);
            if(memberOnMaster == null || (memberOnMaster.getMemberDeleted() != null && memberOnMaster.getMemberDeleted() == 1)) return getRoleList(memberId, companyId);
            
            if(memberOnMaster.getGroup().getCompanyId() != companyId) { // trường hợp member login vào công ty group.
                Member memberOnCompanyGroup = multitenancyService.findMemberOnSlaveById(companyId, memberId);
                if(memberOnCompanyGroup == null || (memberOnCompanyGroup.getMemberDeleted() != null && memberOnCompanyGroup.getMemberDeleted() == 1)) return new ArrayList<>();
                
                int groupOnCompanyGroupId = memberOnCompanyGroup.getGroupId();
                
                List<SystemUseAuthRel> groupsystemUseAuthRels;
                List<SystemUseAuthRel> membersystemUseAuthRels;
                        
                // lấy danh sách quyền member theo group thuộc công ty.
                query = em_master.createNativeQuery(sql, SystemUseAuthRel.class);
                query.setParameter(1, companyId);
                query.setParameter(2, SystemUseAuthRel.GROUP_FLAG);
                query.setParameter(3, groupOnCompanyGroupId);
                groupsystemUseAuthRels = query.getResultList();
                
                // lấy danh sách quyền member theo công ty.
                query = em_master.createNativeQuery(sql, SystemUseAuthRel.class);
                query.setParameter(1, companyId);
                query.setParameter(2, SystemUseAuthRel.MEMBER_FLAG);
                query.setParameter(3, memberId);
                membersystemUseAuthRels = query.getResultList();
                
                List<MemberAuth> memberAuths = new ArrayList<>();
                if(groupsystemUseAuthRels != null) {
                    for(SystemUseAuthRel suar : groupsystemUseAuthRels) {
                        
                        int roleId = suar.getSystemUseAuthRelPK().getRoleId();
                        Role role = roleService.find(roleId);
                        if(role.getRoleDeleted() != null && role.getRoleDeleted() == 1) continue;
                        
                        List<RolePageMethodRel> rolePageMethodRels = rolePageMethodService.findByRoleId(roleId);
                        for(RolePageMethodRel rolePageMethodRel : rolePageMethodRels) {
                            if(rolePageMethodRel.getPage() != null &&
                                    rolePageMethodRel.getPage().getPageDeleted() != null && rolePageMethodRel.getPage().getPageDeleted() == 1) continue;
                            
                            List<SystemModule> systemModules = rolePageMethodRel.getPage().getModuleList();
                            if(systemModules == null || systemModules.isEmpty()) continue;
                            
                            for(SystemModule systemModule : systemModules) {
                                if(systemModule.getModuleDeleted() != null && systemModule.getModuleDeleted() == 1) continue;
                                
                                MemberAuth memberAuth = new MemberAuth();
                                MemberAuthPK memberAuthPK = new MemberAuthPK();
                            
                                memberAuthPK.setCompany_id(companyId);
                                memberAuthPK.setMember_id(memberId);
                                memberAuthPK.setMethod(rolePageMethodRel.getMethod().getMethodName());
                                memberAuthPK.setPage(rolePageMethodRel.getPage().getPageName());
                                memberAuthPK.setModule(systemModule.getModuleName());
                                
                                memberAuth.setMemberAuthPK(memberAuthPK);
                                memberAuth.setRole_flag(SystemUseAuthRel.GROUP_FLAG);
                                
                                memberAuths.add(memberAuth);
                            }
                        }
                    }
                } // kết thúc vòng lặp lấy role theo group.
                
                if(membersystemUseAuthRels != null) {
                    for(SystemUseAuthRel suar : membersystemUseAuthRels) {

                        int roleId = suar.getSystemUseAuthRelPK().getRoleId();
                        Role role = roleService.find(roleId);
                        if(role.getRoleDeleted() != null && role.getRoleDeleted() == 1) continue;
                        
                        List<RolePageMethodRel> rolePageMethodRels = rolePageMethodService.findByRoleId(roleId);
                        for(RolePageMethodRel rolePageMethodRel : rolePageMethodRels) {
                            if(rolePageMethodRel.getPage() != null &&
                                    rolePageMethodRel.getPage().getPageDeleted() != null && rolePageMethodRel.getPage().getPageDeleted() == 1) continue;
                            
                            List<SystemModule> systemModules = rolePageMethodRel.getPage().getModuleList();
                            if(systemModules == null || systemModules.isEmpty()) continue;
                            
                            for(SystemModule systemModule : systemModules) {
                                if(systemModule.getModuleDeleted() != null && systemModule.getModuleDeleted() == 1) continue;
                                
                                MemberAuth memberAuth = new MemberAuth();
                                MemberAuthPK memberAuthPK = new MemberAuthPK();
                            
                                memberAuthPK.setCompany_id(companyId);
                                memberAuthPK.setMember_id(memberId);
                                memberAuthPK.setMethod(rolePageMethodRel.getMethod().getMethodName());
                                memberAuthPK.setPage(rolePageMethodRel.getPage().getPageName());
                                memberAuthPK.setModule(systemModule.getModuleName());
                                
                                memberAuth.setMemberAuthPK(memberAuthPK);
                                memberAuth.setRole_flag(SystemUseAuthRel.MEMBER_FLAG);
                                
                                memberAuths.add(memberAuth);
                            }
                        }
                    }
                } // kết thúc vòng lặp lấy role theo member.
                
                return memberAuths;
            } else { // lấy role như bình thường.
                return getRoleList(memberId, companyId);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
            query = null; // release.
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<MemberAuth> getRoleList(int memberId, int companyId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            Query q = em_master.createNativeQuery("SELECT `company_id`, `member_id`, `role_flag`, "
                + "`module_name` as `module`, `page_name` as `page`, `method_name` as `method` "
                + "FROM `view_system_auth` WHERE `member_id` = ? AND `company_id` = ?", MemberAuth.class);
            q.setParameter(1, memberId);
            q.setParameter(2, companyId);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    @Override
    public Member findByLoginIdPassword(String memberLoginId, String memberPassword) {
        if (StringUtils.isBlank(memberLoginId) || StringUtils.isBlank(memberPassword)) return null;
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Member c WHERE c.memberLoginId = :memberLoginId AND c.memberPassword = :memberPassword AND c.memberDeleted = :memberDeleted";
            Member member = em_master.createQuery(sql, Member.class).setParameter("memberLoginId", memberLoginId).setParameter("memberPassword", memberPassword).setParameter("memberDeleted", 0).getSingleResult();
            return member;
        } catch (NoResultException e) {
            LOGGER.error(e.getMessage());
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    @Override
    public List<Member> findByGroupList(List<Group> groupList, short isDeleted, List<Integer> memberIds) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            List<Integer> groupIdList = new ArrayList<>();
            for (Group g : groupList) groupIdList.add(g.getGroupId());
            
            String sql = "SELECT o FROM Member o WHERE o.group.groupId IN :groupList AND o.memberDeleted = :isDeleted";
            if(!memberIds.isEmpty()) sql = sql + " OR o.memberId IN :memberIds";
            
            Query query = em_master.createQuery(sql).setParameter("groupList", groupIdList).setParameter("isDeleted", isDeleted);
            if(!memberIds.isEmpty()) query.setParameter("memberIds", memberIds);
            
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    private static final String Q_MEMBERS = "SELECT m FROM Member m";
    @Override
    public List<Member> find(Integer first, Integer pageSize, String sortField, String sortOrder, String where) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            List<Integer> memberIds = multitenancyService.findAllMemberIdsOnSlave(tenantHolder.getCompanyId());
            
            String sql = Q_MEMBERS + " WHERE 1=1 AND " + where;
            if(!memberIds.isEmpty()) sql = sql  + " OR (m.memberId IN :memberIds)  ";
            
            Query query = em_master.createQuery(sql);
            if(!memberIds.isEmpty()) query.setParameter("memberIds", memberIds);
            
            if(first != null) query.setFirstResult(first);
            if(pageSize != null) query.setMaxResults(pageSize);

            List<Member> results = query.getResultList();
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    private static final String Q_TOTAL_MEMBERS = "SELECT count(m.memberId) FROM Member m";
    @Override
    public int total(String where) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            List<Integer> memberIds = multitenancyService.findAllMemberIdsOnSlave(tenantHolder.getCompanyId());
            
            String sql = Q_TOTAL_MEMBERS + " WHERE 1=1 and " + where;
            if(!memberIds.isEmpty()) sql = sql  + " OR (m.memberId IN :memberIds)  ";
            
            Query query = em_master.createQuery(sql);
            if(!memberIds.isEmpty()) query.setParameter("memberIds", memberIds);
            
            Long total = (Long) query.getSingleResult();
            return total.intValue();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return 0;
    }

    private void saveRoleToMemberOnMaster(Member saving, Member logined, Integer[] roleIds, EntityManager em_master) throws Exception {
        if(roleIds == null) return;
        int companyId = saving.getGroup().getCompany().getCompanyId();
        int memberId = saving.getMemberId();
        for (int i = 0; i < roleIds.length; i++) {
            int roleId = roleIds[i];
            SystemUseAuthRel suar = new SystemUseAuthRel(roleId, companyId, SystemUseAuthRel.MEMBER_FLAG, memberId);
            suar.setCreatorId(logined.getMemberId());
            suar.setCreatedTime(Calendar.getInstance().getTime());
            JPAUtils.create(suar, em_master, false);
        }
    }
    
    private void deleteAllRoleOfMemberOnMasterAndSlaveAndGroups(Member member, EntityManager em_master)
            throws Exception {
        int companyId = member.getGroup().getCompanyId();
        int memberId = member.getMemberId();
        List<SystemUseAuthRel> listSystemUseAuthRel = systemUseAuthRelService.findByRoleFlag(companyId, memberId, SystemUseAuthRel.MEMBER_FLAG, null);
        for (int i = 0, length = listSystemUseAuthRel.size(); i < length; i++) {
            SystemUseAuthRel systemUseAuthRel = listSystemUseAuthRel.get(i);
            systemUseAuthRelService.delete(systemUseAuthRel, em_master);
        }
    }
    
    private void copyMemberToGroupDB(Member memberOnMaster, boolean isAsMemberGlobalFlag , List<Company> companyGroups, Map<Integer, Integer> mapCompanyGroupIds)
            throws Exception {
        if(companyGroups == null || companyGroups.isEmpty()) return;
        
        // mark deleted tới các member thuộc công ty group.
        if(!isAsMemberGlobalFlag) {
            for (int i = 0; i < companyGroups.size(); i++) {
                int companySlaveId = memberOnMaster.getGroup().getCompanyId();
                int companyGroupId = companyGroups.get(i).getCompanyId();
                int memberId = memberOnMaster.getMemberId();
                multitenancyService.markDeletedMemberOnCompanyGroup(companySlaveId, companyGroupId, memberId, true);
            }
        } else {
            // tạo 1 bản sao của member tới công ty group.
            for (int i = 0; i < companyGroups.size(); i++) {
                int companyGroupId = companyGroups.get(i).getCompanyId();
                if(mapCompanyGroupIds.containsKey(companyGroupId)) {
                    int groupOnCompanyGroupId = mapCompanyGroupIds.get(companyGroupId);
                    if(groupOnCompanyGroupId > 0)
                        multitenancyService.copyMemberToCompanyGroup(memberOnMaster, companyGroupId, groupOnCompanyGroupId);
                    else {
                        int companySlaveId = memberOnMaster.getGroup().getCompanyId();
                        multitenancyService.markDeletedMemberOnCompanyGroup(companySlaveId, companyGroupId, memberOnMaster.getMemberId(), true);
                    }
                }
            }
        }
    }
    
    @QuickSearchAction(action = QuickSearchAction.UPDATE)
    @Override
    public Member edit(Company logined, Member m, Member memModel, byte[] bytes,
            List<CompanyTargetInfo> phones, List<CompanyTargetInfo> phoneDeleted,
            List<CompanyTargetInfo> mobilePhones, List<CompanyTargetInfo> mobilePhonesDelete,
            List<CompanyTargetInfo> email, List<CompanyTargetInfo> emailDeleted,Integer[] memberGroupRelIds,
            Integer[] roleIds,
            boolean isAsMemberGlobalFlag, List<Company> companyBelongGroup,
            boolean isChangeCompany, Map<Integer, Integer> mapCompanyGroupIds)  throws Exception {
        EntityManager em_master = null;
        EntityTransaction tx_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx_master = beginTransaction(em_master);
            
            // Thông tin chung của MEMBER.
            m.setGroupId(m.getGroup().getGroupId());
            m.setUpdatedTime(Calendar.getInstance().getTime());
            m.setUpdator(memModel);
            m.setMemberDeleted((short)0);
            
            // Cập nhật GROUP cho MEMBER.
            if(m.getGroup() != null && m.getGroup().getGroupId() != null)
                m.setGroup(groupService.find(m.getGroup().getGroupId()));
            
            // Thông tin phục vụ cho QUICKSEARCH.
            String metaContent = _StoreMemberInfo(m, memModel, phones, phoneDeleted, mobilePhones, mobilePhonesDelete, email, emailDeleted, em_master);
            
            // Avartar của MEMBER.
            _StoreLogo(m, bytes);
            Member memberEdited = JPAUtils.edit(m, em_master, false);
            
            // Xóa những GROUP mà MEMBER thuộc.
            _RemoveMemberGroupRelByMemberId(memberEdited.getMemberId(), em_master);
            // Cập nhật GROUP cho MEMBER.
            List<MultipleMemberGroupRel> mmgrsOnMaster = _UpdateMemberGroupRel(memberGroupRelIds, memberEdited.getMemberId(), em_master);
            
            memberEdited.setPhoneFaxMailHomepage(metaContent);
            
            // Cập nhật Avatar của MEMBER.
//            Attachment attachment = uploadLogo2FtpServer(memberEdited, bytes);
            
            // xóa toàn bộ roles của member theo member-flag ở tất cả các công ty bao gồm công ty group mà member có quền đăng nhập vào.
            deleteAllRoleOfMemberOnMasterAndSlaveAndGroups(memberEdited, em_master);
            
            // lưu danh sách role đã chọn với member-flag tại công ty đăng nhập.
            saveRoleToMemberOnMaster(memberEdited, memModel, roleIds, em_master);
            
            // lưu trữ trên master.
            commitAndCloseTransaction(tx_master);
            
            // Cập nhật Avatar của MEMBER.
            Attachment attachment = uploadLogo2FtpServer(memberEdited, bytes);
            
            // copy dữ liệu member xuống công ty group nếu member có quyền đăng nhập.
            copyMemberToGroupDB(memberEdited, isAsMemberGlobalFlag, companyBelongGroup, mapCompanyGroupIds);
            
            // đồng bộ dữ meta-data của member xuống công ty slave.
            multitenancyService.updateMemberOnSlaveDB(memberEdited, mmgrsOnMaster);
            
            return memberEdited;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_master);
            throw e;
        } finally {
            JPAUtils.release(em_master, true);
        }
    }
    
    private List<MultipleMemberGroupRel> _UpdateMemberGroupRel(Integer[] memberGroupRelIds, Integer memberId, EntityManager em_master) throws Exception {
        List<MultipleMemberGroupRel> listMemberGroupRel = new ArrayList<>();
        if (memberGroupRelIds != null && memberGroupRelIds.length > 0) {
            for (Integer memberGroupRel : memberGroupRelIds) {
                MultipleMemberGroupRel groupRel = new MultipleMemberGroupRel(memberId, memberGroupRel);
                listMemberGroupRel.add(groupRel);
            }
        }
        for(MultipleMemberGroupRel mmgr : listMemberGroupRel) {
            JPAUtils.create(mmgr, em_master, false);
        }
        return listMemberGroupRel;
    }
    
    private void _RemoveMemberGroupRelByMemberId(Integer memberId, EntityManager em_master) throws Exception {
        List<MultipleMemberGroupRel> memberGroupRels = groupRelService.findByMemberId(memberId);
        for(MultipleMemberGroupRel mmgr : memberGroupRels) {
            JPAUtils.remove(mmgr, em_master, false);
        }
        JPAUtils.flush(em_master);
    }

    @QuickSearchAction(action = QuickSearchAction.CREATE)
    @Override
    public Member insert(Company logined, Member m, Member memModel, byte[] bytes,
            List<CompanyTargetInfo> phones, List<CompanyTargetInfo> phoneDeleted,
            List<CompanyTargetInfo> mobilePhones, List<CompanyTargetInfo> mobilePhonesDelete,
            List<CompanyTargetInfo> email, List<CompanyTargetInfo> emailDeleted, Integer[] memberGroupRelIds,
            Integer[] roleIds,
            boolean isAsMemberGlobalFlag, List<Company> companyBelongGroup, Map<Integer, Integer> mapCompanyGroupIds)
            throws Exception {
        EntityManager em_master = null;
        EntityTransaction tx_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx_master = beginTransaction(em_master);
            
            // Thông tin chung của GROUP.
            m.setGroupId(m.getGroup().getGroupId());
            m.setCreator(memModel);
            m.setCreatedTime(Calendar.getInstance().getTime());
            m.setMemberDeleted((short) 0);
            Member saved = JPAUtils.create(m, em_master, false);
            // sau khi flush có id của member làm tên ảnh Avartar của Member.
            _StoreLogo(saved, bytes);
            saved.setManualId(saved.getMemberId()); // sử dụng trong việc tách DB.
            
            // Lưu quan hệ giữa MEMBER và GROUP.
            List<MultipleMemberGroupRel> multipleMemberGroupRels = new ArrayList<>();
            if(memberGroupRelIds != null && memberGroupRelIds.length > 0 ){
                for(Integer memberGroupRel : memberGroupRelIds){
                    MultipleMemberGroupRel groupRel = new MultipleMemberGroupRel(saved.getMemberId(), memberGroupRel);
                    multipleMemberGroupRels.add(groupRel);
                }
            }
            for(MultipleMemberGroupRel mmgr : multipleMemberGroupRels) JPAUtils.create(mmgr, em_master, false);
            
            // Lấy thông tin phục vụ cho QUICKSEARCH.
            String metaContent = _StoreMemberInfo(saved, memModel, phones, phoneDeleted, mobilePhones, mobilePhonesDelete, email, emailDeleted, em_master);
            Member edited = JPAUtils.edit(saved, em_master, false);
            edited.setPhoneFaxMailHomepage(metaContent);
            
            // Lưu thông tin AVARTAR của MEMBER.
//            Attachment attachment = uploadLogo2FtpServer(edited, bytes);
            
            // lưu danh sách role đã chọn với member-flag tại công ty đăng nhập.
            saveRoleToMemberOnMaster(edited, memModel, roleIds, em_master);
            
            // lưu trữ trên masterdb.
            commitAndCloseTransaction(tx_master);
            
            // Lưu thông tin AVARTAR của MEMBER.
            Attachment attachment = uploadLogo2FtpServer(edited, bytes);
            
            // copy dữ liệu member xuống công ty group nếu member có quyền đăng nhập.
            // trường hợp admin vào công ty slave(được group với masterdb) thì xảy ra xử lí trường hợp
            // group của member sẽ được chỉnh sửa để lấy group theo công ty admin.
            copyMemberToGroupDB(edited, isAsMemberGlobalFlag, companyBelongGroup, mapCompanyGroupIds);
            
            // đồng bộ dữ meta-data của member xuống công ty slave.
            multitenancyService.createMemberOnSlaveDB(edited, multipleMemberGroupRels);
            
            return edited;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_master);
            throw e;
        } finally {
            JPAUtils.release(em_master, true);
        }
    }
    
    /***
     * Hàm xử lí đẩy logo lên ftp gnext server.
     * @param c
     * @param bytes
     * @throws Exception 
     */
    private Attachment uploadLogo2FtpServer(Member m, byte[] bytes) throws Exception {
        if(m.getGroup() == null) throw new Exception("The id of group is null or empty.");
        if(m.getGroup().getCompany() == null) throw new Exception("The id of company is null or empty.");
        
        int slaveCompanyId = m.getGroup().getCompany().getCompanyId();
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(slaveCompanyId);
            tx_slave = beginTransaction(em_slave);
            
            if(m.getMemberId() == null || m.getMemberId() < 0) throw new Exception("The id of member is null or empty.");
            if(bytes == null || bytes.length < 0) return null;

            List<Server> servers = serverService.getAvailable(m.getGroup().getCompany().getCompanyId(), TransferType.FTP.getType(), ServerFlag.COMMON.getId());
            if(servers == null || servers.isEmpty()) throw new Exception("can't get server for upload logo the member.");
            Server server = servers.get(0);
            
            checkExistLogo(m, server, em_slave);
            
            String path = server.getServerFolder();
            path = path + File.separator + m.getGroup().getCompany().getCompanyId() + File.separator + m.getMemberId() + File.separator;
            String host = server.getServerHost();
            int port = server.getServerPort();
            String username = server.getServerUsername();
            String password = server.getDecryptServerPassword();
            boolean security = getBoolean(server.getServerSsl());
            String protocol = server.getServerProtocol();
            String servertype = server.getServerType();

            TransferType tt = TransferType.getTransferType(servertype);
            Parameter param = Parameter.getInstance(tt).manualconfig(true).storeDb(false);
            param.host(host).port(port).username(username).password(password).security(security).protocol(protocol)
                    .uploadfilename(m.getMemberImage()).uploadpath(path).createfolderifnotexists();
            FileTransferFactory.getTransfer(param).upload(new ByteArrayInputStream(bytes));
            gnext.dbutils.model.Attachment attachment = param.getAttachment();
            if(attachment == null) throw new Exception("Upload logo error.");
            Attachment ea = MapObjectUtil.convert(attachment);
            ea.setAttachmentTargetType(AttachmentTargetType.MEMBER.getId());
            ea.setAttachmentTargetId(m.getMemberId());
            ea.setServer(server);
            ea.setCompany(m.getGroup().getCompany());
            ea.setCreatorId(m.getCreator().getMemberId());
            
            Attachment synAttachment = JPAUtils.create(ea, em_slave, false);
            
            commitAndCloseTransaction(tx_slave);
            return synAttachment;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    /***
     * Hàm xử lí kiểm tra logo của member, nếu có thực hiện xóa trong db và ftp server.
     * @param logined
     * @param c 
     */
    private void checkExistLogo(Member m, Server server, EntityManager em_slave)  throws Exception {
        Company c = m.getGroup().getCompany();
        if(c == null || c.getCompanyId() == null) return;
        
        int atId = AttachmentTargetType.MEMBER.getId();
        List<Attachment> attachments = attachmentService.search(m.getGroup().getCompanyId(), atId, m.getMemberId(), (short) 0);
        if(attachments != null && !attachments.isEmpty())
            for(Attachment attachment : attachments) {
                String delRemotePath = attachment.getAttachmentPath() + File.separator + attachment.getAttachmentName();
                delFileRemote(server, delRemotePath);
                JPAUtils.remove(attachment, em_slave, false);
            }
    }
    
    private void delFileRemote(Server server, String delRemotePath) throws Exception {
        String host = server.getServerHost();
        int port = server.getServerPort();
        String username = server.getServerUsername();
        String password = server.getDecryptServerPassword();
        boolean security = getBoolean(server.getServerSsl());
        String protocol = server.getServerProtocol();
        String servertype = server.getServerType();
        
        DeleteParameter delParam = DeleteParameter.getInstance(TransferType.getTransferType(servertype));
        delParam.type(TransferType.FTP).host(host).port(port).username(username).password(password).security(security).protocol(protocol);
        delParam.deletePath(delRemotePath).folder(false);
        
        FileTransferFactory.getTransfer(delParam).delete();
    }
    
    /**
     * Hàm xử lí chuyển đổi Short sang Boolean.
     * @param flag
     * @return 
     */
    private boolean getBoolean(Short flag) {
        if (flag != null && flag == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    private void _UpdateLogoName(Member m) {
        String fn = m.getMemberImage();
        
        if(!StringUtils.isEmpty(fn) && !fn.startsWith(m.getMemberId() + "-"))
            fn = m.getMemberId() + "-" + m.getMemberImage();
        
        m.setMemberImage(fn);
    }
    
    private void _StoreLogo(Member m, byte[] bytes) {
        if (bytes == null) return;
        _UpdateLogoName(m);
    }
    
    private String _StoreMemberInfo(Member m, Member memModel,
            List<CompanyTargetInfo> phones, List<CompanyTargetInfo> phoneDeleted,
            List<CompanyTargetInfo> mobilePhones, List<CompanyTargetInfo> mobilePhonesDeleted,
            List<CompanyTargetInfo> email, List<CompanyTargetInfo> emailDeleted,
            EntityManager em_master) throws Exception{
        StringBuilder metaContent = new StringBuilder();
        
        _SaveMemberTargetInfo(phones, m, memModel, CompanyTargetInfo.COMPANY_FLAG_TYPE_PHONE, em_master);
        _DeleteMemberTargetInfo(phoneDeleted, em_master);
        buildContentQuickSearch(metaContent, phones);

        _SaveMemberTargetInfo(mobilePhones, m, memModel, CompanyTargetInfo.COMPANY_FLAG_TYPE_MOBILE, em_master);
        _DeleteMemberTargetInfo(mobilePhonesDeleted, em_master);
        buildContentQuickSearch(metaContent, mobilePhones);

        _SaveMemberTargetInfo(email, m, memModel, CompanyTargetInfo.COMPANY_FLAG_TYPE_EMAIL, em_master);
        _DeleteMemberTargetInfo(emailDeleted, em_master);
        buildContentQuickSearch(metaContent, email);

        return metaContent.toString();
    }
    
    private void buildContentQuickSearch(StringBuilder meta, List<CompanyTargetInfo> ctis) {
        for(CompanyTargetInfo cti : ctis) {
            if(cti.getCompanyTargetData() != null && !cti.getCompanyTargetData().isEmpty())
                meta.append(cti.getCompanyTargetData()).append(";");
        }
    }
    
    private void _SaveMemberTargetInfo(List<CompanyTargetInfo> cti, Member m, Member memberModel, short flagType, EntityManager em_master) throws Exception{
        Integer memberId = m.getMemberId();
        for (CompanyTargetInfo companyTargetInfo : cti) {
            if(companyTargetInfo.getCompanyTargetInfoPK() != null && companyTargetInfo.getCompanyTargetInfoPK().getTargetId() < 0)
                companyTargetInfo.setCompanyTargetInfoPK(null);
            
            if (companyTargetInfo.getCompanyTargetInfoPK() == null && !StringUtils.isEmpty(companyTargetInfo.getCompanyTargetData())) {
                CompanyTargetInfoPK companyTargetInfoPK = new CompanyTargetInfoPK(CompanyTargetInfo.COMPANY_TARGET_MEMBER, memberId, flagType);
                _CreateMemberInfo(companyTargetInfoPK, companyTargetInfo, memberModel, em_master);
            } else if (companyTargetInfo.getCompanyTargetInfoPK() != null && !StringUtils.isEmpty(companyTargetInfo.getCompanyTargetData())) {
                JPAUtils.edit(companyTargetInfo, em_master, false);
            } else if (companyTargetInfo.getCompanyTargetInfoPK() != null && StringUtils.isEmpty(companyTargetInfo.getCompanyTargetData())) {
                companyTargetInfo.setCompanyTargetDeleted((short) 1);
                JPAUtils.edit(companyTargetInfo, em_master, false);
            }
        }
    }
    
    @Override
    public void _CreateMemberInfo(CompanyTargetInfoPK ctiPk, CompanyTargetInfo cti, Member m, EntityManager em_master) throws Exception{
        cti.setCompanyTargetInfoPK(ctiPk);
        cti.setCreatedTime(Calendar.getInstance().getTime());
        cti.setCompanyTargetDeleted((short) 0);
        cti.setCreatorId(m.getMemberId());
        JPAUtils.create(cti, em_master, false);
    }

    private void _DeleteMemberTargetInfo(List<CompanyTargetInfo> companyDeleted, EntityManager em_master) throws Exception{
        for (CompanyTargetInfo companyTargetInfo : companyDeleted) {
            if (companyTargetInfo.getCompanyTargetInfoPK() != null
                    && companyTargetInfo.getCompanyTargetInfoPK().getTargetId() != null && companyTargetInfo.getCompanyTargetInfoPK().getTargetId() > 0
                    && companyTargetInfo.getCompanyTargetInfoPK().getCompanyTarget() != null
                    && companyTargetInfo.getCompanyTargetInfoPK().getCompanyTargetId() != null
                    && companyTargetInfo.getCompanyTargetInfoPK().getCompanyFlagType() != null)
                JPAUtils.edit(companyTargetInfo, em_master, false);
        }
    }
    
    @Override
    public boolean basicAuth(String loginId, String password) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "select * from view_system_basic_auth where basic_login_id = ? and basic_group_password = ? ;";
            Query q = em_master.createNativeQuery(sql).setParameter(1, loginId).setParameter(2, password);
            Long result = (Long) q.getSingleResult();
            return result == 1;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Object[] findBasicAuth(String loginId) {
        EntityManager em_master = null;
        try{
            em_master = masterEntityManager.getEntityManager();
            String sql = "select * from view_system_basic_auth where basic_login_id = ?;";
            Query q = em_master.createNativeQuery(sql).setParameter(1, loginId);
            return (Object[])q.getSingleResult();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }
    
    @Override
    public void persitCompanyIdAfterBasicAuth(Integer companyId) {
        tenantHolder.setCompanyId(companyId);
    }
    
    @Override
    public boolean isGlobalIpPass(String globalIP, String basicLoginId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            Query q = em_master.createQuery("SELECT o.companyGlobalIp FROM Company o WHERE o.companyBasicLoginId = :basicLoginId AND o.companyDeleted = :companyDeleted ")
                .setParameter("basicLoginId", basicLoginId)
                .setParameter("companyDeleted", 0);
            return chkGlobalIP(q, globalIP);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return false;
    }

    @Override
    public boolean isGlobalIpUserPass(String globalIP, String basicLoginId, String loginId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            Query q = em_master.createQuery("SELECT m.group.company.companyGlobalIp FROM Member m WHERE m.memberLoginId = :memberLoginId AND m.memberDeleted = :memberDeleted AND m.memberFirewall = :memberFirewall AND m.group.company.companyBasicLoginId = :companyBasicLoginId")
                .setParameter("memberLoginId", loginId)
                .setParameter("memberDeleted", 0)
                .setParameter("memberFirewall", 0)
                .setParameter("companyBasicLoginId", basicLoginId);
            return chkGlobalIP(q, globalIP);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return false;
    }
    
    @SuppressWarnings("IndexOfReplaceableByContains")
    private boolean chkGlobalIP(Query globalIPs, String globalIP) {
        try{
            String allowIpStr = globalIPs.getSingleResult() == null ? StringUtils.EMPTY : String.valueOf(globalIPs.getSingleResult()).trim();
            if(!StringUtils.isEmpty(allowIpStr)){
                globalIP = globalIP.trim();
                return (allowIpStr.indexOf(globalIP) > -1);
            }else{
                return true; // Neu cong ty khong set dia chi GlobalIP -> cho pass
            }
        }catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }
    
    @Override
    public void updateLastLoginTime(Member m) throws Exception {
        try {
            m.setLastLoginTime(new Date());
            this.edit(m);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void updateLastLogoutTime(int memberId) throws Exception {
        EntityManager em_master = null;
        EntityTransaction tx_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx_master = beginTransaction(em_master);
            
            Member m = em_master.find(Member.class, memberId);
            m.setLastLogoutTime(new Date());
            JPAUtils.edit(m, em_master, false);
            commitAndCloseTransaction(tx_master);
        } catch (Exception e) {
            rollbackAndCloseTransaction(tx_master);
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
    }

    @Override
    public String getUserMailFristByUserId(int userId) {
        if(userId <= 0) return null;
        EntityManager em_master = null;
        try{
            em_master = masterEntityManager.getEntityManager();
            String sql = String.valueOf(StringBuilderUtil.getUserMailFristByUserId(userId));
            Query q = em_master.createNativeQuery(sql);
            if(q != null) return String.valueOf(q.getSingleResult());
        } catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    @Override
    public List<Member> findMailMemberList(int groupId, int memberId) {
        if(groupId <= 0) return null;
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = String.valueOf(StringBuilderUtil.getUserMailList(groupId, memberId));
            Query q = em_master.createNativeQuery(sql, Member.class);
            if(q == null) return null;
            return q.getResultList();
        } catch (Exception ex) {
            LOGGER.error("[MemberServiceImpl.findMailMemberList()]", ex);
            return null;
        }
    }
    
    @Override
    public Member findByUsername(String username) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Member c WHERE c.memberLoginId = :memberLoginId AND c.memberDeleted = :memberDeleted";
            return em_master.createQuery(sql, Member.class).setParameter("memberLoginId", username).setParameter("memberDeleted", 0).getSingleResult();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }
    
    /**
     * Tìm tất cả các member trong công ty theo username kể cả các member đã xóa.
     * @param username
     * @param companyId
     * @return 
     */
    @Override
    public Member findByUsername(String username, Integer companyId) {
        EntityManager em_master = null;
        try{
            em_master = masterEntityManager.getEntityManager();
            String queryStr = "SELECT m FROM Member m WHERE m.memberLoginId = :username AND m.memberDeleted = :memberDeleted AND m.group.company.companyId = :companyId AND m.group.company.companyDeleted = :companyDeleted ";
            Query query = em_master.createQuery(queryStr,Member.class)
                                    .setParameter("username", String.format("GN%03d%s", companyId, username))
                                    .setParameter("memberDeleted", 0)
                                    .setParameter("companyId", companyId)
                                    .setParameter("companyDeleted", 0);
            List<Member> members = query.getResultList();
            if(members != null && !members.isEmpty()) return members.get(0);
        }catch(Exception ex){
            LOGGER.error(ex.getLocalizedMessage());
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }
    
    @Override
    public List<Member> findByCompanyId(int companyId) {
        EntityManager em_master = null;
        try{
            em_master = masterEntityManager.getEntityManager();
            String query = "SELECT m FROM Member m WHERE m.group.company.companyId= :companyId AND m.memberDeleted = :memberDeleted ";
            List<Member> members = em_master.createQuery(query,Member.class)
                    .setParameter("companyId", companyId)
                    .setParameter("memberDeleted", 0)
                    .getResultList();
            if(members != null && !members.isEmpty()) return members;
        } catch(Exception ex){
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Member> findListByIds(List<Integer> memberIdList) {
        if(memberIdList == null || memberIdList.isEmpty()) return new ArrayList<>();
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            Query q = em_master.createQuery("SELECT m FROM Member m WHERE m.memberId IN :list");
            q.setParameter("list", memberIdList);
            return q.getResultList();
        } catch(Exception ex){
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

}
