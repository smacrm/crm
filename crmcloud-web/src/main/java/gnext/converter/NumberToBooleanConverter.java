/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author tungdt
 */
@FacesConverter(value = "gnext.inputSwitch.converter.numberToBooleanConverter")
public class NumberToBooleanConverter extends AbstractConverter<Short> {

    @Override
    protected String convert2Text(FacesContext fc, UIComponent ui, Short e) {
       if(e == null || e == 0) return "true";
       return "false";
    }

    @Override
    protected Short convert2Bean(FacesContext fc, UIComponent ui, String text) {
        if("true".equals(text)) return (short) 0;
        return (short) 1;
    }

}
