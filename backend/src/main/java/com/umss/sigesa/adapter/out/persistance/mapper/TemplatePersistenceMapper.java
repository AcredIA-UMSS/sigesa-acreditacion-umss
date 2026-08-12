package com.umss.sigesa.adapter.out.persistance.mapper;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplatePhaseJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateSubphaseJpaEntity;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateStatus;
import com.umss.sigesa.domain.model.TemplateSubphase;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TemplatePersistenceMapper {

    public Template toDomain(TemplateJpaEntity entity) {
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
                .phases(entity.getPhases() == null ? List.of() : entity.getPhases().stream()
                        .map(this::toPhaseDomain)
                        .collect(Collectors.toList()))
                .build();
    }

    /** Metadatos de plantilla sin cargar fases/subfases (enriquecimiento de listados). */
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
                .phases(List.of())
                .build();
    }

    public TemplateJpaEntity toJpaEntity(Template domain) {
        LocalDateTime now = LocalDateTime.now();
        TemplateJpaEntity entity = TemplateJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .type(domain.getType())
                .status(domain.getStatus() != null ? domain.getStatus().name() : TemplateStatus.DRAFT.name())
                .createdAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : now)
                .updatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt() : now)
                .phases(new ArrayList<>())
                .build();

        if (domain.getPhases() != null) {
            domain.getPhases().forEach(phaseDomain -> {
                TemplatePhaseJpaEntity phaseEntity = TemplatePhaseJpaEntity.builder()
                        .id(phaseDomain.getId())
                        .name(phaseDomain.getName())
                        .order(phaseDomain.getOrder())
                        .description(phaseDomain.getDescription())
                        .template(entity)
                        .subphases(new ArrayList<>())
                        .build();

                if (phaseDomain.getSubphases() != null) {
                    phaseDomain.getSubphases().forEach(subphaseDomain -> phaseEntity.getSubphases().add(
                            TemplateSubphaseJpaEntity.builder()
                                    .id(subphaseDomain.getId())
                                    .name(subphaseDomain.getName())
                                    .order(subphaseDomain.getOrder())
                                    .referenceUrl(subphaseDomain.getReferenceUrl())
                                    .description(subphaseDomain.getDescription())
                                    .templatePhase(phaseEntity)
                                    .build()
                    ));
                }
                entity.getPhases().add(phaseEntity);
            });
        }
        return entity;
    }

    private TemplatePhase toPhaseDomain(TemplatePhaseJpaEntity entity) {
        return TemplatePhase.builder()
                .id(entity.getId())
                .name(entity.getName())
                .order(entity.getOrder())
                .description(entity.getDescription())
                .subphases(entity.getSubphases() == null ? List.of() : entity.getSubphases().stream()
                        .map(this::toSubphaseDomain).collect(Collectors.toList()))
                .build();
    }

    private TemplateSubphase toSubphaseDomain(TemplateSubphaseJpaEntity entity) {
        return TemplateSubphase.builder()
                .id(entity.getId())
                .name(entity.getName())
                .order(entity.getOrder())
                .referenceUrl(entity.getReferenceUrl())
                .description(entity.getDescription())
                .build();
    }

    private TemplateStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return TemplateStatus.PUBLISHED;
        }
        return TemplateStatus.valueOf(status);
    }
}
