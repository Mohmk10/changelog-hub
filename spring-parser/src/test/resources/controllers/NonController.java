package com.example.services;

import org.springframework.stereotype.Service;

@Service
public class NonController {

    public void doSomething() {
        // This is not a controller
    }

    public String processData(String input) {
        return input.toUpperCase();
    }
}
