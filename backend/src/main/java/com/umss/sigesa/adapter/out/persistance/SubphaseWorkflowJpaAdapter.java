package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.SubphaseJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataSubphaseRepository;
import com.umss.sigesa.application.port.out.SubphaseWorkflowPort;
import com.umss.sigesa.domain.model.SubphaseState;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class SubphaseWorkflowJpaAdapter implements SubphaseWorkflowPort {

    private final EvidenceJpaRepository evidenceRepository;
    private final SpringDataSubphaseRepository subphaseRepository;

    public SubphaseWorkflowJpaAdapter(EvidenceJpaRepository evidenceRepository,
                                      SpringDataSubphaseRepository subphaseRepository) {
        this.evidenceRepository = evidenceRepository;
        this.subphaseRepository = subphaseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasBlockingEvidence(UUID subphaseId) {
        return evidenceRepository.countBySubphaseId(subphaseId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID subphaseId) {
        return subphaseRepository.existsById(subphaseId);
    }

    @Override
    @Transactional(readOnly = true)
    public SubphaseState getCurrentState(UUID subphaseId) {
        SubphaseJpaEntity entity = subphaseRepository.findById(subphaseId)
                .orElseThrow(() -> new IllegalArgumentException("Subfase no encontrada: " + subphaseId));
        return SubphaseState.valueOf(entity.getStatus());
    }

    @Override
    @Transactional
    public void updateState(UUID subphaseId, SubphaseState newState) {
        SubphaseJpaEntity entity = subphaseRepository.findById(subphaseId)
                .orElseThrow(() -> new IllegalArgumentException("Subfase no encontrada: " + subphaseId));
        entity.setStatus(newState.name());
        subphaseRepository.save(entity);
    }
}
