package com.sonam.authsystem.repository;

import com.sonam.authsystem.entity.Role;
import com.sonam.authsystem.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRole(Role role);
}
