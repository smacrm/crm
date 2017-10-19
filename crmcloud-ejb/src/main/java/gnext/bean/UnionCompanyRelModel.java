/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public class UnionCompanyRelModel implements Serializable {
    private static final long serialVersionUID = 2369553148416228906L;
    
    @Getter @Setter private String companyUnionKey;
    @Getter @Setter private List<Integer> ids;
    @Getter @Setter private List<Company> companys;

    public UnionCompanyRelModel() {
        
    }
    
    public UnionCompanyRelModel(String companyUnionKey, List<Integer> ids, List<Company> companys) {
        this.companys = companys;
        this.companyUnionKey = companyUnionKey;
        this.ids = ids;
    }

    public String getDisplayGroup() {
        StringBuilder strBuilder = new StringBuilder();
        for (Integer id : ids) {
            if (!StringUtils.isEmpty(getCompanyName(id))) {
                strBuilder.append(getCompanyName(id)).append(",");
            }
        }

        if (strBuilder.indexOf(",") > 0) {
            strBuilder.deleteCharAt(strBuilder.lastIndexOf(","));
        }

        return strBuilder.toString();
    }

    @Override
    public String toString() {
        return companyUnionKey;
    }

    private String getCompanyName(Integer id) {
        for (Company c : companys) {
            if (c.getCompanyId().equals(id)) {
                return c.getCompanyName();

            }
        }
        return StringUtils.EMPTY;
    }
}
