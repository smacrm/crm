/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.validator;

import gnext.util.ByteUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;

/**
 *
 * @author daind
 */
@FacesValidator("gnext.validator.KatakanaValidator")
public class KatakanaValidator extends BaseValidator {

    @Override
    public boolean doValidate(Object value) {
        String val = getAsString(value);
        return ByteUtil.isKatakana(val, null);
    }

    @Override
    protected void onFaile(FacesContext facesContext, UIComponent component, Object value) {
        UIInput uIInput = ((UIInput) component);
        String title = uIInput.getAttributes().get("title").toString();
        uIInput.setValid(false);
        JsfUtil.addErrorMessage(
                JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME,
                        "validator.katakana.invalid", title));
    }
    
}
