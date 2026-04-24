package com.example.meetball.domain.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    public static final String PROVIDER_GOOGLE = "GOOGLE";
    public static final String STATUS_ACTIVE = "ACTIVE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(name = "social_provider", nullable = false, length = 100)
    private String socialProvider;

    @Column(name = "social_identifier", nullable = false, length = 100)
    private String socialIdentifier;

    @Column(length = 30)
    private String name;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 10)
    private String gender;

    @Column(name = "account_status", nullable = false, length = 20)
    private String accountStatus = STATUS_ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Account google(String email, String socialIdentifier) {
        Account account = new Account();
        account.email = email;
        account.socialProvider = PROVIDER_GOOGLE;
        account.socialIdentifier = socialIdentifier;
        account.accountStatus = STATUS_ACTIVE;
        account.createdAt = LocalDateTime.now();
        return account;
    }

    public void updateGoogleIdentity(String email, String socialIdentifier) {
        this.email = email;
        this.socialProvider = PROVIDER_GOOGLE;
        this.socialIdentifier = socialIdentifier;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBasicInfo(String name, String phoneNumber, LocalDate birthDate, String gender) {
        this.name = sanitize(name, 30);
        this.phoneNumber = sanitize(phoneNumber, 30);
        this.birthDate = birthDate;
        this.gender = sanitize(gender, 10);
        this.updatedAt = LocalDateTime.now();
    }

    private String sanitize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
