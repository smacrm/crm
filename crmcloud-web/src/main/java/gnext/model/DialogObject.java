/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gnext.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;

/**
 *
 * @author HUONG
 */
public class DialogObject {

    public static void openDialog(String path) {
        if(StringUtils.isBlank(path)) return;
        Map<String,Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("resizable", false);
        options.put("responsive", true);
        RequestContext.getCurrentInstance().openDialog(path ,options,null);
    }

    public static void openDialog(String path, Map<String,Object> options) {
        if(StringUtils.isBlank(path)) return;
        options.put("modal", true);
        options.put("responsive", true);
        RequestContext.getCurrentInstance().openDialog(path ,options,null);
    }

    public static void openDialog(String path, Map<String,Object> options, Map<String, List<String>> params) {
        if(StringUtils.isBlank(path)) return;
        options.put("modal", true);
        options.put("responsive", true);
        RequestContext.getCurrentInstance().openDialog(path ,options,params);
    }
}
