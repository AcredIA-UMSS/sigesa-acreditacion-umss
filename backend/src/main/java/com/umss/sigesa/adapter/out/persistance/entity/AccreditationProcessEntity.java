package com.umss.sigesa.adapter.out.persistance.entity;

import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accreditation_process")
@Getter
@Setter
@NoArgsConstructor
public class AccreditationProcessEntity {

    @Id
    private UUID id;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "career_id", nullable = false)
    private UUID careerId;

    @Column(nullable = false, length = 20)
    private String period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProcessType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProcessStatus status;

    @Column(name = "taxonomy_snapshot_version", nullable = false, length = 50)
    private String taxonomySnapshotVersion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
