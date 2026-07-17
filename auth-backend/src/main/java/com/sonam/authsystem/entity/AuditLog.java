package com.sonam.authsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditEventType eventType;

    // Email involved in the event. Doesn't have to correspond to a real
    // account - e.g. a failed login attempt against an email that doesn't exist.
    @Column(length = 150)
    private String email;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 255)
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
