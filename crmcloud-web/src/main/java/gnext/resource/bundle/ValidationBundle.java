/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.resource.bundle;

import gnext.security.annotation.SecurePage;
import gnext.util.ResourceUtil;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author daind
 */
@ManagedBean(name = "validator")
@SessionScoped
@SecurePage(module = SecurePage.Module.NONE, require = false)
public class ValidationBundle extends ResourceProvider { 
    private static final long serialVersionUID = -2041008951566521474L;
    @Override
    protected String getBaseName() {
        return ResourceUtil.BUNDLE_VALIDATOR_NAME;
    }
}
