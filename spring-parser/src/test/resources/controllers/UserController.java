package com.example.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    public List<User> getUsers(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) String search) {
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return null;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody @Valid UserCreateRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return null;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
    }

    @Deprecated
    @GetMapping("/{id}/legacy")
    public User getLegacyUser(@PathVariable Long id) {
        return null;
    }
}

class User {
    private Long id;
    private String name;
    private String email;
}

class UserCreateRequest {
    private String name;
    private String email;
}

class UserUpdateRequest {
    private String name;
    private String email;
}
