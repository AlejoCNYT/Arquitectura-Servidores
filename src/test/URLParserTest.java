import com.mycompany.httpserver.URLParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class URLParserTest {
    @Test
    public void parsesQueryParam() {
        URLParser p = new URLParser("/stocks?symbol=IBM");
        assertEquals("IBM", p.params().get("symbol"));
    }

}
