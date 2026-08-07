package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResponsibleAssignment {

    private UUID id;
    private UUID processId;
    private UUID userId;
    private UUID assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime revokedAt;

    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return revokedAt == null;
    }
}
