package gnext.controller.issue.bean;

import gnext.util.ClassUtil;
import static gnext.util.ObjectUtil.serializeObjectToString;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Mar 8, 2017
 */
public class PersitBean implements Serializable{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ClassUtil.class);
    
    @Getter @Setter private Integer issueId;
    @Getter @Setter private Integer companyId;
    @Getter @Setter private List<Integer> memberList = new ArrayList<>();
    @Getter @Setter private Date expiredDate;
    
    public PersitBean() {}

    public PersitBean(Integer issueId, Integer companyId) {
        this.issueId = issueId;
        this.companyId = companyId;
    }
    
    public String getSyncKey(){
        try {
            return serializeObjectToString(this);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return "";
        }
    }

    @Override
    public String toString() {
        return getSyncKey();
    }
}
