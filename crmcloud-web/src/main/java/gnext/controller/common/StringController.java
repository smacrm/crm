/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.common;

import com.mysql.jdbc.StringUtils;
import gnext.util.DateUtil;
import java.io.Serializable;
import java.util.Calendar;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 *
 * @author daind
 */
@ManagedBean(name = "stringController", eager = true)
@RequestScoped
public class StringController implements Serializable {
    private static final long serialVersionUID = 4438017063325248586L;
    
    public String substring(String text, int length) {
        if(text == null || text.isEmpty()) return text;
        if(text.length() <= length) return text;
        return text.substring(0, length) + "..." ;
    }
    
    public String showIfNullOrEmpty(String value) {
        if(StringUtils.isNullOrEmpty(value)) return "";
        if("null".equalsIgnoreCase(value)) return "";
        return value;
    }
    
    public String getNow() {
        return DateUtil.getDateToString(Calendar.getInstance().getTime(), DateUtil.PATTERN_JP_SLASH);
    }
}
