package microframework.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Extrae un parámetro de consulta (?name=...) con defaultValue opcional */
@Retention(RUNTIME)
@Target(java.lang.annotation.ElementType.PARAMETER)
public @interface RequestParam {
    String value();
    String defaultValue() default "";
}
