package com.mycompany.webapp.controllers;



@microframework.annotations.RestController
public class HelloApiController {

    @microframework.annotations.GetMapping("/hello")
    public String hello(@microframework.annotations.RequestParam(value = "name", defaultValue = "World") String name) {
        return "{\"ok\":true,\"message\":\"Hola " + esc(name) + "\"}";
    }

    // 🔧 Alias que el front usa desde app.js
    @microframework.annotations.GetMapping("/app/hello")
    public String appHello(@microframework.annotations.RequestParam(value = "name", defaultValue = "World") String name) {
        return hello(name);
    }

    @microframework.annotations.GetMapping("/api/hello")
    public String apiHello(@microframework.annotations.RequestParam(value = "name", defaultValue = "World") String name) {
        return hello(name);
    }

    private static String esc(String s) { return s == null ? "" : s.replace("\"", "\\\""); }
}
