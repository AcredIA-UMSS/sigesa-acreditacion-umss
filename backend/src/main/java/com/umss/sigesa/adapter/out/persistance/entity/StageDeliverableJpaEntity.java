package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stage_deliverables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageDeliverableJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    private MethodologicalStageJpaEntity stage;

    @Column(name = "deliverable_code", nullable = false, length = 64)
    private String deliverableCode;

    @Column(name = "approval_status", nullable = false, length = 32)
    private String approvalStatus;

    @Column(name = "document_asset_id")
    private UUID documentAssetId;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "technical_observations", columnDefinition = "TEXT")
    private String technicalObservations;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
