package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import com.umss.sigesa.application.port.out.IndicatorStateHistoryPort;
import com.umss.sigesa.domain.model.Role;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class IndicatorStateHistoryJpaAdapter implements IndicatorStateHistoryPort {

    private final IndicatorStateHistoryJpaRepository repository;

    public IndicatorStateHistoryJpaAdapter(IndicatorStateHistoryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void recordTransition(UUID indicatorId, String previousState, String newState, UUID actorId, Role role) {
        IndicatorStateHistoryEntity entity = new IndicatorStateHistoryEntity();
        entity.setId(UUID.randomUUID());
        entity.setIndicatorId(indicatorId);
        entity.setPreviousState(previousState);
        entity.setNewState(newState);
        entity.setActorId(actorId);
        entity.setRole(role.name());
        entity.setCreatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findLatestState(UUID indicatorId) {
        return repository.findTopByIndicatorIdOrderByCreatedAtDesc(indicatorId)
                .map(IndicatorStateHistoryEntity::getNewState);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findLatestHistoryId(UUID indicatorId) {
        return repository.findTopByIndicatorIdOrderByCreatedAtDesc(indicatorId)
                .map(IndicatorStateHistoryEntity::getId);
    }
}
