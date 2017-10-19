/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.jobs;

import gnext.quartz.DisableConcurrentExecutionJob;
import gnext.quartz.annotation.Job;
import gnext.quartz.annotation.Trigger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Job(name = "ExampleDisableConcurrentExecutionJob", group = "EXAMPLE", startOnLoad = false,
        trigger = @Trigger(cron = "0/5 * * * * ?", description = "will run every five seconds"),
        description = "")
public class ExampleDisableConcurrentExecutionJob extends DisableConcurrentExecutionJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(ExampleDisableConcurrentExecutionJob.class);
    @Override
    protected void run(JobExecutionContext jec, JobDataMap parameters) {
        long t = System.currentTimeMillis();
        System.out.println("start: " + t);
        for(int i=0; i< 10; i++) {
            System.out.println("i = " + i);
        }
        try { Thread.sleep(10000); } catch (Exception e) { }
        System.out.println("stop: " + t);
    }
    
    @Override
    protected void stop() { }
}
