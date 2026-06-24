package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Template;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepositoryPort {
    Optional<Template> findById(UUID id);
}

