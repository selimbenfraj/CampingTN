package com.camping.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User entity – works with both H2 (dev) and PostgreSQL (prod).
 *
 * Roles are stored as a simple comma-separated string in one column
 * (avoids a join table for this demo).  Spring Security sees them as
 * List<String> via the @Convert below.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)   // UUID primary key
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;    // BCrypt hash

    private String phone;
    private String governorate;

    /** Stored as "USER,ADMIN" – converted by RolesConverter */
    @Convert(converter = RolesConverter.class)
    @Column(name = "roles")
    private List<String> roles;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // ─── Password-reset fields ────────────────────────────────────────────────
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ─── JPA AttributeConverter for List<String> ─────────────────────────────
    @Converter
    public static class RolesConverter
            implements AttributeConverter<List<String>, String> {

        @Override
        public String convertToDatabaseColumn(List<String> roles) {
            if (roles == null || roles.isEmpty()) return "USER";
            return String.join(",", roles);
        }

        @Override
        public List<String> convertToEntityAttribute(String value) {
            if (value == null || value.isBlank()) return List.of("USER");
            return List.of(value.split(","));
        }
    }
}