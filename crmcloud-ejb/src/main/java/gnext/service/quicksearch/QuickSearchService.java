/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.quicksearch;

import gnext.interceptors.QuickSearch;
import gnext.service.EntityService;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface QuickSearchService extends EntityService<QuickSearch>{
    public QuickSearch search(String quickSearchModule, Integer quickSearchTargetId);
    public List<QuickSearch> find(int first, int pageSize, String where, String fulltextseach);
    public int total(String where, String fulltextseach);
}
