package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "improvement_plan_items")
@Getter
@Setter
@NoArgsConstructor
public class ImprovementPlanItemEntity {

    @Id
    private UUID id;

    @Column(name = "process_id", nullable = false)
    private UUID processId;

    @Column(name = "source_indicator_id")
    private UUID sourceIndicatorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String objective;

    @Column(columnDefinition = "TEXT")
    private String activity;

    @Column(name = "responsible_id")
    private UUID responsibleId;

    private LocalDate deadline;

    @Column(nullable = false)
    private int progress;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(name = "progress_evidence_asset_id")
    private UUID progressEvidenceAssetId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
