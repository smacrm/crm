/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.cron;

import org.quartz.CronExpression;
import org.quartz.DateBuilder;

/**
 *
 * @author daind
 */
public class CronScheduleBuilder extends org.quartz.CronScheduleBuilder {
    public CronScheduleBuilder(CronExpression cronExpression) {super(cronExpression);}
    
    public static String generate(Cron cron) {
        if(cron.getType() == Cron.MIN) return dailyMinute(cron.getMinute());
        if(cron.getType() == Cron.HOUR) return dailyHour(cron.getMinute());
        if(cron.getType() == Cron.DAY) return dailyDay(cron.getHour(), cron.getMinute());
        if(cron.getType() == Cron.WEEK) return dailyWeekly(cron.getDayOfWeek(), cron.getHour(), cron.getMinute());
        if(cron.getType() == Cron.MONTH) return dailyMonth(cron.getDay(), cron.getHour(), cron.getMinute());
        if(cron.getType() == Cron.YEAR) return dailyYear(cron.getMonth(), cron.getDay(), cron.getHour(), cron.getMinute());
        return dailyOneMinute();
    }
    
    public static String dailyOneMinute() { return "0 0/1 * * * ?"; }
    
    public static String dailyMinute(int minute) {
        DateBuilder.validateMinute(minute);
        return String.format("0 0/%d * * * ?", minute);
    }
    
    public static String dailyHour(int minute) {
        DateBuilder.validateMinute(minute);
        return String.format("0 0/%d * * * ?", minute);
    }
    
    public static String dailyDay(int hour, int minute) {
        DateBuilder.validateHour(hour);
        DateBuilder.validateMinute(minute);
        return String.format("0 %d %d ? * *", minute, hour);
    }
    
    public static String dailyWeekly(int dayOfWeek, int hour, int minute) {
        DateBuilder.validateDayOfWeek(dayOfWeek);
        DateBuilder.validateHour(hour);
        DateBuilder.validateMinute(minute);
        return String.format("0 %d %d ? * %d", minute, hour, dayOfWeek);
    }
    
    public static String dailyMonth(int dayOfMonth, int hour, int minute) {
        DateBuilder.validateDayOfMonth(dayOfMonth);
        DateBuilder.validateHour(hour);
        DateBuilder.validateMinute(minute);
        return String.format("0 %d %d %d * ?", minute, hour, dayOfMonth);
    }
    
    public static String dailyYear(int month, int dayOfMonth, int hour, int minute) {
        DateBuilder.validateMonth(month);
        DateBuilder.validateDayOfMonth(dayOfMonth);
        DateBuilder.validateHour(hour);
        DateBuilder.validateMinute(minute);
        return String.format("0 %d %d %d %d ?", minute, hour, dayOfMonth, month);
    }
}
