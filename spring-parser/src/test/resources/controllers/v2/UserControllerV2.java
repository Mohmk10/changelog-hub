package com.example.controllers.v2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * V2 of UserController with breaking changes:
 * - Removed: GET /{id}/legacy endpoint
 * - Changed: GET / now requires 'status' parameter (was optional)
 * - Changed: GET /{id} parameter type from Long to String
 * - Added: GET /{id}/profile endpoint
 */
@RestController
@RequestMapping("/api/users")
public class UserControllerV2 {

    // BREAKING: Added required parameter 'status'
    @GetMapping
    public List<User> getUsers(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = true) String status) {  // NEW REQUIRED PARAM
        return null;
    }

    // BREAKING: Changed parameter type from Long to String
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {  // Long -> String
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

    // REMOVED: GET /{id}/legacy endpoint (breaking change)

    // NEW: Added profile endpoint
    @GetMapping("/{id}/profile")
    public UserProfile getUserProfile(@PathVariable String id) {
        return null;
    }
}

class User {
    private String id;  // Changed from Long to String
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
