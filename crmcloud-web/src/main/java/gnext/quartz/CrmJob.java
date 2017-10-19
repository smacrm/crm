/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz;

import gnext.log.FolderRollingFileAppender;
import gnext.service.EntityService;
import gnext.utils.Console;
import java.io.Serializable;
import java.util.Map;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author daind
 */
public abstract class CrmJob extends FolderRollingFileAppender implements InterruptableJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(CrmJob.class);
    protected abstract void run(JobExecutionContext jec, JobDataMap parameters);
    protected abstract void stop();
    public Trigger buidTrigger() { return null; }
    protected boolean ignoreExcetion() { return true; }
    
    /***
     * @param jec
     * @throws JobExecutionException 
     */
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        JobDataMap parameters = jec.getJobDetail().getJobDataMap();
        try { run(jec, parameters);
        } catch (Exception e) {
            Console.log(e);
            MAINLOGGER.error(e.getMessage(), e);
            JobExecutionException jee = new JobExecutionException(e);
            if (ignoreExcetion()) {
                parameters.put("denominator", "1");
                jee.setRefireImmediately(true);
            } else {
                jee.setUnscheduleAllTriggers(true);
                throw jee;
            }
        }
    }
    
    /***
     * @throws UnableToInterruptJobException 
     */
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        try { stop();
        } catch (Exception e) {
            MAINLOGGER.error(e.getMessage(), e);
        }
    }
    
    /***
     * utility for lookup EJB services.
     * 
     * @param <T>
     * @param parameters
     * @param serviceName
     * @return 
     */
    protected <T> T getService(JobDataMap parameters, String serviceName) {
        try {
            Map<String, EntityService> services = (Map<String, EntityService>) parameters.get("services");
            return (T) services.get(serviceName);
        } catch (Exception e) {
            MAINLOGGER.error(e.getMessage(), e);
        }
        return null;
    }
    
    /***
     * utility for lookup managed beans.
     * 
     * @param <T>
     * @param parameters
     * @param serviceName
     * @return 
     */
    protected <T> T getManagedBean(JobDataMap parameters, String serviceName) {
        try {
            Map<String, Serializable> managedBeans = (Map<String, Serializable>) parameters.get("managedBeans");
            return (T) managedBeans.get(serviceName);
        } catch (Exception e) {
            MAINLOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
