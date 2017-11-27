/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.jobs;

import gnext.quartz.DisableConcurrentExecutionJob;
import gnext.quartz.annotation.Job;
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
@Job(name = "ThrowExceptionJob", group = "EXCEPTION", startOnLoad = false,
        trigger = @Trigger(cron = "0/10 * * * * ?"),
        description = "")
public class ThrowExceptionJob extends DisableConcurrentExecutionJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(ThrowExceptionJob.class);
    @Override
    protected void run(JobExecutionContext jec, JobDataMap parameters) {
        Console.log("gnext.quartz.jobs.ThrowExceptionJob.run()");
        int rezo = 0;
        int a = 4 / rezo;
        Console.log("gnext.quartz.jobs.ThrowExceptionJob.run()" + a);
    }

    @Override
    protected void stop() {
        Console.log("gnext.quartz.jobs.ThrowExceptionJob.stop()");
    }

    @Override
    protected boolean ignoreExcetion() {
        return true;
    }

}
