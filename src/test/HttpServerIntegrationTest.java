import com.mycompany.httpserver.HttpRequest;
import com.mycompany.httpserver.HttpResponse;
import com.mycompany.httpserver.HttpServer;
import org.junit.Test;
import java.net.URI;
import java.net.http.HttpClient;
// OJO: NO importes java.net.http.HttpRequest ni java.net.http.HttpResponse
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpServerIntegrationTest {

    @Test
    public void server_starts_and_serves_routes_and_static_index() throws Exception {
        // Configurar microframework (estas clases son de este paquete)
        HttpServer.staticfiles("/static");
        HttpServer.get("/app/test", (HttpRequest req, HttpResponse resp) -> "{\"ok\":true}");

        // Arrancar en hilo daemon para no bloquear los tests
        Thread t = new Thread(() -> {
            try {
                HttpServer.startServer(new String[]{"36101"});
            } catch (Exception ignored) {}
        });
        t.setDaemon(true);
        t.start();
        Thread.sleep(300); // pequeño margen para que arranque

        HttpClient client = HttpClient.newHttpClient();

        // 1) Ruta GET registrada
        java.net.http.HttpResponse<String> r1 = client.send(
                java.net.http.HttpRequest.newBuilder(URI.create("http://localhost:36101/app/test?x=1"))
                        .GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, r1.statusCode());
        assertTrue(r1.body().contains("{\"ok\":true}"));

        // 2) Index estático
        java.net.http.HttpResponse<String> r2 = client.send(
                java.net.http.HttpRequest.newBuilder(URI.create("http://localhost:36101/"))
                        .GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, r2.statusCode());
        assertTrue(r2.body().toLowerCase().contains("http server demo"));
    }
}
