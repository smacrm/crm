/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.utils;

import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 *
 * @author hungpham
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface AddToElasticSearch {
    public boolean add() default true;
    public String name() default "";
}
