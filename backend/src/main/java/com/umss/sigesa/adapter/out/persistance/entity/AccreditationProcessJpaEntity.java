package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "accreditation_processes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccreditationProcessJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "career_id", nullable = false)
    private UUID careerId;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(length = 20)
    private String period;

    @Column(length = 20)
    private String type;

    @Column(nullable = false)
    private String status;

    @Column(name = "taxonomy_snapshot_version", length = 50)
    private String taxonomySnapshotVersion;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PhaseJpaEntity> phases = new ArrayList<>();
}
