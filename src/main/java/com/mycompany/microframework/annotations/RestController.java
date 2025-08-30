package microframework.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Marca una clase como componente web */
@Retention(RUNTIME)
@Target(TYPE)
public @interface RestController {}
