/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail;

import gnext.bean.mail.MailData;
import gnext.bean.mail.MailExplode;
import gnext.controller.AbstractController;
import gnext.controller.issue.IssueController;
import gnext.controller.mail.parse.StandardMailParse;
import gnext.bean.issue.Issue;
import gnext.model.mail.MailDataModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecurePage;
import gnext.service.mail.MailDataService;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "mail2IssueController", eager = true)
@RequestScoped()
@SecurePage(module = SecurePage.Module.MAIL, require = false)
public class Mail2IssueController extends AbstractController<MailDataModel> {
    private static final long serialVersionUID = -8365212757437363227L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Mail2IssueController.class);
    
    @ManagedProperty(value = "#{issueController}")
    @Getter @Setter private IssueController issueController;
    @EJB @Getter @Setter private MailDataService mailDataService;
    
    @SuppressWarnings("UnusedAssignment")
    public void processExplodeMail(Integer maildataid) {
        try {
            MailData _md = mailDataService.searchById(getCurrentCompanyId(), maildataid);
            List<MailExplode> explodes = mailDataService.searchExplodes(_md);
            
            Map<String, List<SelectItem>> select = issueController.getSelect();
            if(select == null || select.size() <= 0) {
                issueController.loadContextData();
                select = issueController.getSelect();
            }
            
            Issue issue =  new StandardMailParse().parse(_md.getMailDataBody(), explodes);
            issue.setMailData(_md);
            issueController.createIssueEmail(issue);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
