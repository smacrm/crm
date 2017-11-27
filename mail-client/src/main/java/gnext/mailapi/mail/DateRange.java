/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.mail;

import java.util.Calendar;
import java.util.Date;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.DateTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;

/**
 *
 * @author daind
 */
public final class DateRange {
    
    private final Date minDate;
    private final Date maxDate;
    
    public DateRange(String pior) {
        if(pior == null || pior.isEmpty()) {
            minDate = null;
            maxDate = null;
            return;
        }
        int _pior = Integer.parseInt(pior);
        
        Calendar cal = Calendar.getInstance();
        resetTime(cal);
        maxDate = new Date(cal.getTimeInMillis());
        
        cal.add(Calendar.DAY_OF_MONTH, -(_pior)); 
        minDate = new Date(cal.getTimeInMillis());
    }
    
    private void resetTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
    }
    
    public SearchTerm buildReceivedDateTerm() {
        if(minDate == null || maxDate == null) return null;
        DateTerm minDateTerm = new ReceivedDateTerm(ComparisonTerm.GE, minDate);
        DateTerm maxDateTerm = new ReceivedDateTerm(ComparisonTerm.LE, maxDate);
        
        return new AndTerm(minDateTerm, maxDateTerm);
    }
    
    public SearchTerm buildSentDateTermTerm() {
        if(minDate == null || maxDate == null) return null;
        DateTerm minDateTerm = new SentDateTerm(ComparisonTerm.GE, minDate);
        DateTerm maxDateTerm = new SentDateTerm(ComparisonTerm.LE, maxDate);
        
        return new AndTerm(minDateTerm, maxDateTerm);
    }
    
    // getter and setter.
    public Date getMinDate() { return minDate; }
    public Date getMaxDate() { return maxDate; }
}
