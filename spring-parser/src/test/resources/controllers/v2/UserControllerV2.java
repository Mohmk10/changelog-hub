package com.example.controllers.v2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserControllerV2 {

    @GetMapping
    public List<User> getUsers(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = true) String status) {  
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {  
        return null;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody @Valid UserCreateRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody UserUpdateRequest request) {
        return null;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String id) {
    }

    @GetMapping("/{id}/profile")
    public UserProfile getUserProfile(@PathVariable String id) {
        return null;
    }
}

class User {
    private String id;  
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

class UserProfile {
    private String id;
    private String displayName;
    private String avatarUrl;
    private String bio;
}
