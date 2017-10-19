/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.jobs;

import gnext.quartz.DisableConcurrentExecutionJob;
import gnext.quartz.annotation.Job;
import gnext.quartz.annotation.JobParameter;
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
@Job(name = "NotStartedJob", group = "NOTSTART", startOnLoad = false,
        trigger = @Trigger(cron = "0/10 * * * * ?"),
        parameters = {
            @JobParameter(key = "key-08", value = "value-08"),
            @JobParameter(key = "key-09", value = "value-09")
        },
        description = "")
public class NotStartedJob extends DisableConcurrentExecutionJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(NotStartedJob.class);
    @Override
    protected void run(JobExecutionContext jec, JobDataMap parameters) {
        Console.log("gnext.quartz.jobs.NotStartedJob.run()");
    }

    @Override
    protected void stop() {
        Console.log("gnext.quartz.jobs.NotStartedJob.stop()");
    }

}
