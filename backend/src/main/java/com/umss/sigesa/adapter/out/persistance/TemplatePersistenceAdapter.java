package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.mapper.TemplatePersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateRepository;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.model.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TemplatePersistenceAdapter implements TemplatePort {

    private final SpringDataTemplateRepository repository;
    private final TemplatePersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Template> findById(UUID templateId) {
        return repository.findWithPhasesById(templateId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Template> findMetadataById(UUID templateId) {
        return repository.findById(templateId).map(mapper::toDomainMetadata);
    }
}
