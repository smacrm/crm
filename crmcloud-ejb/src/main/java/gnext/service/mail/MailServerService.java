/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail;

import javax.ejb.Local;
import gnext.bean.mail.MailServer;
import gnext.service.EntityService;
import java.util.List;


/**
 *
 * @author hungpham
 */
@Local
public interface MailServerService extends EntityService<MailServer> {
    public List<MailServer> find(int first, int pageSize, String sortField, String sortOrder, String where);
    public int total(final String where);
    
    public List<MailServer> search(int companyId, Boolean serverDeleted);
    public MailServer search(int companyId, String serverName);
}
