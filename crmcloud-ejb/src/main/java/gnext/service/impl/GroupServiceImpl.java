/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.impl;

import gnext.bean.Company;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.role.Role;
import gnext.bean.role.SystemUseAuthRel;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.multitenancy.service.MultitenancyService;
import gnext.service.CompanyService;
import gnext.service.GroupService;
import gnext.service.role.RoleService;
import gnext.service.role.SystemUseAuthRelService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.utils.JPAUtils;
import javax.persistence.EntityTransaction;

/**
 *
 * @author daind
 */
@Stateless
public class GroupServiceImpl extends AbstractService<Group> implements GroupService {
    private static final long serialVersionUID = -8797346367452916995L;
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);
    
    @EJB @Getter @Setter private SystemUseAuthRelService systemUseAuthRelService;
    @EJB @Getter @Setter private RoleService roleService;
    @EJB @Getter @Setter private CompanyService companyService;
    @EJB private MultitenancyService multitenancyService;
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public GroupServiceImpl() { super(Group.class); }
    
    /**
     * Danh sách group theo company logined.
     * @param companyId
     * @return 
     */
    @Override
    public List<Group> findByCompanyId(int companyId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Group c WHERE c.company.companyId = :companyId";
            return em_master.createQuery(sql, Group.class).setParameter("companyId", companyId).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    /**
     * Danh sách groups theo company và trạng thái.
     * @param companyId
     * @param isDeleted
     * @return 
     */
    @Override
    public List<Group> findByCompanyId(int companyId, short isDeleted) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = null;
            if(isDeleted == 0) {
                sql = "SELECT c FROM Group c WHERE c.company.companyId = :companyId AND (c.groupDeleted is null OR c.groupDeleted = 0)";
            } else {
                sql = "SELECT c FROM Group c WHERE c.company.companyId = :companyId AND c.groupDeleted = 1";
            }
            return em_master.createQuery(sql, Group.class).setParameter("companyId", companyId).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    /**
     * Tìm group theo id.
     * @param groupId
     * @return 
     */
    @Override
    public Group findByGroupId(final int groupId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Group c WHERE c.groupId = :groupId";
            return em_master.createQuery(sql, Group.class).setParameter("groupId", groupId).getSingleResult();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }
    
    /**
     * Danh sác group theo cha.
     * @param groupParentId
     * @return 
     */
    @Override
    public List<Group> findByGroupParentId(int groupParentId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Group c WHERE c.parent.groupId = :groupParentId";
            return em_master.createQuery(sql, Group.class).setParameter("groupParentId", groupParentId).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    private static final String Q_GROUPS = "SELECT g FROM Group g";
    @Override
    public List<Group> find(int first, int pageSize, String sortField, String sortOrder, String where) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            StringBuilder sql = new StringBuilder(Q_GROUPS);
            sql.append(" WHERE 1=1 and "); // điều kiện giả.
            sql.append(where); // điều kiện người dùng tạo.
            
            Query query = em_master.createQuery(sql.toString());
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    private static final String Q_TOTAL_GROUPS = "SELECT count(g.groupId) FROM Group g";
    @Override
    public int total(String where) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            StringBuilder sql = new StringBuilder(Q_TOTAL_GROUPS);
            sql.append(" WHERE 1=1 and "); // điều kiện giả.
            sql.append(where); // điều kiện người dùng tạo.
            
            Query query = em_master.createQuery(sql.toString());
            Long total = (Long) query.getSingleResult();
            return total.intValue();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return 0;
    }
    
    private void deceivedParent(Group g) { if(g.getParent() != null && (g.getParent().getGroupId() == null || g.getParent().getGroupId() <= 0)) g.setParent(null); }
    
    /**
     * Cập nhật GROUP và ROLES cho GROUP đó.
     * @param groupOnMaster
     * @param m
     * @param roleIds
     * @return 
     * @throws java.lang.Exception 
     */
    @Override
    public Group update(Group groupOnMaster, Member m, Integer[] roleIds) throws Exception {
        EntityManager em_master = null;
        EntityTransaction tx = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx = beginTransaction(em_master);
            
            deceivedParent(groupOnMaster);
            groupOnMaster.setUpdator(m);
            groupOnMaster.setUpdatedTime(Calendar.getInstance().getTime());
            if(StringUtils.isNotBlank(groupOnMaster.getGroupTreeId())) groupOnMaster.setGroupTreeId(groupOnMaster.getGroupTreeId() + "-" + groupOnMaster.getGroupId());
            _UpdateRoleToGroup(groupOnMaster, m, roleIds, em_master);
            
            JPAUtils.edit(groupOnMaster, em_master, false);
            
            // đồng bộ group xuống slavedb.
            multitenancyService.updateGroupOnSlaveDB(groupOnMaster);
            
            commitAndCloseTransaction(tx);
            return groupOnMaster;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx);
            throw e;
        } finally {
            JPAUtils.release(em_master, true);
        }
    }
    
    /**
     * Thêm mới GROUP và cập nhật ROLES cho GROUP đó.
     * Clone đồng thời GROUP trên Master tới Slave.
     * @param g
     * @param m
     * @param roleIds
     * @return 
     * @throws java.lang.Exception 
     */
    @Override
    public Group insert(Group g, Member m, Integer[] roleIds) throws Exception {
        EntityManager em_master = null;
        EntityTransaction tx_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx_master = beginTransaction(em_master);
            
            deceivedParent(g);
            g.setCreator(m);
            g.setCreatedTime(Calendar.getInstance().getTime());
            g.setGroupDeleted((short) 0);
            Group groupOnMaster = JPAUtils.create(g, em_master, false);
            
            groupOnMaster.setManualId(groupOnMaster.getGroupId()); // sử dụng trong việc tách DB.
            if(StringUtils.isNotBlank(groupOnMaster.getGroupTreeId()))
                groupOnMaster.setGroupTreeId(groupOnMaster.getGroupTreeId() + "-" + groupOnMaster.getGroupId());
            _UpdateRoleToGroup(groupOnMaster, m, roleIds, em_master);
            
            // đồng bộ xuống slavedb.
            multitenancyService.createGroupOnSlaveDB(groupOnMaster);
            
            commitAndCloseTransaction(tx_master);
            return groupOnMaster;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_master);
            throw e;
        } finally {
            JPAUtils.release(em_master, true);
        }
    }
    
    /**
     * Cập nhật ROLES cho GROUP, những ROLE cũ sẽ xóa khỏi DB.
     * @param saving
     * @param m
     * @param roleIds
     * @throws Exception 
     */
    private void _UpdateRoleToGroup(Group saving, Member m, Integer[] roleIds, EntityManager em_master) throws Exception {
        /* xóa toàn bộ group hiện có */
        List<Role> roles = _FindRoleByCompany(saving.getCompany());
        Integer cid = saving.getCompany().getCompanyId();
        for (Role role : roles) {
            SystemUseAuthRel suar = systemUseAuthRelService.find(cid, saving.getGroupId(), SystemUseAuthRel.GROUP_FLAG, role.getRoleId());
            if(suar == null) continue;
            JPAUtils.remove(suar, em_master, false);
        }
        JPAUtils.flush(em_master);
        
        /* lưu thông tin group mới */
        if(roleIds == null) return;
        for (int i = 0; i < roleIds.length; i++) {
            SystemUseAuthRel suar = new SystemUseAuthRel(roleIds[i], cid, SystemUseAuthRel.GROUP_FLAG, saving.getGroupId()); 
            suar.setCreatorId(m.getCreator().getMemberId());
            suar.setCreatedTime(Calendar.getInstance().getTime());
            JPAUtils.create(suar, em_master, false);
        }
    }
    
    /**
     * Tìm tất cả các role của company kể cả các role có trạng thái là ẩn.
     * @param c
     * @return
     * @throws Exception 
     */
    private List<Role> _FindRoleByCompany(Company c) throws Exception {
        return roleService.search(c.getCompanyId(), StringUtils.EMPTY);
    }
    
    /**
     * Tìm theo tên group thuộc công ty.
     * @param groupName
     * @param companyId
     * @return 
     */
    @Override
    public List<Group> findByGroupName(String groupName, int companyId) {
        EntityManager em_master = null;
        try{
            em_master = masterEntityManager.getEntityManager();
            String query = "SELECT g FROM Group g WHERE g.groupName = :groupName AND g.company.companyId = :companyId";
            return em_master.createQuery(query,Group.class).setParameter("groupName", groupName).setParameter("companyId", companyId).getResultList();
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    /**
     * Danh sách groups theo tree.
     * @param companyId
     * @param disableRootGroup
     * @return 
     */
    @Override
    public List<Group> findGroupTree(Integer companyId, List<Group> disableRootGroup) {
        List<Group> root;
        if(disableRootGroup != null && companyId == null) {
            root = disableRootGroup;
        }else {
            root = findRootGroup(companyId);
        }
        List<Group> treeGroupData = new ArrayList<>();
        buidGroupTreeData(treeGroupData, root);
        buildGroupTreeLevel(treeGroupData, disableRootGroup);
        return treeGroupData;
    }
    
    /**
     * Danh sách group cha trong công ty.
     * @param currentCompanyId
     * @return 
     */
    @Override
    public List<Group> findRootGroup(Integer currentCompanyId) {
        EntityManager em_master = null;
        try{
            em_master = masterEntityManager.getEntityManager();
            String sql="SELECT gr FROM Group gr WHERE gr.parent = null and gr.company.companyId=:companyId";
            return em_master.createQuery(sql,Group.class).setParameter("companyId", currentCompanyId).getResultList();
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    /**
     * Tìm tất cả các group con của group cha
     * @param treeGroupsData
     * @param rootGroups
     * @return 
     */
    private List<Group> buidGroupTreeData(List<Group> treeGroupsData, List<Group> rootGroups){
        for(int i = 0 ; i < rootGroups.size() ; i ++){
            treeGroupsData.add(rootGroups.get(i));
            List<Group> groupsChild = findByGroupParentId(rootGroups.get(i).getGroupId());
            if(!groupsChild.isEmpty()){
                buidGroupTreeData(treeGroupsData,groupsChild);
            }
        }
        return treeGroupsData;
    }
    
    /**
     * Tạo treeLevel để giúp hiện thị duới dạng cây, truyền vào null nếu ở case tạo mới
     * @param groups
     * @param disableGroups
     * @return 
     */
    private List<Group> buildGroupTreeLevel(List<Group> groups, List<Group> disableGroups) {
       if(disableGroups == null) return buildGroupTreeLevel(groups);
       
       for(int i= 0 ; i < groups.size();i++){
           for(int j = 0 ; j < disableGroups.size();j++){
               if(groups.get(i).equals(disableGroups.get(j))){
                   groups.get(i).setDisabled(true);
               }
           }
       }
       return buildGroupTreeLevel(groups);
    }
    
    /**
     * Tạo treeLevel cho từng group
     * @param groups
     * @return 
     */
    private List<Group> buildGroupTreeLevel(List<Group> groups) {
        for (int i = 0; i < groups.size(); i++) {
            for (int j = i + 1; j < groups.size(); j++) {
                if (groups.get(j).getParent() != null) {
                    if (groups.get(i).getGroupId().equals(groups.get(j).getParent().getGroupId())) {
                          int treeLevel = groups.get(i).getTreeLevel() + 1;
                          groups.get(j).setTreeLevel(treeLevel);
                    }
                }
            }
        }
        return groups;
    }

    @Override
    public List<Group> findByGroupIsExists(int companyId) {
        EntityManager em_master = null;
         try{
            em_master = masterEntityManager.getEntityManager();
            return em_master.createQuery("SELECT c FROM Group c WHERE c.company.companyId = :companyId AND c.groupDeleted = 0 ", Group.class)
                    .setParameter("companyId", companyId)
                    .getResultList();
        } catch(Exception e){
            LOGGER.error(e.getLocalizedMessage());
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    
    @Override
    public List<Group> findByTarget(final int companyId, String target) {
        EntityManager em_master = null;
        try{
            em_master = masterEntityManager.getEntityManager();
            String query = "SELECT g FROM Group g WHERE g.target = :target AND g.company.companyId = :companyId";
            return em_master.createQuery(query,Group.class).setParameter("target", target).setParameter("companyId", companyId).getResultList();
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
}
