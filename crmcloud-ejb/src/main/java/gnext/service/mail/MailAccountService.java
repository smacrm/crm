/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail;

import gnext.bean.MailAccount;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface MailAccountService extends EntityService<MailAccount> {
    public List<MailAccount> find(int first, int pageSize, String sortField, String sortOrder, String where);
    public int total(final String where);
    
    public MailAccount search(int cid, String accountName);
    public List<MailAccount> search(int cid);

    public List<MailAccount> getSendAccountList(int comId);
    
    /**
     * Kiểm tra xem mail address trong công ty đã được sử dụng chưa
     * @param mailAddress
     * @param companyId
     * @return 
     */
    public Boolean isMaillAddressExist(String mailAddress, Integer companyId);
}
