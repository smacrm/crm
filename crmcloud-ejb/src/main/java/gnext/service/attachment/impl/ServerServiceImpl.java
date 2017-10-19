/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.attachment.impl;

import gnext.bean.attachment.Server;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.multitenancy.TenantHolder;
import gnext.service.attachment.ServerService;
import gnext.service.impl.AbstractService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Stateless
public class ServerServiceImpl extends AbstractService<Server> implements ServerService {
    private static final long serialVersionUID = 5429012617613228029L;
    private final Logger LOGGER = LoggerFactory.getLogger(ServerServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    public ServerServiceImpl() { super(Server.class); }
    
    @Override
    public List<Server> search(Integer companyId) {
        if(companyId == null) return new ArrayList<>();
        
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String sql = "SELECT s FROM Server s WHERE s.company.companyId=:companyId AND s.serverDeleted = 0";
            return em_slave.createQuery(sql) .setParameter("companyId", companyId) .getResultList();  
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    private static final String Q_SERVER = "select sv from Server sv";
    @Override
    public List<Server> find(int first, int pageSize, String sortField, String sortOrder, String where) {
        EntityManager em_slave = null;
        try{
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String sql = Q_SERVER + " where 1=1 and "+where;
            Query query = em_slave.createQuery(sql).setFirstResult(first).setMaxResults(pageSize);
            List<Server> results = query.getResultList();
            if(results != null && !results.isEmpty()) return results;
        }catch(Exception e){
            LOGGER.error(e.getMessage(),e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    private static final String Q_TOTAL_SERVER = "SELECT count(sv.serverId) FROM Server sv";
    @Override
    public int total(String where) {
        EntityManager em_slave = null;
        try{
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String sql = Q_TOTAL_SERVER + " where 1=1 and "+where;
            Query query = em_slave.createQuery(sql);
            Long total = (Long) query.getSingleResult();
            return total.intValue();
        }catch(Exception e){
            LOGGER.error(e.getMessage(),e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return 0;
    }

    /**
     * Hàm được xử dụng để kiểm tra trùng tên server khi tạo mới hay chỉnh sửa.
     * @param serverName
     * @param companyId
     * @return 
     */
    @Override
    public Server search(String serverName, Integer companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(companyId);
            String sql = "SELECT s FROM Server s WHERE s.company.companyId=:companyId and s.serverName = :serverName AND s.serverDeleted = 0";
            List<Server> results = em_slave.createQuery(sql, Server.class).setParameter("companyId", companyId).setParameter("serverName", serverName).getResultList();
            if(results != null && !results.isEmpty()) return results.get(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public List<Server> search(int comId, String type, int flag) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(comId);
            String sql = "SELECT s FROM Server s WHERE s.company.companyId=:companyId AND s.serverType=:serverType AND s.serverFlag=:serverFlag AND s.serverDeleted = 0";
            List<Server> servers = em_slave.createQuery(sql, Server.class).setParameter("companyId", comId).setParameter("serverType", type).setParameter("serverFlag", flag).getResultList();
            
            if(servers == null || servers.isEmpty())
                return searchServerGnext(type, flag);
            else
                return servers;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<Server> getAvailable(int comId, String type, int flag) {
        List<Server> list = search(comId, type, flag);
        
        // lấy theo flag COMMON nếu không có server nào theo flag chỉ định.
        if(list.isEmpty() && flag != ServerFlag.COMMON.getId()){
            list = search(comId, type, ServerFlag.COMMON.getId());
        }
        
        // trường hợp này dùng cho hệ thống cũ chung một DB.
        // trong hệ thống tách DB mới thì mỗi công ty đều có ít nhất 1 server(COMMON) được clone từ MASTER.
        // if have no servers -> get [flag] server of GNEXT
        if(list.isEmpty()){
            list = searchServerGnext(type, flag);
        }
        
        //if not is common and have no servers -> get [common] server of GNEXT
        if(list.isEmpty() && flag != ServerFlag.COMMON.getId()){
            list = searchServerGnext(type, ServerFlag.COMMON.getId());
        }
        /////////////////
        
        // duyệt tất cả các servers và kiểm tra kết nối.
        if(list != null && !list.isEmpty()) {
            for (Iterator<Server> iter = list.iterator(); iter.hasNext(); ) {
                Server server = iter.next();
                
                try {
                    Parameter param = Parameter.getInstance(TransferType.getTransferType(server.getServerType())).manualconfig(true).storeDb(false);
                    param.type(TransferType.getTransferType(server.getServerType())).host(server.getServerHost())
                            .port(server.getServerPort())
                            .username(server.getServerUsername())
                            .password(server.getDecryptServerPassword());

                    if(server.getServerSsl() != null && server.getServerSsl() == 1){
                        param.security(true);
                    }else {
                        param.security(false);
                    }

                    FileTransferFactory.getTransfer(param).test();
                } catch (Exception ex) {
                    iter.remove();
                }
            }
        }
        
        return list;
    }

    /**
     * Hàm trả về danh sách SERVERS của GNEXT.
     * @param type
     * @param flag
     * @return 
     */
    @Override
    public List<Server> searchServerGnext(String type, int flag) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            String sql = "SELECT s FROM Server s WHERE s.company.companyId=1 AND s.serverType=:serverType AND s.serverFlag=:serverFlag AND s.serverDeleted = 0";
            return em_slave.createQuery(sql, Server.class).setParameter("serverType", type).setParameter("serverFlag", flag).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }
}
