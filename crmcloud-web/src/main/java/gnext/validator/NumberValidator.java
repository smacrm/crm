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

/**
 *
 * @author daind
 */
@FacesValidator("gnext.validator.NumberValidator")
public class NumberValidator extends BaseValidator {

    private boolean mainFaile = false;
    
    private double getNumber(Object value) throws NumberFormatException {
        return Double.parseDouble(getAsString(value));
    }
    
    private void alertToComponent(UIComponent component, String message) {
        UIInput uIInput = ((UIInput) component);
        uIInput.setValid(false);
        JsfUtil.addErrorMessage(message);
    }
    
    @Override
    public boolean doValidate(Object value) {
        try {
            getNumber(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public void onFaile(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        mainFaile = true;
        String title = component.getAttributes().get("title").toString();
        alertToComponent(component, JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.digit.invalid", title));
    }
    
    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
        super.validate(facesContext, component, value);
        
        // trường hợp validation chính(kiểm tra có phải là số) là faile thì ko làm gì.
        if(mainFaile) return;
        
        // ngược lại, kiểm tra các kiều kiện khác kèm theo component.
        UIInput uIInput = ((UIInput) component);
        String allownegative = String.valueOf(uIInput.getAttributes().get("allownegative"));
        if("false".equalsIgnoreCase(allownegative)) {
            double nv = getNumber(value);
            if(nv < 0) {
                String title = component.getAttributes().get("title").toString();
                alertToComponent(component, JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.digit.negative.notallow", title));
            }
        }
    }
    
}
