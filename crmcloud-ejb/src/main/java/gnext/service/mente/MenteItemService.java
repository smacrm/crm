/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.mente;

import gnext.bean.mente.MenteItem;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author tungdt
 */
@Local
public interface MenteItemService extends EntityService<MenteItem>{
    public List<MenteItem> findByMenteOptionValue(String language, String itemData, String itemName, Integer companyId);
}
