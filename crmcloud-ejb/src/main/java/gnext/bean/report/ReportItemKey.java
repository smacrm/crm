package gnext.bean.report;

import java.io.Serializable;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Nov 29, 2016
 */
@Embeddable
public class ReportItemKey implements Serializable {
    @Setter @Getter private int year;
    @Setter @Getter private int month;
    @Setter @Getter private String id;

    public ReportItemKey() {
    }

    public ReportItemKey(int year, int month, String id) {
        this.year = year;
        this.month = month;
        this.id = id;
    }
}
