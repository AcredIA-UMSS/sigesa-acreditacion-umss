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
@Table(name = "indicator_state_history")
@Getter
@Setter
@NoArgsConstructor
public class IndicatorStateHistoryEntity {

    @Id
    private UUID id;

    @Column(name = "indicator_id", nullable = false)
    private UUID indicatorId;

    @Column(name = "previous_state", length = 20)
    private String previousState;

    @Column(name = "new_state", nullable = false, length = 20)
    private String newState;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(nullable = false, length = 10)
    private String role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
