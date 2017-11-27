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
@FacesValidator("gnext.validator.DomainValidator")
public class DomainValidator extends BaseValidator {

    public static final String DOMAIN_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9-_]{0,61}[a-zA-Z0-9]{0,1}\\.([a-zA-Z]{1,6}|[a-zA-Z0-9-]{1,30}\\.[a-zA-Z]{2,3})$";

    @Override
    public boolean doValidate(final Object value) {
        return Pattern.compile(DOMAIN_PATTERN).matcher(getAsString(value)).matches();
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
                        "validator.domain.invalid", title));
    }
}
