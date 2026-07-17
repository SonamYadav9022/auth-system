package com.sonam.authsystem.service;

import com.sonam.authsystem.entity.AuditEventType;
import com.sonam.authsystem.entity.RefreshToken;
import com.sonam.authsystem.entity.User;
import com.sonam.authsystem.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogService auditLogService;

    @Value("${security.refresh.expiration-days}")
    private long expirationDays;

    public record RotationResult(User user, String rawToken) {}

    public String createToken(User user) {
        String raw = generateRawToken();

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(raw));
        entity.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));
        entity.setCreatedAt(LocalDateTime.now());
        refreshTokenRepository.save(entity);

        return raw;
    }

    /**
     * Validates a refresh token and issues a new one in its place (rotation).
     * If a token that was already rotated gets presented again, that's a signal
     * the token was stolen and someone else already used it - so instead of
     * quietly failing, every refresh token for that user is revoked.
     */
    @Transactional
    public RotationResult rotate(String rawToken, String ipAddress, String userAgent) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllForUser(stored.getUser().getId());
            auditLogService.record(AuditEventType.TOKEN_REUSE_DETECTED, stored.getUser().getEmail(),
                    ipAddress, userAgent, "Rotated refresh token was reused - all sessions revoked");
            throw new BadCredentialsException("Refresh token reuse detected, all sessions revoked");
        }

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return new RotationResult(stored.getUser(), createToken(stored.getUser()));
    }

    /** Returns the email of the user the revoked token belonged to, if it existed. */
    public Optional<String> revoke(String rawToken) {
        return refreshTokenRepository.findByTokenHash(hash(rawToken))
                .map(t -> {
                    t.setRevoked(true);
                    refreshTokenRepository.save(t);
                    return t.getUser().getEmail();
                });
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            // Every JVM ships SHA-256, this should be unreachable.
            throw new IllegalStateException(e);
        }
    }
}
