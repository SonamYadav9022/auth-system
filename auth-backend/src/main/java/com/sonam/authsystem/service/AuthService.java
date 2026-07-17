package com.sonam.authsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.sonam.authsystem.repository.UserRepository;
import com.sonam.authsystem.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.sonam.authsystem.dto.*;
import com.sonam.authsystem.entity.AuditEventType;
import com.sonam.authsystem.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;

    @Value("${security.login.max-attempts}")
    private int maxAttempts;

    @Value("${security.login.lockout-minutes}")
    private long lockoutMinutes;

    private static final DateTimeFormatter LOCK_MSG_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        auditLogService.record(AuditEventType.REGISTER, user.getEmail(), ipAddress, userAgent, null);

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.createToken(user);
        return new AuthResponse(accessToken, refreshToken, user.getName(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // Don't reveal whether the email exists - same message as a bad password
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditLogService.record(AuditEventType.LOGIN_FAILURE, request.getEmail(),
                            ipAddress, userAgent, "No account with this email");
                    return new BadCredentialsException("Invalid email or password");
                });

        if (isLocked(user)) {
            auditLogService.record(AuditEventType.LOGIN_FAILURE, user.getEmail(), ipAddress, userAgent,
                    "Attempted login while account locked");
            throw new LockedException("Account locked due to too many failed login attempts. Try again after "
                    + user.getLockedUntil().format(LOCK_MSG_FORMAT));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            boolean justLocked = registerFailedAttempt(user);
            auditLogService.record(AuditEventType.LOGIN_FAILURE, user.getEmail(), ipAddress, userAgent,
                    "Incorrect password");
            if (justLocked) {
                auditLogService.record(AuditEventType.ACCOUNT_LOCKED, user.getEmail(), ipAddress, userAgent,
                        "Locked after " + maxAttempts + " failed attempts");
            }
            throw new BadCredentialsException("Invalid email or password");
        }

        resetFailedAttempts(user);
        auditLogService.record(AuditEventType.LOGIN_SUCCESS, user.getEmail(), ipAddress, userAgent, null);

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.createToken(user);
        return new AuthResponse(accessToken, refreshToken, user.getName(), user.getEmail(), user.getRole());
    }

    public AuthResponse refresh(String refreshToken, String ipAddress, String userAgent) {
        RefreshTokenService.RotationResult result = refreshTokenService.rotate(refreshToken, ipAddress, userAgent);
        User user = result.user();
        auditLogService.record(AuditEventType.TOKEN_REFRESH, user.getEmail(), ipAddress, userAgent, null);
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(accessToken, result.rawToken(), user.getName(), user.getEmail(), user.getRole());
    }

    public void logout(String refreshToken, String ipAddress, String userAgent) {
        refreshTokenService.revoke(refreshToken)
                .ifPresent(email -> auditLogService.record(AuditEventType.LOGOUT, email, ipAddress, userAgent, null));
    }

    private boolean isLocked(User user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
    }

    /** @return true if this failed attempt just pushed the account into a lockout */
    private boolean registerFailedAttempt(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        boolean justLocked = false;
        if (attempts >= maxAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
            justLocked = true;
        }
        userRepository.save(user);
        return justLocked;
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }
}