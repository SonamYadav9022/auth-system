package com.sonam.authsystem.controller;

import com.sonam.authsystem.dto.UserSummaryResponse;
import com.sonam.authsystem.entity.User;
import com.sonam.authsystem.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Demonstrates permission-based authorization: these endpoints check for a
 * specific granted permission (e.g. "admin:manage-users") rather than just
 * the ADMIN role, so access can later be tuned per-permission without
 * introducing new roles.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "Permission-guarded user management endpoints")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('admin:read')")
    @Operation(summary = "List all users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserSummaryResponse>> listUsers() {
        List<UserSummaryResponse> users = userRepository.findAll().stream()
                .map(u -> new UserSummaryResponse(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .toList();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    @Operation(summary = "Delete a user account", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (target.getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new RuntimeException("You can't delete your own account from this endpoint");
        }

        userRepository.delete(target);
        return ResponseEntity.noContent().build();
    }
}
