package gnext.service.mente;

import gnext.bean.mente.MenteItem;
import gnext.bean.mente.MenteOptionDataValue;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author hungpham
 * @since Nov 7, 2016
 */
@Local
public interface MenteService extends EntityService<MenteItem>{
    public List<String> getDynamicRoot();
    public List<MenteItem> getAllDynamicLevel(String module, int companyId);
    public List<MenteItem> getAllDynamicLevel(String module, Integer parentId, int companyId);
    
    public List<String> getAllStaticLevel(short comFlag, int companyId);
    public List<MenteItem> getAllStaticLevel(String parent, int companyId);
    
    public List<MenteItem> getAllMenteItemStaticLevel(int itemLevel, int companyId);
    public List<MenteOptionDataValue> getAllDataValueOfMeteItem(int menteItemId, int companyId);
    
    public List<MenteItem> findByName(String name, int companyId);
    public List<MenteItem> getAllLevels(int companyId);
    public List<MenteItem> setReloadCachedAllLevelList(int companyId);
    public List<MenteItem> getRootLevels(String node, int companyId);

    public int removeItemNotInList(int companyId, String parentName, List<Integer> importedIdList);
    public int removeAll(int companyId, String sue_issue_product_id);
    
    // Caching
    public void reloadCache();
    public void reloadCache(Integer companyId);
}

