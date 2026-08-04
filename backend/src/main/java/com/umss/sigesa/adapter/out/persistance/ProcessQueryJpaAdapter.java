package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.ProcessPersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProcessQueryJpaAdapter implements ProcessQueryPort {

    private final SpringDataAccreditationProcessRepository repository;
    private final ProcessPersistenceMapper mapper;

    @Override
    public List<ProcessListItem> findAllSummaryItems() {
        return repository.findAllByOrderByStartDateDesc().stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public List<ProcessListItem> findSummaryItemsByCareerIds(List<UUID> careerIds) {
        if (careerIds == null || careerIds.isEmpty()) {
            return List.of();
        }
        return repository.findByCareerIdInOrderByStartDateDesc(careerIds).stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccreditationProcess> findDetailById(UUID id) {
        return repository.findWithPhasesById(id)
                .map(entity -> {
                    // Subfases: segunda carga lazy (evita MultipleBagFetchException).
                    entity.getPhases().forEach(phase -> phase.getSubphases().size());
                    return mapper.toDomain(entity);
                });
    }

    private ProcessListItem toListItem(AccreditationProcessJpaEntity entity) {
        long phaseCount = repository.countPhasesByProcessId(entity.getId());
        long subphaseCount = repository.countSubphasesByProcessId(entity.getId());
        return new ProcessListItem(
                entity.getId(),
                entity.getCareerId(),
                entity.getTemplateId(),
                entity.getStatus(),
                entity.getStartDate(),
                (int) phaseCount,
                (int) subphaseCount
        );
    }
}
