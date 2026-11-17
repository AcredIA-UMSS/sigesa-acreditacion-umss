package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.out.NormativeStructurePort;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;

import java.util.List;
import java.util.UUID;

/**
 * Clona el árbol normativo v2 de una plantilla publicada hacia un proceso recién creado.
 */
public class ProcessNormativeTreeCloner {

    private final NormativeStructurePort structurePort;

    public ProcessNormativeTreeCloner(NormativeStructurePort structurePort) {
        this.structurePort = structurePort;
    }

    public void cloneFromTemplate(UUID processId, List<TemplateLevel1Node> templateLevel1Nodes) {
        if (templateLevel1Nodes == null || templateLevel1Nodes.isEmpty()) {
            return;
        }

        for (TemplateLevel1Node templateLevel1 : templateLevel1Nodes) {
            Level1Node savedLevel1 = structurePort.saveLevel1(processId, Level1Node.builder()
                    .name(templateLevel1.getName())
                    .order(templateLevel1.getOrder())
                    .description(templateLevel1.getDescription())
                    .status(PhaseState.ABIERTA)
                    .build());

            cloneLevel2Nodes(savedLevel1.getId(), templateLevel1.getLevel2Nodes());
        }
    }

    private void cloneLevel2Nodes(UUID level1Id, List<TemplateLevel2Node> templateLevel2Nodes) {
        if (templateLevel2Nodes == null) {
            return;
        }
        for (TemplateLevel2Node templateLevel2 : templateLevel2Nodes) {
            Level2Node savedLevel2 = structurePort.saveLevel2(level1Id, Level2Node.builder()
                    .name(templateLevel2.getName())
                    .order(templateLevel2.getOrder())
                    .description(templateLevel2.getDescription())
                    .build());

            cloneLevel3Nodes(savedLevel2.getId(), templateLevel2.getLevel3Nodes());
        }
    }

    private void cloneLevel3Nodes(UUID level2Id, List<TemplateLevel3Node> templateLevel3Nodes) {
        if (templateLevel3Nodes == null) {
            return;
        }
        for (TemplateLevel3Node templateLevel3 : templateLevel3Nodes) {
            Level3Node savedLevel3 = structurePort.saveLevel3(level2Id, Level3Node.builder()
                    .name(templateLevel3.getName())
                    .order(templateLevel3.getOrder())
                    .description(templateLevel3.getDescription())
                    .build());

            cloneIndicators(savedLevel3.getId(), templateLevel3.getIndicators());
        }
    }

    private void cloneIndicators(UUID level3Id, List<TemplateNormativeIndicator> templateIndicators) {
        if (templateIndicators == null) {
            return;
        }
        for (TemplateNormativeIndicator templateIndicator : templateIndicators) {
            structurePort.saveIndicator(level3Id, NormativeIndicator.builder()
                    .code(templateIndicator.getCode())
                    .description(templateIndicator.getDescription())
                    .weight(templateIndicator.getWeight())
                    .order(templateIndicator.getOrder())
                    .referenceUrl(templateIndicator.getReferenceUrl())
                    .status(IndicatorState.PENDIENTE)
                    .build());
        }
    }
}
