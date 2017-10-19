package gnext.util;

import com.sun.faces.component.visit.FullVisitContext;
import java.util.Iterator;
import java.util.List;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 */
public class JsfUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsfUtil.class);
    
    public static void addErrorMessage(Exception ex, String defaultMsg) {
        String msg = ex.getLocalizedMessage();
        if (msg != null && msg.length() > 0) {
            addErrorMessage(msg);
        } else {
            addErrorMessage(defaultMsg);
        }
    }

    public static void addErrorMessages(List<String> messages) {
        for (String message : messages) {
            addErrorMessage(message);
        }
    }

    public static void addErrorMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
        FacesContext.getCurrentInstance().validationFailed(); // Invalidate JSF page if we raise an error message

    }

    public static void addExclamationMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
        FacesContext.getCurrentInstance().validationFailed();
    }

    public static void addSuccessMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
        FacesContext.getCurrentInstance().addMessage("successInfo", facesMsg);
    }

    public static boolean isValidationFailed() {
        return FacesContext.getCurrentInstance().isValidationFailed();
    }

    public static boolean isDummySelectItem(UIComponent component, String value) {
        for (UIComponent children : component.getChildren()) {
            if (children instanceof UISelectItem) {
                UISelectItem item = (UISelectItem) children;
                if (item.getItemValue() == null && item.getItemLabel().equals(value)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public static String getComponentMessages(String clientComponent, String defaultMessage) {
        FacesContext fc = FacesContext.getCurrentInstance();
        UIComponent component = UIComponent.getCurrentComponent(fc).findComponent(clientComponent);
        if (component instanceof UIInput) {
            UIInput inputComponent = (UIInput) component;
            if (inputComponent.isValid()) {
                return defaultMessage;
            } else {
                Iterator<FacesMessage> iter = fc.getMessages(inputComponent.getClientId());
                if (iter.hasNext()) {
                    return iter.next().getDetail();
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Cập nhật trạng thái component từ managedbean.
     * @param clientId 
     */
    public static void updateForClientId(String clientId) {
        try {
            RequestContext.getCurrentInstance().update(clientId);
        } catch (Exception e) {
//            LOGGER.error(e.getMessage(), e);
        }
    }
    
    /**
     * Ham tra ve The Component phia client-side.
     * @param id
     * @return 
     */
    public static UIComponent findComponent(final String id) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            UIViewRoot root = context.getViewRoot();
            final UIComponent[] found = new UIComponent[1];
            root.visitTree(new FullVisitContext(context), (VisitContext context1, UIComponent component) -> {
                if (component.getId().equals(id)) {
                    found[0] = component;
                    return VisitResult.COMPLETE;
                }
                return VisitResult.ACCEPT;
            });
            return found[0];
        } catch (Exception e) { }
        
        return null;
    }

    /**
     * Ham tra ve Managed-Bean trong container cua primefaces.
     * @param <T>
     * @param beanName
     * @param claz
     * @return 
     */
    public static <T> T getManagedBean(final String beanName, final Class<T> claz) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            ELContext elContext = fc.getELContext();
            
            Object bean = elContext.getELResolver().getValue(elContext, null, beanName);
            if(bean == null) return null;
            
            return (T) bean;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static ResourceUtil getResource() {
        return getManagedBean("resource_util", ResourceUtil.class);
    }
    
    /**
     * Danh dau The Component phia Client-Side la khong hop le.
     * @param clientId
     * @param event 
     */
    public static void setInvalidComponent(String clientId, ActionEvent event) {
        List<UIComponent> components = event.getComponent().getParent().getChildren();
        for (UIComponent uIComponent : components) {
            if (uIComponent.getClientId().equals(clientId)) {
                UIInput uIInput = (UIInput) uIComponent;
                uIInput.setValid(false);
            }
        }
    }
    
    /**
     * Ham execute ham JavaScript phia client-side.
     * @param script 
     */
    public static void executeClientScript(String script){
        RequestContext.getCurrentInstance().execute(script);
    }
    
    /**
     * Xu li clear bo state cua table tren client khi su dung ajax.
     * @param componentId id cua component.
     */
    public static void clearStateOfDataTable(String componentId) {
        try {
            UIComponent table = findComponent(componentId);
            if(table == null) return;
            table.setValueExpression("sortBy", null);
        } catch (Exception e) { }
    }
    
    /**
     * Execute function jsf-el
     * @param command 
     * @return  
     */
    public static Object executeJsfCommand(String command) {
        FacesContext context = FacesContext.getCurrentInstance();

        ExpressionFactory factory = context.getApplication().getExpressionFactory();
        MethodExpression methodExpression = factory.createMethodExpression(
                context.getELContext(), "#{" + command + "}", Void.class, new Class[]{});
        return methodExpression.invoke(context.getELContext(), null);
    }
}
