package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.in.NormativeStructureUseCases;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NormativeStructurePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.exception.IndicatorHasEvidenceException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.PhaseState;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class NormativeStructureCommandService implements NormativeStructureUseCases {

    private final ProcessQueryPort processQueryPort;
    private final NormativeStructurePort structurePort;
    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final NormativeIndicatorWorkflowPort indicatorWorkflowPort;
    private final NormativeStructureGuard guard;

    public NormativeStructureCommandService(ProcessQueryPort processQueryPort,
                                            NormativeStructurePort structurePort,
                                            NormativeHierarchyQueryPort hierarchyQueryPort,
                                            NormativeIndicatorWorkflowPort indicatorWorkflowPort,
                                            NormativeStructureGuard guard) {
        this.processQueryPort = processQueryPort;
        this.structurePort = structurePort;
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.indicatorWorkflowPort = indicatorWorkflowPort;
        this.guard = guard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Level1Node addLevel1(UUID processId, String name, Integer order, String description) {
        guard.loadActiveProcess(processQueryPort, processId);
        List<Level1Node> existing = loadLevel1Nodes(processId);
        guard.ensureUniqueLevel1Order(existing, order, null);

        return structurePort.saveLevel1(processId, Level1Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .status(PhaseState.ABIERTA)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Level1Node updateLevel1(UUID processId, UUID level1Id, String name, Integer order, String description) {
        guard.loadActiveProcess(processQueryPort, processId);
        if (order != null) {
            guard.ensureUniqueLevel1Order(loadLevel1Nodes(processId), order, level1Id);
        }
        return structurePort.updateLevel1(processId, level1Id, Level1Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLevel1(UUID processId, UUID level1Id) {
        guard.loadActiveProcess(processQueryPort, processId);
        ensureDeletableIndicators(structurePort.findIndicatorsUnderLevel1(level1Id));
        structurePort.deleteLevel1(processId, level1Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Level2Node addLevel2(UUID level1Id, String name, Integer order, String description) {
        UUID processId = structurePort.findProcessIdByLevel1(level1Id);
        guard.loadActiveProcess(processQueryPort, processId);
        Level1Node level1 = requireLevel1(processId, level1Id);
        guard.ensureUniqueLevel2Order(level1.getLevel2Nodes(), order, null);

        return structurePort.saveLevel2(level1Id, Level2Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Level2Node updateLevel2(UUID level2Id, String name, Integer order, String description) {
        UUID level1Id = structurePort.findLevel1IdByLevel2(level2Id);
        UUID processId = structurePort.findProcessIdByLevel2(level2Id);
        guard.loadActiveProcess(processQueryPort, processId);
        if (order != null) {
            Level1Node level1 = requireLevel1(processId, level1Id);
            guard.ensureUniqueLevel2Order(level1.getLevel2Nodes(), order, level2Id);
        }
        return structurePort.updateLevel2(level1Id, level2Id, Level2Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLevel2(UUID level2Id) {
        UUID level1Id = structurePort.findLevel1IdByLevel2(level2Id);
        UUID processId = structurePort.findProcessIdByLevel2(level2Id);
        guard.loadActiveProcess(processQueryPort, processId);
        Level2Node level2 = requireLevel2(processId, level2Id);
        if (level2.getLevel3Nodes() != null) {
            for (Level3Node level3 : level2.getLevel3Nodes()) {
                ensureDeletableIndicators(level3.getIndicators());
            }
        }
        structurePort.deleteLevel2(level1Id, level2Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Level3Node addLevel3(UUID level2Id, String name, Integer order, String description) {
        UUID processId = structurePort.findProcessIdByLevel2(level2Id);
        guard.loadActiveProcess(processQueryPort, processId);
        Level2Node level2 = requireLevel2(processId, level2Id);
        guard.ensureUniqueLevel3Order(level2.getLevel3Nodes(), order, null);

        return structurePort.saveLevel3(level2Id, Level3Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Level3Node updateLevel3(UUID level3Id, String name, Integer order, String description) {
        UUID level2Id = structurePort.findLevel2IdByLevel3(level3Id);
        UUID processId = structurePort.findProcessIdByLevel3(level3Id);
        guard.loadActiveProcess(processQueryPort, processId);
        if (order != null) {
            Level2Node level2 = requireLevel2(processId, level2Id);
            guard.ensureUniqueLevel3Order(level2.getLevel3Nodes(), order, level3Id);
        }
        return structurePort.updateLevel3(level2Id, level3Id, Level3Node.builder()
                .name(name)
                .order(order)
                .description(description)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLevel3(UUID level3Id) {
        UUID level2Id = structurePort.findLevel2IdByLevel3(level3Id);
        UUID processId = structurePort.findProcessIdByLevel3(level3Id);
        guard.loadActiveProcess(processQueryPort, processId);
        Level3Node level3 = requireLevel3(processId, level3Id);
        ensureDeletableIndicators(level3.getIndicators());
        structurePort.deleteLevel3(level2Id, level3Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NormativeIndicator addIndicator(UUID level3Id, String code, String description, BigDecimal weight,
                                           Integer order, String referenceUrl) {
        UUID processId = structurePort.findProcessIdByLevel3(level3Id);
        guard.loadActiveProcess(processQueryPort, processId);
        Level3Node level3 = requireLevel3(processId, level3Id);
        guard.ensureUniqueIndicatorOrder(level3.getIndicators(), order, null);

        NormativeIndicator indicator = NormativeIndicator.builder()
                .code(code)
                .description(description)
                .weight(weight != null ? weight : BigDecimal.ONE)
                .order(order)
                .referenceUrl(referenceUrl)
                .status(IndicatorState.PENDIENTE)
                .build();
        guard.ensureIndicatorComplete(indicator);
        return structurePort.saveIndicator(level3Id, indicator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NormativeIndicator updateIndicator(UUID indicatorId, String code, String description, BigDecimal weight,
                                              Integer order, String referenceUrl) {
        UUID level3Id = structurePort.findLevel3IdByIndicator(indicatorId);
        UUID processId = structurePort.findProcessIdByIndicator(indicatorId);
        guard.loadActiveProcess(processQueryPort, processId);
        NormativeIndicator current = hierarchyQueryPort.findIndicatorById(indicatorId)
                .orElseThrow(() -> new ProcessNotFoundException("Indicador no encontrado: " + indicatorId));
        Level3Node level3 = requireLevel3(processId, level3Id);

        if (order != null) {
            guard.ensureUniqueIndicatorOrder(level3.getIndicators(), order, indicatorId);
        }

        NormativeIndicator patch = NormativeIndicator.builder()
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
        UUID processId = structurePort.findProcessIdByIndicator(indicatorId);
        guard.loadActiveProcess(processQueryPort, processId);
        NormativeIndicator indicator = hierarchyQueryPort.findIndicatorById(indicatorId)
                .orElseThrow(() -> new ProcessNotFoundException("Indicador no encontrado: " + indicatorId));
        ensureDeletableIndicator(indicator);
        structurePort.deleteIndicator(level3Id, indicatorId);
    }

    private List<Level1Node> loadLevel1Nodes(UUID processId) {
        return hierarchyQueryPort.findProcessTree(processId)
                .map(NormativeHierarchyQueryPort.ProcessNormativeTree::level1Nodes)
                .orElse(List.of());
    }

    private void ensureDeletableIndicators(List<NormativeIndicator> indicators) {
        if (indicators == null) {
            return;
        }
        for (NormativeIndicator indicator : indicators) {
            ensureDeletableIndicator(indicator);
        }
    }

    private void ensureDeletableIndicator(NormativeIndicator indicator) {
        if (indicatorWorkflowPort.isWorkflowStarted(indicator.getId())) {
            throw new IndicatorHasEvidenceException(indicator.getId());
        }
    }

    private Level1Node requireLevel1(UUID processId, UUID level1Id) {
        return loadLevel1Nodes(processId).stream()
                .filter(node -> level1Id.equals(node.getId()))
                .findFirst()
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 1 no encontrado: " + level1Id));
    }

    private Level2Node requireLevel2(UUID processId, UUID level2Id) {
        return loadLevel1Nodes(processId).stream()
                .flatMap(level1 -> level1.getLevel2Nodes().stream())
                .filter(node -> level2Id.equals(node.getId()))
                .findFirst()
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 2 no encontrado: " + level2Id));
    }

    private Level3Node requireLevel3(UUID processId, UUID level3Id) {
        return loadLevel1Nodes(processId).stream()
                .flatMap(level1 -> level1.getLevel2Nodes().stream())
                .flatMap(level2 -> level2.getLevel3Nodes().stream())
                .filter(node -> level3Id.equals(node.getId()))
                .findFirst()
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 3 no encontrado: " + level3Id));
    }
}
