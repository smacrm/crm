package gnext.model;

import java.io.Serializable;
import javax.ejb.Stateful;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@ManagedBean(name="dateTimeModel", eager = true)
@SessionScoped
public class DateTimeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Setter
    @Getter
    private String datetime;

    @Setter
    @Getter
    private String datetimenotsecond;

    @Setter
    @Getter
    private String date;

    @Setter
    @Getter
    private String time;

    @Setter
    @Getter
    private String timenotsecond;
}
