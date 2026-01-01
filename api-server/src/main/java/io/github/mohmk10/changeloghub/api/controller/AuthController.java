package io.github.mohmk10.changeloghub.api.controller;

import io.github.mohmk10.changeloghub.api.dto.UserDto;
import io.github.mohmk10.changeloghub.api.entity.User;
import io.github.mohmk10.changeloghub.api.security.JwtTokenProvider;
import io.github.mohmk10.changeloghub.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.toDto(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(Map.of("valid", false));
        }

        String token = authHeader.substring(7);
        boolean isValid = jwtTokenProvider.validateToken(token);

        if (isValid) {
            UUID userId = jwtTokenProvider.getUserIdFromToken(token);
            return ResponseEntity.ok(Map.of("valid", true, "userId", userId.toString()));
        }

        return ResponseEntity.ok(Map.of("valid", false));
    }
}
