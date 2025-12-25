package com.example.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    @GetMapping
    public List<Order> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CookieValue(name = "session", required = false) String sessionId) {
        return null;
    }

    @GetMapping("/{orderId}")
    public Optional<Order> getOrderById(@PathVariable Long orderId) {
        return Optional.empty();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        return null;
    }

    @PutMapping("/{orderId}/status")
    public Order updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return null;
    }

    @RequestMapping(value = "/{orderId}/cancel", method = RequestMethod.POST)
    public Order cancelOrder(@PathVariable Long orderId) {
        return null;
    }
}

class Order {
    private Long id;
    private String status;
    private Double total;
}

class OrderRequest {
    private List<OrderItem> items;
    private String shippingAddress;
}

class OrderItem {
    private String productId;
    private int quantity;
}
