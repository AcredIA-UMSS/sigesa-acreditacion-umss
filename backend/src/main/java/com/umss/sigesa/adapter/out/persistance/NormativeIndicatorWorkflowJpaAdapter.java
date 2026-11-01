package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataLevel1NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeIndicatorRepository;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.PhaseState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NormativeIndicatorWorkflowJpaAdapter implements NormativeIndicatorWorkflowPort {

    private final SpringDataNormativeIndicatorRepository indicatorRepository;
    private final SpringDataLevel1NodeRepository level1NodeRepository;
    private final EvidenceJpaRepository evidenceRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean hasBlockingEvidence(UUID indicatorId) {
        return evidenceRepository.countByNormativeIndicatorId(indicatorId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWorkflowStarted(UUID indicatorId) {
        NormativeIndicatorJpaEntity entity = indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));
        IndicatorState state = IndicatorState.valueOf(entity.getStatus());
        return state != IndicatorState.PENDIENTE || hasBlockingEvidence(indicatorId);
    }

    @Override
    @Transactional
    public void updateIndicatorStatus(UUID indicatorId, IndicatorState newStatus) {
        NormativeIndicatorJpaEntity entity = indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));
        entity.setStatus(newStatus.name());
        entity.setUpdatedAt(LocalDateTime.now());
        indicatorRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateLevel1Status(UUID level1Id, PhaseState newStatus) {
        Level1NodeJpaEntity entity = level1NodeRepository.findById(level1Id)
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 1 no encontrado: " + level1Id));
        entity.setStatus(newStatus.name());
        entity.setUpdatedAt(LocalDateTime.now());
        level1NodeRepository.save(entity);
    }
}
