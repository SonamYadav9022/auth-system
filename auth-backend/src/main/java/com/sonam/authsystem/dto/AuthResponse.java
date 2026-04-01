package com.sonam.authsystem.dto;

import com.sonam.authsystem.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private Role Role;
}