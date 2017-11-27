package gnext.dbutils.processor;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author daind
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Table {

    String name();

    String description() default "";
}
