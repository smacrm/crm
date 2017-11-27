/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.role.impl;

import gnext.bean.role.Role;
import gnext.bean.role.SystemUseAuthRel;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.impl.AbstractService;
import gnext.service.role.RoleService;
import gnext.service.role.SystemUseAuthRelService;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.utils.JPAUtils;

/**
 *
 * @author daind
 */
@Stateless
public class SystemUseAuthRelServiceImpl extends AbstractService<SystemUseAuthRel> implements SystemUseAuthRelService {
    private static final long serialVersionUID = 2263769554203853004L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemUseAuthRelServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public SystemUseAuthRelServiceImpl() { super(SystemUseAuthRel.class); }

    @EJB private RoleService roleService;
    
    /**
     * Trả về danh sách users và groups theo flag trong công ty loại bỏ các role có trạng thái là ẩn.
     * @param companyId
     * @param groupMemberId
     * @param groupMemberFlag
     * @param hidden
     * @return 
     */
    @Override
    public List<SystemUseAuthRel> findByRoleFlag(int companyId, int groupMemberId, short groupMemberFlag, Short hidden) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            
            String sql = null;
            if(hidden != null) {
                sql = "select * from crm_system_use_auth_rel where"
                    + " group_member_id={0} and group_member_flag={1} and company_id={2}"
                    + " and role_id in (select role_id from crm_role where role_flag=" + hidden + " and company_id={2})";
                sql = MessageFormat.format(sql, groupMemberId, groupMemberFlag, companyId);
            } else {
                sql = "select * from crm_system_use_auth_rel where"
                    + " group_member_id={0} and group_member_flag={1} and company_id={2}";
                sql = MessageFormat.format(sql, groupMemberId, groupMemberFlag, companyId);
            }
            return em_master.createNativeQuery(sql, SystemUseAuthRel.class).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }
    
    /**
     * Trả về user hoặc group theo flag của role trong công ty.
     * @param companyId
     * @param groupMemberId
     * @param groupMemberFlag
     * @param roleId
     * @return 
     */
    @Override
    public SystemUseAuthRel find(int companyId, int groupMemberId, short groupMemberFlag, int roleId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            
            String sql = "SELECT c FROM SystemUseAuthRel c WHERE c.crmSystemUseAuthRelPK.groupMemberId = :groupMemberId AND c.crmSystemUseAuthRelPK.groupMemberFlag = :groupMemberFlag AND c.crmSystemUseAuthRelPK.companyId = :companyId AND c.crmSystemUseAuthRelPK.roleId = :roleId";
            return em_master.createQuery(sql, SystemUseAuthRel.class)
                    .setParameter("groupMemberId", groupMemberId)
                    .setParameter("groupMemberFlag", groupMemberFlag)
                    .setParameter("companyId", companyId)
                    .setParameter("roleId", roleId)
                    .getSingleResult();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    /**
     * Trả về danh sách users hoặc group theo flag.
     * @param groupMemberId
     * @param groupMemberFlag
     * @return 
     */
    @Override
    public List<SystemUseAuthRel> find(int groupMemberId, short groupMemberFlag) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            
            String sql = "SELECT c FROM SystemUseAuthRel c WHERE c.crmSystemUseAuthRelPK.groupMemberId = :groupMemberId AND c.crmSystemUseAuthRelPK.groupMemberFlag = :groupMemberFlag";
            return em_master.createQuery(sql, SystemUseAuthRel.class)
                    .setParameter("groupMemberId", groupMemberId)
                    .setParameter("groupMemberFlag", groupMemberFlag)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    /**
     * Trả về danh sách users và groups theo role trong công ty.
     * @param companyId
     * @param roleId
     * @return 
     */
    @Override
    public List<SystemUseAuthRel> find(int companyId, int roleId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            
            Query q = em_master.createQuery("SELECT o FROM SystemUseAuthRel o where o.crmSystemUseAuthRelPK.roleId = :roleId and o.crmSystemUseAuthRelPK.companyId = :companyId", SystemUseAuthRel.class)
                    .setParameter("roleId", roleId)
                    .setParameter("companyId", companyId);
            return q.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    /**
     * Trả về danh sách user hoặc group theo flag và role.
     * @param roleId
     * @param groupMemberFlag
     * @return 
     */
    public List<SystemUseAuthRel> findByFlag(int roleId, short groupMemberFlag) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            Query q = em_master.createQuery("SELECT o FROM SystemUseAuthRel o"
                    + " where o.crmSystemUseAuthRelPK.roleId = :roleId"
                    + " AND o.crmSystemUseAuthRelPK.groupMemberFlag = :groupMemberFlag", SystemUseAuthRel.class)
                    .setParameter("roleId", roleId);
            q.setParameter("roleId", roleId)
                    .setParameter("groupMemberFlag", groupMemberFlag);
            
            List<SystemUseAuthRel> systemUseAuthRels = q.getResultList();
            
            if(systemUseAuthRels == null || systemUseAuthRels.isEmpty()) return new ArrayList<>();
            return systemUseAuthRels;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public void delete(SystemUseAuthRel systemUseAuthRel, EntityManager em_master) throws Exception {
        try {
            Role role = roleService.find(systemUseAuthRel.getSystemUseAuthRelPK().getRoleId());
            if(role != null && role.getRoleFlag() == Role.ROLE_HIDDEN) { // trường hợp là role ẩn thì xóa khỏi DB.
                roleService.removeRelation(role.getRoleId(), 1, em_master);
            } else { // chỉ xóa quan hệ user hoặc group với role.
                JPAUtils.remove(systemUseAuthRel, em_master, false);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
}

