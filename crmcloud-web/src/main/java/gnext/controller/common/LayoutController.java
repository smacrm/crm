package gnext.controller.common;

import gnext.controller.issue.IssueController;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.util.HTTPResReqUtil;
import gnext.util.JsfUtil;
import gnext.util.Pagination;
import gnext.util.ResourceUtil;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.themeswitcher.ThemeSwitcher;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author hungpham
 */
@ManagedBean(name = "layout", eager = true)
@SessionScoped
@SecurePage(value="Layout", module = SecurePage.Module.NONE, require=false)
public class LayoutController implements Serializable {

    private static final long serialVersionUID = -162995978115451030L;

    final private Logger logger = LoggerFactory.getLogger(LayoutController.class);
    
    @ManagedProperty(value = "#{userModel}") 
    @Getter @Setter private UserModel userModel;
    
    @ManagedProperty(value = "#{loginController}") 
    @Getter @Setter private LoginController loginController;

    private String centerBottom;
    @Setter @Getter private String selectedTheme;
    
    @Setter @Getter private String rowsPerPageTemplate;
    @Setter @Getter private String defaultRowsOnPage;
    @Setter @Getter private String paginatorTemplate;
    @Setter @Getter private String sizeLimitUpload;
    @Setter @Getter private String allowTypetUpload;
    
    @PostConstruct
    public void init() {
        this.centerBottom = "/modules/issue/list.xhtml";
        this.selectedTheme = UserModel.getLogined().getTheme();
        
        this.rowsPerPageTemplate = Pagination.ROWSPERPAGETEMPLATE;
        this.defaultRowsOnPage = String.valueOf(Pagination.DEFAULT_ROWS_ON_PAGE);
        this.paginatorTemplate = "{RowsPerPageDropdown} {FirstPageLink} {PreviousPageLink} {CurrentPageReport} {NextPageLink} {LastPageLink}";
        this.sizeLimitUpload = "1048576"; // 1MB
        this.allowTypetUpload = "/(\\.|\\/)(gif|jpe?g|png)$/";
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        
        logger.info("LoggedIn User is " + currentPrincipalName);
    }
    
    @SecureMethod(value=SecureMethod.Method.UPDATE, require = false)
    public void saveTheme(AjaxBehaviorEvent ajax) {
        Object oSelectedTheme = ((ThemeSwitcher)ajax.getSource()).getValue();
        if( null != oSelectedTheme ){
            String value = oSelectedTheme.toString();
            
            this.selectedTheme = value;
            userModel.setTheme(value);
        }
    }

    public void sendToPage() {
        String toPage = HTTPResReqUtil.getRequestParameter("toPage");
        if(toPage == null || toPage.isEmpty()) return;
        this.centerBottom = toPage;
    }
    
    public String getCurrentPageReportTemplate() {
        return "{startRecord} - {endRecord} " + JsfUtil.getResource().message( ResourceUtil.BUNDLE_MSG, "label.currentpagereport", "") + " {totalRecords}";
    }
    
    public String getComponentForUpdateInSiderbarLeft() {
        return "sidebarLeft:menuListGroup, sidebarLeft:menuSearchGroup, sidebarLeft:issueLamp";
    }
    
    public String getEmptyMessage() {
        return "レコードが見つかりません";
    }

    public void onPageLoad() throws UnsupportedEncodingException{
        Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        Boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        String id = StringUtils.defaultIfEmpty(params.get("id"), "");
        String custId = StringUtils.defaultIfEmpty(params.get("custId"), "");
        
        // kiểm tra nếu trong module customer thì lấy id là custid.
//        String custId = StringUtils.defaultIfEmpty(params.get("custId"), "");
//        if(!StringUtils.isEmpty(custId)) id = custId;
        
        boolean isExecuteScript = false;
        if(!centerBottom.contains("issue") || isAjax ){
            if(StringUtils.isEmpty(id) && StringUtils.isEmpty(custId)){
                RequestContext.getCurrentInstance().execute(String.format("window.history.pushState(null, null, '?s=%s')", this.getCurrentEaseyEncrypt()));
            } else if(!StringUtils.isEmpty(id) && StringUtils.isEmpty(custId)) {
                RequestContext.getCurrentInstance().execute(String.format("window.history.pushState(null, null, '?s=%s&id=%s')", this.getCurrentEaseyEncrypt(), id));
            } else if(StringUtils.isEmpty(id) && !StringUtils.isEmpty(custId)) {
                RequestContext.getCurrentInstance().execute(String.format("window.history.pushState(null, null, '?s=%s&custId=%s')", this.getCurrentEaseyEncrypt(), custId));
            } else {
                RequestContext.getCurrentInstance().execute(String.format("window.history.pushState(null, null, '?s=%s&id=%s&custId=%s')", this.getCurrentEaseyEncrypt(), id, custId));
            }
            isExecuteScript = true;
        }
        
        if(!centerBottom.contains("issue") || centerBottom.contains("issue_list")){
            if(!isExecuteScript){
                RequestContext.getCurrentInstance().execute(String.format("window.history.pushState(null, null, '?s=%s')", this.getCurrentEaseyEncrypt()));
            }
        }
    }
    
    public String getCurrentEaseyEncrypt() throws UnsupportedEncodingException{
        return this.easeyEncrypt(centerBottom);
    }
    
    public String easeyEncrypt(String strip) throws UnsupportedEncodingException {
        byte[] encryptArray = Base64.encodeBase64(strip.getBytes());
        String encstr = new String(encryptArray, "UTF-8");
        return encstr;
    }

    public String easeyDecrypt(String secret) throws UnsupportedEncodingException {
        byte[] dectryptArray = secret.getBytes();
        byte[] decarray = Base64.decodeBase64(dectryptArray);
        String decstr = new String(decarray, "UTF-8");
        return decstr;
    }
    
    public String getCenter() throws UnsupportedEncodingException {
        Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        
        Boolean isPost = "POST".equals(request.getMethod());
        Boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        
        if(!"true".equals(params.get("force"))){
            String action = params.get("s");
            if(!StringUtils.isEmpty(action)){
                centerBottom = easeyDecrypt(action);
            }
        }
        return centerBottom;
    }
    public void setCenter(String centerBottom) {
        if("/modules/issue/edit.xhtml".equals(this.centerBottom) && !this.centerBottom.equals(centerBottom)){
            //neu di chuyen tu man hinh edit issue sang 1 man hinh khac, thi load bo issue ra khoi danh sach hold
            JsfUtil.getManagedBean("issueController", IssueController.class).unHoldIssue();
        }
        this.centerBottom = centerBottom;
    }
}
