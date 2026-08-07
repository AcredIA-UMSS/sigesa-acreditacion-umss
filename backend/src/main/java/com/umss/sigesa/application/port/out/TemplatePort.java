package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Template;

import java.util.Optional;
import java.util.UUID;

public interface TemplatePort {
    Optional<Template> findById(UUID templateId);

    /** Solo metadatos (nombre, tipo, estado) — sin árbol fases/subfases. */
    Optional<Template> findMetadataById(UUID templateId);
}
