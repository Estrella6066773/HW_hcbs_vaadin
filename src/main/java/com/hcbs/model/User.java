package com.hcbs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.hcbs.util.PhoneNumbers;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user", indexes = {
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public User(String username, String email, String passwordHash, String fullName, UserRole role) {
        this.username = username;
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = createdAt;
        email = normalizeEmail(email);
        if (username != null) {
            username = username.trim();
        }
        phone = PhoneNumbers.normalize(phone);
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
        email = normalizeEmail(email);
        if (username != null) {
            username = username.trim();
        }
        phone = PhoneNumbers.normalize(phone);
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
