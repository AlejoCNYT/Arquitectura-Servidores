package com.mycompany.webapp.controllers;

import com.mycompany.httpserver.HttpRequest;

@microframework.annotations.RestController
public class StocksApiController {

    @microframework.annotations.GetMapping("/stocks")
    public String stocks(
            HttpRequest req,
            @microframework.annotations.RequestParam(value = "symbol", defaultValue = "") String symbol,
            @microframework.annotations.RequestParam(value = "stockSymbol", defaultValue = "") String stockSymbol,
            @microframework.annotations.RequestParam(value = "ticker", defaultValue = "") String ticker,
            @microframework.annotations.RequestParam(value = "s", defaultValue = "") String s
    ) {
        String chosen = firstNonBlank(symbol, stockSymbol, ticker, s);
        if (isBlank(chosen)) {
            // Fallback por si el front usa otro nombre raro
            chosen = fromReq(req, "symbol", "stockSymbol", "ticker", "s", "q");
        }
        if (isBlank(chosen)) chosen = "AAPL";

        // Puedes calcular un precio dummy distinto por símbolo para que se note el cambio
        double price = 100 + Math.abs(chosen.toUpperCase().hashCode() % 500) / 10.0;

        return "{"
                + "\"ok\":true,"
                + "\"symbol\":\"" + esc(chosen) + "\","
                + "\"price\":" + String.format(java.util.Locale.US, "%.2f", price) + ","
                + "\"currency\":\"USD\""
                + "}";
    }

    /* ---------------- helpers ---------------- */

    private static String esc(String s){ return s==null? "" : s.replace("\"","\\\""); }
    private static boolean isBlank(String s){ return s==null || s.isEmpty(); }
    private static String firstNonBlank(String... xs){
        for (String x: xs) if (!isBlank(x)) return x;
        return null;
    }
    // Intenta leer cualquier nombre desde HttpRequest si tu framework no lo pasó por @RequestParam
    private static String fromReq(HttpRequest req, String... names){
        try {
            // getQueryMap()
            var m = req.getClass().getMethod("getQueryMap");
            Object o = m.invoke(req);
            if (o instanceof java.util.Map<?,?> map) {
                for (String n: names) {
                    Object v = map.get(n);
                    if (v != null) return String.valueOf(v);
                }
            }
        } catch (Exception ignored) {}
        try {
            // getParameter(String)/getQueryParam(String)/getValue(String)...
            for (String method : new String[]{"getParameter","getQueryParam","getValue","getParam"}) {
                var mm = req.getClass().getMethod(method, String.class);
                for (String n: names) {
                    Object v = mm.invoke(req, n);
                    if (v != null) return String.valueOf(v);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
