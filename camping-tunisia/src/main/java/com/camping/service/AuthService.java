package com.camping.service;

import com.camping.dto.AuthDTO;
import com.camping.model.User;
import com.camping.repository.UserRepository;
import com.camping.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;

    @Value("${app.admin-signup-code:CAMPINGTN-ADMIN}")
    private String adminSignupCode;

    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        Set<String> roles = new HashSet<>(Set.of("USER"));
        if (request.getAdminCode() != null && !request.getAdminCode().isBlank()) {
            if (!request.getAdminCode().trim().equals(adminSignupCode)) {
                throw new RuntimeException("Invalid admin code");
            }
            roles.add("ADMIN");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .governorate(request.getGovernorate())
                .roles(roles)
                .active(true)
                .build();
        user = userRepository.save(user);
        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(ud);
        return buildAuthResponse(user, token);
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(ud);
        return buildAuthResponse(user, token);
    }

    public AuthDTO.AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildAuthResponse(user, null);
    }

    public AuthDTO.AuthResponse updateProfile(String currentEmail, AuthDTO.ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new RuntimeException("Email already registered");
            }
        });

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setGovernorate(request.getGovernorate());
        user.setCity(request.getCity());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new RuntimeException("Password must contain at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        UserDetails ud = userDetailsService.loadUserByUsername(saved.getEmail());
        String token = jwtUtil.generateToken(ud);
        return buildAuthResponse(saved, token);
    }

    private AuthDTO.AuthResponse buildAuthResponse(User user, String token) {
        return AuthDTO.AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .governorate(user.getGovernorate())
                .city(user.getCity())
                .roles(user.getRoles())
                .userId(user.getId())
                .build();
    }
}
