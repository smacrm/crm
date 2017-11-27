/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.system;

import com.google.gson.Gson;
import gnext.bean.job.Batch;
import gnext.bean.job.Command;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.model.authority.UserModel;
import gnext.model.search.SearchFilter;
import gnext.quartz.cron.Cron;
import gnext.quartz.cron.CronScheduleBuilder;
import gnext.quartz.jobs.manual.ManualJob;
import gnext.quartz.listener.trigger.ManualTriggerListener;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.attachment.ServerService;
import gnext.service.job.BatchService;
import gnext.service.job.CommandService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import gnext.utils.Console;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import static org.quartz.impl.matchers.KeyMatcher.keyEquals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "batchController", eager = true)
@ApplicationScoped
@SecurePage(module = SecurePage.Module.SYSTEM)
public class BatchController extends AbstractController {
    private static final long serialVersionUID = -150419742742995271L;
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchController.class);

    @EJB private BatchService batchService;
    @EJB private CommandService commandService;
    @EJB private ServerService serverService;
    
    @Getter @Setter private Scheduler scheduler;
    @Getter @Setter private Batch batch;
    
    @Getter @Setter private List<Batch> batchs;
    @Setter private List<Batch> companyBatchs;
    
    @Getter @Setter private List<Command> commands;
    @Getter @Setter private Map<Batch, Map<JobDetail,Trigger>> jobs;
    @Getter @Setter private boolean performShutdown = true;
    @Getter @Setter private boolean waitOnShutdown = false;
    private LayoutController layout;
    private Batch find(int batchId) {
        if(batchs == null || batchs.isEmpty()) return null;
        for(Batch b : batchs) {
            if(b.getBatchId() == batchId)  return b;
        }
        return null;
    }
    
    /**
     * Hàm resoled tất cả các ejb service vào mapping.
     * @return 
     */
    private Map<String, Object> serviceResoled() {
        Map<String, Object> services = new HashMap();
        services.put("batchService", batchService);
        services.put("commandService", commandService);
        services.put("serverService", serverService);
        return services;
    }
    
    private void printAllJobGroupName() throws Exception {
        if (this.scheduler == null) return;
        if(this.scheduler.getJobGroupNames() == null) return;
        for(String jobGroupName : this.scheduler.getJobGroupNames()) {
            Console.log("Job Name: " + jobGroupName);
        }
    }
    
    private JobKey findJobKey(Batch b) throws Exception {
        if (this.scheduler == null) return null;
        if (b == null) return null;
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(b.getBatchGroup()))) {
            String jobName = jobKey.getName();
            if (jobName.equals(b.getBatchName())) return jobKey;
        }
        return null;
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void pause(ActionEvent event) {
        if(scheduler == null) return;
        int batchId = Integer.parseInt(getParameter("batchId"));
        Batch b = find(batchId);
        if(b == null) return;
        try {
            JobKey jobKey = findJobKey(b);
            if (jobKey == null) return;
            scheduler.pauseJob(jobKey);
            b.setBatchStatus(Batch.PAUSE);
            b.setPauseDate(DateUtil.now());
        } catch (Exception e) {
            b.setBatchStatus(Batch.FAILED);
            LOGGER.error(e.getMessage(), e);
        }
        try { batchService.edit(b);  } catch (Exception e) { }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void resume(ActionEvent event) {
        if(scheduler == null) return;
        int batchId = Integer.parseInt(getParameter("batchId"));
        Batch b = find(batchId);
        if(b == null) return;
        try {
            // tìm kiếm nếu scheduler đang pause job thì resume job đó.
            JobKey jobKey = findJobKey(b);
            if(jobKey != null) {
                scheduler.resumeJob(jobKey);
            // nếu không cần kiểm tra trong danh sách jobs đã tải nếu có thì start job lên.    
            } else if(jobs.containsKey(b)) {
                Map<JobDetail, Trigger> details = jobs.get(b);
                if(details == null || details.isEmpty()) return;
                Iterator it = details.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<JobDetail, Trigger> pair = (Map.Entry<JobDetail, Trigger>) it.next();
                    startJob(b, pair.getKey(), pair.getValue());
                    return;
                }
            // nếu không tồn tại ở bất kì đâu cần cho scheuler và controller biết về job này.    
            } else {
                loadBatch(b, true);
            }
            b.setBatchStatus(Batch.RUNNING);
            b.setStartDate(DateUtil.now());
        } catch (Exception e) {
            b.setBatchStatus(Batch.FAILED);
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
        try { batchService.edit(b);  } catch (Exception e) { }
    }
    
    private void prepareViewPage() {
        batch = new Batch();
        commands = commandService.findByCompanyId(getCurrentCompanyId());
    }
    
    private void prepareEditPage() {
        final int batchId = Integer.parseInt(this.getParameter("batchId"));
        Batch b = find(batchId);
        if(b == null) return;
        batch = b; // tham chiếu tới batch được chon.
        commands = commandService.findByCompanyId(getCurrentCompanyId());
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    @Override public void show(ActionEvent event) {
        final String showType = this.getParameter("showType");
        if("addNew".equals(showType)) {
            prepareViewPage();
            this.layout.setCenter("/modules/system/batch/create.xhtml");
        } else if("edit".equals(showType)) {
            prepareEditPage();
            this.layout.setCenter("/modules/system/batch/edit.xhtml");
        }
    }
    
    private boolean validation(Batch b) {
        String cron = b.getBatchCron();
        boolean c = org.quartz.CronExpression.isValidExpression(cron);
        if(!c) {
            JsfUtil.addErrorMessage(validationBundle.getString("validator.cron.invalid", b.getBatchName()));
            return false;
        }
        if(b.getBatchId() != null) {
            
        }
        if(b.getBatchId() == null) {
            
        }
        return true;
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    @Override public void save(ActionEvent event) {
        try {
            if(batch == null) return;
            batch.setCompany(getCurrentCompany());
            batch.setCreator(UserModel.getLogined().getMember());
            batch.setBatchDeleted((short) 0);
            batch.setBatchFlag((short) 0);
            batch.setBatchStatus(Batch.NOTSTARTED);
            batch.setCreatedTime(DateUtil.now());
            batch.setBatchFlag(batch.isBooleanBatchFlag()?(short)1:(short)0);
            if(!StringUtils.isEmpty(batch.getBatchCommand())) {
                batch.setCommand(commandService.find(Integer.parseInt(batch.getBatchCommand())));
            }
            generateCron(); if(!validation(batch)) return;
            batchService.create(batch);
            batchs.add(batch);
            load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.create.success", batch.getBatchName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    @Override public void update(ActionEvent event) {
        try {
            if(batch == null) return;
            batch.setUpdatedTime(DateUtil.now());
            batch.setUpdator(UserModel.getLogined().getMember());
            batch.setBatchFlag(batch.isBooleanBatchFlag()?(short)1:(short)0);
            if(!StringUtils.isEmpty(batch.getBatchCommand())) {
                batch.setCommand(commandService.find(Integer.parseInt(batch.getBatchCommand())));
            }
            generateCron(); if(!validation(batch)) return;
            batchService.edit(batch);
            
            runAfterUpdate();
            
            load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.update.success", batch.getBatchName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }
    
    @SecureMethod(SecureMethod.Method.DELETE)
    @Override
    public void delete(ActionEvent event) {
        try {
             if(scheduler == null) return;
            int batchId = Integer.parseInt(getParameter("batchId"));
            Batch b = find(batchId);
            if(b == null) return;
            
            JobKey jobKey = findJobKey(b);
            if (jobKey != null) {
                scheduler.interrupt(jobKey);
                scheduler.deleteJob(jobKey);
            }
            batchService.remove(b);
            if(jobs != null && jobs.containsKey(b)) jobs.remove(b);
            if(batchs != null && batchs.contains(b)) batchs.remove(b);
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(getCurrentCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.delete.success", b.getBatchName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }
    
    /**
     * Hàm phục vụ cho việc khi cập nhật thông tin cho BATCH.
     * sau khi cập nhật cần xóa bỏ job cũ và execute theo BATCH mới sửa.
     */
    private void runAfterUpdate() throws Exception {
        // Xóa job hiện thời đang chạy.
        JobKey jobKey = findJobKey(batch);
        if (jobKey != null) {
            scheduler.interrupt(jobKey);
            scheduler.deleteJob(jobKey);
        }
        // Chạy lại job
        if(jobs != null && jobs.containsKey(batch)) jobs.remove(batch);
        loadBatch(batch, true);
    }
    
    private void generateCron() {
        Cron cron = new Gson().fromJson(batch.getBatchCronJson(), Cron.class);
        batch.setBatchCron(CronScheduleBuilder.generate(cron));
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public String viewStatus(int batchId) {
        if(batchs == null || batchs.isEmpty()) return StringUtils.EMPTY;
        Batch b = find(batchId);
        if(b == null) return StringUtils.EMPTY;
        int status = b.getBatchStatus();
        if(status == Batch.NOTSTARTED) return "NOTSTARTED";
        if(status == Batch.FAILED) return "FAILED";
        if(status == Batch.PAUSE) return "PAUSE";
        if(status == Batch.RUNNING) return "RUNNING";
        return StringUtils.EMPTY;
    }
    
    private StdSchedulerFactory getInstanceScheduler(String configFile) throws SchedulerException {
        StdSchedulerFactory factory = null;
        if (configFile != null) factory = new StdSchedulerFactory(configFile); else factory = new StdSchedulerFactory();
        return factory;
    }
    
    private String getIdentityTriggerName(String jobName) {
        return "TRIGGER-" + jobName + "-" + System.currentTimeMillis();
    }
    
    private void lookupBatch() {
        batchs = batchService.findAll();
        if(batchs == null) batchs = new ArrayList<>();
    }
    
    private void initJobs() throws Exception {
        lookupBatch();
        if(jobs == null) jobs = new HashMap<>();
        for(Batch b : batchs) {
            loadBatch(b, StatusUtil.getBoolean(b.getBatchFlag()));
        }
    }
    
    private void loadBatch(Batch p_batch, boolean start) throws Exception {
        String jn = p_batch.getBatchName();
        String jg = p_batch.getBatchGroup();
        String jc = p_batch.getBatchCron();
        String tn = getIdentityTriggerName(jn);
        
        // create a instance for ManualJob
        JobDetail jobDetail = newJob(ManualJob.class).withIdentity(jn, jg).build();
        jobDetail.getJobDataMap().put(ManualJob.BATCH_PARAMETER, p_batch);
        jobDetail.getJobDataMap().put(ManualJob.SERVICE_PARAMETER, serviceResoled());
        
        // create a trigger for this job
        
        // withMisfireHandlingInstructionDoNothing: All misfired executions are discarded, the scheduler simply waits for next scheduled time.
        // Example scenario: the executions scheduled at 9 and 10 AM are discarded, so basically nothing happens. The next scheduled execution (at 11 AM) runs on time.
        // Trigger jobTrigger = newTrigger().withIdentity(tn, jg).withSchedule(cronSchedule(jc).withMisfireHandlingInstructionDoNothing()).build();
        Trigger jobTrigger = newTrigger().withIdentity(tn, jg).withSchedule(cronSchedule(jc)).build();
        scheduler.getListenerManager().addTriggerListener(new ManualTriggerListener(tn), keyEquals(triggerKey(tn, jg)));
        
        Map<JobDetail, Trigger> data = new HashMap();
        data.put(jobDetail, jobTrigger);
        jobs.put(p_batch, data);
        
        if(!start) return;
        startJob(p_batch, jobDetail, jobTrigger);
    }
    
    private boolean startJob(Batch b, JobDetail jobDetail, Trigger jobTrigger) {
        if(scheduler == null) return false;
        try {
            Date ft = this.scheduler.scheduleJob(jobDetail, jobTrigger);
            b.setStartDate(ft);
            b.setBatchStatus(Batch.RUNNING);
        } catch (Exception e) {
            b.setBatchStatus(Batch.FAILED);
        }
        try { batchService.edit(b);  } catch (Exception e) { }
        return true;
    }

    /**
     * Lấy theo công ty đăng nhập.
     * @return 
     */
    public List<Batch> getCompanyBatchs() {
        if(batchs == null || batchs.isEmpty()) return new ArrayList();
        
        companyBatchs = new ArrayList<>();
        for(Batch b : batchs) {
            if(b.getCompany().getCompanyId().intValue() == getCurrentCompanyId().intValue()) {
                companyBatchs.add(b);
            }
        }
        
        return companyBatchs;
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    @PostConstruct
    public void init() {
        if(this.scheduler != null) return;
        try {
            StdSchedulerFactory factory = getInstanceScheduler(null);
            // Always want to get the scheduler, even if it isn't starting, 
            // to make sure it is both initialized and registered.
            scheduler = factory.getScheduler();
            // start up the scheduler (jobs do not start to fire until
            // the scheduler has been started)
            scheduler.start();
            initJobs();
        } catch (Exception e) {
            LOGGER.error("Quartz Scheduler failed to initialize: " + e.toString());
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    @PreDestroy
    public void destroy() {
        if (!performShutdown) return;
        try {
            if (scheduler != null) scheduler.shutdown(waitOnShutdown);
            LOGGER.info("Quartz Scheduler successful shutdown.");
            if(batchs == null || batchs.isEmpty()) return;
            for(Batch b : batchs) {
                b.setBatchStatus(Batch.NOTSTARTED);
                b.setPauseDate(DateUtil.now());
                batchService.edit(b);
            }
        } catch (Exception e) {
            LOGGER.error( "Quartz Scheduler failed to shutdown cleanly: " + e.toString());
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        layout = JsfUtil.getManagedBean("layout", LayoutController.class);
        layout.setCenter("/modules/system/batch/index.xhtml");
    }
    
    @SecureMethod(value = SecureMethod.Method.SEARCH, require = false)
    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public boolean allowPause(int batchid) {
        Batch b = find(batchid);
        if(b == null) return false;
        return b.getBatchStatus() == Batch.RUNNING;
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public boolean allowResume(int batchid) {
        Batch b = find(batchid);
        if(b == null) return false;
        return (b.getBatchStatus() == Batch.NOTSTARTED || b.getBatchStatus() == Batch.PAUSE  || b.getBatchStatus() == Batch.FAILED);
    }
}
