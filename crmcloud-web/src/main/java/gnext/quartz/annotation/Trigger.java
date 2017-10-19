/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.annotation;

/**
 *
 * @author daind
 */
public @interface Trigger {

    /**
     * A cron-like expression, extending the usual UN*X definition to include
     * triggers on the second as well as minute, hour, day of month, month and
     * day of week. e.g. {@code "0 * * * * MON-FRI"} means once per minute on
     * weekdays (at the top of the minute - the 0th second).
     *
     * @return an expression that can be parsed to a cron schedule
     */
    String cron() default "";

    /**
     * mark a job user cron or owner trigger.
     *
     * @return state.
     */
    boolean useCron() default true;

    /**
     * trigger description.
     *
     * @return trigger description.
     */
    String description() default "";

}
