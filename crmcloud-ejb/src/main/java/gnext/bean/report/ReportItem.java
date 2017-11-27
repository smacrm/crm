package gnext.bean.report;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Nov 29, 2016
 */
@Entity
public class ReportItem implements Serializable{
    @EmbeddedId 
    @Setter @Getter private ReportItemKey key;
    @Setter @Getter private int current;
    @Setter @Getter private int last;

    public ReportItem() {
    }
    
    public ReportItem(int year, int month, String itemId, int current, int last) {
        this.key = new ReportItemKey(year, month, itemId);
        this.current = current;
        this.last = last;
    }
    
}
