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
 */
public abstract class SearchBase {
    @Setter @Getter public String operator;
    @Setter @Getter public transient boolean found;
    
    protected abstract void doSearch(final MailData md);
    
    public static boolean callexpress(List<SearchBase> sbs) {
        return false;
    }
}
