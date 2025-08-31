import com.mycompany.microframework.core.ClassScanner;
import com.mycompany.webapp.controllers.GreetingController;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ClassScannerTest {

    @Test
    public void findsGreetingControllerOnBasePackage() {
        List<Class<?>> list = ClassScanner.findControllers("com.mycompany.webapp");
        assertTrue("GreetingController should be discovered", list.contains(GreetingController.class));
    }
}
