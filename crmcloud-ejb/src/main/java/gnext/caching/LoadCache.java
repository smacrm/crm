/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.caching;

import gnext.service.config.ConfigService;
import gnext.service.customize.AutoFormItemService;
import gnext.service.label.LabelService;
import gnext.service.mente.MenteService;
import java.io.Serializable;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author daind
 */
@ApplicationScoped
public class LoadCache implements Serializable {
    @EJB private ConfigService configService;
    @EJB private LabelService labelService;
    @EJB private MenteService menteService;
    @EJB private AutoFormItemService dynamicLabelService;
    
    public void onStartApplication() {
        // config
        configService.reloadCache();
                
        // mente
        menteService.reloadCache();
        
        // label
        labelService.reloadCache();
        
        // Reload all dynamic label
        dynamicLabelService.reloadCache();
    }
    
    @PreDestroy
    public void close() {
        // Nothing here!
        
    }
}
