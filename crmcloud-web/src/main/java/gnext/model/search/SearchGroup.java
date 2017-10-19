package gnext.model.search;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
public class SearchGroup {
    
    public static enum OPERATOR{AND, OR};
    
    @Setter @Getter
    private String operator;
    
    @Setter @Getter
    private List<SearchField> filters;
    
}
