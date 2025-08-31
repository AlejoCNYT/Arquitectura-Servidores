import org.junit.Test;
import static org.junit.Assert.*;

public class StaticResourcesExistTest {
    @Test public void indexHtmlExists() {
        assertNotNull(getClass().getResource("/static/index.html"));
    }
    @Test public void assetsExist() {
        assertNotNull(getClass().getResource("/static/css/styles.css"));
        assertNotNull(getClass().getResource("/static/js/app.js"));
        assertNotNull(getClass().getResource("/static/images/logo.png"));
    }
}
