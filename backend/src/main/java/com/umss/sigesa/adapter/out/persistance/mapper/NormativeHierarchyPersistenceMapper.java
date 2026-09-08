package com.umss.sigesa.adapter.out.persistance.mapper;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorObservationJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level2NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level3NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel2NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel3NodeJpaEntity;
import com.umss.sigesa.domain.model.IndicatorObservation;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.SubphaseObservationStatus;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NormativeHierarchyPersistenceMapper {

    public Level1Node toLevel1Domain(Level1NodeJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Level1Node.builder()
                .id(entity.getId())
                .name(entity.getName())
                .order(entity.getOrder())
                .description(entity.getDescription())
                .status(parsePhaseState(entity.getStatus()))
                .legacyPhaseId(entity.getLegacyPhaseId())
                .level2Nodes(entity.getLevel2Nodes() == null ? List.of() : entity.getLevel2Nodes().stream()
                        .map(this::toLevel2Domain)
                        .toList())
                .build();
    }

    public Level2Node toLevel2Domain(Level2NodeJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Level2Node.builder()
                .id(entity.getId())
                .name(entity.getName())
                .order(entity.getOrder())
                .description(entity.getDescription())
                .level3Nodes(entity.getLevel3Nodes() == null ? List.of() : entity.getLevel3Nodes().stream()
                        .map(this::toLevel3Domain)
                        .toList())
                .build();
    }

    public Level3Node toLevel3Domain(Level3NodeJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Level3Node.builder()
                .id(entity.getId())
                .name(entity.getName())
                .order(entity.getOrder())
                .description(entity.getDescription())
                .indicators(entity.getIndicators() == null ? List.of() : entity.getIndicators().stream()
                        .map(this::toIndicatorDomain)
                        .toList())
                .build();
    }

    public NormativeIndicator toIndicatorDomain(NormativeIndicatorJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return NormativeIndicator.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .weight(entity.getWeight())
                .order(entity.getOrder())
                .referenceUrl(entity.getReferenceUrl())
                .status(parseIndicatorState(entity.getStatus()))
                .legacySubphaseId(entity.getLegacySubphaseId())
                .build();
    }

    public TemplateLevel1Node toTemplateLevel1Domain(TemplateLevel1NodeJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return TemplateLevel1Node.builder()
                .id(entity.getId())
                .name(entity.getName())
                .order(entity.getOrder())
                .description(entity.getDescription())
                .level2Nodes(entity.getLevel2Nodes() == null ? List.of() : entity.getLevel2Nodes().stream()
                        .map(this::toTemplateLevel2Domain)
                        .toList())
                .build();
    }

    public TemplateLevel2Node toTemplateLevel2Domain(TemplateLevel2NodeJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return TemplateLevel2Node.builder()
                .id(entity.getId())
                .name(entity.getName())
                .order(entity.getOrder())
                .description(entity.getDescription())
                .level3Nodes(entity.getLevel3Nodes() == null ? List.of() : entity.getLevel3Nodes().stream()
                        .map(this::toTemplateLevel3Domain)
                        .toList())
                .build();
    }

    public TemplateLevel3Node toTemplateLevel3Domain(TemplateLevel3NodeJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return TemplateLevel3Node.builder()
                .id(entity.getId())
                .name(entity.getName())
                .order(entity.getOrder())
                .description(entity.getDescription())
                .indicators(entity.getIndicators() == null ? List.of() : entity.getIndicators().stream()
                        .map(this::toTemplateIndicatorDomain)
                        .toList())
                .build();
    }

    public TemplateNormativeIndicator toTemplateIndicatorDomain(TemplateIndicatorJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return TemplateNormativeIndicator.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .weight(entity.getWeight())
                .order(entity.getOrder())
                .referenceUrl(entity.getReferenceUrl())
                .build();
    }

    public IndicatorObservation toObservationDomain(IndicatorObservationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return IndicatorObservation.builder()
                .id(entity.getId())
                .indicatorId(entity.getIndicator() != null ? entity.getIndicator().getId() : null)
                .authorId(entity.getAuthorId())
                .authorRole(entity.getAuthorRole())
                .body(entity.getBody())
                .status(parseObservationStatus(entity.getStatus()))
                .resolvedAt(entity.getResolvedAt())
                .resolvedVersionId(entity.getResolvedVersionId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PhaseState parsePhaseState(String status) {
        if (status == null || status.isBlank()) {
            return PhaseState.ABIERTA;
        }
        return PhaseState.valueOf(status);
    }

    private IndicatorState parseIndicatorState(String status) {
        if (status == null || status.isBlank()) {
            return IndicatorState.PENDIENTE;
        }
        return IndicatorState.valueOf(status);
    }

    private SubphaseObservationStatus parseObservationStatus(String status) {
        if (status == null || status.isBlank()) {
            return SubphaseObservationStatus.OPEN;
        }
        return SubphaseObservationStatus.valueOf(status);
    }
}
