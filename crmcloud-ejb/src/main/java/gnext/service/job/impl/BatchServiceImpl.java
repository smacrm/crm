/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.job.impl;

import gnext.bean.job.Batch;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.impl.AbstractService;
import gnext.service.job.BatchService;
import gnext.utils.JPAUtils;
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
public class BatchServiceImpl extends AbstractService<Batch> implements BatchService {
    private static final long serialVersionUID = -4042398664000373379L;
    private final Logger LOGGER = LoggerFactory.getLogger(BatchServiceImpl.class);
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public BatchServiceImpl() { super(Batch.class); }

    @Override
    public Batch search(Integer companyId, Integer batchId, boolean batchDeleted) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT b FROM Batch b WHERE b.company.companyId=:companyId AND b.batchId=:batchId AND ";
            String sqlDel = "(b.batchDeleted=:batchDeleted=0 OR b.batchDeleted IS NULL)";
            if(batchDeleted) sqlDel = "b.batchDeleted=:batchDeleted=1";
            sql = sql + sqlDel;
            List<Batch> batchs = em_master.createQuery(sql ,Batch.class)
                        .setParameter("companyId", companyId)
                        .setParameter("batchId", batchId)
                        .getResultList();
            if(batchs != null && !batchs.isEmpty()) return batchs.get(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }
}
