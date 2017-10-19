/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 * @param <E> A Bean will apply this converter
 */
public abstract class AbstractConverter<E> implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConverter.class);
    
    @Override
    public Object getAsObject(FacesContext fc, UIComponent ui, String text) {
        return convert2Bean(fc, ui, text);
    }
    
    @Override
    public String getAsString(FacesContext fc, UIComponent ui, Object o) {
        try {
            return convert2Text(fc, ui, (E) o);
        } catch (Exception e) {
            //LOGGER.error(e.getLocalizedMessage(), e);
            return o.toString();
        }
    }
    
    protected abstract String convert2Text(FacesContext fc, UIComponent ui, E e);
    protected abstract E convert2Bean(FacesContext fc, UIComponent ui, String text);
}
