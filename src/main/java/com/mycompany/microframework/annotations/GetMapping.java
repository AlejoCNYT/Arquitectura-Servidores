package microframework.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Mapea un método a una ruta GET (solo retorna String) */
@Retention(RUNTIME)
@Target(METHOD)
public @interface GetMapping {
    String value();
}
