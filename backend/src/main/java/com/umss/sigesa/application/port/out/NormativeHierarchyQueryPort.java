package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lectura del árbol normativo v2.0 (proceso y plantilla).
 * Coexiste con {@link ProcessStructurePort} legacy hasta retiro M5.
 */
public interface NormativeHierarchyQueryPort {

    record ProcessNormativeTree(UUID processId, List<Level1Node> level1Nodes) {
    }

    record TemplateNormativeTree(UUID templateId, List<TemplateLevel1Node> level1Nodes) {
    }

    record NormativeIndicatorContext(
            UUID indicatorId,
            UUID processId,
            UUID careerId,
            UUID level1Id,
            String level1Name,
            String code,
            String description,
            IndicatorState status) {
    }

    record Level1Context(
            UUID level1Id,
            UUID processId,
            UUID careerId,
            String level1Name,
            PhaseState status) {
    }

    record IndicatorStatusItem(
            UUID indicatorId,
            String code,
            String name,
            IndicatorState status,
            Integer order) {
    }

    boolean hasNormativeTreeForProcess(UUID processId);

    boolean hasNormativeTreeForTemplate(UUID templateId);

    long countLevel1NodesByProcessId(UUID processId);

    long countIndicatorsByProcessId(UUID processId);

    long countLevel1NodesByTemplateId(UUID templateId);

    long countIndicatorsByTemplateId(UUID templateId);

    Optional<ProcessNormativeTree> findProcessTree(UUID processId);

    Optional<TemplateNormativeTree> findTemplateTree(UUID templateId);

    Optional<NormativeIndicator> findIndicatorById(UUID indicatorId);

    Optional<TemplateNormativeIndicator> findTemplateIndicatorById(UUID indicatorId);

    Optional<NormativeIndicatorContext> findIndicatorContext(UUID indicatorId);

    Optional<Level1Context> findLevel1Context(UUID level1Id);

    List<IndicatorStatusItem> listIndicatorsWithStatusByLevel1Id(UUID level1Id);
}
