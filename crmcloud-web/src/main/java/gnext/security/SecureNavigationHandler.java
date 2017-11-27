/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.security;

import java.util.Map;
import java.util.Set;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

/**
 *
 * @author hungpham
 */
public class SecureNavigationHandler extends ConfigurableNavigationHandler{

   
    private NavigationHandler wrapped;

    public SecureNavigationHandler(NavigationHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void handleNavigation(FacesContext context, String from, String outcome) {

        //System.err.println(String.format("SecureNavigationHandler: %s %s", from, outcome));

        wrapped.handleNavigation(context, from, outcome);
    }

    @Override
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome) {
        return (wrapped instanceof ConfigurableNavigationHandler)
            ? ((ConfigurableNavigationHandler) wrapped).getNavigationCase(context, fromAction, outcome)
            : null;
    }

    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases() {
        return (wrapped instanceof ConfigurableNavigationHandler)
            ? ((ConfigurableNavigationHandler) wrapped).getNavigationCases()
            : null;
    }

}