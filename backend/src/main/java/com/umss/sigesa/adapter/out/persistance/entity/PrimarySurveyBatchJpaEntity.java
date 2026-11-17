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
@Table(name = "primary_survey_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrimarySurveyBatchJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "process_id", nullable = false)
    private AccreditationProcessJpaEntity process;

    @Column(nullable = false, length = 32)
    private String audience;

    @Column(name = "responses_count", nullable = false)
    private Integer responsesCount;

    @Column(name = "document_asset_id")
    private UUID documentAssetId;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
}
