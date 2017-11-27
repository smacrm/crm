package gnext.bean.automail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Aug 29, 2017
 */
public class SimpleAutoMail implements Serializable{

    private static final long serialVersionUID = -8173344376435389493L;
    
    @Getter @Setter private Integer autoId;
    @Getter @Setter private Integer issueId;
    @Getter @Setter private String issueViewCode;
    @Getter @Setter private Integer companyId;
    @Getter @Setter private Integer optionId;
    @Getter @Setter private List<String> toList = new ArrayList<>();
    @Getter @Setter private List<String> ccList = new ArrayList<>();
    
    @Getter @Setter private List<Integer> toIntList = new ArrayList<>();
    @Getter @Setter private List<Integer> ccIntList = new ArrayList<>();
}
