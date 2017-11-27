package gnext.controller.system;

import gnext.bean.issue.Issue;
import gnext.controller.common.LayoutController;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.issue.IssueService;
import gnext.util.JsfUtil;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Modifier
 *
 * @author hungpham
 * @since 2017/06
 */
@ManagedBean(name = "dataModifyController")
@SessionScoped
@SecurePage(module = SecurePage.Module.SYSTEM)
public class DataModifyController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(DataModifyController.class);

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @EJB private IssueService issueService;
    
    private int companyId;
    
    @PostConstruct
    public void init(){
        companyId = UserModel.getLogined().getCompanyId();
    }
    
    /**
     * Search and reset quick search data for current company
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void resetQuickSearch(){
        try {
            List<Issue> issues = issueService.findByCompanyId(companyId);
            for(Issue item : issues) {
                issueService.edit(item);
            }
            JsfUtil.addSuccessMessage("Process " + (issues.size()) + " items");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * Search and reset quick search data for all companies
     */
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void resetQuickSearchAll(){
        try {
            List<Issue> issues = issueService.findAll();
            for(Issue item : issues) {
                issueService.edit(item);
            }
            JsfUtil.addSuccessMessage("Process " + (issues.size()) + " items");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load(){
        layout.setCenter("/modules/system/data/dmc.xhtml");
    }
}
