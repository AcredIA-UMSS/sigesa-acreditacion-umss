package com.umss.sigesa.adapter.out.persistance.entity;

import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_state", nullable = false, length = 20)
    private IndicatorState previousState;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_state", nullable = false, length = 20)
    private IndicatorState newState;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_role", nullable = false, length = 10)
    private Role actorRole;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
