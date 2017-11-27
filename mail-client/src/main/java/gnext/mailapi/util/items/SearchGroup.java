/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util.items;

import gnext.dbutils.model.MailData;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 * @see #crmcloud-web/gnext.model.search.SearchGroup
 */
public class SearchGroup extends SearchBase {
    @Setter @Getter private List<SearchField> filters;
    
    @Override
    public void doSearch(final MailData md) {
        if(filters == null || filters.isEmpty()) return;
        for (SearchField sf : filters) sf.doSearch(md);
        _parse();
    }
    
    private void _parse() {
        for (SearchField sf : filters) {
            if (sf.found) {
                this.found = true; break;
            }
        }
    }
}
