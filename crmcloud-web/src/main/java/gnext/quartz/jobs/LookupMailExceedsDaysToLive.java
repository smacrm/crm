/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.jobs;

import gnext.bean.mail.MailData;
import gnext.quartz.DisableConcurrentExecutionJob;
import gnext.quartz.annotation.Job;
import gnext.quartz.annotation.Trigger;
import gnext.service.mail.MailAccountService;
import gnext.service.mail.MailDataService;
import java.util.Calendar;
import java.util.Date;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@Job(name = "LookupMailExceedsDaysToLive", group = "MAIL", startOnLoad = false,
        trigger = @Trigger(cron = "0/5 * * * * ?", description = "will run every five seconds"),
        description = "exceeds the number of days to live")
public class LookupMailExceedsDaysToLive extends DisableConcurrentExecutionJob {
    private static final Logger MAINLOGGER = LoggerFactory.getLogger(LookupMailExceedsDaysToLive.class);
    private MailAccountService mailAccountService;
    private MailDataService mailDataService;
    
    @Override
    protected void run(JobExecutionContext jec, JobDataMap parameters) {
        long t = System.currentTimeMillis();
        System.out.println("start: " + t);
        for(int i=0; i< 10; i++) {
            System.out.println("i = " + i);
        }
        try { Thread.sleep(10000); } catch (Exception e) { }
        System.out.println("stop: " + t);
        
//        if(true) return;
//        if (_checkServices(parameters)) return;
//
//        String inbox = String.valueOf(EmailUtil.getExplodeType(EmailUtil.INBOX));
//        String trash = String.valueOf(EmailUtil.getExplodeType(EmailUtil.TRASH));
//
//        List<MailAccount> accounts = mailAccountService.findAll();
//        for (MailAccount account : accounts) {
//            if (account.getAccountIsDeleted() != null && account.getAccountIsDeleted() == 1) continue;
//            Integer dayOfLive = account.getAccountDeleteReceivedDays();
//            if (dayOfLive != null && dayOfLive > 0) {
//                List<MailData> datas = mailDataService.searchByAccountId(inbox, account.getAccountId());
//                for (MailData data : datas) {
//                    Date md = _getDate(data);
//                    if (md != null && _compareWithNow(-dayOfLive, md) > 0) {
//                        mailDataService.moveToFolder(data.getCompany(), MailFolder.DATA_MAIL_FOLDER_TRASH, Arrays.asList(data.getMailDataId()));
//                    }
//                }
//            }
//        }
    }
    
    @Override
    protected void stop() {
        MAINLOGGER.info("calling stop method.");
    }
    
    private long _compareWithNow(int days, final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, days);
        return date.getTime() - calendar.getTime().getTime();
    }

    private boolean _checkServices(final JobDataMap parameters) {
        mailAccountService = getService(parameters, "mailAccountService");
        mailDataService = getService(parameters, "mailDataService");
        return mailAccountService == null || mailDataService == null;
    }

    private Date _getDate(MailData data) {
        if (data.getMailDataDeleteFlag() == 1) return null;

        Date md = data.getUpdatedTime();
        if (md == null) md = data.getCreatedTime();
        return md;
    }

}
