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
@Table(name = "evidence_version")
@Getter
@Setter
@NoArgsConstructor
public class EvidenceVersionEntity {

    @Id
    private UUID id;

    @Column(name = "evidence_id", nullable = false)
    private UUID evidenceId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "criterion_id", nullable = false)
    private UUID criterionId;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
