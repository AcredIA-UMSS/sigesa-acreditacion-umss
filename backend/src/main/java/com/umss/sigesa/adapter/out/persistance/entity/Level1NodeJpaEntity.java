package com.umss.sigesa.adapter.out.persistance.entity;

import com.umss.sigesa.domain.model.PhaseState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "level1_nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Level1NodeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "level1_order", nullable = false)
    private Integer order;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String status = PhaseState.ABIERTA.name();

    @Column(name = "legacy_phase_id")
    private UUID legacyPhaseId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private AccreditationProcessJpaEntity process;

    @OneToMany(mappedBy = "level1Node", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Level2NodeJpaEntity> level2Nodes = new ArrayList<>();
}
