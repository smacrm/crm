/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail;

import gnext.bean.mail.MailExplode;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface MailExplodeService extends EntityService<MailExplode> {
    public List<MailExplode> search(Integer companyId, short mailExplodeDeleted);
    public MailExplode search(Integer companyId, String mailExplodeTitle);
    public Integer getMaxOrder(Integer companyId);
}
