/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author HUONG
 */
public class DateUtil {

    public final static String PATTERN_CSV_EXPORT_DATE = "yyyyMMdd_HHmm";
    public final static String PATTERN_JP_SLASH_HH_MM = "yyyy/MM/dd HH:mm";
    public final static String PATTERN_JP_SLASH = "yyyy/MM/dd";
    public final static String PATTERN_JP_HYPHEN = "yyyy-MM-dd";
    public final static String PATTERN_SLASH = "dd/MM/yyyy";
    public final static String PATTERN_HYPHEN = "dd/MM/yyyy";

    public static String getDate(String locale) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public enum PATTERN{
        JP("yyyy/MM/dd HH:mm"),
        EN("yyyy-MM-dd HH:mm"),
        VI("dd/MM/yyyy HH:mm");
        
        @Getter @Setter
        private String value;
        
        private PATTERN(String value){
            this.value = value;
        }
        
    }

    public interface SYMBOL_DATE {

        String YEAR = "yyyy";
        String MONTH = "MM";
        String DAY = "dd";
        String TIME = "HH";
        String MINUTE = "mm";
        String SECOND = "ss";
    }

    public interface SYMBOL {

        String COMMA = "\\.";
        String SLASH = "/";
        String HYPHEN = "-";
        String FROMTO = "〜";
    }

    public static String getTimeZone() {
        Calendar c = Calendar.getInstance();
        TimeZone tz = c.getTimeZone();
        long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
        String retz = String.format("GMT+%d", hours);
        return retz;
    }

    public static Date getRandomDate(Date base) {
        Calendar date = Calendar.getInstance();
        date.setTime(base);
        date.add(Calendar.DATE, ((int) (Math.random() * 30)) + 1);
        return date.getTime();
    }

    public Date getInitialDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), Calendar.FEBRUARY, calendar.get(Calendar.DATE), 0, 0, 0);
        return calendar.getTime();
    }

    public static String getDateNowToString(String locale) {
        if (locale == null) {
            return null;
        }
        Calendar date = Calendar.getInstance();
        String patten = PATTERN_JP_SLASH;
        if (!"ja".equals(locale)) {
            patten = PATTERN_SLASH;
        }
        return (new SimpleDateFormat(patten)).format(date.getTime());
    }

    public static String getDateToString(Date date, String patten) {
        try {
            if (date == null || patten == null) {
                return null;
            }
            return (new SimpleDateFormat(patten)).format(date);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    public static String getStringYYYYMMDD(String date,String code, String mode) {
        try {
            if (date == null || mode == null) {
                return null;
            }
            String patten = SYMBOL_DATE.YEAR + SYMBOL_DATE.MONTH + SYMBOL_DATE.DAY;
            SimpleDateFormat sdf = new SimpleDateFormat(getDate(code, mode));
            Date formatDate = sdf.parse(date);
            return (new SimpleDateFormat(patten)).format(formatDate);
        } catch (ParseException e) {
            return StringUtils.EMPTY;
        }
    }

    public static String getDateYYYYMMDD(String date,String code, String mode) {
        try {
            if (date == null || mode == null) {
                return null;
            }
            String patten = getDate(code, mode);
            SimpleDateFormat sdf = new SimpleDateFormat(SYMBOL_DATE.YEAR + SYMBOL_DATE.MONTH + SYMBOL_DATE.DAY);
            Date formatDate = sdf.parse(date);
            System.err.println((new SimpleDateFormat(patten)).format(formatDate));
            return (new SimpleDateFormat(patten)).format(formatDate);
        } catch (ParseException e) {
            return StringUtils.EMPTY;
        }
    }

    public static String getDateTime(String code, String mode, boolean notSec) {
        if (StringUtils.isBlank(mode)) {
            return StringUtils.EMPTY;
        }
        String date = SYMBOL_DATE.YEAR + mode + SYMBOL_DATE.MONTH + mode + SYMBOL_DATE.DAY;
        if (!"ja".equals(code)) {
            date = SYMBOL_DATE.DAY + mode + SYMBOL_DATE.MONTH + mode + SYMBOL_DATE.YEAR;
        }
        if (notSec) {
            date += StringUtils.SPACE + getTime(notSec);
        } else {
            date += StringUtils.SPACE + getTime(notSec);
        }
        return date;
    }

    public static String getDate(String code, String mode) {
        if (!"ja".equals(code)) {
            return SYMBOL_DATE.DAY + mode + SYMBOL_DATE.MONTH + mode + SYMBOL_DATE.YEAR;
        }
        return SYMBOL_DATE.YEAR + mode + SYMBOL_DATE.MONTH + mode + SYMBOL_DATE.DAY;
    }

    public static String getTime(boolean notSec) {
        if (notSec) {
            return SYMBOL_DATE.TIME + ":" + SYMBOL_DATE.MINUTE;
        }
        return SYMBOL_DATE.TIME + ":" + SYMBOL_DATE.MINUTE + ":" + SYMBOL_DATE.SECOND;
    }

    public static Date now() {
        return Calendar.getInstance().getTime();
    }

    public static Integer getIssueCodeNow() {
        return Integer.valueOf(new SimpleDateFormat("yyMM").format(now()));
    }

    public static long timeToResetPasswordDate(Integer days, final Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime().getTime() - Calendar.getInstance().getTime().getTime();
    }

    public static Date isDate(String date, String format) {
        DateFormat s = new SimpleDateFormat(format);
        try {
            s.setLenient(false);
            return s.parse(date);
        } catch (ParseException ex) {
            return null;
        }
    }
    
    /**
     * Parse date string with various pattern
     * @param data
     * @return 
     */
    public static Date parseDate(String data){
        Date date = null;
        for(PATTERN p : PATTERN.values()){
            if( (date = isDate(data, p.getValue())) != null){
                return date;
            }
        }
        return date;
    }

    public static Date getAddSupDate(Integer num) {
        if(num == null) return null;
        Calendar c = Calendar.getInstance();
        if(num == 0) return c.getTime();
        c.add(Calendar.MONTH, num);
        return c.getTime();
    }
}
