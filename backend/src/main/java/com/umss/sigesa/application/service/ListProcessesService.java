package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.ListProcessesUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessRepositoryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListProcessesService implements ListProcessesUseCase {

    private final AccreditationProcessRepositoryPort processRepository;

    public ListProcessesService(AccreditationProcessRepositoryPort processRepository) {
        this.processRepository = processRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessSummary> list(ProcessStatus status, UUID careerId, String period) {
        return processRepository.findAll(status, careerId, period).stream()
                .map(this::toSummary)
                .toList();
    }

    private ProcessSummary toSummary(AccreditationProcess process) {
        return new ProcessSummary(
                process.getId(),
                process.getTemplateId(),
                process.getCareerId(),
                process.getPeriod(),
                process.getType(),
                process.getStatus(),
                process.getTaxonomySnapshotVersion(),
                process.getCreatedAt()
        );
    }
}
