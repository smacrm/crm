/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.converter;

import gnext.bean.project.DynamicColumn;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

/**
 * Converter for dynamic column on project CRUD form
 * 
 * @author hungpham
 * @since 2016/10
 */
@FacesConverter(value = "DynamicColumnConverter")
public class DynamicColumnConverter extends AbstractConverter<DynamicColumn> {
    @Override
    protected String convert2Text(FacesContext fc, UIComponent ui, DynamicColumn e) {
        if(e == null) return StringUtils.EMPTY;
        return e.getName();
    }
    
    @Override
    protected DynamicColumn convert2Bean(FacesContext fc, UIComponent ui, String text) {
        PickList  p = (PickList) ui;
        DualListModel<DynamicColumn> dl = (DualListModel) p.getValue();
        for(DynamicColumn dc : dl.getSource()){
            if(dc.getId().equals(text)) return dc;
        }
        for(DynamicColumn dc : dl.getTarget()){
            if(dc.getId().equals(text))  return dc;
        }
        return null;
    }
}
