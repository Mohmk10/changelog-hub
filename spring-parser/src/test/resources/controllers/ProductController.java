package com.example.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<Product> getProducts(
            @RequestParam(name = "category", required = false) String category,
            @RequestHeader("X-Store-Id") String storeId) {
        return null;
    }

    @GetMapping("/{productId}")
    public Product getProduct(@PathVariable("productId") String id) {
        return null;
    }

    @PostMapping
    public Product createProduct(@RequestBody ProductRequest request) {
        return null;
    }

    @PatchMapping("/{productId}")
    public Product updateProduct(
            @PathVariable("productId") String id,
            @RequestBody ProductRequest request) {
        return null;
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable("productId") String id) {
    }
}

class Product {
    private String id;
    private String name;
    private Double price;
    private String category;
}

class ProductRequest {
    private String name;
    private Double price;
    private String category;
}
