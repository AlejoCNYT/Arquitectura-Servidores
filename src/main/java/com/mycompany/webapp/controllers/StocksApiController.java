package com.mycompany.webapp.controllers;

import com.mycompany.httpserver.HttpRequest;
import microframework.annotations.GetMapping;
import microframework.annotations.RequestParam;
import microframework.annotations.RestController;

import java.util.Locale;

@RestController
public class StocksApiController {

    // El front llama a /stocks?symbol=XYZ
    @GetMapping("/stocks")
    public String stocks(HttpRequest req,
                         @RequestParam(value = "symbol", defaultValue = "AAPL") String symbol) {

        // Precio “dummy” pero dependiente del símbolo (cambiará al cambiar el texto)
        double price = 100 + Math.abs(symbol.toUpperCase().hashCode() % 500) / 10.0;

        return "{"
                + "\"ok\": true,"
                + "\"symbol\": \"" + symbol.replace("\"","\\\"") + "\","
                + "\"price\": " + String.format(Locale.US, "%.1f", price) + ","
                + "\"currency\": \"USD\""
                + "}";
    }
}
