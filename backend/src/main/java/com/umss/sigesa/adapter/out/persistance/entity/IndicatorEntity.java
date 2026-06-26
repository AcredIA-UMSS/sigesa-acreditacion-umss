package com.umss.sigesa.adapter.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "indicator")
@Getter
@Setter
@NoArgsConstructor
public class IndicatorEntity {

    @Id
    private UUID id;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "criterion_id", nullable = false)
    private UUID criterionId;

    @Column(name = "phase_id")
    private UUID phaseId;
}
