package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorObservationJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataIndicatorObservationRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeIndicatorRepository;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.domain.model.IndicatorObservation;
import com.umss.sigesa.domain.model.IndicatorObservationStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class NormativeIndicatorObservationJpaAdapter implements NormativeIndicatorObservationPort {

    private final SpringDataIndicatorObservationRepository observationRepository;
    private final SpringDataNormativeIndicatorRepository indicatorRepository;

    public NormativeIndicatorObservationJpaAdapter(
            SpringDataIndicatorObservationRepository observationRepository,
            SpringDataNormativeIndicatorRepository indicatorRepository) {
        this.observationRepository = observationRepository;
        this.indicatorRepository = indicatorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndicatorObservation> findLatestOpenByIndicatorId(UUID indicatorId) {
        return observationRepository
                .findFirstByIndicator_IdAndStatusOrderByCreatedAtDesc(
                        indicatorId, IndicatorObservationStatus.OPEN.name())
                .map(NormativeIndicatorObservationJpaAdapter::toDomain);
    }

    @Override
    @Transactional
    public IndicatorObservation save(IndicatorObservation observation) {
        NormativeIndicatorJpaEntity indicator = indicatorRepository.findById(observation.getIndicatorId())
                .orElseThrow(() -> new IllegalStateException(
                        "Indicador normativo no encontrado: " + observation.getIndicatorId()));

        IndicatorObservationJpaEntity entity = new IndicatorObservationJpaEntity();
        entity.setId(observation.getId());
        entity.setIndicator(indicator);
        entity.setAuthorId(observation.getAuthorId());
        entity.setAuthorRole(observation.getAuthorRole());
        entity.setBody(observation.getBody());
        entity.setStatus(observation.getStatus() != null
                ? observation.getStatus().name()
                : IndicatorObservationStatus.OPEN.name());
        entity.setResolvedAt(observation.getResolvedAt());
        entity.setResolvedVersionId(observation.getResolvedVersionId());
        entity.setCreatedAt(observation.getCreatedAt());
        entity.setUpdatedAt(observation.getUpdatedAt());
        return toDomain(observationRepository.save(entity));
    }

    private static IndicatorObservation toDomain(IndicatorObservationJpaEntity entity) {
        return IndicatorObservation.builder()
                .id(entity.getId())
                .indicatorId(entity.getIndicator().getId())
                .authorId(entity.getAuthorId())
                .authorRole(entity.getAuthorRole())
                .body(entity.getBody())
                .status(entity.getStatus() != null
                        ? IndicatorObservationStatus.valueOf(entity.getStatus())
                        : IndicatorObservationStatus.OPEN)
                .resolvedAt(entity.getResolvedAt())
                .resolvedVersionId(entity.getResolvedVersionId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
