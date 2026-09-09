package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.ProcessPersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProcessQueryJpaAdapter implements ProcessQueryPort {

    private final SpringDataAccreditationProcessRepository repository;
    private final ProcessPersistenceMapper mapper;

    private static final String ARCHIVED_STATUS = "ARCHIVED";

    @Override
    public List<ProcessListItem> findAllSummaryItems() {
        return repository.findAllByOrderByStartDateDesc().stream()
                .filter(entity -> !ARCHIVED_STATUS.equals(entity.getStatus()))
                .map(this::toListItem)
                .toList();
    }

    @Override
    public List<ProcessListItem> findSummaryItemsByCareerIds(List<UUID> careerIds) {
        if (careerIds == null || careerIds.isEmpty()) {
            return List.of();
        }
        return repository.findByCareerIdInOrderByStartDateDesc(careerIds).stream()
                .filter(entity -> !ARCHIVED_STATUS.equals(entity.getStatus()))
                .map(this::toListItem)
                .toList();
    }

    @Override
    public Optional<AccreditationProcess> findDetailById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    private ProcessListItem toListItem(AccreditationProcessJpaEntity entity) {
        return new ProcessListItem(
                entity.getId(),
                entity.getCareerId(),
                entity.getTemplateId(),
                entity.getStatus(),
                entity.getStartDate()
        );
    }
}
