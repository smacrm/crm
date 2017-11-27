/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.system;

import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.model.search.SearchFilter;
import gnext.quartz.CrmJob;
import static org.quartz.JobBuilder.newJob;

import gnext.quartz.annotation.Job;
import gnext.quartz.annotation.JobParameter;
import gnext.quartz.annotation.Jobs;
import gnext.quartz.annotation.Trigger;
import gnext.quartz.proxy.JobModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.CompanyService;
import gnext.service.EntityService;
import gnext.service.attachment.AttachmentService;
import gnext.service.automail.AutoMailService;
import gnext.service.config.ConfigService;
import gnext.service.issue.IssueEscalationSampleService;
import gnext.service.issue.IssueService;
import gnext.service.label.LabelService;
import gnext.service.mail.MailAccountService;
import gnext.service.mail.MailDataService;
import gnext.util.ClassUtil;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.utils.Console;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ActionEvent;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Matcher;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "jobController", eager = true)
@ApplicationScoped
@SecurePage(module = SecurePage.Module.SYSTEM, require = false)
public class JobController extends AbstractController {
    private static final long serialVersionUID = 340277201467422229L;
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    
    @Getter @Setter private boolean performShutdown = true;
    @Getter @Setter private boolean waitOnShutdown = false;
    @Getter @Setter private Scheduler scheduler;
    @Getter @Setter private List<JobModel> models;
    @EJB private MailAccountService mailAccountService;
    @EJB private CompanyService companyService;
    @EJB private MailDataService mailDataService;
    @EJB private AttachmentService attachmentService;
    @EJB private ConfigService configService;
    @EJB private AutoMailService autoMailService;
    @EJB private IssueService issueService;
    @EJB private IssueEscalationSampleService issueEscalationSampleService;
    @EJB private LabelService labelService;
    
    private final Map<String, EntityService<?>> services = new HashMap<>();
    private final Map<String, Serializable> managedBeans = new HashMap<>();
    
    public static final String getIdentityTriggerName(String jobName) {
        return "TRIGGER-" + jobName + "-" + System.currentTimeMillis();
    }
    
    private void _PutServices() {
        services.clear();
        services.put("mailAccountService", mailAccountService);
        services.put("companyService", companyService);
        services.put("mailDataService", mailDataService);
        services.put("mailAttachmentService", attachmentService);
        services.put("configService", configService);
        services.put("autoMailService", autoMailService);
        services.put("issueService", issueService);
        services.put("issueEscalationSampleService", issueEscalationSampleService);
        services.put("labelService", labelService);
    }
    
    private StdSchedulerFactory _GetSchedulerFactory(String configFile) throws SchedulerException {
        StdSchedulerFactory factory;
        if (configFile != null) {
            factory = new StdSchedulerFactory(configFile);
        } else {
            factory = new StdSchedulerFactory();
        }
        return factory;
    }
    
    private JobKey _GetJob(JobModel model) {
        if (this.scheduler == null) return null;
        try {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(model.getGroup()))) {
                String jobName = jobKey.getName();
                if (jobName.equals(model.getName())) return jobKey;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    private boolean _PauseJob(JobModel model) {
        if (this.scheduler == null) return false;
        try {
            JobKey jobKey = _GetJob(model);
            if (jobKey != null) {
                scheduler.pauseJob(jobKey);
                model.setStopDate(DateUtil.now());
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
        return false;
    }
    
    private boolean _ResumeJob(JobModel model) {
        if (this.scheduler == null) return false;
        try {
            JobKey jobKey = _GetJob(model);
            if (jobKey != null) {
                scheduler.resumeJob(jobKey);
            } else {
                _StartJob(model);
            }
            model.setStopDate(null);
            model.setStartDate(DateUtil.now());
            
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
        return false;
    }
    
    private boolean _DeleteJob(JobModel model) {
        if (this.scheduler == null) return false;
        try {
            JobKey jobKey = _GetJob(model);
            if (jobKey != null) {
                scheduler.interrupt(jobKey);
                scheduler.deleteJob(jobKey);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
        return false;
    }
    
    private Date _ScheduleJob(Job job, Class<? extends CrmJob> clazz) {
        try {
            CrmJob crmJob = clazz.newInstance();
            JobDetail jobDetail = newJob(clazz).withIdentity(job.name(), job.group()).build();
            // parameters
            if (job.parameters() != null && job.parameters().length > 0) {
                for (JobParameter parameter : job.parameters()) {
                    jobDetail.getJobDataMap().put(parameter.key(), parameter.value());
                }
            }
            jobDetail.getJobDataMap().put("services", services);
            
            jobDetail.getJobDataMap().put("managedBeans", managedBeans);

            // trigger
            Trigger trigger = job.trigger();
            String triggerName = getIdentityTriggerName(job.name());
            org.quartz.Trigger quartzTrigger = null;
            if (trigger.useCron()) {
                // withMisfireHandlingInstructionDoNothing: All misfired executions are discarded, the scheduler simply waits for next scheduled time.
                // Example scenario: the executions scheduled at 9 and 10 AM are discarded, so basically nothing happens. The next scheduled execution (at 11 AM) runs on time.
                // quartzTrigger = newTrigger().withIdentity(triggerName, job.group()).withSchedule(cronSchedule(trigger.cron()).withMisfireHandlingInstructionDoNothing()).build();
                quartzTrigger = newTrigger().withIdentity(triggerName, job.group()).withSchedule(cronSchedule(trigger.cron())).build();
            } else {
                quartzTrigger = crmJob.buidTrigger();
            }

            // listeners
            if (job.listeners().length > 0) {
                for (String listener : job.listeners()) {
                    if (!StringUtils.isEmpty(listener)) {
                        listener = "gnext.quartz.listener." + listener;
                        Class<?> cl = Class.forName(listener);
                        JobListener jobListener = (JobListener) cl.newInstance();
                        Matcher<JobKey> matcher = KeyMatcher.keyEquals(jobDetail.getKey());
                        this.scheduler.getListenerManager().addJobListener(jobListener, matcher);
                    }
                }
            }
            // starting...
//            if(this.scheduler.checkExists(jobDetail.getKey())) this.scheduler.deleteJob(jobDetail.getKey());
            Date ft = this.scheduler.scheduleJob(jobDetail, quartzTrigger);
            Console.log(jobDetail.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: " + quartzTrigger.getDescription());
            return ft;
        } catch (Exception e) {
            Console.log(e);
            logger.error(e.getMessage(), e);
        }
        
        return null;
    }
    
    private void _StartJob(JobModel jobModel) {
        Job job = jobModel.getJob();
        Class<? extends CrmJob> clazz = jobModel.getClazz();
        Date startDate = _ScheduleJob(job, clazz);
        if (startDate != null) {
            jobModel.setStatus(JobModel.RUNNING);
            jobModel.setStartDate(startDate);
        }
    }
    
    private void _StartJob(Job job, Class<? extends CrmJob> clazz) {
        try {
            JobModel jobModel = new JobModel(models.size() + 1, job.trigger(), job);
            jobModel.setClazz(clazz);
            jobModel.setStatus(JobModel.PAUSE);
            models.add(jobModel);
            if (job.startOnLoad() && !job.disable()) {
                Date startDate = _ScheduleJob(job, clazz);
                if (startDate != null) {
                    jobModel.setStatus(JobModel.RUNNING);
                    jobModel.setStartDate(startDate);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    private void _LoadAllJob() {
        if (models == null) {
            models = new ArrayList<>();
        } else {
            models.clear();
        }
        
        if (this.scheduler == null) return;
        try {
            List<Class<?>> classs = ClassUtil.getClasses("gnext.quartz.jobs");
            for (Class<?> clazz : classs) {
                Annotation[] annotations = clazz.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(Job.class)) {
                        Job job = clazz.getAnnotation(Job.class);
                        _StartJob(job, (Class<? extends CrmJob>) clazz);
                    } else if (annotation.annotationType().equals(Jobs.class)) {
                        Jobs jobs = clazz.getAnnotation(Jobs.class);
                        for (Job job : jobs.jobs()) {
                            _StartJob(job, (Class<? extends CrmJob>) clazz);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Console.log(e);
            logger.error(e.getMessage(), e);
        }
    }
    
    @PostConstruct
    public void init() {
        if(this.scheduler != null) return;
        try {
            StdSchedulerFactory factory = _GetSchedulerFactory(null);
            // Always want to get the scheduler, even if it isn't starting, 
            // to make sure it is both initialized and registered.
            scheduler = factory.getScheduler();
            // start up the scheduler (jobs do not start to fire until
            // the scheduler has been started)
            scheduler.start();
            _PutServices();
            _LoadAllJob();
        } catch (Exception e) {
            logger.error("Quartz Scheduler failed to initialize: " + e.toString());
        }
    }
    
    @PreDestroy
    public void destroy() {
        if (!performShutdown) return;
        try {
            if (scheduler != null) scheduler.shutdown(waitOnShutdown);
            logger.info("Quartz Scheduler successful shutdown.");
        } catch (Exception e) {
            logger.error( "Quartz Scheduler failed to shutdown cleanly: " + e.toString());
        }
    }
    
    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        LayoutController layout = JsfUtil.getManagedBean("layout", LayoutController.class);
        layout.setCenter("/modules/system/quartz/index.xhtml");
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void pause(ActionEvent event) {
        try {
            int jobId = Integer.parseInt(getParameter("jobId"));
            for (JobModel model : models) {
                if (model.getJobId() == jobId) {
                    if (_PauseJob(model))  model.setStatus(JobModel.PAUSE);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void resume(ActionEvent event) {
        try {
            int jobId = Integer.parseInt(getParameter("jobId"));
            for (JobModel model : models) {
                if (model.getJobId() == jobId) {
                    if (_ResumeJob(model)) model.setStatus(JobModel.RUNNING);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.DELETE, require = false)
    @Override
    public void delete(ActionEvent event) {
        try {
            int jobId = Integer.parseInt(getParameter("jobId"));
            for (JobModel model : models) {
                if (model.getJobId() == jobId) {
                    if (_DeleteJob(model)) model.setStatus(JobModel.DELETED);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void pauseAll(ActionEvent event) {
        try {
            for (JobModel model : models) {
                if (_PauseJob(model)) model.setStatus(JobModel.PAUSE);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.SEARCH)
    public void reload() {
        try {
            for (JobModel model : models) _DeleteJob(model);
            _LoadAllJob();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
}
