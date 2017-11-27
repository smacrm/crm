package gnext.dbutils.processor;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author daind
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Column {
    boolean generated() default false;
    
    String name() default "";

    String description() default "";

}
