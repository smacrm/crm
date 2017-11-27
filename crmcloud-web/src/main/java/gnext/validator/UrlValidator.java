/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.validator;

/**
 *
 * @author daind
 */
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

@FacesValidator("gnext.validator.UrlValidator")
public class UrlValidator extends BaseValidator {

    public static final String URL_PATTERN = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";

    @Override
    public boolean doValidate(Object value) {
        return getAsString(value).matches(URL_PATTERN);
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
                        "validator.url.invalid", title));
    }

}
