package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import com.umss.sigesa.application.port.out.EvidenceLookupPort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EvidenceLookupJpaAdapter implements EvidenceLookupPort {

    private final EvidenceJpaRepository evidenceRepository;
    private final IndicatorJpaRepository indicatorRepository;

    public EvidenceLookupJpaAdapter(EvidenceJpaRepository evidenceRepository,
                                    IndicatorJpaRepository indicatorRepository) {
        this.evidenceRepository = evidenceRepository;
        this.indicatorRepository = indicatorRepository;
    }

    @Override
    public Optional<EvidenceRef> findById(UUID evidenceId) {
        return evidenceRepository.findById(evidenceId).flatMap(this::toRef);
    }

    @Override
    public List<EvidenceRef> listByProgramId(UUID programId) {
        List<EvidenceRef> refs = new ArrayList<>();
        for (IndicatorEntity indicator : indicatorRepository.findByProgramId(programId)) {
            evidenceRepository.findByIndicatorId(indicator.getId())
                    .flatMap(evidence -> toRef(evidence, indicator))
                    .ifPresent(refs::add);
        }
        return List.copyOf(refs);
    }

    private Optional<EvidenceRef> toRef(EvidenceEntity evidence) {
        return indicatorRepository.findById(evidence.getIndicatorId())
                .flatMap(indicator -> toRef(evidence, indicator));
    }

    private Optional<EvidenceRef> toRef(EvidenceEntity evidence, IndicatorEntity indicator) {
        if (!indicator.getId().equals(evidence.getIndicatorId())) {
            return Optional.empty();
        }
        return Optional.of(new EvidenceRef(
                evidence.getId(),
                indicator.getId(),
                indicator.getProgramId(),
                indicator.getCode(),
                indicator.getTitle()));
    }
}
