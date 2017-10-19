/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.resource.bundle;

import gnext.security.annotation.SecurePage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author daind
 */
@ManagedBean(name = "companyBundle")
@SessionScoped
@SecurePage(module = SecurePage.Module.NONE, require = false)
public class CompanyBundle extends ResourceProvider {
    private static final long serialVersionUID = 8449365827158117542L;
    public static final String BUNDLE_NAME = "gnext.resource.company.company";
    @Override
    protected String getBaseName() {
        return BUNDLE_NAME;
    }
    
}
