package com.umss.sigesa.adapter.out.persistance.mapper;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateJpaEntity;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class TemplatePersistenceMapper {

    public Template toDomain(TemplateJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return toDomainMetadata(entity);
    }

    /** Metadatos de plantilla (enriquecimiento de listados). */
    public Template toDomainMetadata(TemplateJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Template.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .status(parseStatus(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TemplateJpaEntity toJpaEntity(Template domain) {
        LocalDateTime now = LocalDateTime.now();
        return TemplateJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .type(domain.getType())
                .evaluatorModel(domain.getType())
                .status(domain.getStatus() != null ? domain.getStatus().name() : TemplateStatus.DRAFT.name())
                .createdAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : now)
                .updatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt() : now)
                .level1Nodes(new ArrayList<>())
                .build();
    }

    private TemplateStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return TemplateStatus.PUBLISHED;
        }
        return TemplateStatus.valueOf(status);
    }
}
