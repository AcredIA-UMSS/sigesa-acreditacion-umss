package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AppUser {

    private final UUID id;
    private final Email email;
    private final Role role;
    private UserStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;

    public AppUser(UUID id, Email email, Role role, UserStatus status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, email, role, status, createdAt, updatedAt, null, null, null);
    }

    public AppUser(UUID id, Email email, Role role, UserStatus status,
                   LocalDateTime createdAt, LocalDateTime updatedAt,
                   String firstName, String lastName, String phoneNumber) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public UUID getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = UserStatus.DEACTIVATED;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFullName() {
        return UserProfile.fullName(firstName, lastName);
    }
}
