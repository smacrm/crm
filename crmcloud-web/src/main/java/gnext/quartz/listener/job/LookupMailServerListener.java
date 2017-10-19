/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.listener.job;

import gnext.utils.Console;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 *
 * @author daind
 */
public class LookupMailServerListener implements JobListener {
    private String name;
    public LookupMailServerListener(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

    /**
     * Scheduler sẽ gọi phương thức khi JobDetail có sẵn thực thi.
     * @param context 
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        Console.log("gnext.quartz.listener.LookupMailServerListener.jobToBeExecuted()");
    }

    /**
     * Scheduler sẽ gọi phương thức khi mà JobDetail sẵn sàng thực thi
     * nhưng triggerListener ngăn cấm việc thực thi.
     * @param context 
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        Console.log("gnext.quartz.listener.LookupMailServerListener.jobExecutionVetoed()");
    }

    /**
     * Scheduler sẽ gọi phương thức sau khi JobDetail đã thực thi xong.
     * @param context
     * @param jobException 
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        Console.log("gnext.quartz.listener.LookupMailServerListener.jobWasExecuted()");
    }

}