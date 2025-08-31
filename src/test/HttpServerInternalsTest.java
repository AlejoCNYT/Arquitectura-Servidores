import com.mycompany.httpserver.HttpServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class    HttpServerInternalsTest {

    @org.testng.annotations.Test
    void normalizeRoute_makesLowercase_andLeadingSlash() throws Exception {
        Method m = HttpServer.class.getDeclaredMethod("normalizeRoute", String.class);
        m.setAccessible(true);
        assertEquals("/foo", m.invoke(null, "Foo"));
        assertEquals("/", m.invoke(null, ""));
        assertEquals("/hello", m.invoke(null, "  HELLO  "));
    }

    @org.testng.annotations.Test
    void staticfiles_setsAssetsBasePath() throws Exception {
        HttpServer.staticfiles("/static");
        Field f = HttpServer.class.getDeclaredField("assetsBasePath");
        f.setAccessible(true);
        String v = (String) f.get(null);
        assertTrue(v.replace('\\','/').endsWith("/static"));
    }
}
