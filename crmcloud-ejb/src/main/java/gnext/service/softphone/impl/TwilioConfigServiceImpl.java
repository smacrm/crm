package gnext.service.softphone.impl;

import com.google.gson.Gson;
import gnext.bean.softphone.TwilioConfig;
import gnext.multitenancy.TenantHolder;
import gnext.service.MemberService;
import gnext.service.impl.AbstractService;
import gnext.service.softphone.TwilioConfigService;
import gnext.utils.JPAUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author hungpham
 */
@Stateless
public class TwilioConfigServiceImpl extends AbstractService<TwilioConfig> implements TwilioConfigService{
    private static final long serialVersionUID = -1620536817605121247L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioConfigServiceImpl.class);
    
    @Inject TenantHolder tenantHolder;
    @Override protected EntityManager getEntityManager() {
        return JPAUtils.getSlaveEntityManager(tenantHolder);
    }
    
    @EJB MemberService memberService;

    public TwilioConfigServiceImpl() {
        super(TwilioConfig.class);
    }

    @Override
    public List<TwilioConfig> getByCompanyId(Integer companyId) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            return em_slave.createQuery("SELECT o FROM TwilioConfig o WHERE o.companyId = :companyId")
                .setParameter("companyId", companyId)
                .getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public TwilioConfig getByPhonenumber(String phoneNumber) {
        EntityManager em_slave = null;
        try{
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            return (TwilioConfig) em_slave.createQuery("SELECT o FROM TwilioConfig o WHERE o.phoneNumber = :phoneNumber")
                    .setParameter("phoneNumber", phoneNumber)
                    .getSingleResult();
        } catch(Exception e){
            TwilioConfig o = new TwilioConfig();
            return o;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }

    @Override
    public List<TwilioConfig> find(int first, int pageSize, String sortField, String sortOrder, String where) {
        EntityManager em_slave = null;
        try {
            em_slave = JPAUtils.getSlaveEntityManager(tenantHolder);
            
            String sql = "SELECT c FROM TwilioConfig c WHERE 1=1 AND " + where;
            Query query = em_slave.createQuery(sql);
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            List<TwilioConfig> results = query.getResultList();
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_slave, true);
        }
        return new ArrayList<>();
    }

    @Override
    public TwilioConfig getByUserId(Integer companyId, Integer memberId) {
        List<TwilioConfig> listConfig = this.getByCompanyId(companyId);
        for(TwilioConfig c: listConfig){
            List<Double> allowMemberList = new ArrayList<>();
            try{
                allowMemberList.addAll(new Gson().fromJson(c.getAllowMemberList(), List.class));
            }catch(Exception e){}
            if(this.isContains(allowMemberList, memberId)){
                return c;
            }
        }
        return null;
    }
    
    private boolean isContains(List<Double> memberListId, Integer memberId){
        for(Double item : memberListId){
            if(item.intValue() == memberId.intValue()){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<Double> getAllowMemberAdded(Integer companyId, Integer ignoreConfigId){
        List<TwilioConfig> listConfig = this.getByCompanyId(companyId);
        List<Double> allowMemberAddedList = new ArrayList<>(); //must Double type because of GSON return Double type list, not Integer type list
        for(TwilioConfig c: listConfig){
            if(ignoreConfigId != null && Objects.equals(c.getTwilioId(), ignoreConfigId)) continue;
            try{
                allowMemberAddedList.addAll(new Gson().fromJson(c.getAllowMemberList(), List.class));
            }catch(Exception e){}
        }
        
        return allowMemberAddedList;
    }
}