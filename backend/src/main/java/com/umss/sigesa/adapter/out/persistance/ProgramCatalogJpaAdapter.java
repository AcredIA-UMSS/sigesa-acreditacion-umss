package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ProgramJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataProgramRepository;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProgramCatalogJpaAdapter implements ProgramCatalogPort {

    private final SpringDataProgramRepository repository;

    public ProgramCatalogJpaAdapter(SpringDataProgramRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ProgramEntry> search(String query) {
        String normalized = normalizeQuery(query);
        return repository.searchActive(normalized).stream()
                .map(this::toEntry)
                .toList();
    }

    @Override
    public Optional<ProgramEntry> findById(UUID id) {
        return repository.findById(id)
                .filter(ProgramJpaEntity::isActive)
                .map(this::toEntry);
    }

    private ProgramEntry toEntry(ProgramJpaEntity entity) {
        return new ProgramEntry(entity.getId(), entity.getCode(), entity.getName());
    }

    private static String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
