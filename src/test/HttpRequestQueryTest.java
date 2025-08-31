import com.mycompany.httpserver.HttpRequest;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpRequestQueryTest {

    @Test
    void parsesQueryParams_andUrlDecoding() {
        URI u = URI.create("/app/hello?name=Ana%20Mar%C3%ADa&x=1&empty=");
        HttpRequest req = new HttpRequest(u);
        assertEquals("Ana María", req.getValues("name"));
        Map<String,String> all = req.getQueryMap();
        assertEquals("1", all.get("x"));
        assertTrue(all.containsKey("empty"));
    }
}
