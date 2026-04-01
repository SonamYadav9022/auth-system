package com.sonam.authsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "Protected APIs", description = "Role-based access endpoints")
public class ApiController {

    @GetMapping("/api/public")
    @Operation(summary = "Public endpoint - no auth needed")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        return ResponseEntity.ok(Map.of("message", "This is public content. Anyone can see this!"));
    }

    @GetMapping("/api/user")
    @Operation(summary = "User endpoint - USER or ADMIN role required",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> userEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Welcome! You have USER access."));
    }

    @GetMapping("/api/admin")
    @Operation(summary = "Admin endpoint - ADMIN role only",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> adminEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Welcome Admin! You have full access."));
    }
}