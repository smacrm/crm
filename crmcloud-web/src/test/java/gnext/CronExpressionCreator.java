/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext;

import java.io.Serializable;

/**
 *
 * @author daind
 */
public class CronExpressionCreator implements Serializable {
    private static final long serialVersionUID = -3734693697428351530L;
    
    public static void main(String[] args) {
//        CronExpressionCreator pCron = new CronExpressionCreator();
//        pCron.setTime("12:00 PM");
//        pCron.setMON(true);
//        pCron.setStartDate("12-05-2011");
//        System.out.println(pCron.getCronExpression());

        String cron = generateCronExpression("5", "*", "*", "*", "*", "*", "*");
        System.out.println("gnext.CronExpressionCreator.main()" + cron);
    }

    /**
    * Generate a CRON expression is a string comprising 6 or 7 fields separated by white space.
    *
    * @param seconds    mandatory = yes. allowed values = {@code  *9    * / , -}
    * @param minutes    mandatory = yes. allowed values = {@code  0-59    * / , -}
    * @param hours      mandatory = yes. allowed values = {@code 0-23   * / , -}
    * @param dayOfMonth mandatory = yes. allowed values = {@code 1-31  * / , - ? L W}
    * @param month      mandatory = yes. allowed values = {@code 1-12 or JAN-DEC    * / , -}
    * @param dayOfWeek  mandatory = yes. allowed values = {@code 0-6 or SUN-SAT * / , - ? L #}
    * @param year       mandatory = no. allowed values = {@code 1970–2099    * / , -}
    * @return a CRON Formatted String.
    */
    private static String generateCronExpression(final String seconds, final String minutes, final String hours,
            final String dayOfMonth,
            final String month, final String dayOfWeek, final String year) {
        return String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s", seconds, minutes, hours, dayOfMonth, month, dayOfWeek, year);
    }
    
    public String getCronExpression() {
        String time = getTime();
        String[] time1 = time.split("\\:");
        String[] time2 = time1[1].split("\\ ");

        String hour = "";
        if (time2[1].equalsIgnoreCase("PM")) {
            Integer hourInt = Integer.parseInt(time1[0]) + 12;
            if (hourInt == 24) {
                hourInt = 0;
            }
            hour = hourInt.toString();
        } else {
            hour = time1[0];
        }

        String minutes = time2[0];
        String cronExp = "";
        if (isRecurring()) {
            String daysString = "";
            StringBuilder sb = new StringBuilder(800);
            boolean moreConditions = false;

            if (isSUN()) {
                sb.append("SUN");
                moreConditions = true;
            }

            if (isMON()) {
                if (moreConditions) {
                    sb.append(",");
                }
                sb.append("MON");
                moreConditions = true;
            }

            if (isTUE()) {
                if (moreConditions) {
                    sb.append(",");
                }

                sb.append("TUE");
                moreConditions = true;
            }

            if (isWED()) {
                if (moreConditions) {
                    sb.append(",");
                }

                sb.append("WED");
                moreConditions = true;
            }

            if (isTHU()) {
                if (moreConditions) {
                    sb.append(",");
                }
                sb.append("THU");
                moreConditions = true;
            }

            if (isFRI()) {
                if (moreConditions) {
                    sb.append(",");
                }
                sb.append("FRI");
                moreConditions = true;
            }

            if (isSAT()) {
                if (moreConditions) {
                    sb.append(",");
                }
                sb.append("SAT");
                moreConditions = true;
            }

            daysString = sb.toString();

            cronExp = "0 " + minutes + " " + hour + " ? * " + daysString;
        } else {
            String startDate = getStartDate();
            String[] dateArray = startDate.split("\\-");
            String day = dateArray[0];
            if (day.charAt(0) == '0') {
                day = day.substring(1);
            }

            String month = dateArray[1];

            if (month.charAt(0) == '0') {
                month = month.substring(1);
            }

            String year = dateArray[2];
            cronExp = "0 " + minutes + " " + hour + " " + day + " " + month + " ? " + year;

        }
        return cronExp;
    }

    String startDate;
    String time;
    boolean recurring;
    boolean SUN;
    boolean MON;
    boolean TUE;
    boolean WED;
    boolean THU;
    boolean FRI;
    boolean SAT;

    /**
     * @return the startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * The date set should be of the format (MM-DD-YYYY for example 25-04-2011)
     *
     * @param startDate the startDate to set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * The time set should be of the format (HH:MM AM/PM for example 12:15 PM)
     *
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public boolean isSUN() {
        return SUN;
    }

    public void setSUN(boolean sUN) {
        SUN = sUN;
    }

    public boolean isMON() {
        return MON;
    }

    /**
     * @param mON the mON to set
     */
    public void setMON(boolean mON) {
        MON = mON;
    }

    public boolean isTUE() {
        return TUE;
    }

    public void setTUE(boolean tUE) {
        TUE = tUE;
    }

    public boolean isWED() {
        return WED;
    }

    public void setWED(boolean wED) {
        WED = wED;
    }

    public boolean isTHU() {
        return THU;
    }

    public void setTHU(boolean tHU) {
        THU = tHU;
    }

    public boolean isFRI() {
        return FRI;
    }

    public void setFRI(boolean fRI) {
        FRI = fRI;
    }

    public boolean isSAT() {
        return SAT;
    }

    public void setSAT(boolean sAT) {
        SAT = sAT;
    }

    public int hashCode() {
        return this.getCronExpression().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof CronExpressionCreator) {
            if (((CronExpressionCreator) obj).getCronExpression().equalsIgnoreCase(this.getCronExpression())) {
                return true;
            }
        } else {
            return false;
        }
        return false;

    }

}
