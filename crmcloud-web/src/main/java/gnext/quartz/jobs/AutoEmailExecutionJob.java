package gnext.quartz.jobs;

import gnext.bean.Company;
import gnext.bean.automail.SimpleAutoMail;
import gnext.quartz.ConcurrentExecutionJob;
import gnext.quartz.annotation.Job;
import gnext.quartz.annotation.Trigger;
import gnext.service.CompanyService;
import gnext.service.automail.AutoMailService;
import gnext.service.config.ConfigService;
import gnext.service.issue.IssueEscalationSampleService;
import gnext.service.issue.IssueService;
import gnext.service.label.LabelService;
import gnext.service.mail.MailAccountService;
import java.util.List;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpd
 */
@Job(name = "AutoEmailExecutionJob", group = "AUTO_MAIL", startOnLoad = true,
        trigger = @Trigger(cron = "0 0 * * * ?", description = "Auto send mail for all companies every 1 hours at 0 minute, 0 second"),
        description = "")
public class AutoEmailExecutionJob extends ConcurrentExecutionJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoEmailExecutionJob.class);
    
    private CompanyService companyService;
    private ConfigService configService;
    private MailAccountService mailAccountService;
    private AutoMailService autoMailService;
    private IssueService issueService;
    private IssueEscalationSampleService issueEscalationSampleService;
    
    private LabelService labelService;

    @Override
    protected void run(JobExecutionContext jec, JobDataMap parameters) {
        System.out.println("=== Start Auto ===");
        if (_checkServices(parameters)) {
            
            // tìm tất cả các công ty trên MasterDb.
            if(companyService == null) return;
            List<Company> companys = companyService.findAll();
            if(companys == null || companys.isEmpty()) return;
            
            for(Company company : companys) {
                if(company.isDeleted()) continue;
                
                List<SimpleAutoMail> data = autoMailService.findRequiredSendWithNoIssue(company.getCompanyId());
                if(data == null || data.isEmpty()) continue;
                
                for(SimpleAutoMail item: data) {
                    try {
                        gnext.controller.mail.AutoMailService.run(item, configService, mailAccountService, autoMailService, issueService, issueEscalationSampleService, labelService);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
            
        } else {
            LOGGER.error("AutoMailService: no service");
        }
    }

    private boolean _checkServices(final JobDataMap parameters) {
        companyService = getService(parameters, "companyService");
        configService = getService(parameters, "configService");
        autoMailService = getService(parameters, "autoMailService");
        mailAccountService = getService(parameters, "mailAccountService");
        issueService = getService(parameters, "issueService");
        issueEscalationSampleService = getService(parameters, "issueEscalationSampleService");
        labelService = getService(parameters, "labelService");
        return autoMailService != null && configService != null && mailAccountService != null && issueService != null && issueEscalationSampleService != null && labelService != null;
    }

    @Override
    protected void stop() {
        System.out.println("Stop Auto send mail => " + autoMailService);
    }
}
