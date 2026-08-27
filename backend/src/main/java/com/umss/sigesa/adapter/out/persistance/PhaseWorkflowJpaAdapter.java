package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.PhaseJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.SubphaseJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataPhaseRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataSubphaseRepository;
import com.umss.sigesa.application.port.out.PhaseWorkflowPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.SubphaseState;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PhaseWorkflowJpaAdapter implements PhaseWorkflowPort {

    private final SpringDataPhaseRepository phaseRepository;
    private final SpringDataSubphaseRepository subphaseRepository;

    public PhaseWorkflowJpaAdapter(SpringDataPhaseRepository phaseRepository,
                                   SpringDataSubphaseRepository subphaseRepository) {
        this.phaseRepository = phaseRepository;
        this.subphaseRepository = subphaseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhaseContext> findPhaseContext(UUID processId, UUID phaseId) {
        return phaseRepository.findWithProcessById(processId, phaseId)
                .map(entity -> new PhaseContext(
                        entity.getId(),
                        entity.getProcess().getId(),
                        entity.getProcess().getCareerId(),
                        entity.getName()));
    }

    @Override
    @Transactional(readOnly = true)
    public PhaseState getCurrentState(UUID phaseId) {
        PhaseJpaEntity entity = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new ProcessNotFoundException("Fase no encontrada: " + phaseId));
        return entity.getStatus() != null
                ? PhaseState.valueOf(entity.getStatus())
                : PhaseState.ABIERTA;
    }

    @Override
    @Transactional
    public void updateState(UUID phaseId, PhaseState newState) {
        PhaseJpaEntity entity = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new ProcessNotFoundException("Fase no encontrada: " + phaseId));
        entity.setStatus(newState.name());
        phaseRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubphaseStatusItem> listSubphasesWithStatus(UUID phaseId) {
        return subphaseRepository.findByPhaseIdOrderByOrderAsc(phaseId).stream()
                .sorted(Comparator.comparingInt(SubphaseJpaEntity::getOrder))
                .map(this::toStatusItem)
                .toList();
    }

    private SubphaseStatusItem toStatusItem(SubphaseJpaEntity entity) {
        SubphaseState status = entity.getStatus() != null
                ? SubphaseState.valueOf(entity.getStatus())
                : SubphaseState.PENDIENTE;
        return new SubphaseStatusItem(
                entity.getId(),
                entity.getName(),
                status,
                entity.getOrder());
    }
}
