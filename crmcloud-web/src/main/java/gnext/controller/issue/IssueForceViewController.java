package gnext.controller.issue;

import gnext.controller.common.LoginController;
import gnext.controller.issue.bean.PersitBean;
import gnext.security.SecurityConfig;
import gnext.security.annotation.SecurePage;
import gnext.service.MemberService;
import gnext.util.JsfUtil;
import gnext.util.ObjectUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Mar 8, 2017
 */
@ManagedBean(name = "ifvc")
@SessionScoped()
@SecurePage(module = SecurePage.Module.NONE, require = false)
public class IssueForceViewController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(IssueForceViewController.class);

    @EJB @Getter @Setter
    private MemberService memberService;

    public void execute(String encryptCode) throws ServletException, IOException {
        PersitBean bean = isValidToken(encryptCode);
        LoginController loginController = JsfUtil.getManagedBean("loginController", LoginController.class);
        
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        StringBuilder redirectCode = new StringBuilder();
        if (bean != null) {
            if (loginController.getMember() == null || StringUtils.isEmpty(loginController.getMember().getUsername())) {
                redirectCode.append("?r=");
                redirectCode.append(encryptCode);
            }else if(bean.getMemberList().contains(loginController.getMember().getUserId())){
                IssueController issueController = JsfUtil.getManagedBean("issueController", IssueController.class);
                issueController.show(bean.getIssueId());
                context.redirect(issueController.outcome(bean.getIssueId())); return;
            }else{
                redirectCode.append("?r=");
                redirectCode.append(encryptCode);
                redirectCode.append("&error");
            }
        }
        
        loginController.setShowLoginSessionAlert(null);
        context.redirect(String.format("%s%s", SecurityConfig.PAGE_LOGOUT_PROCESS, redirectCode.toString()));
    }

    public PersitBean isValidToken(String encryptCode) {
        if(StringUtils.isEmpty(encryptCode)) return null;
        PersitBean bean = null;
        try {
            bean = ObjectUtil.deserializeObjectFromString(encryptCode, PersitBean.class);
        } catch (Exception e) {}
        if (bean == null) return null;
        if (bean.getCompanyId() == null) return null;
        if (bean.getIssueId() == null) return null;
        if (bean.getExpiredDate() != null) {
            long secs = ((new Date()).getTime() - bean.getExpiredDate().getTime());
            if (secs > 0) return null;
        }
        if (bean.getMemberList().isEmpty()) return null;
        return bean;
    }

}