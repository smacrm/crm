/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.converter;

import gnext.bean.role.Page;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@FacesConverter("pagenameConverter")
public class PageNameConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageNameConverter.class.getName());
    
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
        String selectedValue = request.getParameter(uic.getClientId()+"_input");
        if(StringUtils.isEmpty(selectedValue) && !StringUtils.isEmpty(value)){
            return value;
        }
        return selectedValue;
    }
 
    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if(object == null) return "";
        try{
            Page o = (Page) object;
            return o.getPageId().toString();
        }catch(ClassCastException e){
            LOGGER.error(e.getMessage(), e);
            return object.toString();
        }
    }   
}      
