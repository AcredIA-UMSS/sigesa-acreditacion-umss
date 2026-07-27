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
@Table(name = "evidence")
@Getter
@Setter
@NoArgsConstructor
public class EvidenceEntity {

    @Id
    private UUID id;

    @Column(name = "indicator_id", nullable = false, unique = true)
    private UUID indicatorId;

    @Column(name = "latest_version_id")
    private UUID latestVersionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
