/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail;

import gnext.bean.Member;
import gnext.bean.mail.MailPerson;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface MailPersonService extends EntityService<MailPerson> {
    public List<MailPerson> search(Integer cid, Short deleted);
    public MailPerson searchMailPersonByMemberId(Integer companyId, Integer memberId);
    public void batchUpdate(List<MailPerson> memberSources, List<MailPerson> memberTarget) throws Exception;
    public MailPerson search(Integer cid, Integer personId);
}
