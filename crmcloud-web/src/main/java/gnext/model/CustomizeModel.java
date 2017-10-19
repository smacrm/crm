package gnext.model;

import java.io.Serializable;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author gnextadmin
 */
@SessionScoped
public class CustomizeModel implements Serializable {

    @Getter @Setter
    private String key;

    @Getter @Setter
    private String label;

    @Getter @Setter
    private Integer type;

    @Getter @Setter
    private String value;

    @Getter @Setter
    private String[] values;
}
