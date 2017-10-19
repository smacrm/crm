/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.security;

import com.sun.faces.application.ActionListenerImpl;
import gnext.controller.common.LayoutController;
import gnext.controller.issue.IssueAttachmentController;
import gnext.controller.issue.IssueCommentController;
import gnext.controller.issue.IssueController;
import gnext.controller.issue.IssueCustSupportController;
import gnext.controller.issue.IssueSupportController;
import gnext.controller.issue.IssueTodoController;
import gnext.util.JsfUtil;
import java.io.IOException;
import java.util.List;
import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.ActionSource2;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.MethodExpressionActionListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author daind
 */
public abstract class BaseActionListener extends ActionListenerImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseActionListener.class);
    
    protected static final Class<?>[] ISSUE_WINDOW = {
        IssueController.class,
        IssueTodoController.class,
        IssueAttachmentController.class,
        IssueSupportController.class,
        IssueCustSupportController.class,
        IssueCommentController.class,
        
    };
    
    protected FacesContext getFacesContext() {
        final FacesContext context = FacesContext.getCurrentInstance();
        return context;
    }
    
//    protected void swichTabWindowBaseOnClassType(Class<?> clazz, final ActionEvent event) {
//        boolean isIssueWindow = false;
//        Object layout = JsfUtil.getManagedBean("layout");
//        
//        if(event.getComponent().getAttributes().containsKey("windowType")) {
//            String wt = event.getComponent().getAttributes().get("windowType").toString();
//            if(LayoutController.ISSUE_WINDOW.equals(wt)) {
//                isIssueWindow = true;
//            } else if(LayoutController.OTHER_WINDOW.equals(wt)) {
//                isIssueWindow = false;
//            }
//        } else {
//            for(Class<?> c : ISSUE_WINDOW) {
//                if(clazz.isAssignableFrom(c)) {
//                    isIssueWindow = true; break;
//                }
//            }
//        }
//        
//        if (isIssueWindow) {
//            if (layout != null) { ((LayoutController) layout).swichWindowTab(LayoutController.ISSUE_WINDOW); }
//        } else {
//            if (layout != null) { ((LayoutController) layout).swichWindowTab(LayoutController.OTHER_WINDOW); }
//        }
//    }
//    
//    protected void setDefaultWindowTabType() {
//        Object layout = JsfUtil.getManagedBean("layout");
//        if (layout != null) { ((LayoutController) layout).swichWindowTab(LayoutController.OTHER_WINDOW); }
//    }
//    
    protected String getELExpress(final ActionEvent event) {
        final UIComponent source = event.getComponent();
        final ActionSource2 actionSource = (ActionSource2) source;
        MethodExpression binding;
        binding = actionSource.getActionExpression();
        
        String expr = null;
        if (binding != null) {
            expr = binding.getExpressionString();
        } else {
            MethodExpressionActionListener bindingActionListener;
            for (ActionListener al : actionSource.getActionListeners()) {
                try {
                    if(!(al instanceof MethodExpressionActionListener)) continue;
                    bindingActionListener = (MethodExpressionActionListener) al;
                    MethodExpression m1, m2;

                    m1 = (MethodExpression) FieldUtils.readField(bindingActionListener, "methodExpressionOneArg", true);
                    m2 = (MethodExpression) FieldUtils.readField(bindingActionListener, "methodExpressionZeroArg", true);

                    if (null != m1 && null != m2 && m1.getExpressionString().equals(m2.getExpressionString())) {
                        expr = m2.getExpressionString();
                    }
                } catch (IllegalAccessException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        
        return expr;
    }
    
    private List<GrantedAuthority> roles;
    protected boolean hasRole(String role){
        if(roles == null) roles = (List<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (GrantedAuthority g : roles) {
            if (g.getAuthority().equals(role) || g.getAuthority().equals(SecurityService.Role.ROLE_MASTER.name())) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean isAjax() {
        PartialViewContext pvc = FacesContext.getCurrentInstance().getPartialViewContext();
        return pvc.isAjaxRequest();
    }
    
    protected void force403() throws IOException, ServletException {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) context.getRequest();
        HttpServletResponse response = (HttpServletResponse) context.getResponse();
        response.setHeader("Cache-Control", "no-cache, no-store");
        if (this.isAjax()) {
            FacesMessage msg = new FacesMessage("Request restrict!", "ERROR MSG");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN); //Send error request restrict
    }
}
