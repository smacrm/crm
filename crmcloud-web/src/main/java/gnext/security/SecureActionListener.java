package gnext.security;

import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since 2016/09
 */
public class SecureActionListener extends BaseActionListener implements ActionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecureActionListener.class);

    @Override
    public void processAction(final ActionEvent event) {
        String el = getELExpress(event);
        
        if (!StringUtils.isEmpty(el)) {
            // Truong hop khong phai la EL cua PrimeFaces thi cho phep chay luon.
            if (!(el.startsWith("#") || el.startsWith("$"))) {
                try{ super.processAction(event); }catch(Exception e){}
                return;
            }
            
            final int idx = el.indexOf('.');
            final String module = el.substring(0, idx).substring(2);
            final String tmp = el.substring(idx + 1);
            String action = tmp.substring(0, (tmp.length() - 1));
            
            // https://github.com/idbrii/ctrlp-funky/blob/master/autoload/ctrlp/funky/java.vim
            Pattern p = Pattern.compile("\\w+\\s*");
            Matcher m = p.matcher(action);
            if (m.find()) action = m.group();
            
            final ELContext elContext = getFacesContext().getELContext();
            final ExpressionFactory factory = getFacesContext().getApplication().getExpressionFactory();
            
            final ValueExpression ve = factory.createValueExpression(elContext, "#{" + module + '}', Object.class);
            final Object result = ve.getValue(elContext);
            
            // Check if the target method is a secured method and check security accordingly.
            if (result.getClass().isAnnotationPresent(SecurePage.class)) {
                
                // Tuy thuoc vao tab hien thoi nguoi dung dang su dung se chon dung layout cho nguoi dung.
//                swichTabWindowBaseOnClassType(result.getClass(), event);
                
                SecurePage pageAnnotation = result.getClass().getAnnotation(SecurePage.class);
                if(pageAnnotation.require() == true){
                    final Method[] methods = result.getClass().getDeclaredMethods();
                    for (final Method method : methods) {
                        if (method.getName().equals(action)) {
                            if (method.isAnnotationPresent(SecureMethod.class)) {
                                SecureMethod methodAnnotation = method.getAnnotation(SecureMethod.class);

                                String moduleName = pageAnnotation.module().toString();
                                String pageName = StringUtils.isEmpty(pageAnnotation.value()) ? result.getClass().getSimpleName() : pageAnnotation.value();
                                String methodName = methodAnnotation.value() == SecureMethod.Method.NONE ? method.getName() : methodAnnotation.value().toString();

                                String log = String.format("Check module [%s] page [%s] method [%s]", moduleName, pageName, methodName);
                                if (methodAnnotation.require() == false
                                        || hasRole( String.format("ROLE_%s_%s_%s", StringUtils.upperCase(moduleName), StringUtils.upperCase(pageName), StringUtils.upperCase(methodName)))) {
                                    log += " => Accept";
                                    super.processAction(event);
                                } else {
                                    log += " => Denied";
                                    FacesMessage msg = new FacesMessage("Request Restrict!", "ERROR MSG");
                                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                                    FacesContext.getCurrentInstance().addMessage(null, msg);
                                }
                                LOGGER.info(log);
                            } else {
                                FacesMessage msg = new FacesMessage("Request Restrict!", "ERROR MSG");
                                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                                FacesContext.getCurrentInstance().addMessage(null, msg);
                                LOGGER.info(String.format("Module [%s] Page [%s] method %s NOT SECURE => Denied", pageAnnotation.module(),
                                        (StringUtils.isEmpty(pageAnnotation.value()) ? result.getClass().getName() : pageAnnotation.value()),
                                        method.getName()));
                            }
                            return;
                        }
                    }
                } else {
                    try{
                        super.processAction(event);
                    } catch(IllegalStateException ise) {
                        //Xuat hien loi nay khi redirect trang bang cach su dung 
                        //<f:metadata>
                        //    <f:viewAction action="#{loginController.checkAutoGrant}" />
                        //</f:metadata>
                    }
                    return;
                }
            } else {
                LOGGER.error(String.format("Page [%s] NOT SECURE", result.getClass().getName()));
            }
        }
        
        //for testing only - force all permitted
        // super.processAction(event);
        //end testing

        // for production - uncomment below lines
//        try {
//            this.force403();
//            //super.processAction(event);
//        } catch (IOException ex) {
//            logger.error(ex.getMessage(), ex);
//        } catch (ServletException ex) {
//            logger.error(ex.getMessage(), ex);
//        }
    }
}
