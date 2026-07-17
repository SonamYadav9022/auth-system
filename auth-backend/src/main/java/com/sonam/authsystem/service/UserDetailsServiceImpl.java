package com.sonam.authsystem.service;

import com.sonam.authsystem.entity.User;
import com.sonam.authsystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<GrantedAuthority> authorities = new ArrayList<>();
        // Spring Security expects roles prefixed with "ROLE_" for hasRole()/hasAnyRole()
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        // Fine-grained permissions for hasAuthority(), e.g. "user:delete"
        permissionService.getPermissionNames(user.getRole())
                .forEach(name -> authorities.add(new SimpleGrantedAuthority(name)));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}