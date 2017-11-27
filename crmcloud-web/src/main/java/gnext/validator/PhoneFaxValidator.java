/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.validator;

import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import java.util.regex.Pattern;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author daind
 */
@FacesValidator("gnext.validator.PhoneFaxValidator")
public class PhoneFaxValidator extends BaseValidator {

    public static final String PHONE_FAX_PATTERN = "^\\(?(\\d+)\\)?[- ]?(\\d+)[- ]?(\\d+)$";

    @Override
    public boolean doValidate(Object value) {
        Pattern pattern = Pattern.compile(PHONE_FAX_PATTERN);
        return pattern.matcher(getAsString(value)).matches();
    }

    @Override
    public void onFaile(FacesContext facesContext,
            UIComponent component, Object value)
            throws ValidatorException {
        UIInput uIInput = ((UIInput) component);
        String title = uIInput.getAttributes().get("title").toString();
        uIInput.setValid(false);
        JsfUtil.addErrorMessage(
                JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME,
                        "validator.telfax.invalid", title));
    }

}
