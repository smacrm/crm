/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author daind
 */
public abstract class BaseValidator implements Validator {

    /***
     * Hàm kiểm tra tính đúng đắn của dữ liệu.
     * @param value là giá trị người dùng nhập vào.
     * @return false - Nếu value không hợp lệ.
     *          true - Nếu value là hợp lệ.
     */
    public abstract boolean doValidate(final Object value);
    
    /***
     * Hàm xử lí nếu giá trị người dùng nhập vào là không hợp lệ.
     * @param facesContext
     * @param component
     * @param value 
     */
    protected abstract void onFaile(FacesContext facesContext,
            UIComponent component, Object value);

    /**
     * Hàm kiểm tra có check NULL không.
     * Mặc định là bỏ qua việc check giá trị NULL hoặc rỗng.
     * @return 
     */
    protected boolean ignoreNull() {
        return true;
    }

    /***
     * Hàm xử lí chính.
     * Primeface sẽ gọi hàm này để kiểm trá giá trị người dùng nhập.
     * @param facesContext
     * @param component
     * @param value
     * @throws ValidatorException 
     */
    @Override
    public void validate(FacesContext facesContext,
            UIComponent component, Object value)
            throws ValidatorException {
        if (ignoreNull() && value == null) return;
        if (!doValidate(value)) onFaile(facesContext, component, value);
    }

    /***
     * Hàm xử lí lấy giá trị là String từ value.
     * @param value là giá trị người dùng nhập vào.
     * @return 
     */
    protected String getAsString(final Object value) {
        if(value == null) return null;
        return String.valueOf(value);
    }
}
