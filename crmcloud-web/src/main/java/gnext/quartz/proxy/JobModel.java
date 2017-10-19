/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.proxy;

import gnext.model.BaseModel;
import gnext.quartz.CrmJob;
import gnext.quartz.annotation.Job;
import gnext.quartz.annotation.Trigger;
import gnext.util.DateUtil;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class JobModel extends BaseModel {
    private static final long serialVersionUID = 1487019476154794404L;
    
    public static String RUNNING = "RUNNING";
    public static String PAUSE = "PAUSE";
    public static String DELETED = "DELETED";
    
    @Getter @Setter private int jobId;
    @Getter @Setter private String cron;
    @Getter @Setter private String name;
    @Getter @Setter private String group;
    @Getter @Setter private String description;
    @Getter @Setter private String status;
    @Getter @Setter private Class<? extends CrmJob> clazz;
    @Getter @Setter private Job job;
    @Getter @Setter private Date startDate;
    @Getter @Setter private Date stopDate;

    public JobModel(int jobId, Trigger trigger, Job job) {
        this.cron = trigger.cron();
        this.name = job.name();
        this.group = job.group();
        this.description = job.description();
        this.jobId = jobId;
        this.job = job;
    }
    
    public String getDisplayStartDate() {
        return DateUtil.getDateToString(startDate, DateUtil.PATTERN_JP_SLASH_HH_MM);
    }
    
    public String getDisplayStopDate() {
        return DateUtil.getDateToString(stopDate, DateUtil.PATTERN_JP_SLASH_HH_MM);
    }
}
