package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateEntity;
import com.umss.sigesa.application.port.out.TemplateRepositoryPort;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.Taxonomy;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TemplateJpaAdapter implements TemplateRepositoryPort {

    private final TemplateJpaRepository jpaRepository;

    public TemplateJpaAdapter(TemplateJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Template> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private Template toDomain(TemplateEntity entity) {
        return new Template(
                entity.getId(),
                entity.isValidated(),
                new Taxonomy(entity.getTaxonomyVersion())
        );
    }
}
