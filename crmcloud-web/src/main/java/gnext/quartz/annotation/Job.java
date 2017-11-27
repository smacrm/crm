/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author daind
 */
@Target({ElementType.METHOD,
    ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Job {

    /**
     * job name
     *
     * @return name of job
     */
    String name();

    /**
     * job group
     *
     * @return name of group
     */
    String group();

    /**
     * job status
     *
     * @return status of job
     */
    boolean startOnLoad() default false;

    /**
     * job status
     *
     * @return status of job
     */
    boolean disable() default false;

    /**
     * job group
     *
     * @return name of group
     */
    String description() default "";

    /**
     * job parameters
     *
     * @return parameter's job
     */
    public JobParameter[] parameters() default {};

    /**
     * job parameters
     *
     * @return parameter's job
     */
    public Trigger trigger();

    /**
     * job listeners
     *
     * @return parameter's listener
     */
    public String[] listeners() default {};
    
}
