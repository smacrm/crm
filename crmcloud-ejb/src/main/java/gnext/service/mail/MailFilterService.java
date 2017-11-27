/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail;

import gnext.bean.mail.MailFilter;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface MailFilterService extends EntityService<MailFilter> {
    public List<MailFilter> search(Integer companyId, Short mailFilterDeleted);
    public MailFilter search(Integer companyId, String mailFilterTitle);
    public Integer getMaxOrder(Integer companyId);
}
