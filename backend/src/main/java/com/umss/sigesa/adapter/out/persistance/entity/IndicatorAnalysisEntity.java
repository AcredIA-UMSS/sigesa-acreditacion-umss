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
        name = "indicator_analysis",
        uniqueConstraints = @UniqueConstraint(name = "uk_indicator_analysis_indicator", columnNames = "indicator_id")
)
@Getter
@Setter
@NoArgsConstructor
public class IndicatorAnalysisEntity {

    @Id
    private UUID id;

    @Column(name = "indicator_id", nullable = false, unique = true)
    private UUID indicatorId;

    @Column(name = "process_id", nullable = false)
    private UUID processId;

    @Column(name = "foda_quadrant", length = 32)
    private String fodaQuadrant;

    @Column(length = 32)
    private String compliance;

    @Column(columnDefinition = "TEXT")
    private String synthesis;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
