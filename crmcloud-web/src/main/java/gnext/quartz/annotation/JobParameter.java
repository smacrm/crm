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
public @interface JobParameter {

    public String key();

    public String value();
    
    /**
     * parameter description.
     *
     * @return parameter description.
     */
    String description() default "";
}
