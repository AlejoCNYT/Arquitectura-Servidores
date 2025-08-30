package com.mycompany.webapplication;

import com.mycompany.httpserver.HttpRequest;
import com.mycompany.httpserver.HttpResponse;
import com.mycompany.httpserver.HttpServer;

public class WebAplication {

    public static void main(String[] args) throws Exception {
        // Sirve src/main/resources/static -> target/classes/static
        HttpServer.staticfiles("/static");

        // Rutas que usa el front
        HttpServer.get("/app/hello", (HttpRequest req, HttpResponse resp) ->
                "{\"message\":\"Hello " + escape(req.getValues("name")) + "\"}"
        );

        HttpServer.get("/app/pi", (req, resp) ->
                "{\"pi\":\"" + Math.PI + "\"}"
        );

        // ---- /stocks: proxy sencillo que devuelve JSON siempre ----
// ---- /stocks: proxy que soporta key "demo" y siempre devuelve JSON ----
        HttpServer.get("/stocks", (req, resp) -> {
            String requested = req.getValues("symbol");
            if (requested == null || requested.isBlank()) requested = "IBM";

            String apiKey = System.getenv().getOrDefault("ALPHAVANTAGE_API_KEY", "demo");
            boolean usingDemo = "demo".equalsIgnoreCase(apiKey);

            // Con la key demo AlphaVantage solo permite IBM
            String effectiveSymbol = usingDemo ? "IBM" : requested;

            String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY"
                    + "&symbol=" + java.net.URLEncoder.encode(effectiveSymbol, java.nio.charset.StandardCharsets.UTF_8)
                    + "&apikey=" + java.net.URLEncoder.encode(apiKey, java.nio.charset.StandardCharsets.UTF_8);

            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(10))
                        .build();

                java.net.http.HttpRequest httpReq = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .timeout(java.time.Duration.ofSeconds(15))
                        .GET()
                        .build();

                java.net.http.HttpResponse<String> r =
                        client.send(httpReq, java.net.http.HttpResponse.BodyHandlers.ofString());

                String body = (r.body() == null) ? "" : r.body();

                // Si la respuesta trae "Note"/"Information" (límite o demo), encapsulamos con un aviso claro
                if (usingDemo && !requested.equalsIgnoreCase("IBM")) {
                    return "{\"notice\":\"demo key in use; AlphaVantage solo permite 'IBM'. "
                            + "Requested='" + escape(requested) + "', served='IBM'.\","
                            + "\"data\":" + body + "}";
                }
                // Si vino un mensaje de límite o info, al menos devuélvelo como JSON válido
                if (body.contains("\"Note\"") || body.contains("\"Information\"")) {
                    return "{\"upstream\":" + body + "}";
                }
                return body; // JSON normal
            } catch (Exception e) {
                return "{\"error\":\"" + escape(e.getMessage()) + "\"}";
            }
        });

        // Arranca en 35000
        HttpServer.startServer(new String[]{"35000"});
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
