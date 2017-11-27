/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.validator;

import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.utils.Console;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.springframework.util.StringUtils;

/**
 *
 * @author daind
 */
@FacesValidator("gnext.validator.DomainIpValidator")
public class DomainIpValidator implements Validator {

    @Override
    public void validate(FacesContext facesContext,
            UIComponent component, Object value)
            throws ValidatorException {
        if(value == null) return;
        
        BaseValidator ipValidator = new IpValidator();
        BaseValidator domainValidator = new DomainValidator();

        UIInput uIInput = ((UIInput) component);
        String title = uIInput.getAttributes().get("title").toString();
        
        if (!(ipValidator.doValidate(value) || domainValidator.doValidate(value))) {
            uIInput.setValid(false);
            JsfUtil.addErrorMessage(
                    JsfUtil.getResource().message(
                            ResourceUtil.BUNDLE_VALIDATOR_NAME,
                            "validator.domainip.invalid", title));
        } else {
            String testConnection = String.valueOf(uIInput.getAttributes()
                    .get("testConnection"));
            String val = String.valueOf(value);
            if ("true".equalsIgnoreCase(testConnection)
                    && !StringUtils.isEmpty(val) && !Console.ping(val)) {
                uIInput.setValid(false);
                JsfUtil.addErrorMessage(
                        JsfUtil.getResource().message(
                                ResourceUtil.BUNDLE_VALIDATOR_NAME,
                                "validator.domainip.invalid",
                                title));
            }
        }
    }
}
