/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.jobs;

import gnext.controller.system.JobController;
import gnext.quartz.DisableConcurrentExecutionJob;
import gnext.quartz.annotation.Job;
import gnext.quartz.annotation.Trigger;
import gnext.utils.Console;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Job(name = "BuildTriggerJob", group = "BUILD", startOnLoad = false,
        trigger = @Trigger(useCron = false, description = "use owner trigger"),
        description = "")
public class BuildTriggerJob extends DisableConcurrentExecutionJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(BuildTriggerJob.class);
    @Override
    protected void run(JobExecutionContext jec, JobDataMap parameters) {
        Console.log("gnext.quartz.jobs.BuildTriggerJob.run()");
    }

    @Override
    protected void stop() {
        Console.log("gnext.quartz.jobs.BuildTriggerJob.stop()");
    }

    @Override
    public org.quartz.Trigger buidTrigger() {
        String triggerName = JobController.getIdentityTriggerName("BuildTriggerJob");
        return TriggerBuilder.newTrigger().withIdentity(triggerName, "BUILD")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().
                        withIntervalInSeconds(10).repeatForever()).build();
    }
}
