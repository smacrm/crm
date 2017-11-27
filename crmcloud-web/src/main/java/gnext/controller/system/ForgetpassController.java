/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.system;

import gnext.bean.CompanyTargetInfo;
import gnext.bean.Member;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.dbutils.model.MailData;
import gnext.dbutils.util.FileUtil;
import gnext.mailapi.EmailBuilder;
import gnext.mailapi.mail.SendEmail;
import gnext.mailapi.util.InterfaceUtil;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.CompanyTargetInfoService;
import gnext.service.MemberService;
import gnext.service.config.ConfigService;
import gnext.util.JsfUtil;
import gnext.mailapi.util.MailUtil;
import gnext.security.SecurityConfig;
import gnext.util.StatusUtil;
import gnext.util.StringUtil;
import gnext.utils.EncoderUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;

/**
 *
 * @author havd
 */
@ManagedBean(name = "forgetpassController")
@SessionScoped
@SecurePage(module = SecurePage.Module.SYSTEM, require = false)
public class ForgetpassController extends AbstractController implements Serializable {
    private static final long serialVersionUID = -6721085306061669349L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ForgetpassController.class);
    
    @ManagedProperty(value = "#{layout}") @Getter @Setter private LayoutController layout;
    
    @Getter @Setter private String username;
    @Getter @Setter private String password;
    @Getter @Setter private String passwordConfirm;
    
    @EJB MemberService memberService;
    @EJB ConfigService configService;
    @EJB CompanyTargetInfoService companyTargetInfoService;
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        layout.setCenter("forgotpassword.xhtml");
    }
    @PostConstruct
    public void init() {
    }
    
    public void back() throws IOException{
        FacesContext.getCurrentInstance().getExternalContext().redirect(SecurityConfig.PAGE_LOGIN_URL);
    }
    
    public void forgetPassword() throws IOException {
        try {
            String companyBasicLoginId = SecurityContextHolder.getContext().getAuthentication().getName();
            Object[] o = memberService.findBasicAuth(companyBasicLoginId);
            String usernamePrefix = "";
            if(o != null){
                int companyId = NumberUtils.toInt(o[2].toString());
                usernamePrefix = StringUtil.generatePrefixLoginId(this.username, companyId);
            }
            Member member = memberService.findByUsername(usernamePrefix);
            if (member != null) {
                String passwordRandom = getRandomPassword();

                // gửi mail thông báo mật khẩu mới.
                if(!sendMail(member,passwordRandom)) return;

                // cập nhật mật khẩu mới.
                member.setMemberPassword(EncoderUtil.getPassEncoder().encode(passwordRandom));
                member.setMemberResetPwdDatetime(Calendar.getInstance().getTime());
                memberService.edit(member);

                // chuyển tới trang login.xhtml.
                FacesContext facesContext = FacesContext.getCurrentInstance();
                removeMessage(facesContext);
                JsfUtil.addSuccessMessage(validationBundle.getString("validator.forgetpass.sentmail.success"));
                ExternalContext externalContext = facesContext.getExternalContext();    
                externalContext.getFlash().setKeepMessages(true);
                externalContext.redirect(externalContext.getRequestContextPath() + SecurityConfig.PAGE_LOGIN_URL);
            }else{
                focusUsernameConfirmValidate();
                JsfUtil.addErrorMessage(validationBundle.getString("validator.forgetpass.account.notfound"));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    public void resetPassword() {
        try {
            if (!validatePassword())  return;
        
            String companyBasicLoginId = SecurityContextHolder.getContext().getAuthentication().getName();
            Member member = memberService.findByUsername(companyBasicLoginId);

            if (member != null) {
                member.setMemberPassword(EncoderUtil.getPassEncoder().encode(password));
                member.setMemberResetPwdDatetime(null);
                memberService.edit(member);                                                                                                                                                                                     

                FacesContext facesContext = FacesContext.getCurrentInstance();
                removeMessage(facesContext);
                JsfUtil.addSuccessMessage(validationBundle.getString("validator.forgetpass.reset.success"));
                ExternalContext externalContext = facesContext.getExternalContext();    
                externalContext.getFlash().setKeepMessages(true);
                externalContext.redirect(externalContext.getRequestContextPath() + SecurityConfig.PAGE_LOGIN_URL);                                                                               
            } else {
                focusUsernameConfirmValidate();
                JsfUtil.addErrorMessage(validationBundle.getString("validator.forgetpass.account.notfound"));
            }           
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    private void removeMessage(FacesContext facesContext) {
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        if (sessionMap.containsKey(WebAttributes.AUTHENTICATION_EXCEPTION)) {
            sessionMap.remove(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
    
    private String getRandomPassword(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        return RandomStringUtils.random( 6, characters );
    }
    
    
    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * hàm gửi mail sử dụng mail api.
     * @param member
     * @param passwordRandom
     * @return 
     */
    private boolean sendMail(Member member, String passwordRandom) {
        try {
            Properties properties = FileUtil.loadConf(gnext.utils.StringUtil.DEFAULT_DB_PROPERTIES);
            List<CompanyTargetInfo> memberTargetInfosEmail = companyTargetInfoService
                    .find(CompanyTargetInfo.COMPANY_TARGET_MEMBER, member.getMemberId(),
                            CompanyTargetInfo.COMPANY_FLAG_TYPE_EMAIL, StatusUtil.UN_DELETED);
            String to = null;
            if (memberTargetInfosEmail != null && memberTargetInfosEmail.size() > 0) {
                to = memberTargetInfosEmail.get(0).getCompanyTargetData();
            }
            if (StringUtils.isEmpty(to)) {
                focusUsernameConfirmValidate();
                JsfUtil.addErrorMessage(validationBundle.getString("validator.forgetpass.to.invalid"));
                return false;
            }
            EmailBuilder<SendEmail> eb = new EmailBuilder<>(new SendEmail(), InterfaceUtil.Type.SMTP);
            eb.host(properties.getProperty("admin.host")).port(Integer.parseInt(String.valueOf(properties.get("admin.port"))))
                    .username((String) properties.get("admin.user")).password((String) properties.get("admin.pwd"))
                    .ssl(true).auth(true);
            SendEmail se = eb.builder();
            se.setFrom((String) properties.get("admin.from"));
            se.setRecipient(new String[]{to});
            se.setSubject("Gnext change your password");
            se.setMessage("Your new password: " + passwordRandom);
            se.setPriority("1");
            se.setContentType("text/html;charset=utf-8");
            MailData mailSent = MailUtil._Alert(se);
            if (mailSent != null) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            JsfUtil.addErrorMessage(e.getLocalizedMessage());
        }
        return false;
    }

    private boolean validatePassword() {
        Pattern upperCasePatten = Pattern.compile("[A-Z ]");
        Pattern lowerCasePatten = Pattern.compile("[a-z ]");
        Boolean flag = true;
        if (this.password.length() < 6) {
            focusPasswordValidate();
            JsfUtil.addErrorMessage(validationBundle.getString("validator.forgetpass.password.invalid"));
            flag =false;
        }
        if (this.passwordConfirm.length() < 6) {
            JsfUtil.addErrorMessage(validationBundle.getString("validator.forgetpass.password.invalid"));
            focusPasswordConfirmValidate();
            flag =false;
        }
        if(!upperCasePatten.matcher(password).find()
                ||!lowerCasePatten.matcher(password).find()){
            focusPasswordValidate();
            JsfUtil.addErrorMessage(validationBundle.getString("validator.forgetpass.password.invalid"));
            flag =false;
        }
        if (!upperCasePatten.matcher(passwordConfirm).find()
                || !lowerCasePatten.matcher(passwordConfirm).find()) {
            focusPasswordConfirmValidate();
            JsfUtil.addErrorMessage(validationBundle.getString("validator.forgetpass.password.invalid"));
            flag = false;
        }
        if (!this.password.equals(this.passwordConfirm)) {
            focusPasswordValidate();
            focusPasswordConfirmValidate();
            JsfUtil.addErrorMessage(validationBundle.getString("validator.forgetpass.password.incorrect"));
            flag =false;
        }
        return flag;
    }
    
    private void focusPasswordValidate() {
        UIComponent component = JsfUtil.findComponent("j_password");
        if(component == null) return;
        UIInput uiInput = (UIInput) component;
        uiInput.setValid(false);
    }

    private void focusPasswordConfirmValidate() {
        UIComponent component = JsfUtil.findComponent("j_password_confirm");
        if(component == null) return;
        UIInput uiInputPassConfir = (UIInput) component;
        uiInputPassConfir.setValid(false);
    }
    
    private void focusUsernameConfirmValidate() {
        UIComponent component = JsfUtil.findComponent("j_username");
        if(component == null) return;
        UIInput uiInputPassConfir = (UIInput) component;
        uiInputPassConfir.setValid(false);
    }
    
}
