/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext;

import java.text.ParseException;
import org.apache.commons.lang3.time.DateUtils;

/**
 *
 * @author daind
 */
public class Main {
    public static void main(String args[]) throws ParseException {
        String date = "2017/02/09";
        try{
            System.out.println("1 >> "+DateUtils.parseDate(date));
        }catch(ParseException pe){}
        try{
            System.out.println("2 >> "+DateUtils.parseDateStrictly(date));
        }catch(ParseException pe){}
        //System.out.println("gnext.Main.main()" + org.quartz.CronExpression.isValidExpression("0 0 0/3 1/1 * ? *"));
    }
}
