package gnext.rest.iteply.bean;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Dec 26, 2016
 */
public class History implements Serializable{
    @Getter @Setter private String ID;
    @Getter @Setter private String InOutFlag;
    @Getter @Setter private String Date;
    @Getter @Setter private String StartTime;
    @Getter @Setter private String FinishTime;
    @Getter @Setter private String MyTelNumber;
    @Getter @Setter private String OtherTelNumber;
    @Getter @Setter private String OtherName;
    @Getter @Setter private String IssueCode;
    @Getter @Setter private String RecordFile;
    @Getter @Setter private String UniqueID;

    public History() {
    }
    
}
