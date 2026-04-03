package com.sonam.authsystem.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sonam.authsystem.dto.AuthResponse;
import com.sonam.authsystem.dto.LoginRequest;
import com.sonam.authsystem.dto.RegisterRequest;
import com.sonam.authsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Log registration for monitoring
        logger.info("New registration attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Fixed the variable name from loginRequest to request
        logger.info("Login attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }
}