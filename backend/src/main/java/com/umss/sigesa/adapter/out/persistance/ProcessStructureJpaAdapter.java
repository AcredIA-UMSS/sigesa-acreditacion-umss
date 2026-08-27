package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.PhaseJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.SubphaseJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.ProcessPersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataPhaseRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataSubphaseRepository;
import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.Subphase;
import com.umss.sigesa.domain.model.SubphaseState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProcessStructureJpaAdapter implements ProcessStructurePort {

    private final SpringDataAccreditationProcessRepository processRepository;
    private final SpringDataPhaseRepository phaseRepository;
    private final SpringDataSubphaseRepository subphaseRepository;
    private final ProcessPersistenceMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public AccreditationProcess loadActiveProcess(UUID processId) {
        return mapper.toDomain(loadProcessWithTree(processId));
    }

    @Override
    @Transactional
    public Phase savePhase(UUID processId, Phase phase) {
        ensureProcessExists(processId);

        PhaseJpaEntity phaseEntity = phase.getId() == null ? null
                : phaseRepository.findByIdAndProcessId(phase.getId(), processId).orElse(null);

        if (phaseEntity == null) {
            phaseEntity = PhaseJpaEntity.builder()
                    .name(phase.getName())
                    .order(phase.getOrder())
                    .description(phase.getDescription())
                    .status(phase.getStatus() != null
                            ? phase.getStatus().name()
                            : PhaseState.ABIERTA.name())
                    .process(entityManager.getReference(AccreditationProcessJpaEntity.class, processId))
                    .build();
            phaseEntity = phaseRepository.save(phaseEntity);
        } else {
            phaseEntity.setName(phase.getName());
            phaseEntity.setOrder(phase.getOrder());
            phaseEntity.setDescription(phase.getDescription());
            if (phase.getStatus() != null) {
                phaseEntity.setStatus(phase.getStatus().name());
            }
            phaseEntity = phaseRepository.save(phaseEntity);
        }
        List<SubphaseJpaEntity> subphases = subphaseRepository.findByPhaseIdOrderByOrderAsc(phaseEntity.getId());
        return Phase.builder()
                .id(phaseEntity.getId())
                .name(phaseEntity.getName())
                .order(phaseEntity.getOrder())
                .description(phaseEntity.getDescription())
                .status(phaseEntity.getStatus() != null
                        ? PhaseState.valueOf(phaseEntity.getStatus())
                        : PhaseState.ABIERTA)
                .subphases(subphases.stream().map(this::toSubphaseDomain).toList())
                .build();
    }

    @Override
    @Transactional
    public Subphase saveSubphase(UUID processId, UUID phaseId, Subphase subphase) {
        findPhaseOrThrow(processId, phaseId);

        SubphaseJpaEntity subphaseEntity = subphase.getId() == null ? null
                : subphaseRepository.findByIdAndPhaseId(subphase.getId(), phaseId).orElse(null);

        if (subphaseEntity == null) {
            subphaseEntity = SubphaseJpaEntity.builder()
                    .name(subphase.getName())
                    .order(subphase.getOrder())
                    .referenceUrl(subphase.getReferenceUrl())
                    .description(subphase.getDescription())
                    .requirements(subphase.getRequirements())
                    .status(subphase.getStatus() != null
                            ? subphase.getStatus().name()
                            : SubphaseState.PENDIENTE.name())
                    .phase(entityManager.getReference(PhaseJpaEntity.class, phaseId))
                    .build();
            subphaseEntity = subphaseRepository.save(subphaseEntity);
        } else {
            subphaseEntity.setName(subphase.getName());
            subphaseEntity.setOrder(subphase.getOrder());
            subphaseEntity.setReferenceUrl(subphase.getReferenceUrl());
            subphaseEntity.setDescription(subphase.getDescription());
            subphaseEntity.setRequirements(subphase.getRequirements());
            subphaseEntity = subphaseRepository.save(subphaseEntity);
        }

        return toSubphaseDomain(subphaseEntity);
    }

    @Override
    @Transactional
    public void deletePhase(UUID processId, UUID phaseId) {
        PhaseJpaEntity phaseEntity = findPhaseOrThrow(processId, phaseId);
        phaseRepository.delete(phaseEntity);
    }

    @Override
    @Transactional
    public void deleteSubphase(UUID processId, UUID phaseId, UUID subphaseId) {
        findPhaseOrThrow(processId, phaseId);
        SubphaseJpaEntity subphaseEntity = subphaseRepository.findByIdAndPhaseId(subphaseId, phaseId)
                .orElseThrow(() -> new ProcessNotFoundException("Subfase no encontrada en la fase: " + subphaseId));
        subphaseRepository.delete(subphaseEntity);
    }

    @Override
    @Transactional
    public void reorderPhases(UUID processId, List<UUID> phaseIdsInOrder) {
        List<PhaseJpaEntity> phases = phaseRepository.findByProcessIdOrderByOrderAsc(processId);
        Map<UUID, PhaseJpaEntity> phasesById = phases.stream()
                .collect(Collectors.toMap(PhaseJpaEntity::getId, Function.identity()));

        for (int index = 0; index < phaseIdsInOrder.size(); index++) {
            PhaseJpaEntity phase = phasesById.get(phaseIdsInOrder.get(index));
            if (phase == null) {
                throw new ProcessNotFoundException("Fase no encontrada en el proceso: " + phaseIdsInOrder.get(index));
            }
            phase.setOrder(index + 1);
        }

        phaseRepository.saveAll(phases);
    }

    @Override
    @Transactional
    public void reorderSubphases(UUID processId, UUID phaseId, List<UUID> subphaseIdsInOrder) {
        findPhaseOrThrow(processId, phaseId);
        List<SubphaseJpaEntity> subphases = subphaseRepository.findByPhaseIdOrderByOrderAsc(phaseId);
        Map<UUID, SubphaseJpaEntity> subphasesById = subphases.stream()
                .collect(Collectors.toMap(SubphaseJpaEntity::getId, Function.identity()));

        for (int index = 0; index < subphaseIdsInOrder.size(); index++) {
            SubphaseJpaEntity subphase = subphasesById.get(subphaseIdsInOrder.get(index));
            if (subphase == null) {
                throw new ProcessNotFoundException(
                        "Subfase no encontrada en la fase: " + subphaseIdsInOrder.get(index));
            }
            subphase.setOrder(index + 1);
        }

        subphaseRepository.saveAll(subphases);
    }

    private AccreditationProcessJpaEntity loadProcessWithTree(UUID processId) {
        AccreditationProcessJpaEntity entity = processRepository.findWithPhasesById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        entity.getPhases().forEach(phase -> phase.getSubphases().size());
        return entity;
    }

    private AccreditationProcessJpaEntity findProcessOrThrow(UUID processId) {
        return processRepository.findById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
    }

    private void ensureProcessExists(UUID processId) {
        if (!processRepository.existsById(processId)) {
            throw new ProcessNotFoundException(processId);
        }
    }

    private PhaseJpaEntity findPhaseOrThrow(UUID processId, UUID phaseId) {
        return phaseRepository.findByIdAndProcessId(phaseId, processId)
                .orElseThrow(() -> new ProcessNotFoundException("Fase no encontrada en el proceso: " + phaseId));
    }

    private Phase toPhaseDomain(PhaseJpaEntity entity) {
        PhaseState status = entity.getStatus() != null
                ? PhaseState.valueOf(entity.getStatus())
                : PhaseState.ABIERTA;
        return Phase.builder()
                .id(entity.getId())
                .name(entity.getName())
                .order(entity.getOrder())
                .description(entity.getDescription())
                .status(status)
                .subphases(entity.getSubphases() == null ? List.of() : entity.getSubphases().stream()
                        .map(this::toSubphaseDomain)
                        .toList())
                .build();
    }

    private Subphase toSubphaseDomain(SubphaseJpaEntity entity) {
        SubphaseState status = entity.getStatus() != null
                ? SubphaseState.valueOf(entity.getStatus())
                : SubphaseState.PENDIENTE;
        return Subphase.builder()
                .id(entity.getId())
                .name(entity.getName())
                .order(entity.getOrder())
                .referenceUrl(entity.getReferenceUrl())
                .description(entity.getDescription())
                .requirements(entity.getRequirements())
                .status(status)
                .build();
    }
}
