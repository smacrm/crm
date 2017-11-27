/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.common;

import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.ServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author root
 */
@ManagedBean(name = "localeController")
@SessionScoped()
public class LocaleController implements Serializable {
    final private Logger logger = LoggerFactory.getLogger(LocaleController.class);
    
    private static final long serialVersionUID = 1L;
    @Getter private final Locale[] availableLocales =  { Locale.ENGLISH ,Locale.JAPANESE, Locale.forLanguageTag("vi") };
    public static String MANAGED_BEAN_NAME = "localeController";
    
    @Getter @Setter private String locale;
    private static Map<String, Object> locales;

    @PostConstruct
    public void init() {
        try{
            if(locale == null) {
                locale = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
            }
            logger.info("Set locale " + locale);
        }catch(Exception npe){
            //locale = FacesContext.getCurrentInstance().getApplication().getDefaultLocale().getLanguage();
            locale = "ja";
            logger.info("Get default locale " + locale);
        }
    }
    public Map<String, Object> getLocales() {
        locales = new LinkedHashMap<>();
        for (Locale _locale : availableLocales) {
            String name = JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG,"language_" + _locale.getLanguage());
            locales.put(name, _locale);
        }
        return locales;
    }
    
    public String getLocaleName(String lang){
        for (Locale _locale : availableLocales) {
            if(_locale.getLanguage().equals(lang)){
                String name = JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG,"language_" + _locale.getLanguage());
                return name;
            }
        }
        return lang;
    }

    //value change event listener
    public void localeChanged(ValueChangeEvent e) {
        Locale newLocaleValue = new Locale(e.getNewValue().toString());
        FacesContext faces = FacesContext.getCurrentInstance();
        ServletResponse res = (ServletResponse) faces.getExternalContext().getResponse();
        res.setLocale(newLocaleValue);
        faces.getViewRoot().setLocale(newLocaleValue);
        locale = newLocaleValue.getLanguage();
    }
}
