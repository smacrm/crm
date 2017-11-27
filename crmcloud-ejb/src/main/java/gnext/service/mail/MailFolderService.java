/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mail;

import gnext.bean.mail.MailFolder;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface MailFolderService extends EntityService<MailFolder> {
    /**
     * Tìm folder theo công ty và trạng thái của folder.
     * @param companyId
     * @param mailFolderIsDeleted
     * @return 
     */
    public List<MailFolder> search(Integer companyId, Short mailFolderIsDeleted);
    public int delete(final MailFolder mf) throws Exception;
    public MailFolder search(int companyId, String folderName, Short mailFolderIsDeleted);
    /**
     * Tìm folder theo companyId và folderId. 
     * @param companyId
     * @param mailFolderId
     * @return 
     */
    public MailFolder search(Integer companyId, Integer mailFolderId);
}
