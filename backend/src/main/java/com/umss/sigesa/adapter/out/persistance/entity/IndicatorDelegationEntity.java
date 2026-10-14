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
@Table(name = "indicator_delegations")
@Getter
@Setter
@NoArgsConstructor
public class IndicatorDelegationEntity {

    @Id
    private UUID id;

    @Column(name = "process_id", nullable = false)
    private UUID processId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "level1_id")
    private UUID level1Id;

    @Column(name = "level2_id")
    private UUID level2Id;

    @Column(name = "indicator_id")
    private UUID indicatorId;

    @Column(nullable = false, length = 32)
    private String permission;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
