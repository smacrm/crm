/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role.impl;

import gnext.bean.Company;
import gnext.bean.Member;
import gnext.bean.role.Method;
import gnext.bean.role.Page;
import gnext.bean.role.Role;
import gnext.bean.role.RolePageMethodRel;
import gnext.bean.role.SystemUseAuthRel;
import gnext.bean.role.SystemUseAuthRelPK;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.impl.AbstractService;
import gnext.service.role.RoleService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityTransaction;

/**
 *
 * @author hungpham
 */
@Stateless
public class RoleServiceImpl extends AbstractService<Role> implements RoleService {
    private static final long serialVersionUID = -5995076948107558908L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public RoleServiceImpl() { super(Role.class); }
    
    @Override
    public void delete(Member memberLogined, Integer roleId) {
        if(roleId == null) return;
        
        EntityManager em_master = null;
        EntityTransaction tx_master = null;
        
        try {
            em_master = masterEntityManager.getEntityManager();
            if(em_master == null) return;
            
            tx_master = beginTransaction(em_master);
            
            Role role = em_master.find(Role.class, roleId);
            if(role == null) return;
            
            if(role.getCreatorId() != null && role.getCreatorId() == 1
                        && role.getCompanyId() != Company.MASTER_COMPANY_ID
                        && memberLogined.getMemberId() == Member.SUPER_ADMIN_MEMBER_ID) {
                String sql = "select * from crm_role where company_id=? and (role_deleted is null or role_deleted=0)";
                Query query = em_master.createNativeQuery(sql, Role.class);
                query.setParameter(1, role.getCompanyId());

                List<Role> roles = query.getResultList();
                for(Role r : roles) {
                    removeRelation(r.getRoleId(), 2, em_master);
                }
            } else {
                removeRelation(roleId, 2, em_master);
            }
            
            commitAndCloseTransaction(tx_master);
            
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_master);
            
        } finally {
            JPAUtils.release(em_master, true);
        }
    }
    
    /**
     * Thực hiện xóa quan hệ ROLE với các MEMBER và GROUP.
     * <br>Hàm xử lí sẽ luôn luôn xóa toàn bộ users hoặc group có liên quan tới role.
     * <br>Trong trường hợp xử lí đặc biệt cần lưu lại users và group để tạo quan hệ với roles sau khi thực hiện hàm này.
     * @param roleId
     * @param type 
     *          <ul>
     *              <li>0: Xóa quan hệ và quyền của ROLE nhưng giữ lại ROLE trong DB.</li>
     *              <li>1: Xóa quan hệ và quyền của ROLE và xóa luôn ROLE khỏi DB.</li>
     *              <li>2: Xóa quan hệ với ROLE nhưng giữ lại quyền của ROLE và cập nhật trạng thái của ROLE là đã xóa.</li>
     *          </ul>
     * @param em_master
     */
    @Override
    public void removeRelation(Integer roleId, int type, EntityManager em_master)
            throws Exception {
        if(roleId == null) return;
        
        EntityTransaction tx_master = null;
        try {
            // trường hợp chưa có connection có nghĩa đang ở trong 1 transaction
            // cần tạo mới connection và start 1 transaction mới.
            if(null == em_master) {
                em_master = masterEntityManager.getEntityManager();
                tx_master = beginTransaction(em_master);
            }
            Query q = null;
            
            // xóa toàn bộ quền trên role. áp dụng khi chỉnh sửa member liên quan tới công ty group.
            if(type != 2) {
                q = em_master.createQuery("DELETE FROM RolePageMethodRel o WHERE o.rolePageMethodRelPK.roleId = :roleId").setParameter("roleId", roleId);
                q.executeUpdate();
            }
            
            // xóa toàn bộ những member nào liên quan tới role ở tất cả các công ty nếu member đó có sử dụng role.
            q = em_master.createQuery("DELETE FROM SystemUseAuthRel o WHERE o.crmSystemUseAuthRelPK.roleId = :roleId").setParameter("roleId", roleId);
            q.executeUpdate();
            
            switch (type) {
                case 1: // xóa role khỏi masterdb, áp dụng khi chỉnh sửa member liên quan tới công ty group.
                    q = em_master.createQuery("DELETE FROM Role r WHERE r.roleId = :roleId").setParameter("roleId", roleId);
                    q.executeUpdate();
                    break;
                case 2: // cập nhật trạng thái của role trên masterdb, áp dụng khi xóa role từ màn hình quản lí.
                    q = em_master.createQuery("UPDATE Role r SET r.roleDeleted = 1 WHERE r.roleId = :roleId").setParameter("roleId", roleId);
                    q.executeUpdate();
                    break;
                case 0:
                    // không làm j cả.
                    break;
                default:
                    break;
            }
            
            if(tx_master != null) commitAndCloseTransaction(tx_master);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if(tx_master != null) rollbackAndCloseTransaction(tx_master);
            throw e;
        } finally {
            if(tx_master != null) JPAUtils.release(em_master, true);
        }
    }
    
    /**
     * Xử lí cập nhật hoặc thêm mới role trên masterdb.
     * @param role
     * @param selectedGroup
     * @param selectedMember
     * @param pageActionIds
     * @throws Exception 
     */
    @Override
    public void createOrUpdate(Member memLogined, Role role, List<String> selectedGroup, List<String> selectedMember, Map<Integer, List<Integer>> pageActionIds)
            throws Exception {
        if(role == null) return;
        boolean isCreateNew = (role.getRoleId() == null || role.getRoleId() < 0);
        
        EntityManager em_master = null;
        EntityManager em_slave = null;
        EntityTransaction tx = null;
        try {
            em_master = masterEntityManager.getEntityManager();
//            em_slave = JPAUtils.getSlaveEntityManager(role.getCompanyId());
            tx = beginTransaction(em_master);
            
            // xóa các role cũ đi.
            if(isCreateNew) {
                JPAUtils.create(role, em_master, false);
            } else {
                JPAUtils.edit(role, em_master, false);
                // xóa toàn bộ quền trên role. áp dụng khi chỉnh sửa member liên quan tới công ty group(RolePageMethodRel).
                // xóa toàn bộ những member nào liên quan tới role ở tất cả các công ty nếu member đó có sử dụng role(SystemUseAuthRel).
                removeRelation(role.getRoleId(), 0, em_master);
            }
            
            // bổ sung group(thuoc cong ty dang chon) đã chọn vào role.
//            List<Integer> groupIds = new ArrayList<>();
            if(selectedGroup != null && !selectedGroup.isEmpty()) {
                for (String groupId : selectedGroup) {
                    if(StringUtils.isEmpty(groupId)) continue;
                    
                    int igroupid = Integer.parseInt(groupId);
                    
//                    groupIds.add(Integer.parseInt(groupId));

                    SystemUseAuthRelPK relPK = new SystemUseAuthRelPK();
                    relPK.setRoleId(role.getRoleId());
                    relPK.setCompanyId(role.getCompanyId());
                    relPK.setGroupMemberFlag((short) 0);
                    relPK.setGroupMemberId(igroupid);
                    JPAUtils.create(new SystemUseAuthRel(relPK), em_master, false);
                }
            }
            
            // bổ sung member đã chọn vào role.
//            List<Integer> memberIds = new ArrayList<>();
            if(selectedMember != null && !selectedMember.isEmpty()) {
                for (String memberId : selectedMember) {
                    if(StringUtils.isEmpty(memberId)) continue;
                    
                    int imemberid = Integer.parseInt(memberId);
                    
//                    Member m = em_slave.find(Member.class, imemberid);
//                    if(m != null && m.getMemberGlobalFlag() != null && m.getMemberGlobalFlag() == 1) memberIds.add(imemberid);
                    
                    SystemUseAuthRelPK relPK = new SystemUseAuthRelPK();
                    relPK.setRoleId(role.getRoleId());
                    relPK.setCompanyId(role.getCompanyId());
                    relPK.setGroupMemberFlag((short) 1);
                    relPK.setGroupMemberId(imemberid);
                    JPAUtils.create(new SystemUseAuthRel(relPK), em_master, false);
                }
            }
            
            // tạo mối quan hệ role-page-method[action].            
            for (Integer pageId : pageActionIds.keySet()) {
                List<Integer> actions = pageActionIds.get(pageId);
                for(Integer actionId : actions) {
                    JPAUtils.create(new RolePageMethodRel(role.getRoleId(), pageId, actionId), em_master, false);
                }
            }
            
            // C A S E: super-admin vào sửa role(được cấp cho admin) ở công ty khác.
            if(!isCreateNew) {
                if(role.getCreatorId() != null && role.getCreatorId() == 1
                        && role.getCompanyId() != Company.MASTER_COMPANY_ID
                        && memLogined.getMemberId() == Member.SUPER_ADMIN_MEMBER_ID) {
                    
                    String sql = "select * from crm_role where company_id=? and (role_deleted is null or role_deleted=0)";
                    Query query = em_master.createNativeQuery(sql, Role.class);
                    query.setParameter(1, role.getCompanyId());
                    
                    List<Role> roles = query.getResultList();
                    for(Role r : roles) {
                        if(r.getRoleId().intValue() == role.getRoleId().intValue()) continue;
                        
                        
                        List<RolePageMethodRel> relations = r.getRolePageMethodList();
                        if(relations == null) continue;
                        
                        for(RolePageMethodRel relation : relations) {
                            Page relationPage = relation.getPage();
                            List<Method> relationMethods = relationPage.getMethodList();
                            if(relationMethods == null) continue;
                            
                            for(Method method : relationMethods) {
                                Integer relationPageId = relationPage.getPageId();
                                Integer relationActionId = method.getMethodId();
                                
                                boolean isDelete = true;
                                
                                for (Integer pageId : pageActionIds.keySet()) {
                                    List<Integer> actions = pageActionIds.get(pageId);
                                    
                                    for(Integer actionId : actions) {
                                        if(relationPageId.intValue() == pageId.intValue() && relationActionId.intValue() == actionId.intValue()) {
                                            isDelete = false;
                                        }
                                    }
                                    
                                    
                                }
                                
                                if(isDelete) em_master.remove(relation);
                            }
                            
                        }
                    }
                }
            }
            
            commitAndCloseTransaction(tx);
            
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            // lỗi, lấy lại dữ liệu cũ.
            rollbackAndCloseTransaction(tx);
            throw e;
        } finally {
            JPAUtils.release(em_master, true);
            JPAUtils.release(em_slave, true);
        }
    }
    
    /**
     * Tìm kiếm role cùng query người dùng manual.
     * @param companyId
     * @param query
     * @return 
     */
    @Override
    public List<Role> search(int companyId, String query) {
        EntityManager em_master = masterEntityManager.getEntityManager();
        try {
            StringBuilder nq = new StringBuilder();
            if( companyId == 0 ){
                nq.append("SELECT c FROM Role c WHERE c.roleFlag = ").append(Role.ROLE_UN_HIDDEN);
            } else {
                nq.append("SELECT c FROM Role c WHERE c.roleFlag = ").append(Role.ROLE_UN_HIDDEN).append(" AND c.companyId = :companyId");
            }
            if( !StringUtils.isEmpty(query) ) nq.append(" AND ").append(query);
            Query q = em_master.createQuery(nq.toString());
            if( companyId != 0 ) q.setParameter("companyId", companyId);
            
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
}
