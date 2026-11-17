package com.umss.sigesa.application.port.out;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ProgramCatalogPort {

    record ProgramEntry(UUID id, String code, String name) {}

    List<ProgramEntry> search(String query);

    Optional<ProgramEntry> findById(UUID id);

    default Map<UUID, ProgramEntry> findByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        Map<UUID, ProgramEntry> result = new HashMap<>();
        for (UUID id : ids) {
            findById(id).ifPresent(entry -> result.put(id, entry));
        }
        return result;
    }
}
