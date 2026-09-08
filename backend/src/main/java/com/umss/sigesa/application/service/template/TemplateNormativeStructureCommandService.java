package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.TemplateNormativeStructureUseCases;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.application.port.out.TemplateNormativeStructurePort;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class TemplateNormativeStructureCommandService implements TemplateNormativeStructureUseCases {

    private final TemplateManagementPort templateManagementPort;
    private final TemplateNormativeStructurePort structurePort;
    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final TemplateNormativeStructureGuard guard;

    public TemplateNormativeStructureCommandService(TemplateManagementPort templateManagementPort,
                                                    TemplateNormativeStructurePort structurePort,
                                                    NormativeHierarchyQueryPort hierarchyQueryPort,
                                                    TemplateNormativeStructureGuard guard) {
        this.templateManagementPort = templateManagementPort;
        this.structurePort = structurePort;
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.guard = guard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateLevel1Node addLevel1(UUID templateId, String name, Integer order, String description) {
        guard.loadDraftTemplate(templateManagementPort, templateId);
        List<TemplateLevel1Node> existing = loadLevel1Nodes(templateId);
        guard.ensureUniqueLevel1Order(existing, order, null);

        return structurePort.saveLevel1(templateId, TemplateLevel1Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateLevel1Node updateLevel1(UUID templateId, UUID level1Id, String name, Integer order,
                                           String description) {
        guard.loadDraftTemplate(templateManagementPort, templateId);
        if (order != null) {
            guard.ensureUniqueLevel1Order(loadLevel1Nodes(templateId), order, level1Id);
        }
        return structurePort.updateLevel1(templateId, level1Id, TemplateLevel1Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLevel1(UUID templateId, UUID level1Id) {
        guard.loadDraftTemplate(templateManagementPort, templateId);
        structurePort.deleteLevel1(templateId, level1Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateLevel2Node addLevel2(UUID level1Id, String name, Integer order, String description) {
        UUID templateId = structurePort.findTemplateIdByLevel1(level1Id);
        guard.loadDraftTemplate(templateManagementPort, templateId);
        TemplateLevel1Node level1 = requireLevel1(templateId, level1Id);
        guard.ensureUniqueLevel2Order(level1.getLevel2Nodes(), order, null);

        return structurePort.saveLevel2(level1Id, TemplateLevel2Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateLevel2Node updateLevel2(UUID level2Id, String name, Integer order, String description) {
        UUID level1Id = structurePort.findLevel1IdByLevel2(level2Id);
        UUID templateId = structurePort.findTemplateIdByLevel2(level2Id);
        guard.loadDraftTemplate(templateManagementPort, templateId);
        if (order != null) {
            TemplateLevel1Node level1 = requireLevel1(templateId, level1Id);
            guard.ensureUniqueLevel2Order(level1.getLevel2Nodes(), order, level2Id);
        }
        return structurePort.updateLevel2(level1Id, level2Id, TemplateLevel2Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLevel2(UUID level2Id) {
        UUID level1Id = structurePort.findLevel1IdByLevel2(level2Id);
        UUID templateId = structurePort.findTemplateIdByLevel2(level2Id);
        guard.loadDraftTemplate(templateManagementPort, templateId);
        structurePort.deleteLevel2(level1Id, level2Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateLevel3Node addLevel3(UUID level2Id, String name, Integer order, String description) {
        UUID templateId = structurePort.findTemplateIdByLevel2(level2Id);
        guard.loadDraftTemplate(templateManagementPort, templateId);
        TemplateLevel2Node level2 = requireLevel2(templateId, level2Id);
        guard.ensureUniqueLevel3Order(level2.getLevel3Nodes(), order, null);

        return structurePort.saveLevel3(level2Id, TemplateLevel3Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateLevel3Node updateLevel3(UUID level3Id, String name, Integer order, String description) {
        UUID level2Id = structurePort.findLevel2IdByLevel3(level3Id);
        UUID templateId = structurePort.findTemplateIdByLevel3(level3Id);
        guard.loadDraftTemplate(templateManagementPort, templateId);
        if (order != null) {
            TemplateLevel2Node level2 = requireLevel2(templateId, level2Id);
            guard.ensureUniqueLevel3Order(level2.getLevel3Nodes(), order, level3Id);
        }
        return structurePort.updateLevel3(level2Id, level3Id, TemplateLevel3Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLevel3(UUID level3Id) {
        UUID level2Id = structurePort.findLevel2IdByLevel3(level3Id);
        UUID templateId = structurePort.findTemplateIdByLevel3(level3Id);
        guard.loadDraftTemplate(templateManagementPort, templateId);
        structurePort.deleteLevel3(level2Id, level3Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateNormativeIndicator addIndicator(UUID level3Id, String code, String description, BigDecimal weight,
                                                   Integer order, String referenceUrl) {
        UUID templateId = structurePort.findTemplateIdByLevel3(level3Id);
        guard.loadDraftTemplate(templateManagementPort, templateId);
        TemplateLevel3Node level3 = requireLevel3(templateId, level3Id);
        guard.ensureUniqueIndicatorOrder(level3.getIndicators(), order, null);

        TemplateNormativeIndicator indicator = TemplateNormativeIndicator.builder()
                .code(code)
                .description(description)
                .weight(weight != null ? weight : BigDecimal.ONE)
                .order(order)
                .referenceUrl(referenceUrl)
                .build();
        guard.ensureIndicatorComplete(indicator);
        return structurePort.saveIndicator(level3Id, indicator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateNormativeIndicator updateIndicator(UUID indicatorId, String code, String description,
                                                      BigDecimal weight, Integer order, String referenceUrl) {
        UUID level3Id = structurePort.findLevel3IdByIndicator(indicatorId);
        UUID templateId = structurePort.findTemplateIdByIndicator(indicatorId);
        guard.loadDraftTemplate(templateManagementPort, templateId);
        TemplateNormativeIndicator current = hierarchyQueryPort.findTemplateIndicatorById(indicatorId)
                .orElseThrow(() -> new TemplateNotFoundException("Indicador de plantilla no encontrado: " + indicatorId));
        TemplateLevel3Node level3 = requireLevel3(templateId, level3Id);

        if (order != null) {
            guard.ensureUniqueIndicatorOrder(level3.getIndicators(), order, indicatorId);
        }

        TemplateNormativeIndicator patch = TemplateNormativeIndicator.builder()
                .code(code != null ? code : current.getCode())
                .description(description != null ? description : current.getDescription())
                .weight(weight != null ? weight : current.getWeight())
                .order(order != null ? order : current.getOrder())
                .referenceUrl(referenceUrl != null ? referenceUrl : current.getReferenceUrl())
                .build();
        guard.ensureIndicatorComplete(patch);
        return structurePort.updateIndicator(level3Id, indicatorId, patch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIndicator(UUID indicatorId) {
        UUID level3Id = structurePort.findLevel3IdByIndicator(indicatorId);
        UUID templateId = structurePort.findTemplateIdByIndicator(indicatorId);
        guard.loadDraftTemplate(templateManagementPort, templateId);
        structurePort.deleteIndicator(level3Id, indicatorId);
    }

    private List<TemplateLevel1Node> loadLevel1Nodes(UUID templateId) {
        return hierarchyQueryPort.findTemplateTree(templateId)
                .map(NormativeHierarchyQueryPort.TemplateNormativeTree::level1Nodes)
                .orElse(List.of());
    }

    private TemplateLevel1Node requireLevel1(UUID templateId, UUID level1Id) {
        return loadLevel1Nodes(templateId).stream()
                .filter(node -> level1Id.equals(node.getId()))
                .findFirst()
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 1 no encontrado: " + level1Id));
    }

    private TemplateLevel2Node requireLevel2(UUID templateId, UUID level2Id) {
        return loadLevel1Nodes(templateId).stream()
                .flatMap(level1 -> level1.getLevel2Nodes().stream())
                .filter(node -> level2Id.equals(node.getId()))
                .findFirst()
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 2 no encontrado: " + level2Id));
    }

    private TemplateLevel3Node requireLevel3(UUID templateId, UUID level3Id) {
        return loadLevel1Nodes(templateId).stream()
                .flatMap(level1 -> level1.getLevel2Nodes().stream())
                .flatMap(level2 -> level2.getLevel3Nodes().stream())
                .filter(node -> level3Id.equals(node.getId()))
                .findFirst()
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 3 no encontrado: " + level3Id));
    }
}
