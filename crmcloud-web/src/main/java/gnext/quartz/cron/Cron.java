/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.cron;

import java.io.Serializable;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Lưu định dạng json cho cron.
 * ex:
 *      hour: {type:hour, minute: 0-59} -> {"type":"1","minute":"0","hour":"*","day":"*","month":"*","dow":"*"} -> ?
 *      day:  {type:day, hour:03, minute:42} -> {"type":"2","minute":"0","hour":"0","day":"*","month":"*","dow":"*"}    -> dailyAtHourAndMinute
 *      week: {type:week, day:1-7, hour:03, minute:42}  -> weeklyOnDayAndHourAndMinute
 *      month:{type:month, day:1-31, hour:03, minute:42} -> {"type":"4","minute":"0","hour":"0","day":"1","month":"*","dow":"*"}    -> monthlyOnDayAndHourAndMinute
 *      year: {type:year, day:1-31, month:1-12, hour:03, minute:42 } -> {"type":"5","minute":"0","hour":"0","day":"1","month":"1","dow":"*"}   -> ? 
 * 
 * @author daind
 */
public class Cron implements Serializable {
    private static final long serialVersionUID = 6340081102970587731L;
    
    public static final int MIN = 0;
    public static final int HOUR = 1;
    public static final int DAY = 2;
    public static final int WEEK = 3;
    public static final int MONTH = 4;
    public static final int YEAR = 5;
    
    @Setter private String type;
    @Setter private String minute;
    @Setter private String hour;
    @Setter private String day;
    @Setter private String month;
    @Setter private String dow;
    
    public int getType() { return NumberUtils.toInt(type, 0); }
    public int getMinute() {return NumberUtils.toInt(minute, 0);}
    public int getHour() {return NumberUtils.toInt(hour, 0);}
    public int getDay() {return NumberUtils.toInt(day, 0);}
    public int getMonth() {return NumberUtils.toInt(month, 0);}
    public int getDayOfWeek() {return NumberUtils.toInt(dow, 0);}
}
