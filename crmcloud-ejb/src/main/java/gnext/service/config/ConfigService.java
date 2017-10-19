/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.config;

import gnext.bean.config.Config;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;


/**
 *
 * @author hungpham
 */
@Local
public interface ConfigService extends EntityService<Config>{
    
    public void reloadCache();
    public void reloadCache(Integer companyId);
    
    public List<Config> search(String query);
    public String get(String key);
    public Integer getInt(String key);
    public Boolean getBoolean(String key);
}
