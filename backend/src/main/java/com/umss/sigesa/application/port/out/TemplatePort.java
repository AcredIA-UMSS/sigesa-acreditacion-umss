package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Template;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface TemplatePort {
    Optional<Template> findById(UUID templateId);

    /** Solo metadatos (nombre, tipo, estado) — sin árbol fases/subfases. */
    Optional<Template> findMetadataById(UUID templateId);

    default Map<UUID, Template> findMetadataByIds(Collection<UUID> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Template> result = new HashMap<>();
        for (UUID id : templateIds) {
            findMetadataById(id).ifPresent(template -> result.put(id, template));
        }
        return result;
    }
}
