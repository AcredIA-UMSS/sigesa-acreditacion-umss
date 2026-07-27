package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateSubphase;
import com.umss.sigesa.domain.model.Taxonomy;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TemplatePersistenceAdapter implements TemplatePort {

    private final SpringDataTemplateRepository repository;

    @Override
    public Optional<Template> findById(UUID templateId) {
        return repository.findById(templateId).map(this::toDomain);
    }

    @Override
    public List<Template> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Template save(Template template) {
        TemplateJpaEntity entity = repository.findById(template.getId())
                .orElseGet(() -> TemplateJpaEntity.builder().id(template.getId()).build());

        entity.setName(template.getName());
        entity.setType(template.getType());
        entity.setValidated(template.isValidated());
        entity.setTaxonomyVersion(template.getTaxonomy() != null ? template.getTaxonomy().version() : null);
        entity.setActivePeriod(template.getActivePeriod());
        entity.setActivatedAt(template.getActivatedAt());

        return toDomain(repository.save(entity));
    }

    private Template toDomain(TemplateJpaEntity entity) {
        return Template.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .validated(entity.isValidated())
                .taxonomy(entity.getTaxonomyVersion() != null ? new Taxonomy(entity.getTaxonomyVersion()) : null)
                .activePeriod(entity.getActivePeriod())
                .activatedAt(entity.getActivatedAt())
                .phases(entity.getPhases().stream().map(p ->
                        TemplatePhase.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .order(p.getOrder())
                                .subphases(p.getSubphases().stream().map(s ->
                                        TemplateSubphase.builder()
                                                .id(s.getId())
                                                .name(s.getName())
                                                .order(s.getOrder())
                                                .build()
                                ).collect(Collectors.toList()))
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}