package com.sonam.authsystem.config;

import com.sonam.authsystem.entity.Permission;
import com.sonam.authsystem.entity.Role;
import com.sonam.authsystem.entity.RolePermission;
import com.sonam.authsystem.repository.PermissionRepository;
import com.sonam.authsystem.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Grants are modeled as role -> permissions rather than user -> permissions,
 * so adding a new permission to a role doesn't require touching every user
 * row. Runs once at startup; safe to re-run since it checks for existing
 * rows before inserting.
 */
@Component
@RequiredArgsConstructor
public class PermissionSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    private static final Map<String, String> PERMISSIONS = Map.of(
            "user:read", "View your own profile",
            "user:update", "Update your own profile",
            "user:delete", "Delete a user account",
            "admin:read", "View admin-only content",
            "admin:manage-users", "List and remove any user account",
            "audit:read", "View the security audit log"
    );

    private static final Map<Role, List<String>> ROLE_PERMISSIONS = Map.of(
            Role.USER, List.of("user:read", "user:update"),
            Role.ADMIN, List.of("user:read", "user:update", "user:delete",
                    "admin:read", "admin:manage-users", "audit:read")
    );

    @Override
    public void run(String... args) {
        PERMISSIONS.forEach((name, description) ->
                permissionRepository.findByName(name).orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(name);
                    permission.setDescription(description);
                    return permissionRepository.save(permission);
                })
        );

        ROLE_PERMISSIONS.forEach((role, permissionNames) -> {
            List<RolePermission> existing = rolePermissionRepository.findByRole(role);
            List<String> alreadyGranted = existing.stream()
                    .map(rp -> rp.getPermission().getName())
                    .toList();

            for (String permissionName : permissionNames) {
                if (alreadyGranted.contains(permissionName)) {
                    continue;
                }
                Permission permission = permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> new IllegalStateException("Permission not seeded: " + permissionName));

                RolePermission mapping = new RolePermission();
                mapping.setRole(role);
                mapping.setPermission(permission);
                rolePermissionRepository.save(mapping);
            }
        });
    }
}
