package com.sonam.authsystem.service;

import com.sonam.authsystem.entity.Role;
import com.sonam.authsystem.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    // Role -> permissions rarely changes at runtime in this app, so it's
    // cheap to cache rather than hit the DB on every authenticated request.
    private final ConcurrentHashMap<Role, Set<String>> cache = new ConcurrentHashMap<>();

    public Set<String> getPermissionNames(Role role) {
        return cache.computeIfAbsent(role, r -> rolePermissionRepository.findByRole(r).stream()
                .map(rp -> rp.getPermission().getName())
                .collect(Collectors.toUnmodifiableSet()));
    }

    public void invalidate() {
        cache.clear();
    }
}
