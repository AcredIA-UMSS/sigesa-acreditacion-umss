package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evidence_upload_lock")
@Getter
@Setter
@NoArgsConstructor
public class EvidenceUploadLockEntity {

    @Id
    @Column(name = "indicator_id", nullable = false)
    private UUID indicatorId;

    @Column(name = "acquired_at", nullable = false)
    private Instant acquiredAt;
}
