/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gnext.controller.speech;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.UserAgentUtil;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author HUONG
 */
@ManagedBean(name = "speechController")
@SessionScoped()
@SecurePage(module = SecurePage.Module.SPEECHAPI)
public class SpeechAPIController implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LoggerFactory.getLogger(SpeechAPIController.class);

    @SecureMethod(value=SecureMethod.Method.SEARCH)
    public void speechApi(ActionEvent event) {
        /** ChromeやFirefFox以外対応しない */
        if(!UserModel.getLogined().isUserAgentSupportSpeech()) return;
        if(UserAgentUtil.getSpeechSuportLocale(UserModel.getLogined().getLanguage())) {
            JsfUtil.getResource().putErrors(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_INFO, "label.speech.support", false);   
            logger.info(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.speech.support"));
        } else {
            JsfUtil.getResource().putErrors(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_WARN_NAME, "label.speech.notsupport", false);
            logger.info(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.speech.notsupport"));
        }
    }
}