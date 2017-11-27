/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.converter;

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;
import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

@FacesConverter(value = "gnext.picklist.converter.selecitem")
public class SelectItemConverter extends AbstractConverter<SelectItem>{

    @Override
    protected String convert2Text(FacesContext fc, UIComponent ui, SelectItem e) {
        return String.valueOf(e.getValue());
    }

    @Override
    protected SelectItem convert2Bean(FacesContext fc, UIComponent ui, String text) {
        if(!(ui instanceof PickList)) return null;
        Object dualList = ((PickList) ui).getValue();
        DualListModel dl = (DualListModel) dualList;

        List<SelectItem> items = dl.getSource();
        items.addAll(dl.getTarget());
        for (SelectItem item: items) {
            String id = String.valueOf(item.getValue());
            if (text.equals(id)) { return item; }
        }
        return null;
    }
}
