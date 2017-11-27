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
@ManagedBean(name = "quartzBundle")
@SessionScoped
@SecurePage(module = SecurePage.Module.NONE, require = false)
public class QuartzBundle extends ResourceProvider {
    private static final long serialVersionUID = -4266623047274591446L;
    public static final String BUNDLE_NAME = "gnext.resource.quartz.quartz";
    @Override
    protected String getBaseName() {
        return BUNDLE_NAME;
    }
}
