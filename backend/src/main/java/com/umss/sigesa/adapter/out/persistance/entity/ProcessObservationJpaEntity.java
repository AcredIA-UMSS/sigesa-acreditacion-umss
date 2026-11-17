package com.umss.sigesa.adapter.out.persistance.entity;

import com.umss.sigesa.domain.model.observation.ComplianceLevel;
import com.umss.sigesa.domain.model.observation.ObservationKind;
import com.umss.sigesa.domain.model.observation.ObservationPriority;
import com.umss.sigesa.domain.model.observation.ObservationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "process_observation")
@Getter
@Setter
@NoArgsConstructor
public class ProcessObservationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "process_id", nullable = false)
    private UUID processId;

    @Column(name = "evidence_id", nullable = false)
    private UUID evidenceId;

    @Column(name = "indicator_id", nullable = false)
    private UUID indicatorId;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ObservationKind kind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ObservationPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplianceLevel compliance;

    @Column(length = 2000)
    private String recommendation;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ObservationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "observation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ProcessObservationMessageJpaEntity> messages = new ArrayList<>();
}
