package gnext.util;

import gnext.caching.LoadCache;
import gnext.model.authority.UserModel;
import gnext.service.label.LabelService;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author HUONG
 */
@ManagedBean(name = "resource_util", eager = true)
@ApplicationScoped
public class ResourceUtil implements Serializable {
    
    private static final long serialVersionUID = -2269761211465795211L;
    
    public static final String APP_NAME = "CRMCloud";
    
    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_WARN_NAME = "WARN";
    public static final String SEVERITY_ERROR_NAME = "ERROR";
    public static final String SEVERITY_FATAL_NAME = "FATAL";

    public static final String APP_PATH = "/faces";
    public static final String BUNDLE_COMPANY = "gnext.resource.company.company";
    public static final String BUNDLE_MSG = "gnext.resource.locale.crmcloud";
    public static final String BUNDLE_LOG_NAME = "gnext.resource.logs.logs";
    public static final String BUNDLE_VALIDATOR_NAME = "gnext.resource.validator.validator";
    public static final String BUNDLE_ISSUE_NAME = "gnext.resource.issue.issue";
    public static final String BUNDLE_REPORT_NAME = "gnext.resource.report.report";
    public static final String BUNDLE_MAIL_NAME = "gnext.resource.mail.mail";
    public static final String BUNDLE_CUSTOMIZE_NAME = "gnext.resource.customize.customize";
    public static final String BUNDLE_CUSTOMER_NAME = "gnext.resource.customer.customer";
    public static final String BUNDLE_MAINTE_NAME = "gnext.resource.maintenance.maintenance";
    public static final String BUNDLE_SYSTEM = "gnext.resource.system.system";
    public static final String BUNDLE_SOFTPHONE = "gnext.resource.softphone.softphone";
    public static final String BUNDLE_LABEL = "gnext.resource.label.label";
    
    public static final String[] AVAILABLE_CACHE_BUNDLES = {BUNDLE_COMPANY, BUNDLE_MSG, BUNDLE_VALIDATOR_NAME, BUNDLE_ISSUE_NAME, BUNDLE_CUSTOMER_NAME, BUNDLE_REPORT_NAME, BUNDLE_MAIL_NAME, BUNDLE_CUSTOMIZE_NAME, BUNDLE_MAINTE_NAME, BUNDLE_SYSTEM, BUNDLE_SOFTPHONE};
    public static final Locale[] AVAILABLE_CACHE_LOCALES = {Locale.JAPANESE, Locale.ENGLISH, new Locale("vi")};

    @EJB private LabelService labelService;
    @Inject LoadCache loadCache;
    
    @PostConstruct
    public void init() {
        // khởi động cache.
        loadCache.onStartApplication();
    }
    
    public String logMessage(String bundleName,String key){
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        try{
            ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
            if(!key.isEmpty()){
                return String.valueOf(rb.getString(key));
            }
        }catch(MissingResourceException e){}
        return key;
    }

    public Integer logIntMessage(String bundleName,String key){
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
	ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
        Integer val = 0;
        if(!key.isEmpty()){
            val = Integer.valueOf(rb.getString(key));
        } else {
            val = 0;
        }
        return val;
    }
    
    public String message(Integer companyId, String bundleName, String key, Object... params){
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        return message(companyId, bundleName, locale, key, params);
    }

    public String message(Integer companyId, String bundleName, Locale locale, String key, Object... params){
        String dbMessage = findFromEjbCached(bundleName, key, locale, companyId);
        if(!StringUtils.isEmpty(dbMessage)) return dbMessage;
        
        ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
        try {
            return MessageFormat.format(rb.getString(key), params);
        } catch(Exception mre) {
            // Nothing here!
        }
        
        return key;
    }
    
    private String findFromEjbCached(String bundleName, String key, Locale locale, Integer comapnyId) {
        if(comapnyId == null || comapnyId <= 0) return null;
        try {
            return labelService.findByKey(bundleName, key, locale.getLanguage(), comapnyId); //add db check
        } catch (Exception e) {
            // Nothing here!
        }
        return null;
    }
    
    public String message(String bundleName, String key, Object... params){
        return this.message(0, bundleName, key, params);
    }

    public void putErrors(String bundleName,String key){
        putErrors(0, bundleName, key);
    }
    
    public void putErrors(Integer companyId, String bundleName,String key){
        FacesMessage error = new FacesMessage(this.message(companyId, bundleName,key));
        FacesContext.getCurrentInstance().addMessage(null, error);
    }

    public void putMessager(String msg){
        if(StringUtils.isBlank(msg)) return;
        FacesMessage error = new FacesMessage(msg);
        FacesContext.getCurrentInstance().addMessage(null, error);
    }

    public void putErrorsPara(String bundleName,String key){
        putErrorsPara(0, bundleName, key);
    }
    
    public void putErrorsPara(Integer companyId, String bundleName,String key){
        FacesMessage error = new FacesMessage(this.message(companyId, bundleName,key));
        FacesContext.getCurrentInstance().addMessage(null, error);
    }

    public void putSystemErrors(String bundleName,String type,String key,boolean detail){
        this.putErrors(0, bundleName, type, key, detail);
    }
    
    public void putErrors(Integer companyId, String bundleName,String type,String key,boolean detail){
        if (type == null || type.isEmpty()) return;
        Severity sec = FacesMessage.SEVERITY_INFO;
        switch (type) {
            case SEVERITY_WARN_NAME:
                sec = FacesMessage.SEVERITY_WARN;
                break;
            case SEVERITY_ERROR_NAME:
                sec = FacesMessage.SEVERITY_ERROR;
                break;
            case SEVERITY_FATAL_NAME:
                sec = FacesMessage.SEVERITY_FATAL;
                break;
            default:
                break;
        }
        if (sec == null) return;
        FacesMessage error = new FacesMessage(sec, this.message(companyId, bundleName,key),null);
        if (detail) {
            error = new FacesMessage(sec,type, this.message(companyId, bundleName,key));
        }
        FacesContext.getCurrentInstance().addMessage(null, error);
    }

    public void putErrors(Integer companyId, String bundleName, String type, String key, boolean detail, Object... params){
        if (type == null || type.isEmpty()) return;
        Severity sec = FacesMessage.SEVERITY_INFO;
        switch (type) {
            case SEVERITY_WARN_NAME:
                sec = FacesMessage.SEVERITY_WARN;
                break;
            case SEVERITY_ERROR_NAME:
                sec = FacesMessage.SEVERITY_ERROR;
                break;
            case SEVERITY_FATAL_NAME:
                sec = FacesMessage.SEVERITY_FATAL;
                break;
            default:
                break;
        }
        if (sec == null) return;
        String msg;
        if(params != null) {
            msg = this.message(companyId, bundleName, key, params);
        } else {
            msg = this.message(companyId, bundleName, key);
        }
        if(StringUtils.isBlank(msg)) return;
        FacesMessage error = new FacesMessage(sec, msg, null);
        if (detail) {
            error = new FacesMessage(sec,type, msg);
        }
        FacesContext.getCurrentInstance().addMessage(null, error);
        FacesContext.getCurrentInstance().validationFailed();
    }

    public void alertMsgMaxLength(String key1, String bundle1, String key2, String bundle2, Integer key3) {
        if(StringUtils.isBlank(key1) || StringUtils.isBlank(bundle1)
                || StringUtils.isBlank(key2) || StringUtils.isBlank(bundle2) || key3 == null) return;
        Object[] para = new Object[2];
        para[0] = this.message(UserModel.getLogined().getCompanyId()
                , bundle1
                , key1, (Object) null);
        para[1] = key3;
        this.putErrors(
                UserModel.getLogined().getCompanyId()
                , bundle2
                , ResourceUtil.SEVERITY_INFO
                , key2
                , false , para);
    }

    public void alertMsg(String key1, String bundle1, String key2, String bundle2) {
        if(StringUtils.isBlank(key1) || StringUtils.isBlank(bundle1)
                || StringUtils.isBlank(key2) || StringUtils.isBlank(bundle2)) return;
        Object[] para = new Object[1];
        para[0] = this.message(
                UserModel.getLogined().getCompanyId()
                , bundle1
                , key1, para);
        this.putErrors(
                UserModel.getLogined().getCompanyId()
                , bundle2
                , ResourceUtil.SEVERITY_ERROR_NAME
                , key2
                , false , para);
    }

    public void alertMsgInfo(String key1, String bundle1, String key2, String bundle2) {
        if(StringUtils.isBlank(key1) || StringUtils.isBlank(bundle1)
                || StringUtils.isBlank(key2) || StringUtils.isBlank(bundle2)) return;
        Object[] para = new Object[1];
        para[0] = this.message(
                UserModel.getLogined().getCompanyId()
                , bundle1
                , key1, para);
        this.putErrors(
                UserModel.getLogined().getCompanyId()
                , bundle2
                , ResourceUtil.SEVERITY_INFO
                , key2
                , false , para);
    }

    public void alertMsgDynamicInfo(String dynamicMsg, String type, String bundle, String key) {
        if(StringUtils.isBlank(dynamicMsg) || StringUtils.isBlank(bundle)
                || StringUtils.isBlank(type) || StringUtils.isBlank(key)) return;
        Object[] para = new Object[1];
        para[0] = dynamicMsg;
        this.putErrors(UserModel.getLogined().getCompanyId(), bundle, type, key, false , para);
    }

    public String getPageName(String inKey) {
        String outKey = StringUtils.EMPTY;
        if(StringUtils.isBlank(inKey)) return outKey;
        switch (inKey) {
            case "gnext.resource.company":
                outKey = "label.menu.CompanyController";
                break;
            case "gnext.resource.issue":
                outKey = "label.menu.IssueController";
                break;
            case "gnext.resource.customer":
                outKey = "label.menu.CustomerController";
                break;
            case "gnext.resource.report":
                outKey = "label.menu.ReportController";
                break;
            case "gnext.resource.mail":
                outKey = "label.menu.MailListController";
                break;
            case "gnext.resource.customize":
                outKey = "label.menu.CustomizeController";
                break;
            default:
                outKey = inKey;
                break;
        }
        return outKey;
    }
}
