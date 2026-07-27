package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateSubphase;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TemplatePersistenceAdapter implements TemplatePort {

    private final SpringDataTemplateRepository repository;

    @Override
    public Optional<Template> findById(UUID templateId) {
        return repository.findById(templateId).map(entity ->
                Template.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .type(entity.getType())
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
                        .build()
        );
    }
}