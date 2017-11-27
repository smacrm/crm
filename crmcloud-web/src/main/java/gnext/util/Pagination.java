/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import gnext.model.BaseModel;
import gnext.utils.InterfaceUtil.ARRAY_STRING_ICON;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Helper class for lists that need pagination,
 * <font style='color:red'>It is a class immutable.</font>
 *
 * @param <T> The type of the list elements
 * @author <a href="mailto:daind1@vnext.vn">daind</a>
 */
public class Pagination<T extends BaseModel> implements Serializable, Iterable<T> {

    public static final int DEFAULT_ROWS_ON_PAGE = 20;
    public static final String ROWSPERPAGETEMPLATE = "20,30,50,100,150,200";
    
    private List<T> items = new ArrayList<>();

    private int first = 0;
    @Getter @Setter private int limit = DEFAULT_ROWS_ON_PAGE;
    private int total;

    @Getter @Setter private int currentPage = 0; // base-0;
    private int numberOfPages;
    
    public void setup(int total, List<T> items) {
        this.total = total;
        numberOfPages = (total - 1) / limit + 1;

        this.items.clear();
        this.items.addAll(items);
    }
    
    public void reset() {
        first = 0;
        limit = DEFAULT_ROWS_ON_PAGE;
        this.items.clear();
        currentPage = 0;
    }
    
    public List<String> getRowsPerPageTemplate() {
        return Arrays.asList(ROWSPERPAGETEMPLATE.split(ARRAY_STRING_ICON.COMMAR));
    }

    public boolean isFirst() {
        return currentPage == 0;
    }

    public boolean isLast() {
        return currentPage == getLastPage();
    }

    public int getPreviousPage() {
        currentPage = currentPage - 1;
        return currentPage;
    }

    public int getNextPage() {
        currentPage = currentPage + 1;
        return currentPage;
    }

    public int getLastPage() {
        return numberOfPages - 1;
    }

    public int getFrom() {
        if(items != null && items.isEmpty()) return first;
        return first + 1;
    }

    public int getTo() {
        int to = first + limit;
        if (to > total) {
            return first + items.size();
        } else {
            return first + limit;
        }
    }

    public int getFirst() {
        first = currentPage * limit;// 0-0;1-20;2-40
        return first;
    }

    public int getTotal() {
        return total;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

}
