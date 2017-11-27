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
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
@FacesValidator("gnext.validator.RequiredValidator")
public class RequiredValidator extends BaseValidator {

    @Override
    protected boolean ignoreNull() {
        return false;
    }

    @Override
    public boolean doValidate(Object value) {
        String szVal = getAsString(value);
        // Remove space 2byte 
        if(szVal != null){
            szVal = szVal.replace("\u3000", " ").trim();
        }
        return !(szVal == null || StringUtils.isEmpty(szVal));
    }

    @Override
    public void onFaile(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
        UIInput uIInput = ((UIInput) component);
        uIInput.setValid(false);
        String title = StringUtils.EMPTY;
        try{
            title = uIInput.getAttributes().get("title").toString();
        }catch(NullPointerException e){
            try{
                title = uIInput.getAttributes().get("label").toString();
            }catch(NullPointerException e1){}
        }
        JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.required", title));
    }

}
