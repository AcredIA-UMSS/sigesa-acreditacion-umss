package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.out.TemplateNormativeStructurePort;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;

import java.util.List;
import java.util.UUID;

/**
 * Clona el árbol normativo v2 de una plantilla origen hacia otra plantilla DRAFT.
 */
public class TemplateNormativeTreeCloner {

    private final TemplateNormativeStructurePort structurePort;

    public TemplateNormativeTreeCloner(TemplateNormativeStructurePort structurePort) {
        this.structurePort = structurePort;
    }

    public void cloneFromTemplate(UUID targetTemplateId, List<TemplateLevel1Node> sourceLevel1Nodes) {
        if (sourceLevel1Nodes == null || sourceLevel1Nodes.isEmpty()) {
            return;
        }

        for (TemplateLevel1Node sourceLevel1 : sourceLevel1Nodes) {
            TemplateLevel1Node savedLevel1 = structurePort.saveLevel1(targetTemplateId, TemplateLevel1Node.builder()
                    .name(sourceLevel1.getName())
                    .order(sourceLevel1.getOrder())
                    .description(sourceLevel1.getDescription())
                    .build());

            cloneLevel2Nodes(savedLevel1.getId(), sourceLevel1.getLevel2Nodes());
        }
    }

    private void cloneLevel2Nodes(UUID level1Id, List<TemplateLevel2Node> sourceLevel2Nodes) {
        if (sourceLevel2Nodes == null) {
            return;
        }
        for (TemplateLevel2Node sourceLevel2 : sourceLevel2Nodes) {
            TemplateLevel2Node savedLevel2 = structurePort.saveLevel2(level1Id, TemplateLevel2Node.builder()
                    .name(sourceLevel2.getName())
                    .order(sourceLevel2.getOrder())
                    .description(sourceLevel2.getDescription())
                    .build());

            cloneLevel3Nodes(savedLevel2.getId(), sourceLevel2.getLevel3Nodes());
        }
    }

    private void cloneLevel3Nodes(UUID level2Id, List<TemplateLevel3Node> sourceLevel3Nodes) {
        if (sourceLevel3Nodes == null) {
            return;
        }
        for (TemplateLevel3Node sourceLevel3 : sourceLevel3Nodes) {
            TemplateLevel3Node savedLevel3 = structurePort.saveLevel3(level2Id, TemplateLevel3Node.builder()
                    .name(sourceLevel3.getName())
                    .order(sourceLevel3.getOrder())
                    .description(sourceLevel3.getDescription())
                    .build());

            cloneIndicators(savedLevel3.getId(), sourceLevel3.getIndicators());
        }
    }

    private void cloneIndicators(UUID level3Id, List<TemplateNormativeIndicator> sourceIndicators) {
        if (sourceIndicators == null) {
            return;
        }
        for (TemplateNormativeIndicator sourceIndicator : sourceIndicators) {
            structurePort.saveIndicator(level3Id, TemplateNormativeIndicator.builder()
                    .code(sourceIndicator.getCode())
                    .description(sourceIndicator.getDescription())
                    .weight(sourceIndicator.getWeight())
                    .order(sourceIndicator.getOrder())
                    .referenceUrl(sourceIndicator.getReferenceUrl())
                    .build());
        }
    }
}
