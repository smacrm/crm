/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.common;

import gnext.controller.issue.CustomerController;
import gnext.model.DialogObject;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecurePage;
import gnext.util.HTTPResReqUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author root
 */
@ManagedBean(name = "dialogController")
@RequestScoped()
@SecurePage(module = SecurePage.Module.ISSUE, require = false)
public class DialogController implements Serializable {

    private static final long serialVersionUID = 3899135370473668661L;
    final private Logger logger = LoggerFactory.getLogger(DialogController.class);

    @Getter @Setter private UserModel userModel;
    @ManagedProperty(value = "#{customerController}") @Getter @Setter private CustomerController customerController;
    
    @PostConstruct
    public void init() {
        this.userModel = UserModel.getLogined();
    }

    public void openCustDialog(int custId, boolean isView) {
        try {
            customerController.setCustIdViewParamInDialog(custId);
            
            String val = null;
            if(isView) {
                val = "/modules/issue/dialog/show_customer_dialog";
            } else {
                val = "/modules/issue/dialog/customer_dialog";
                Short businessFlag = UserModel.getLogined().getCompany().getCompanyBusinessFlag();
                if(businessFlag != null && businessFlag == 2) {
                    val = "/modules/issue/dialog/customer_special_dialog";
                }
            }
            
            String top = HTTPResReqUtil.getRequestParameter("top");
            String width = HTTPResReqUtil.getRequestParameter("width");
            String height = HTTPResReqUtil.getRequestParameter("height");
            if(!StringUtils.isBlank(val)){
                Map<String,Object> options = new HashMap<>();
                options.put("draggable", true);
                options.put("resizable", false);
                if(NumberUtils.isDigits(top)) {
                    options.put("top", top);
                }
                if(NumberUtils.isDigits(width)) {
                    options.put("contentWidth", width);
                }
                if(NumberUtils.isDigits(height)) {
                    options.put("contentHeight", height);
                }
                options.put("includeViewParams", true);
                DialogObject.openDialog(val, options);
            }
        } catch (Exception ex) {
            logger.error("[DialogController.openDialog()]", ex);
        }
    }
    
    public void openDialog() {
        try {
            String val = HTTPResReqUtil.getRequestParameter("openDialogURL");
            String top = HTTPResReqUtil.getRequestParameter("top");
            String width = HTTPResReqUtil.getRequestParameter("width");
            String height = HTTPResReqUtil.getRequestParameter("height");
            if(!StringUtils.isBlank(val)){
                Map<String,Object> options = new HashMap<>();
                options.put("draggable", true);
                options.put("resizable", false);
                if(NumberUtils.isDigits(top)) {
                    options.put("top", top);
                }
                if(NumberUtils.isDigits(width)) {
                    options.put("contentWidth", width);
                }
                if(NumberUtils.isDigits(height)) {
                    options.put("contentHeight", height);
                }
                options.put("includeViewParams", true);
                DialogObject.openDialog(val, options);
            }
        } catch (Exception ex) {
            logger.error("[DialogController.openDialog()]", ex);
        }
    }

}
