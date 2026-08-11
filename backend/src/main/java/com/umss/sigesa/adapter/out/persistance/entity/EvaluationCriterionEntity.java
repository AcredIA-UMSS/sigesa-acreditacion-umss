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
@Table(name = "evaluation_criterion")
@Getter
@Setter
@NoArgsConstructor
public class EvaluationCriterionEntity {

    @Id
    private UUID id;

    @Column(name = "dimension_id", nullable = false)
    private UUID dimensionId;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 2147483647)
    private String description;
}
