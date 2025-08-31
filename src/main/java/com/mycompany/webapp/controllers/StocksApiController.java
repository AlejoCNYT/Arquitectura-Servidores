package com.mycompany.webapp.controllers;


@microframework.annotations.RestController
public class StocksApiController {

    @microframework.annotations.GetMapping("/stocks")       // alias 1
    public String stocks(@microframework.annotations.RequestParam(value="symbol", defaultValue="AAPL") String symbol){
        return "{"
                + "\"ok\":true,"
                + "\"symbol\":\"" + esc(symbol) + "\","
                + "\"price\":123.45,"
                + "\"currency\":\"USD\""
                + "}";
    }

    @microframework.annotations.GetMapping("/api/stocks")   // alias 2
    public String apiStocks(@microframework.annotations.RequestParam(value="symbol", defaultValue="AAPL") String symbol){
        return stocks(symbol);
    }

    @microframework.annotations.GetMapping("/stock")        // alias 3 (por si el front llama singular)
    public String stock(@microframework.annotations.RequestParam(value="symbol", defaultValue="AAPL") String symbol){
        return stocks(symbol);
    }

    private static String esc(String s){ return s==null? "" : s.replace("\"","\\\""); }
}
