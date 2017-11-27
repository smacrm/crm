package gnext.controller.common;

import gnext.bean.Company;
import gnext.bean.Member;
import gnext.bean.MemberAuth;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.controller.issue.IssueForceViewController;
import gnext.controller.issue.IssueGlobalStatus;
import gnext.controller.issue.bean.PersitBean;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.dbutils.util.Console;
import gnext.dbutils.util.FileUtil;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.model.authority.RoleModel;
import gnext.model.authority.UserModel;
import gnext.multitenancy.TenantHolder;
import gnext.security.SecurityConfig;
import static gnext.security.SecurityService.Role.ROLE_GRANT_ACCESS;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.security.notification.DeviceSessionHandler;
import gnext.security.twofa.TimeBasedOneTimePasswordUtil;
import gnext.service.CompanyService;
import gnext.service.MemberService;
import gnext.service.attachment.AttachmentService;
import gnext.service.attachment.ServerService;
import gnext.service.config.ConfigService;
import gnext.service.label.LabelService;
import gnext.util.JsfUtil;
import gnext.util.LayoutUtil;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import gnext.util.StringUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;

@ManagedBean(name = "loginController")
@SessionScoped()
@SecurePage(module = SecurePage.Module.AUTHENTICATION, require = false)
public class LoginController implements Serializable {
    private static final long serialVersionUID = -366829560929713771L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    
    @EJB private CompanyService companyService;
    @EJB private MemberService memberService;
    @EJB private ConfigService configServiceImpl;
    @EJB private LabelService labelServiceImpl;
    @EJB private ServerService serverService;
    @EJB private AttachmentService attachmentService;
    
    @Setter @Getter public String username;
    @Setter @Getter public String password;
    @Setter @Getter public String code;
    @Setter @Getter private Boolean rememberMe;
    @Getter @Setter private UserModel member;
    @Setter @Getter public String background;
    
    @ManagedProperty(value = "#{localeController}") @Getter @Setter private LocaleController localeController;
    @ManagedProperty(value = "#{ifvc}") @Getter @Setter private IssueForceViewController ifvc;
    @ManagedProperty(value = "#{issueGlobalStatus}") @Getter @Setter private IssueGlobalStatus issueGlobalStatus;
    
    @Inject private TenantHolder tenantHolder;
    @Inject private DeviceSessionHandler sessionHandler;
    
    @Getter @Setter private Boolean showLoginSessionAlert;
    @Getter @Setter private List<Company> grantList = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        String basicId = SecurityContextHolder.getContext().getAuthentication().getName();
        setCompany(basicId);
    
        //////////////////////////// START RELOAD CACHING BELONG LOGGED IN COMPANY ID ////////////////////////////
        reloadCaches();
    }
    
    private void reloadCaches(){
        labelServiceImpl.reloadCache();
    }
    
    private void reloadDeepCaches(){
        configServiceImpl.reloadCache();
    }
    
    private void callbackLoggedIn(){
        //Display first module after loggedin
        displayFirstModule();
        
        //////////////////////////// START RELOAD CACHING BELONG LOGGED IN COMPANY ID ////////////////////////////
        reloadCaches();
        reloadDeepCaches();
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void doLogin(boolean forceClose) throws IOException, ServletException, Exception {
        removeMessage();
        grantList.clear();
        String basicLoginId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!StringUtils.isBlank(this.username) && !StringUtils.isBlank(this.password)) {
            
            FacesContext faces = FacesContext.getCurrentInstance();
            ExternalContext context = faces.getExternalContext();
            RequestDispatcher dispatcher;
            ServletRequest request = (ServletRequest) context.getRequest();
            ServletResponse response = (ServletResponse) context.getResponse();
            
            String redirectCode = request.getParameter("r"); // r = redirect code
            PersitBean persitBean = ifvc.isValidToken(redirectCode);
            if(!StringUtils.isEmpty(redirectCode)){
                if(persitBean == null){
                    this.showLoginSessionAlert = null;
                    context.redirect(SecurityConfig.PAGE_LOGIN_URL);
                    System.out.println("Error: invalid token");
                    return;
                }else{
                    Member m = memberService.findByUsername(username, member.getCompanyId());
                    if(!persitBean.getMemberList().contains(m.getMemberId())){
                        this.showLoginSessionAlert = null;
                        context.redirect(SecurityConfig.PAGE_LOGIN_URL+"?r=" + redirectCode + "&error");
                        return;
                    }
                }
            }
            
            // chuyển sang cho SpringSecurity xử lí request chứa thông tin username và password.
            context.getFlash().setKeepMessages(true);
            dispatcher = request.getRequestDispatcher("/appLogin");
            dispatcher.forward(request, response);
            faces.responseComplete();
            
            // TODO: nên chuyển vào trong handler success của SPRING SECURITY.
            if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                this.member = UserModel.getLogined(); 
                if(forceClose){ //avoid all other sessions
                    sessionHandler.forceCloseIfExistsSession(this.member.getCompanyId(), this.member.getUsername());
                }
                grantList.addAll(companyService.findByCompanyBelongGroup(this.member.getCompany().getCompanyId()));
            }
            this.showLoginSessionAlert = null;
        } else {
            setCompany(basicLoginId);
            /** ログイン失敗した場合 */
            if (StringUtils.isBlank(this.username)) {
                JsfUtil.getResource().putErrors(this.member.getCompanyId(), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_ERROR_NAME, "label.errors.not.login", false);
            }else if (StringUtils.isBlank(this.password)) {
                JsfUtil.getResource().putErrors(this.member.getCompanyId(), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_ERROR_NAME, "label.errors.not.password", false);
            }
            this.showLoginSessionAlert = null;
        }
    }
    
    public void onPageLoad(){
        // remove all holding issue
        if(this.member != null && this.member.getCompany() != null && this.member.getUserId() != null){
            issueGlobalStatus.popAll(this.member.getCompanyId(), this.member.getUserId());
        }
    }

    private void displayFirstModule() {
        
        // remove all holding issue
        issueGlobalStatus.popAll(this.member.getCompanyId(), this.member.getUserId());
        
        String defaultPage = configServiceImpl.get("DEFAULT_PAGE");
        
        if (!StringUtils.isEmpty(defaultPage)) {
            try {
                StringBuilder command = new StringBuilder();
                command.append(StringUtils.uncapitalize(Class.forName(defaultPage).getSimpleName()));
                java.lang.reflect.Method[] methodList = Class.forName(defaultPage).getDeclaredMethods();
                for (final java.lang.reflect.Method method : methodList) {
                    if (method.isAnnotationPresent(SecureMethod.class)) {
                        SecureMethod m = method.getAnnotation(SecureMethod.class);
                        if (m.value() == SecureMethod.Method.INDEX) {
                            command.append(".").append(method.getName());
                            break;
                        }
                    }
                }
                JsfUtil.executeJsfCommand(command.toString());
            } catch (Exception e) {
            }
        }
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public String doLogout() throws IOException, ServletException {
        try {
            //close all session first
            sessionHandler.forceCloseIfExistsSession(this.member.getCompanyId(), this.member.getUsername());

            // remove all holding issue
            issueGlobalStatus.popAll(this.member.getCompanyId(), this.member.getUserId());

            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            RequestDispatcher dispatcher = ((ServletRequest) context.getRequest()).getRequestDispatcher("/appLogout");
            dispatcher.forward((ServletRequest) context.getRequest(), (ServletResponse) context.getResponse());
            FacesContext.getCurrentInstance().responseComplete();

            this.showLoginSessionAlert = null;
            this.memberService.updateLastLogoutTime(this.member.getUserId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public void setCompany(String basicId) {
        if( !StringUtils.isEmpty(basicId) && !"anonymousUser".equals(basicId)){
            Company com = this.companyService.findByCompanyBasicLoginId(basicId);
            setCompany(com);
            loadRemoteCompanyLogo();
        }
    }
    
    public void setCompany(Company com) {
        if(com != null){
            if (this.member == null) this.member = new UserModel();
            this.member.setCompany(com);
            this.member.setCompanyId(com.getCompanyId());
            this.member.setCompanyName(com.getCompanyName());
            this.member.setCompanyLogo(StringUtils.isBlank(com.getCompanyLogo()) ? null : com.getCompanyLogo());
            this.member.setCompanyCopyRight(com.getCompanyCopyRight());
            this.member.setCompanyBusinessFlag(com.getCompanyBusinessFlag());
            String layout = com.getCompanyLayout();
            member.setTheme(layout);
            this.background = LayoutUtil._getTheme(layout);
        }
    }
    
    public void loadRemoteCompanyLogo() {
        try {
            if(this.member == null) return;
            if(this.member.getCompany() == null) return;
            if(this.member.hasRemoteLogo()) return;
            Company c = this.member.getCompany();
            if(StringUtils.isEmpty(c.getCompanyLogo())) return;
            String cl = c.getCompanyLogo();
            List<Server> servers = serverService.getAvailable(c.getCompanyId(), TransferType.FTP.getType(), ServerFlag.COMMON.getId());
            if(servers == null || servers.isEmpty()) return;
            int att = AttachmentTargetType.COMPANY.getId();
            int attc = c.getCompanyId();
            List<Attachment> attachments = attachmentService.search(c.getCompanyId(), att, attc, (short)0);
            if(attachments == null || attachments.isEmpty()) return;
            
            Server server = getOneServer(servers);
            if(server == null) return;
            Attachment attachment = attachments.get(0);
            
            String host = server.getServerHost();
            int port = server.getServerPort();
            String ftpUsername = server.getServerUsername();
            String ftpPassword = server.getDecryptServerPassword();
            boolean security = StatusUtil.getBoolean(server.getServerSsl());
            String protocol = server.getServerProtocol();
            String servertype = server.getServerType();

            TransferType tt = TransferType.getTransferType(servertype);
            Parameter param_ftp = Parameter.getInstance(tt).manualconfig(true).storeDb(false);;
            param_ftp.host(host).port(port).username(ftpUsername).password(ftpPassword).security(security).protocol(protocol);
            
            String remotePath2File = attachment.getAttachmentPath() + File.separator + attachment.getAttachmentName();
            InputStream input = FileTransferFactory.getTransfer(param_ftp).download(remotePath2File);
            this.member.setStreamBytes(FileUtil.copyFromInputStream(input));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }
    
    private Server getOneServer(final List<Server> servers) {
        if(servers == null || servers.isEmpty()) return null;
        for(Server server : servers)
            if(server.getServerDeleted() == null || server.getServerDeleted() == 0) return server;
        return null;
    }
    
    /***
     * Hàm xử lí tải logo từ FTP server.
     */
    public void loadRemoteMemberImage() {
        try {
            if(this.member == null) return;
            
            List<Server> servers = serverService.getAvailable(this.member.getCompany().getCompanyId(), TransferType.FTP.getType(), ServerFlag.COMMON.getId());
            if(servers == null || servers.isEmpty()) return;
            
            int att = AttachmentTargetType.MEMBER.getId();
            int attc = this.member.getMember().getMemberId();
            List<Attachment> attachments = attachmentService.search(this.member.getCompanyId(), att, attc, (short) 0);
            if(attachments == null || attachments.isEmpty()) return;
            
            Server server = getOneServer(servers);
            if(server == null) return;
            Attachment attachment = attachments.get(0);
            
            String host = server.getServerHost();
            int port = server.getServerPort();
            String ftpUsername = server.getServerUsername();
            String ftpPassword = server.getDecryptServerPassword();
            boolean security = StatusUtil.getBoolean(server.getServerSsl());
            String protocol = server.getServerProtocol();
            String servertype = server.getServerType();
            
            TransferType tt = TransferType.getTransferType(servertype);
            Parameter param_ftp = Parameter.getInstance(tt).manualconfig(true).storeDb(false);;
            param_ftp.host(host).port(port).username(ftpUsername).password(ftpPassword).security(security).protocol(protocol);
            
            String remotePath2File = attachment.getAttachmentPath() + File.separator + attachment.getAttachmentName();
            InputStream input = FileTransferFactory.getTransfer(param_ftp).download(remotePath2File);
            this.member.setStreamBytesForMemberImage(FileUtil.copyFromInputStream(input));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }
    
    /**
     * Kiem tra user dang nhap vao cong ty nao trong TH group cong ty, sau do phan quyen cho user theo cong ty do
     * @return
     * @throws Exception 
     */
    public String checkAutoGrant() throws Exception{
        boolean autoGrant = this.grantList.isEmpty() || this.grantList.size() == 1;
        
        Short globalFlag = UserModel.getLogined().getMember().getMemberGlobalFlag();
        if(globalFlag == null || globalFlag == 0){
            autoGrant = true;
        }
        
        if(autoGrant){
            this.grantAccess(this.member.getCompany());
            return SecurityConfig.PAGE_INDEX_URL;
        }
        return null;
    }
    
    /**
     * Thuc hien phan quyen cho user dang nhap
     * 
     * @param company
     * @throws Exception 
     */
    public void grantAccess(Company company) throws Exception {
        // được lưu trữ trước tiên tránh gây lỗi khi sử dụng ISSUE-CONTROLLER.
        tenantHolder.setCompanyId(company.getCompanyId());
        Console.log("The company ID " + tenantHolder.getCompanyId() + " saved!");
        UserModel logined = UserModel.getLogined();
        
        // thông tin công ty đăng nhập bằng BASIC.
        int basicCompanyId = logined.getCompanyId();
        
        List<RoleModel> roleList = logined.getAuthorities();
        roleList.add(new RoleModel(ROLE_GRANT_ACCESS.name(), ROLE_GRANT_ACCESS.name()));
        
        // lấy roles từ công ty tại màn hình grant.
        List<MemberAuth> roleListRaw = memberService.getRoleListV2(logined.getUserId(), company.getCompanyId());
        for (MemberAuth r : roleListRaw) {
            String strRole = StringUtils.upperCase(String.format("ROLE_%s_%s", r.getMemberAuthPK().getPage(), r.getMemberAuthPK().getMethod()));
            String strRoleWithModule = StringUtils.upperCase(String.format("ROLE_%s_%s_%s", r.getMemberAuthPK().getModule(), r.getMemberAuthPK().getPage(), r.getMemberAuthPK().getMethod()));
            roleList.add(new RoleModel(r.toString(), strRole));
            roleList.add(new RoleModel(r.toString(), strRoleWithModule));
        }
        
        // sử dụng quyền mới theo công ty lựa chọn tại màn hình grant.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserModel actualLogined = (UserModel) auth.getPrincipal();
        
        // trường hợp người dùng đăng nhập vào công ty khác, hệ thống sẽ lưu trữ và
        // đánh dấu người dùng đó đăng đăng nhập vào công ty khác, phục vụ cho việc
        // phân quyền và kiểm tra lúc lưu xuống db.
        if(basicCompanyId != company.getCompanyId()) {
            actualLogined.setLoginedOrtherComapny(true);
        }
        
        // quyền mới với công ty lựa chọn từ màn hình grant.
        Authentication newAuth = new UsernamePasswordAuthenticationToken(actualLogined, auth.getCredentials(), roleList);
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        
        logined.setAuthorities(roleList);
        logined.setCompany(company);
        logined.setCompanyId(company.getCompanyId());
        this.member.setCompany(company);
        
        // TODO cần tách ra làm hàm riêng.
        FacesContext faces = FacesContext.getCurrentInstance();
        ExternalContext context = faces.getExternalContext();
        ServletRequest request = (ServletRequest) context.getRequest();
        String redirectCode = request.getParameter("r"); // r = redirect code
        PersitBean persitBean = ifvc.isValidToken(redirectCode);
        if (!StringUtils.isEmpty(redirectCode)) {
            if (persitBean == null) {
                this.showLoginSessionAlert = null;
                context.redirect(SecurityConfig.PAGE_LOGIN_URL);
                System.out.println("Error: invalid token");
                return;
            } else {
                Member m = memberService.findByUsername(username, member.getCompanyId());
                if (!persitBean.getMemberList().contains(m.getMemberId())) {
                    this.showLoginSessionAlert = null;
                    context.redirect(SecurityConfig.PAGE_LOGIN_URL + "?r=" + redirectCode + "&error");
                    return;
                }
            }
        }
        
        if (StringUtils.isEmpty(redirectCode) || persitBean == null) {
            callbackLoggedIn();
            context.redirect(SecurityConfig.PAGE_INDEX_URL + "?s="+JsfUtil.getManagedBean("layout", LayoutController.class).getCurrentEaseyEncrypt());
        } else {
            IssueForceViewController ifvc =  JsfUtil.getManagedBean("ifvc", IssueForceViewController.class);
            ifvc.execute(redirectCode);
        }
            
        loadRemoteMemberImage();
        loadRemoteCompanyLogo();
        
    }

    ///////////////////////////// 2 F A ///////////////////////////////////////    
    public void validate2FA() throws IOException {
        try {
            if(!TimeBasedOneTimePasswordUtil.validation(member.getMember(), code)) {
                JsfUtil.getResource().putErrors(this.member.getCompanyId(), ResourceUtil.BUNDLE_MSG, ResourceUtil.SEVERITY_WARN_NAME, "label.errors.twofactor", false);
                return;
            }
            // do login to website
            this.memberService.updateLastLoginTime(this.member.getMember());
            displayFirstModule();
            FacesContext.getCurrentInstance().getExternalContext().redirect(SecurityConfig.PAGE_INDEX_URL);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    public void updateMessages() throws Exception {
        Exception ex = (Exception) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                .get(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (ex != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ex.getMessage()));
        }
        removeMessage();
    }
    
    private void removeMessage(){
         Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        if (sessionMap.containsKey(WebAttributes.AUTHENTICATION_EXCEPTION)) {
            sessionMap.remove(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
    
    ////////////////////////////// O N E T I M E L O G I N //////////////////////////////
    public void checkSessionAllow(){
        try{
            this.showLoginSessionAlert = sessionHandler != null ? sessionHandler.isExists(this.member.getCompanyId(), StringUtil.generatePrefixLoginId(this.username, this.member.getCompanyId())) != null : false;
        }catch(NullPointerException e){
            this.showLoginSessionAlert = false;
        }
    }
}
