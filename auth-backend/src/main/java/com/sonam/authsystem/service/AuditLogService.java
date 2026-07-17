package com.sonam.authsystem.service;

import com.sonam.authsystem.entity.AuditEventType;
import com.sonam.authsystem.entity.AuditLog;
import com.sonam.authsystem.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public void record(AuditEventType type, String email, String ipAddress, String userAgent, String details) {
        try {
            AuditLog entry = new AuditLog();
            entry.setEventType(type);
            entry.setEmail(email);
            entry.setIpAddress(ipAddress);
            entry.setUserAgent(userAgent);
            entry.setDetails(details);
            entry.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            // Never let a logging failure take down the auth flow it's observing.
            logger.warn("Failed to write audit log entry for event {}: {}", type, ex.getMessage());
        }
    }
}
