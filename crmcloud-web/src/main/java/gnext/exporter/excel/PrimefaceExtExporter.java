/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.exporter.excel;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;

/**
 *
 * @author daind
 */
public class PrimefaceExtExporter extends org.primefaces.component.export.ExcelExporter {
    /**
     * Hàm xử lí exportx excel đối với kiểu dạng boolean thì chuyển sang 0 và 1.
     * @param context
     * @param component
     * @return 
     */
    @Override
    protected String exportValue(FacesContext context, UIComponent component) {
        if(component instanceof SelectBooleanCheckbox) {
            SelectBooleanCheckbox sbc = (SelectBooleanCheckbox) component;
            Object value = sbc.getValue();
            if(value != null) return Boolean.valueOf(String.valueOf(value)) ? "1" : "0";
            return "0";
        }
        return super.exportValue(context, component);
    }
}