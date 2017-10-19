/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.attachment;

import gnext.bean.attachment.Server;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface ServerService extends EntityService<Server> {
    /***
     * Trả về danh sách server theo company_id.
     * @param companyId
     * @return 
     */
    public List<Server> search(Integer companyId);
    public Server search(String serverName, Integer companyId);
    public List<Server> search(int comId, String type, int flag);
    public List<Server> getAvailable(int comId, String type, int flag);

    public List<Server> find(int first, int pageSize, String sortField, String sortOrder, String query);
    public int total(String query);
    
    public List<Server> searchServerGnext(String type, int flag);
}
