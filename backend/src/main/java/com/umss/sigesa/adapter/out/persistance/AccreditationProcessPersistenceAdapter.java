package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;
import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.ProcessPersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccreditationProcessPersistenceAdapter implements AccreditationProcessPort {

    private final SpringDataAccreditationProcessRepository repository;
    private final ProcessPersistenceMapper mapper;

    @Override
    public boolean existsActiveProcessByCareer(UUID careerId) {
        return repository.existsByCareerIdAndStatus(careerId, "ACTIVE");
    }

    @Override
    public boolean existsActiveProcessByCareerAndTypeAndPeriod(UUID careerId, ProcessType type, String period) {
        return repository.existsByCareerIdAndTypeAndPeriodAndStatus(
                careerId, type.name(), period, ProcessStatus.ACTIVE.name());
    }

    @Override
    public AccreditationProcess save(AccreditationProcess process) {
        AccreditationProcessJpaEntity entity = mapper.toJpaEntity(process);
        AccreditationProcessJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<AccreditationProcess> findAll(ProcessStatus status, UUID careerId, String period) {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .filter(process -> status == null || process.getStatus() == status)
                .filter(process -> careerId == null || process.getCareerId().equals(careerId))
                .filter(process -> period == null || period.isBlank() || process.getPeriod().equals(period))
                .toList();
    }

    @Override
    public Optional<AccreditationProcess> findById(UUID processId) {
        return repository.findById(processId).map(mapper::toDomain);
    }
}
