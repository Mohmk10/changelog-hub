package com.example.services;

import org.springframework.stereotype.Service;

@Service
public class NonController {

    public void doSomething() {
        
    }

    public String processData(String input) {
        return input.toUpperCase();
    }
}
