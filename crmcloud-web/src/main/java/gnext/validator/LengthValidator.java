/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.validator;

import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

@FacesValidator("gnext.validator.LengthValidator")
public class LengthValidator extends BaseValidator {

    @Override
    public boolean doValidate(Object value) {
        return false;
    }

    @Override
    public void onFaile(FacesContext facesContext,
            UIComponent component, Object value)
            throws ValidatorException {
        UIInput uIInput = ((UIInput) component);

        String title = (String) uIInput.getAttributes().get("title");
        Integer max = Integer.parseInt(uIInput.getAttributes().get("max").toString());

        String val = value.toString();
        if (val.length() > max) {
            uIInput.setValid(false);
            JsfUtil.addErrorMessage(
                    JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME,
                            "validator.length.invalid", title, max));
        }
    }
}
