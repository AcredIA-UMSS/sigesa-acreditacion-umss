package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.TemplatePersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateRepository;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TemplateManagementJpaAdapter implements TemplateManagementPort {

    private final SpringDataTemplateRepository templateRepository;
    private final SpringDataAccreditationProcessRepository processRepository;
    private final TemplatePersistenceMapper mapper;

    @Override
    @Transactional
    public Template save(Template template) {
        TemplateJpaEntity entity = templateRepository.findById(template.getId())
                .orElseGet(() -> mapper.toJpaEntity(template));

        entity.setName(template.getName());
        entity.setDescription(template.getDescription());
        entity.setType(template.getType());
        entity.setStatus(template.getStatus() != null ? template.getStatus().name() : TemplateStatus.DRAFT.name());
        entity.setUpdatedAt(template.getUpdatedAt() != null ? template.getUpdatedAt() : LocalDateTime.now());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        return mapper.toDomain(templateRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Template> findByIdForEdit(UUID id) {
        return templateRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Template> findAll() {
        return templateRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Template> findByStatus(TemplateStatus status) {
        return templateRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Template> findByStatusAndType(TemplateStatus status, String type) {
        return templateRepository.findByStatusAndType(status.name(), type).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsProcessByTemplateId(UUID templateId) {
        return processRepository.existsByTemplateId(templateId);
    }

    @Override
    @Transactional
    public void delete(UUID templateId) {
        templateRepository.deleteById(templateId);
    }
}
