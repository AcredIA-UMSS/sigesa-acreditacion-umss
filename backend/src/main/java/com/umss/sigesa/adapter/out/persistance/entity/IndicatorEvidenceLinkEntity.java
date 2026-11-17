package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "indicator_evidence_links",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_indicator_evidence_link", columnNames = {"indicator_id", "document_asset_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class IndicatorEvidenceLinkEntity {

    @Id
    private UUID id;

    @Column(name = "indicator_id", nullable = false)
    private UUID indicatorId;

    @Column(name = "document_asset_id", nullable = false)
    private UUID documentAssetId;

    @Column(name = "evidence_id", nullable = false)
    private UUID evidenceId;

    @Column(name = "is_primary_reference", nullable = false)
    private boolean primaryReference;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
