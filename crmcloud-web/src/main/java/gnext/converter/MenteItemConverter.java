package gnext.converter;

import gnext.bean.mente.MenteItem;
import gnext.controller.common.LocaleController;
import gnext.controller.issue.IssueAttachmentController;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * 
 * @author hungpham
 * @since 2017/04
 */
@FacesConverter(value = "gnext.picklist.converter.menteitem")
public class MenteItemConverter extends AbstractConverter<MenteItem>{

    @Override
    protected String convert2Text(FacesContext fc, UIComponent ui, MenteItem e) {
        return null;
    }

    @Override
    protected MenteItem convert2Bean(FacesContext fc, UIComponent ui, String text) {
        IssueAttachmentController service = (IssueAttachmentController) fc.getExternalContext().getSessionMap().get("issueAttachmentController");
        LocaleController localeService = (LocaleController) fc.getExternalContext().getSessionMap().get("localeController");
        for(MenteItem item: service.getCategoryList()){
            if(item.getItemViewData(localeService.getLocale()).equals(text)){
                return item;
            }
        }
        return null;
    }
}
