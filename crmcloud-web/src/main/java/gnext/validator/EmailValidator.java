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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@FacesValidator("gnext.validator.EmailValidator")
public class EmailValidator extends BaseValidator {

    @Override
    public boolean doValidate(Object value) {
        try {
            new InternetAddress(getAsString(value)).validate();;
        } catch (AddressException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void onFaile(FacesContext facesContext,
            UIComponent component, Object value)
            throws ValidatorException {
        UIInput uIInput = ((UIInput) component);
        String title = uIInput.getAttributes().get("title").toString();
        uIInput.setValid(false);
        JsfUtil.addErrorMessage(JsfUtil.getResource().message(
                ResourceUtil.BUNDLE_VALIDATOR_NAME,
                "validator.email.invalid", title));
    }
}
