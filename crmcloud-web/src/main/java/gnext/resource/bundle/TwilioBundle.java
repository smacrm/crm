package gnext.resource.bundle;

import gnext.security.annotation.SecurePage;
import gnext.util.ResourceUtil;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Override resource-bundle on face-config.xml
 * variable is `msg`
 * 
 * @author hungpham
 * @since Oct 27, 2016
 */
@ManagedBean(name = "sf")
@SessionScoped
@SecurePage(module = SecurePage.Module.NONE, require = false)
public class TwilioBundle extends ResourceProvider{

    @Override
    protected String getBaseName() {
        return ResourceUtil.BUNDLE_SOFTPHONE;
    }

}
