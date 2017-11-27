package gnext.resource.bundle;

import gnext.security.annotation.SecurePage;
import gnext.util.ResourceUtil;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Override resource-bundle on face-config.xml
 * variable is `issue`
 * 
 * @author hungpham
 * @since Oct 27, 2016
 */
@ManagedBean(name = "issue")
@SessionScoped
@SecurePage(module = SecurePage.Module.NONE, require = false)
public class IssueBundle extends ResourceProvider{

    @Override
    protected String getBaseName() {
        return ResourceUtil.BUNDLE_ISSUE_NAME;
    }

}
