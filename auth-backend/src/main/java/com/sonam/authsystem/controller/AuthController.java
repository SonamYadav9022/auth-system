package com.sonam.authsystem.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sonam.authsystem.dto.AuthResponse;
import com.sonam.authsystem.dto.LoginRequest;
import com.sonam.authsystem.dto.RefreshRequest;
import com.sonam.authsystem.dto.RegisterRequest;
import com.sonam.authsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController//this made by two annotations - @Controller @ResponseBody
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")

public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  HttpServletRequest httpRequest) {
        // Log registration for monitoring
        logger.info("New registration attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request, clientIp(httpRequest), userAgent(httpRequest)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        // Fixed the variable name from loginRequest to request
        logger.info("Login attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request, clientIp(httpRequest), userAgent(httpRequest)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a valid refresh token for a new access token (rotates the refresh token too)")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request,
                                                 HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken(), clientIp(httpRequest), userAgent(httpRequest)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request,
                                        HttpServletRequest httpRequest) {
        authService.logout(request.getRefreshToken(), clientIp(httpRequest), userAgent(httpRequest));
        return ResponseEntity.noContent().build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
