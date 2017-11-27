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
import gnext.service.config.ConfigService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Job(name = "LookupMailServerJob", group = "MAIL", startOnLoad = false,
        trigger = @Trigger(cron = "0/10 * * * * ?", description = "will run every 10 seconds"),
        parameters = {
            @JobParameter(key = "key-01", value = "value-01"),
            @JobParameter(key = "key-02", value = "value-02")
        },
        description = "",
        listeners = { "LookupMailServerListener", "" })
public class LookupMailServerJob extends DisableConcurrentExecutionJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(LookupMailServerJob.class);
    private ConfigService configService;
    
    @Override
    protected void run(JobExecutionContext jec, JobDataMap parameters) {
//        configService = getService(parameters, "configService");
//        if (configService == null) {
//            return;
//        }
//
//        String mailClientLib = configService.get("PATH_MAIL_MAILCLIENT");
//        String pathMailDb = configService.get("PATH_MAIL_DB");
//
//        if (!StringUtils.isEmpty(mailClientLib) && !StringUtils.isEmpty(pathMailDb)) {
//            String cmd = "java -jar {0} action:{1} db_path:{2} folder:{3} flag:{4}";
//            cmd = MessageFormat.format(cmd, mailClientLib, "receive", pathMailDb, "inbox", "all");
//            Console.exec2ouput(cmd);
//        }
    }

    @Override
    protected void stop() {
        MAINLOGGER.info("calling stop method.");
    }

}
