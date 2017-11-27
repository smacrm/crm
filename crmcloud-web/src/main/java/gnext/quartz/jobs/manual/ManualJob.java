/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.jobs.manual;

import gnext.bean.job.Batch;
import gnext.bean.job.Command;
import gnext.log.ExecLogHangler;
import gnext.quartz.DisableConcurrentExecutionJob;
import gnext.service.job.CommandService;
import gnext.util.WebFileUtil;
import gnext.utils.Console;
import java.io.File;
import java.util.Map;
import lombok.Getter;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class này sử dụng để chạy các batch lấy từ Database.
 * Log cho batch này lấy theo qui tắc batch_id+date.
 * @author daind
 */
public class ManualJob extends DisableConcurrentExecutionJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(ManualJob.class);
    public static final String BATCH_PARAMETER = "BATCH_PARAMETER";
    public static final String SERVICE_PARAMETER ="SERVICE_PARAMETER";
    
    @Getter private Long objId;
    private JobDataMap parameters;
    
    private String buildFileLogName(Batch b) {
        String fn = b.getBatchId() + "." + getFolderFolowDate() + ".log";
        return fn;
    }
    
    private String buildFolderLog(Batch b) {
        String dir = WebFileUtil.BASE_FOLDER_LOG_PATH  + File.separator
                                + b.getBatchId()   + File.separator 
                                + getFolderFolowDate() + File.separator;
        return dir;
    }
    
    @Override
    protected void run(JobExecutionContext jec, JobDataMap parameters) {
        if(!parameters.containsKey(BATCH_PARAMETER)) return;
        this.parameters = parameters;
        
        Batch p_batch = (Batch) parameters.get(BATCH_PARAMETER);
        if(p_batch == null) return;
        
        try {
            String dir = buildFolderLog(p_batch);
            String fn = buildFileLogName(p_batch);
            setupFolderLog(dir, fn, ManualJob.class);            
        } catch (Exception e) {
            MAINLOGGER.error(e.getLocalizedMessage(), e);
        }
        
        try {
            Command command = getCommand(p_batch);
            if(command == null)
                MAINLOGGER.error("Can't read command from database.");
            
            String cmd_exe = command.getCommandValue();
            if(cmd_exe == null || cmd_exe.isEmpty())
                MAINLOGGER.error("Can't read command value from database.");
            
            logInfo("execute command: " + cmd_exe);
            Console.exec(cmd_exe, new ExecLogHangler(FOLDERLOGGER));
        } catch (Exception e) {
            logError(e.getLocalizedMessage(), e);
        }
    }
    
    private Command getCommand(Batch p_batch) {
        Command command = p_batch.getCommand();
        if(command == null) {
            Map<String, Object> services = (Map<String, Object>) parameters.get(SERVICE_PARAMETER);
            CommandService commandService  = (CommandService) services.get("commandService");
            command = commandService.find(Integer.parseInt(p_batch.getBatchCommand()));
        }
        return command;
    }
    
    @Override
    protected Map<String, Object> buildParameters() {
        return parameters;
    }

    @Override
    protected void stop() {
        FOLDERLOGGER.info("System begin interupting the job.");
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (objId != null ? objId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ManualJob)) {
            return false;
        }
        ManualJob other = (ManualJob) object;
        if ((this.objId == null && other.objId != null)
                || (this.objId != null && !this.objId.equals(other.objId))) {
            return false;
        }
        return true;
    }
}
