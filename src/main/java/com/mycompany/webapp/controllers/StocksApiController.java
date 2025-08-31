package com.mycompany.webapp.controllers;

import com.mycompany.httpserver.HttpRequest;
import com.mycompany.httpserver.HttpResponse;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@microframework.annotations.RestController
public class StocksApiController {

    /* ========= RUTAS (mantenemos compatibilidad) ========= */
    @microframework.annotations.GetMapping("/stocks")
    public String stocks(HttpRequest req, HttpResponse resp,
                         @microframework.annotations.RequestParam(value="symbol",      defaultValue="") String symbol,
                         @microframework.annotations.RequestParam(value="stockSymbol", defaultValue="") String stockSymbol,
                         @microframework.annotations.RequestParam(value="ticker",      defaultValue="") String ticker,
                         @microframework.annotations.RequestParam(value="s",           defaultValue="") String s) {
        return handle(req, resp, symbol, stockSymbol, ticker, s);
    }

    @microframework.annotations.GetMapping("/api/stocks")
    public String apiStocks(HttpRequest req, HttpResponse resp,
                            @microframework.annotations.RequestParam(value="symbol",      defaultValue="") String symbol,
                            @microframework.annotations.RequestParam(value="stockSymbol", defaultValue="") String stockSymbol,
                            @microframework.annotations.RequestParam(value="ticker",      defaultValue="") String ticker,
                            @microframework.annotations.RequestParam(value="s",           defaultValue="") String s) {
        return handle(req, resp, symbol, stockSymbol, ticker, s);
    }

    @microframework.annotations.GetMapping("/app/stocks")
    public String appStocks(HttpRequest req, HttpResponse resp,
                            @microframework.annotations.RequestParam(value="symbol",      defaultValue="") String symbol,
                            @microframework.annotations.RequestParam(value="stockSymbol", defaultValue="") String stockSymbol,
                            @microframework.annotations.RequestParam(value="ticker",      defaultValue="") String ticker,
                            @microframework.annotations.RequestParam(value="s",           defaultValue="") String s) {
        return handle(req, resp, symbol, stockSymbol, ticker, s);
    }

    /* ========= LÓGICA ========= */
    private String handle(HttpRequest req, HttpResponse resp,
                          String symbol, String stockSymbol, String ticker, String s) {
        // Content-Type JSON y no cache (si tu HttpResponse lo soporta)
        try { resp.getClass().getMethod("setHeader", String.class, String.class)
                .invoke(resp, "Content-Type", "application/json"); } catch (Exception ignored) {}
        try { resp.getClass().getMethod("setHeader", String.class, String.class)
                .invoke(resp, "Cache-Control", "no-store, no-cache, must-revalidate"); } catch (Exception ignored) {}

        String chosen = firstNonBlank(symbol, stockSymbol, ticker, s);
        if (isBlank(chosen)) {
            // fallback por si el framework no inyectó @RequestParam
            chosen = fromReq(req, "symbol","stockSymbol","ticker","s","q");
        }
        if (isBlank(chosen)) chosen = "AAPL";

        // ===== JSON "largo" determinista por símbolo (mock) =====
        String symU = chosen.toUpperCase(Locale.ROOT);
        int h = Math.abs(symU.hashCode());

        double base    = 50 + (h % 451); // 50..500
        double cents   = ((h / 7) % 100) / 100.0;
        double price   = base + cents;

        double change  = ((h % 1001) / 100.0) - 5.0;      // -5.00 .. +5.00
        double prev    = price - change;
        double pct     = (change / prev) * 100.0;

        double wiggle  = (h % 20) / 10.0;                  // 0.0 .. 1.9
        double open    = prev + (change * 0.35);
        double high    = Math.max(price, open) + wiggle;
        double low     = Math.min(price, open) - wiggle;

        long   volume  = 1_000_000L + (h % 5_000_000L);
        long   shares  = 1_000_000_00L + (h % 900_000_00L); // acciones ficticias
        double mcap    = price * shares;

        String exchange = (h % 2 == 0) ? "NYSE" : "NASDAQ";
        String name     = companyNameFor(symU);

        // Historial sintético (10 puntos)
        StringBuilder history = new StringBuilder("[");
        double p = prev;
        for (int i = 9; i >= 0; i--) {
            double delta = ((h % (i + 5)) - (i / 2.0)) / 50.0;
            p = Math.max(1.0, p + delta);
            history.append("{\"t\":").append(i)
                    .append(",\"price\":").append(fmt(p)).append("}");
            if (i > 0) history.append(',');
        }
        history.append("]");

        String isoNow = DateTimeFormatter.ISO_INSTANT
                .format(Instant.now().atZone(ZoneOffset.UTC));

        // Construcción del JSON extendido
        String json =
                "{"
                        + "\"ok\":true,"
                        + "\"symbol\":\"" + esc(symU) + "\","
                        + "\"name\":\"" + esc(name) + "\","
                        + "\"exchange\":\"" + exchange + "\","
                        + "\"currency\":\"USD\","
                        + "\"price\":" + fmt(price) + ","
                        + "\"change\":" + fmt(change) + ","
                        + "\"changePct\":" + fmt(pct) + ","
                        + "\"open\":" + fmt(open) + ","
                        + "\"high\":" + fmt(high) + ","
                        + "\"low\":" + fmt(low) + ","
                        + "\"prevClose\":" + fmt(prev) + ","
                        + "\"volume\":" + volume + ","
                        + "\"marketCap\":" + fmt(mcap) + ","
                        + "\"lastUpdate\":\"" + isoNow + "\","
                        + "\"history\":" + history.toString()
                        + "}";

        return json;
    }

    /* ========= helpers ========= */
    private static String esc(String s){ return s==null? "" : s.replace("\"","\\\""); }
    private static String fmt(double d){ return String.format(Locale.US, "%.2f", d); }
    private static boolean isBlank(String s){ return s==null || s.isEmpty(); }
    private static String firstNonBlank(String... xs){ for (String x: xs) if (!isBlank(x)) return x; return null; }

    private static String fromReq(HttpRequest req, String... names){
        // Map<String,String> getQueryMap()
        try {
            var m = req.getClass().getMethod("getQueryMap");
            Object o = m.invoke(req);
            if (o instanceof java.util.Map<?,?> map) {
                for (String n: names) { Object v = map.get(n); if (v != null) return String.valueOf(v); }
            }
        } catch (Exception ignored) {}
        // (String)->String: getParameter / getQueryParam / getValue / getParam
        for (String method : new String[]{"getParameter","getQueryParam","getValue","getParam"}) {
            try {
                var mm = req.getClass().getMethod(method, String.class);
                for (String n: names) { Object v = mm.invoke(req, n); if (v != null) return String.valueOf(v); }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static String companyNameFor(String symU) {
        // Nombres ficticios pero razonables
        if (symU.equals("AAPL")) return "Apple Inc.";
        if (symU.equals("MSFT")) return "Microsoft Corporation";
        if (symU.equals("GOOG") || symU.equals("GOOGL")) return "Alphabet Inc.";
        if (symU.equals("AMZN")) return "Amazon.com, Inc.";
        if (symU.equals("META") || symU.equals("FB")) return "Meta Platforms, Inc.";
        if (symU.equals("TSLA")) return "Tesla, Inc.";
        if (symU.equals("IBM"))  return "International Business Machines Corporation";
        return symU + " Corporation";
    }
}
