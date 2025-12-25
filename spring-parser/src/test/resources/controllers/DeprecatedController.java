package com.example.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Deprecated
@RestController
@RequestMapping("/api/v1/legacy")
public class DeprecatedController {

    @GetMapping("/items")
    public List<String> getItems() {
        return null;
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable String id) {
        return null;
    }
}
