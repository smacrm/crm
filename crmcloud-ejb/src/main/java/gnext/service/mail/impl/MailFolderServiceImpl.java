/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail.impl;

import gnext.bean.mail.MailData;
import gnext.bean.mail.MailFolder;
import gnext.service.impl.AbstractService;
import gnext.service.mail.MailDataService;
import gnext.service.mail.MailFolderService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.multitenancy.TenantHolder;
import gnext.utils.JPAUtils;
import javax.persistence.EntityTransaction;

/**
 *
 * @author daind
 */
@Stateless
public class MailFolderServiceImpl extends AbstractService<MailFolder> implements MailFolderService {
    private static final long serialVersionUID = 8980617160448120944L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MailFolderServiceImpl.class);
    
    @EJB private MailDataService mailDataService;
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder); 
    }
    
    public MailFolderServiceImpl() { super(MailFolder.class); }

    @Override
    public List<MailFolder> search(Integer companyId, Short mailFolderIsDeleted) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sws = null;
            if(mailFolderIsDeleted == null || mailFolderIsDeleted == 0) sws = "(mf.mailFolderIsDeleted = 0 OR mf.mailFolderIsDeleted IS NULL)";
            else sws = "(mf.mailFolderIsDeleted = 1)";
            String sql = "SELECT mf FROM MailFolder mf WHERE mf.company.companyId=:companyId AND " + sws;
            Query q = em_slave.createQuery(sql) ;
            q.setParameter("companyId", companyId);
            List<MailFolder> results = q.getResultList();
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public int delete(MailFolder mf) throws Exception {
        int rowOfUpdated = 0;
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            tx_slave = beginTransaction(em_slave);
            
            mf.setMailFolderIsDeleted((short) 1);
            JPAUtils.edit(mf, em_slave, false);
            
            String fromFolder = String.valueOf(mf.getMailFolderId());
            String toFolder = MailFolder.DATA_MAIL_FOLDER_TRASH;
            List<MailData> mds = mailDataService.searchByCompanyId(fromFolder, mf.getCompany().getCompanyId(), null);
            if(mds == null || mds.isEmpty()) {
                commitAndCloseTransaction(tx_slave);
                return 0;
            }
            
            List<Integer> ids = new ArrayList<>();
            for(MailData md : mds) ids.add(md.getMailDataId());
            rowOfUpdated = mailDataService.moveToFolder(mf.getCompany(), toFolder, ids, em_slave);
            commitAndCloseTransaction(tx_slave);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return rowOfUpdated;
    }

    @Override
    public MailFolder search(int companyId, String folderName, Short mailFolderIsDeleted) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "SELECT mf FROM MailFolder mf WHERE mf.mailFolderName=:mailFolderName";
            if(mailFolderIsDeleted != null && mailFolderIsDeleted == 1) {
                sql = sql + " AND mf.mailFolderIsDeleted=1";
            } else {
                sql = sql + " AND (mf.mailFolderIsDeleted=0 OR mf.mailFolderIsDeleted IS NULL)";
            }
            sql = sql + " AND mf.company.companyId=:companyId";

            Query q = em_slave.createQuery(sql, MailFolder.class) ;
            q.setParameter("mailFolderName", folderName);
            q.setParameter("companyId", companyId);

            List<MailFolder> mfs = q.getResultList();
            if(mfs != null && !mfs.isEmpty()) return mfs.get(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }

    @Override
    public MailFolder search(Integer companyId, Integer mailFolderId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "SELECT mf FROM MailFolder mf WHERE mf.mailFolderId = :mailFolderId AND mf.company.companyId = :companyId";
            Query q = em_slave.createQuery(sql, MailFolder.class);
            q.setParameter("mailFolderId", mailFolderId);
            q.setParameter("companyId", companyId);
            MailFolder mf = (MailFolder) q.getSingleResult();
            return mf;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return null;
    }
}
