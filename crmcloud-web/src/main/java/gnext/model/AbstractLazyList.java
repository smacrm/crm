/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model;

import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author daind
 */
public abstract class AbstractLazyList<T> extends LazyDataModel<T> {
    private static final long serialVersionUID = 3327348360742608133L;
    
    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        setPageSize(pageSize);
        setRowCount(getTotal());
        return getResults(first, pageSize, sortField, sortOrder, filters);
    }
    
    protected abstract int getTotal();
    protected abstract List<T> getResults(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters);
}
