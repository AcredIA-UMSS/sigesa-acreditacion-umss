package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class IndicatorJpaAdapter implements IndicatorRepositoryPort {

    private final IndicatorJpaRepository indicatorRepository;
    private final IndicatorStateHistoryJpaRepository historyRepository;

    public IndicatorJpaAdapter(IndicatorJpaRepository indicatorRepository,
                               IndicatorStateHistoryJpaRepository historyRepository) {
        this.indicatorRepository = indicatorRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public Optional<Indicator> findById(UUID id) {
        return indicatorRepository.findById(id).map(this::toDomain);
    }

    @Override
    public IndicatorState getCurrentState(UUID indicatorId) {
        return historyRepository.findTopByIndicatorIdOrderByCreatedAtDesc(indicatorId)
                .map(IndicatorStateHistoryEntity::getNewState)
                .orElse(IndicatorState.PENDIENTE);
    }

    @Override
    public void appendStateHistory(IndicatorStateHistoryEntry entry) {
        IndicatorStateHistoryEntity entity = new IndicatorStateHistoryEntity();
        entity.setId(entry.id());
        entity.setIndicatorId(entry.indicatorId());
        entity.setPreviousState(entry.previousState());
        entity.setNewState(entry.newState());
        entity.setActorId(entry.actorId());
        entity.setActorRole(entry.actorRole());
        entity.setCreatedAt(entry.createdAt());
        historyRepository.save(entity);
    }

    private Indicator toDomain(IndicatorEntity entity) {
        return new Indicator(
                entity.getId(),
                entity.getProgramId(),
                entity.getCriterionId(),
                entity.getPhaseId()
        );
    }
}
