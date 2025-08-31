import com.mycompany.webapp.controllers.GreetingController;
import microframework.annotations.GetMapping;
import microframework.annotations.RequestParam;
import microframework.annotations.RestController;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class AnnotationsPresenceTest {

    @Test
    public void greetingControllerIsAnnotatedAsRestController() {
        assertTrue(GreetingController.class.isAnnotationPresent(RestController.class));
    }

    @Test
    public void greetingMethodHasGetMappingAndRequestParam() throws NoSuchMethodException {
        Method m = GreetingController.class.getDeclaredMethod("greeting", String.class);
        assertTrue(m.isAnnotationPresent(GetMapping.class));
        assertEquals("/greeting", m.getAnnotation(GetMapping.class).value());

        Annotation[][] pas = m.getParameterAnnotations();
        boolean found = false;
        for (Annotation a : pas[0]) {
            if (a instanceof RequestParam rp) {
                assertEquals("name", rp.value());
                assertEquals("World", rp.defaultValue());
                found = true;
            }
        }
        assertTrue("Expected @RequestParam on first parameter", found);
    }
}
