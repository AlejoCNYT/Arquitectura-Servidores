package com.mycompany.webapp.controllers;

import microframework.annotations.GetMapping;
import microframework.annotations.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "Greetings from Microframework!";
    }
}
