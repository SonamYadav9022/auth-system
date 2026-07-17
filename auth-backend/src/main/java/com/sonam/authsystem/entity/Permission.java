package com.sonam.authsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. "user:read", "user:delete", "admin:manage-users"
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;
}
