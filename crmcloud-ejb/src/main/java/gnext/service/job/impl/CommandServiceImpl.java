/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.job.impl;

import gnext.bean.job.Command;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.impl.AbstractService;
import gnext.service.job.CommandService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Stateless
public class CommandServiceImpl extends AbstractService<Command> implements CommandService {
    private static final long serialVersionUID = -3066398787019104221L;
    private final Logger LOGGER = LoggerFactory.getLogger(CommandServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public CommandServiceImpl() { super(Command.class); }

    @Override
    public Command findById(Integer commandId, Integer companyId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Command c WHERE c.company.companyId = :companyId AND c.commandId = :commandId";
            Command query = em_master.createQuery(sql,Command.class).setParameter("commandId", commandId).setParameter("companyId", companyId).getSingleResult();
            return query;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    @Override
    public List<Command> findByCompanyId(Integer companyId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Command c WHERE c.company.companyId = :companyId";
            return em_master.createQuery(sql,Command.class).setParameter("companyId", companyId).getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
}
