package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserProgramAssignment {

    private final UUID id;
    private final UUID userId;
    private final UUID programId;
    private final LocalDateTime assignedAt;
    private LocalDateTime revokedAt;

    public UserProgramAssignment(UUID id, UUID userId, UUID programId,
                                 LocalDateTime assignedAt, LocalDateTime revokedAt) {
        this.id = id;
        this.userId = userId;
        this.programId = programId;
        this.assignedAt = assignedAt;
        this.revokedAt = revokedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getProgramId() {
        return programId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return revokedAt == null;
    }
}
