/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.jobs;

import gnext.quartz.DisableConcurrentExecutionJob;
import gnext.quartz.annotation.Job;
import gnext.quartz.annotation.JobParameter;
import gnext.quartz.annotation.Jobs;
import gnext.quartz.annotation.Trigger;
import gnext.utils.Console;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Jobs(jobs = {
    @Job(name = "MultipleJobWithInstance-01", group = "MULTIPLE", startOnLoad = false,
            trigger = @Trigger(cron = "0/10 * * * * ?"),
            parameters = {
                @JobParameter(key = "key-03", value = "value-03"),
                @JobParameter(key = "key-04", value = "value-04")
            },
            description = ""),
    @Job(name = "MultipleJobWithInstance-02", group = "MULTIPLE", startOnLoad = false,
            trigger = @Trigger(cron = "0/10 * * * * ?"),
            parameters = {
                @JobParameter(key = "key-05", value = "value-05"),
                @JobParameter(key = "key-06", value = "value-06")
            },
            description = "")
})
public class MultipleJobWithInstance extends DisableConcurrentExecutionJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(MultipleJobWithInstance.class);
    @Override
    protected void run(JobExecutionContext jec, JobDataMap parameters) {
        Console.log("gnext.quartz.jobs.MultipleJobWithInstance.run()");
    }

    @Override
    protected void stop() {
        Console.log("gnext.quartz.jobs.MultipleJobWithInstance.stop()");
    }

}
