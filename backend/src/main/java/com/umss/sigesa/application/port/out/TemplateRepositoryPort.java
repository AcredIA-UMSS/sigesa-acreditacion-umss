package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Template;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepositoryPort {
    Optional<Template> findById(UUID id);

    List<Template> findAll();

    Template save(Template template);
}

