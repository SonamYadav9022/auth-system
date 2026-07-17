package com.sonam.authsystem.controller;

import com.sonam.authsystem.entity.AuditLog;
import com.sonam.authsystem.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Admin - Audit Log", description = "View security-relevant events (logins, lockouts, token reuse, deletions)")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('audit:read')")
    @Operation(summary = "List audit log entries, newest first",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<AuditLog>> listAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size)));
    }
}
