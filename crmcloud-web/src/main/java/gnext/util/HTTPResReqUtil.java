/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import gnext.model.authority.UserModel;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.springframework.security.crypto.codec.Base64;

/**
 *
 * @author gnextadmin
 */
public class HTTPResReqUtil {

    /** Requestからパラメタで値を取得
     * @param para
     * @return Value */
    public static String getRequestParameter(String para) {
        if(para == null || para.isEmpty()) return null;
        FacesContext context = FacesContext.getCurrentInstance();
        Map requestMap = context.getExternalContext().getRequestParameterMap();
        para = (String)requestMap.get(para);
        if(para == null || para.isEmpty()) return null;
        return para;
    }

    /** Basic認証から、ログインIDを取得
     * @return ログインID */
    public static String getRequestAuthorizationLoginId() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest httpReq = (HttpServletRequest) context.getExternalContext().getRequest();
        String auth = httpReq.getHeader("Authorization");
        if(StringUtils.isBlank(auth)) return null;
        auth = new String(Base64.decode(auth.substring(6).getBytes()));
        return auth.split(":")[0];
    }

    /** URLを取得
     * @return：BaseURL「http://~/~、https://~/~」
     */
    public static String getHostContext() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest httpReq = (HttpServletRequest) context.getExternalContext().getRequest();
        String url = httpReq.getRequestURL().toString();
        return url.substring(0, url.length() - httpReq.getRequestURI().length()) + httpReq.getContextPath();
    }

    /** ログインユーザー名取得
     * @return String */
    public static String getLoginUserName() {
        return UserModel.getLogined().getFirstName() + StringUtils.SPACE + UserModel.getLogined().getLastName();
    }

    public static void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(0);
    }

    public static void setCloseDialog() {
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("close", true);
    }

//    public static boolean chkGlobalAccessIP(String[] ips) {
//        if(ips==null || ips.length<=0) return true;
//        FacesContext context = FacesContext.getCurrentInstance();
//        HttpServletRequest res = (HttpServletRequest) context.getExternalContext().getRequest();
//        String add = res.getRemoteAddr();        
//        String add1 = res.getRemoteHost();
//        String add3 = res.getHeader("X-Forwarded-For").split("\\s*,\\s*", 2)[0];
//        System.err.println(add+" <  ---  > "+add1);
//        return true;
//    }
}
