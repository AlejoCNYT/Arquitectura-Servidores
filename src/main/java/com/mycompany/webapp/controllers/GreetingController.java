package com.mycompany.webapp.controllers;

import microframework.annotations.GetMapping;
import microframework.annotations.RequestParam;
import microframework.annotations.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        long n = counter.incrementAndGet();
        return "Hola " + name + " (#" + n + ")";
    }
}
