package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subphase_observation")
@Getter
@Setter
@NoArgsConstructor
public class SubphaseObservationEntity {

    @Id
    private UUID id;

    @Column(name = "subphase_id", nullable = false)
    private UUID subphaseId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "author_role", nullable = false, length = 10)
    private String authorRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_version_id")
    private UUID resolvedVersionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
