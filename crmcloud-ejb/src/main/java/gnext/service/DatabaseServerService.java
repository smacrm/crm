/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service;

import gnext.bean.DatabaseServer;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface DatabaseServerService extends java.io.Serializable {
    public DatabaseServer create(DatabaseServer bean);
    public DatabaseServer edit(DatabaseServer bean);
    public DatabaseServer findOneDatabaseServer(Integer companyId);
    public List<DatabaseServer> findAll();
    public DatabaseServer findById(int databaseServerId);
}
