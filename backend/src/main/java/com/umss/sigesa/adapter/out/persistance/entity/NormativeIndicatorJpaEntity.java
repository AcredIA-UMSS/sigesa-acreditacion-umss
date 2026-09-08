package com.umss.sigesa.adapter.out.persistance.entity;

import com.umss.sigesa.domain.model.IndicatorState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "indicators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormativeIndicatorJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal weight = BigDecimal.ONE;

    @Column(name = "indicator_order", nullable = false)
    private Integer order;

    @Column(name = "reference_url", nullable = false, length = 2048)
    private String referenceUrl;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String status = IndicatorState.PENDIENTE.name();

    @Column(name = "legacy_subphase_id")
    private UUID legacySubphaseId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level3_id", nullable = false)
    private Level3NodeJpaEntity level3Node;
}
