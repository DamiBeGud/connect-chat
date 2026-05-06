package com.connectchat.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

    private static final Random RANDOM = new Random();

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column
    private String nickname;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String country;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    @Column(name = "verification_code_expires_at")
    private Instant verificationCodeExpiresAt;

    @Builder.Default
    @Column(name = "is_validation_code_sent", nullable = false)
    private boolean isValidationCodeSent = false;

    public void markValidationCodeSent() {
        isValidationCodeSent = true;
    }

    public void markVerified() {
        isVerified = true;
        verifiedAt = Instant.now();
        verificationCode = null;
        verificationCodeExpiresAt = null;
    }

    @PrePersist
    void addTimestampsBeforeCreate() {
        Instant now = Instant.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (verificationCode == null) {
            generateValidationCode();
        }

        if (verificationCodeExpiresAt == null) {
            verificationCodeExpiresAt = Instant.now().plus(
                5,
                ChronoUnit.MINUTES
            );
        }

        verifiedAt = null;
    }

    @PreUpdate
    void updateTimestampBeforeUpdate() {
        updatedAt = Instant.now();
    }

    void generateValidationCode() {
        verificationCode = String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
