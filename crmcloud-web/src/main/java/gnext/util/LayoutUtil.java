/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import gnext.model.authority.UserModel;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author HUONG
 */
public class LayoutUtil {
    private static final String[] THEMES_NAMES = {"crmcloud",
	"afterdark","afternoon","afterwork","aristo","black-tie"
	,"blitzer"
        /*    ,"bluesky"*/
        ,"bootstrap","casablanca","cruze"
	,"cupertino","dark-hive","delta","dot-luv","eggplant"
	,"excite-bike","flick","glass-x","home","hot-sneaks"
	,"humanity","le-frog","midnight","mint-choc","overcast"
	,"pepper-grinder","redmond","rocket","sam","smoothness"
	,"south-street","start","sunny","swanky-purse","trontastic"
	,"ui-darkness","ui-lightness","vader"
    };

    private static final String[] THEMES_ROLLE = {
        "skin-blue","skin-blue-light"
        ,"skin-black","skin-black-light"
        ,"skin-purple","skin-purple-light"
	,"skin-green","skin-green-light"
        ,"skin-red","skin-red-light"
	,"skin-yellow","skin-yellow-light"
    };

    public static String[] _getPrimeFacesTheme() {
        return THEMES_NAMES;
    }

    public static List<SelectItem> getThemeRolle() {
        List<SelectItem> items = new ArrayList<>();
        for(String str:THEMES_ROLLE) {
            items.add(new SelectItem(
                    str
                    ,JsfUtil.getResource().message(UserModel.getLogined().getCompanyId()
                    ,ResourceUtil.BUNDLE_CUSTOMIZE_NAME
                    ,"label." + str)));
        }
        return items;
    }

    public static String _getTheme(String layout) {
        if(StringUtils.isBlank(layout)) return "background-color: #1E1F2F;color: #fff !important;"; 
        switch (layout) {
        case "skin-blue":
            return "background-color: #3c8dbc;color: #fff !important;";
        case "skin-blue-light":
            return "background-color: #3c8dbc;color: #fff !important;";
        case "skin-black":
            return "background-color: #1E1F2F;color: #fff !important;";
        case "skin-black-light":
            return "background-color: #1E1F2F;color: #fff !important;";
        case "skin-purple":
            return "background-color: #605ca8;color: #fff !important;";
        case "skin-purple-light":
            return "background-color: #605ca8;color: #fff !important;";
        case "skin-green":
            return "background-color: #00a65a;color: #fff !important;";
        case "skin-green-light":
            return "background-color: #00a65a;color: #fff !important;";
        case "skin-red":
            return "background-color: #dd4b39;color: #fff !important;";
        case "skin-red-light":
            return "background-color: #dd4b39;color: #fff !important;";
        case "skin-yellow":
            return "background-color: #f39c12;color: #fff !important;";
        case "skin-yellow-light":
            return "background-color: #f39c12;color: #fff !important;";
        default:
            return "background-color: #1E1F2F;color: #fff !important;";
        }
    }
}
