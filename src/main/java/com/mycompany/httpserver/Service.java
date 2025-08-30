package com.mycompany.httpserver;

@FunctionalInterface
public interface Service {
    String handle(HttpRequest req, HttpResponse resp) throws Exception;
}
