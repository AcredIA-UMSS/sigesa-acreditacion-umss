package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "process_freeze")
@Getter
@Setter
@NoArgsConstructor
public class ProcessFreezeEntity {

    @Id
    private UUID id;

    @Column(name = "process_id", nullable = false, unique = true)
    private UUID processId;

    @Column(nullable = false, length = 32)
    private String mode;

    @Column(name = "frozen_at", nullable = false)
    private LocalDateTime frozenAt;

    @Column(name = "frozen_by")
    private UUID frozenBy;

    @Column(name = "lifted_at")
    private LocalDateTime liftedAt;

    @Column(name = "lifted_by")
    private UUID liftedBy;
}
