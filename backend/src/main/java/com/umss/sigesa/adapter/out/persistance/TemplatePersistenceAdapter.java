package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.mapper.TemplatePersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateRepository;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.model.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TemplatePersistenceAdapter implements TemplatePort {

    private final SpringDataTemplateRepository repository;
    private final TemplatePersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Template> findById(UUID templateId) {
        return repository.findById(templateId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Template> findMetadataById(UUID templateId) {
        return repository.findById(templateId).map(mapper::toDomainMetadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Template> findMetadataByIds(Collection<UUID> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return Map.of();
        }
        return repository.findAllById(templateIds).stream()
                .map(mapper::toDomainMetadata)
                .collect(Collectors.toMap(Template::getId, Function.identity()));
    }
}
