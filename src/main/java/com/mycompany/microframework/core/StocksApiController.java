package com.mycompany.microframework.core;

import com.mycompany.httpserver.HttpRequest;
import com.mycompany.httpserver.HttpResponse;


@microframework.annotations.RestController
public class StocksApiController {

    /* Rutas que el front podría usar */
    @microframework.annotations.GetMapping("/stocks")
    public String stocks(HttpRequest req, HttpResponse resp,
                         @microframework.annotations.RequestParam(value="symbol", defaultValue="") String symbol,
                         @microframework.annotations.RequestParam(value="stockSymbol", defaultValue="") String stockSymbol,
                         @microframework.annotations.RequestParam(value="ticker", defaultValue="") String ticker,
                         @microframework.annotations.RequestParam(value="s", defaultValue="") String s) {
        return handle(req, resp, symbol, stockSymbol, ticker, s);
    }

    @microframework.annotations.GetMapping("/api/stocks")
    public String apiStocks(HttpRequest req, HttpResponse resp,
                            @microframework.annotations.RequestParam(value="symbol", defaultValue="") String symbol,
                            @microframework.annotations.RequestParam(value="stockSymbol", defaultValue="") String stockSymbol,
                            @microframework.annotations.RequestParam(value="ticker", defaultValue="") String ticker,
                            @microframework.annotations.RequestParam(value="s", defaultValue="") String s) {
        return handle(req, resp, symbol, stockSymbol, ticker, s);
    }

    @microframework.annotations.GetMapping("/app/stocks")
    public String appStocks(HttpRequest req, HttpResponse resp,
                            @microframework.annotations.RequestParam(value="symbol", defaultValue="") String symbol,
                            @microframework.annotations.RequestParam(value="stockSymbol", defaultValue="") String stockSymbol,
                            @microframework.annotations.RequestParam(value="ticker", defaultValue="") String ticker,
                            @microframework.annotations.RequestParam(value="s", defaultValue="") String s) {
        return handle(req, resp, symbol, stockSymbol, ticker, s);
    }

    /* ---------------- core ---------------- */
    private String handle(HttpRequest req, HttpResponse resp,
                          String symbol, String stockSymbol, String ticker, String s) {
        // Evita cache + marca JSON (si tu HttpResponse expone estos métodos)
        try { resp.getClass().getMethod("setHeader", String.class, String.class)
                .invoke(resp, "Content-Type", "application/json"); } catch (Exception ignored) {}
        try { resp.getClass().getMethod("setHeader", String.class, String.class)
                .invoke(resp, "Cache-Control", "no-store, no-cache, must-revalidate"); } catch (Exception ignored) {}

        String chosen = firstNonBlank(symbol, stockSymbol, ticker, s);
        if (isBlank(chosen)) chosen = fromReq(req, "symbol","stockSymbol","ticker","s","q");
        if (isBlank(chosen)) chosen = "AAPL";

        // Precio determinista por símbolo (verás cambiar al cambiar texto)
        double price = 100 + Math.abs(chosen.toUpperCase().hashCode() % 500) / 10.0;

        return "{"
                + "\"ok\": true,"
                + "\"symbol\": \"" + esc(chosen) + "\","
                + "\"price\": " + String.format(java.util.Locale.US, "%.2f", price) + ","
                + "\"currency\": \"USD\""
                + "}";
    }

    /* ---------------- helpers ---------------- */
    private static String esc(String s){ return s==null? "" : s.replace("\"","\\\""); }
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
        // (String)->String: getParameter/getQueryParam/getValue/getParam
        for (String method : new String[]{"getParameter","getQueryParam","getValue","getParam"}) {
            try {
                var mm = req.getClass().getMethod(method, String.class);
                for (String n: names) { Object v = mm.invoke(req, n); if (v != null) return String.valueOf(v); }
            } catch (Exception ignored) {}
        }
        return null;
    }
}
