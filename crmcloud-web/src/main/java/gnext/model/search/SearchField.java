package gnext.model.search;

import gnext.model.authority.UserModel;
import static gnext.utils.StringUtil.PREFIX_LOGIN_ID;
import gnext.security.annotation.SecurePage;
import gnext.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hungpham
 */
public class SearchField {
    
    @Setter @Getter
    private String name;
    
    @Setter @Getter
    private String operator;
    
    @Setter @Getter
    private String condition;
    
    @Getter @Setter
    private String type; // field type: date, text, select
    
    @Setter
    private String value;
    
    private String prefixValue = "";
    private String subfixValue = "";
    
    @Setter @Getter
    private String sqlCustomize;
    
    @Getter @Setter private SecurePage.Module module;
    
    /**
     * Lay ve ten cua field dieu kien dua theo type cua field
     * Trong truong hop type = date -> ten cua field se thay doi bang DATE(field_name)
     * @param checkType
     * @return 
     */
    public String getName(boolean checkType){
        if(checkType && SecurePage.Module.ISSUE == getModule()){
            if("date".equalsIgnoreCase(getType())){
                return String.format("DATE(%s)", getName());
            }
            return String.format("multibyte2cv(%s)", getName());
        }
        return getName();
    }

    public String getValue() {
        if(!StringUtils.isEmpty(name) && name.contains(".memberLoginId") && !StringUtils.isEmpty(value)){ //using for searching member login id
            prefixValue = String.format(PREFIX_LOGIN_ID, UserModel.getLogined().getCompanyId());
        }
        return prefixValue + value + subfixValue;
    }
}
