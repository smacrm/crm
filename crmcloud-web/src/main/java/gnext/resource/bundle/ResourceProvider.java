package gnext.resource.bundle;

import gnext.controller.common.LoginController;
import gnext.util.JsfUtil;
import java.io.Serializable;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Custom Resource Bundle provider Get data from Redis if exists either Static
 * Resource Bundle (Using ResourceUtil class)
 *
 * @author hungpham
 * @since Oct 27, 2016
 */
public abstract class ResourceProvider extends HashMap implements Serializable {

    private static final long serialVersionUID = -4261136740610326267L;

    @ManagedProperty(value = "#{loginController}")
    @Getter @Setter
    private LoginController login;

    private Integer companyId;

    @PostConstruct
    public void init() {
        try {
            companyId = login.getMember().getCompanyId();
        } catch (Exception e) {}
    }

    @Override
    public Object get(Object key) {
        if(companyId == null){
            return JsfUtil.getResource().logMessage(getBaseName(), key.toString());
        }
        return JsfUtil.getResource().message(companyId, getBaseName(), key.toString());
    }

    public String getString(Object key, Object... params) {
        if(companyId == null){
            return JsfUtil.getResource().message(getBaseName(), key.toString(), params);
        }
        return JsfUtil.getResource().message(companyId, getBaseName(), key.toString(), params);
    }

    protected abstract String getBaseName();

}
