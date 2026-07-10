package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.GetProcessUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessRepositoryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetProcessService implements GetProcessUseCase {

    private final AccreditationProcessRepositoryPort processRepository;

    public GetProcessService(AccreditationProcessRepositoryPort processRepository) {
        this.processRepository = processRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcessDetail> getById(UUID processId) {
        return processRepository.findById(processId).map(this::toDetail);
    }

    private ProcessDetail toDetail(AccreditationProcess process) {
        return new ProcessDetail(
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
