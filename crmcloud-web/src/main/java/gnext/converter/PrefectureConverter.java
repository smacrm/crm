/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.converter;

import gnext.bean.Prefecture;
import gnext.controller.issue.IssueController;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.apache.commons.lang3.math.NumberUtils;

@FacesConverter(value = "gnext.converter.prefecture")
public class PrefectureConverter implements Converter{

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Integer prefectureId = NumberUtils.toInt(value, 0);
        if(prefectureId > 0){
            IssueController service = (IssueController) context.getExternalContext().getSessionMap().get("issueController");
            for(Prefecture p : service.getPrefectures()){
                if(p.getPrefectureId().equals(prefectureId)){
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value != null ? value.toString() : "";
    }
}
