package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ImprovementPlanItemEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataImprovementPlanItemRepository;
import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ImprovementPlanItemJpaAdapter implements ImprovementPlanItemPort {

    private final SpringDataImprovementPlanItemRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ImprovementPlanItem> findByProcessId(UUID processId) {
        return repository.findByProcessIdOrderByCreatedAtAsc(processId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImprovementPlanItem> findByIdAndProcessId(UUID itemId, UUID processId) {
        return repository.findByIdAndProcessId(itemId, processId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProcessIdAndSourceIndicatorId(UUID processId, UUID sourceIndicatorId) {
        return repository.existsByProcessIdAndSourceIndicatorId(processId, sourceIndicatorId);
    }

    @Override
    @Transactional
    public ImprovementPlanItem save(ImprovementPlanItem item) {
        ImprovementPlanItemEntity entity = repository.findById(item.getId())
                .orElseGet(ImprovementPlanItemEntity::new);
        entity.setId(item.getId());
        entity.setProcessId(item.getProcessId());
        entity.setSourceIndicatorId(item.getSourceIndicatorId());
        entity.setObjective(item.getObjective());
        entity.setActivity(item.getActivity());
        entity.setResponsibleId(item.getResponsibleId());
        entity.setDeadline(item.getDeadline());
        entity.setProgress(item.getProgress());
        entity.setJustification(item.getJustification());
        entity.setProgressEvidenceAssetId(item.getProgressEvidenceAssetId());
        entity.setCreatedAt(item.getCreatedAt());
        entity.setUpdatedAt(item.getUpdatedAt());
        return toDomain(repository.save(entity));
    }

    private ImprovementPlanItem toDomain(ImprovementPlanItemEntity entity) {
        return ImprovementPlanItem.builder()
                .id(entity.getId())
                .processId(entity.getProcessId())
                .sourceIndicatorId(entity.getSourceIndicatorId())
                .objective(entity.getObjective())
                .activity(entity.getActivity())
                .responsibleId(entity.getResponsibleId())
                .deadline(entity.getDeadline())
                .progress(entity.getProgress())
                .justification(entity.getJustification())
                .progressEvidenceAssetId(entity.getProgressEvidenceAssetId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
