package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorAnalysisEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataIndicatorAnalysisRepository;
import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.domain.model.ComplianceJudgment;
import com.umss.sigesa.domain.model.FodaQuadrant;
import com.umss.sigesa.domain.model.IndicatorAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IndicatorAnalysisJpaAdapter implements IndicatorAnalysisPort {

    private final SpringDataIndicatorAnalysisRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<IndicatorAnalysis> findByIndicatorId(UUID indicatorId) {
        return repository.findByIndicatorId(indicatorId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<IndicatorAnalysis> findByProcessId(UUID processId) {
        return repository.findByProcessId(processId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public IndicatorAnalysis save(IndicatorAnalysis analysis) {
        IndicatorAnalysisEntity entity = repository.findByIndicatorId(analysis.getIndicatorId())
                .orElseGet(IndicatorAnalysisEntity::new);
        if (entity.getId() == null) {
            entity.setId(analysis.getId() != null ? analysis.getId() : UUID.randomUUID());
        }
        entity.setIndicatorId(analysis.getIndicatorId());
        entity.setProcessId(analysis.getProcessId());
        entity.setFodaQuadrant(analysis.getFodaQuadrant() != null ? analysis.getFodaQuadrant().name() : null);
        entity.setCompliance(analysis.getCompliance() != null ? analysis.getCompliance().name() : null);
        entity.setSynthesis(analysis.getSynthesis());
        entity.setAuthorId(analysis.getAuthorId());
        entity.setCreatedAt(analysis.getCreatedAt());
        entity.setUpdatedAt(analysis.getUpdatedAt());
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public long countCompleteByProcessId(UUID processId) {
        return repository.countCompleteByProcessId(processId);
    }

    private IndicatorAnalysis toDomain(IndicatorAnalysisEntity entity) {
        return IndicatorAnalysis.builder()
                .id(entity.getId())
                .indicatorId(entity.getIndicatorId())
                .processId(entity.getProcessId())
                .fodaQuadrant(FodaQuadrant.from(entity.getFodaQuadrant()).orElse(null))
                .compliance(ComplianceJudgment.from(entity.getCompliance()).orElse(null))
                .synthesis(entity.getSynthesis())
                .authorId(entity.getAuthorId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
