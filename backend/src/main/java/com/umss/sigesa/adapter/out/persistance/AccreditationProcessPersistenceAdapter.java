package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.ProcessPersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccreditationProcessPersistenceAdapter implements AccreditationProcessPort {

    private final SpringDataAccreditationProcessRepository repository;
    private final ProcessPersistenceMapper mapper;

    @Override
    public boolean existsActiveProcessByCareerAndTemplateType(UUID careerId, String templateType) {
        return repository.existsActiveByCareerIdAndTemplateType(careerId, templateType);
    }

    @Override
    public AccreditationProcess save(AccreditationProcess process) {
        AccreditationProcessJpaEntity entity = mapper.toJpaEntity(process);
        AccreditationProcessJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
