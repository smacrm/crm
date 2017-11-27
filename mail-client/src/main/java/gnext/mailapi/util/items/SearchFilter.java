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
 * @see #crmcloud-web/gnext.model.search.SearchFilter
 */
public class SearchFilter extends SearchBase {
    
    @Setter @Getter private List<SearchGroup> filters;
    public SearchFilter(List<SearchGroup> filters){
        this.filters = filters;
    }
    
    @Override
    public void doSearch(final MailData md) {
        if(filters == null || filters.isEmpty()) return;
        for (SearchGroup sg : filters) sg.doSearch(md);
        _parse();
    }
    
    private void _parse() {
        for (SearchGroup sg : filters) {
            if (sg.found) {
                this.found = true; break;
            }
        }
    }
}
