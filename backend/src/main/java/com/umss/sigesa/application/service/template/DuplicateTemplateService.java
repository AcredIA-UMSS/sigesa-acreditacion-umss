package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.DuplicateTemplateUseCase;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateStatus;
import com.umss.sigesa.domain.model.TemplateSubphase;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuplicateTemplateService implements DuplicateTemplateUseCase {

    private final TemplateManagementPort templateManagementPort;
    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final TemplateNormativeTreeCloner normativeTreeCloner;

    public DuplicateTemplateService(TemplateManagementPort templateManagementPort,
                                    NormativeHierarchyQueryPort hierarchyQueryPort,
                                    TemplateNormativeTreeCloner normativeTreeCloner) {
        this.templateManagementPort = templateManagementPort;
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.normativeTreeCloner = normativeTreeCloner;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Template duplicate(UUID templateId) {
        Template source = templateManagementPort.findByIdForEdit(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Plantilla no encontrada con ID: " + templateId));

        LocalDateTime now = LocalDateTime.now();
        Template copy = Template.builder()
                .id(UUID.randomUUID())
                .name("Copia de " + source.getName())
                .description(source.getDescription())
                .type(source.getType())
                .status(TemplateStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .phases(new ArrayList<>())
                .build();

        if (source.getPhases() != null) {
            source.getPhases().forEach(sourcePhase -> {
                TemplatePhase phaseCopy = TemplatePhase.builder()
                        .name(sourcePhase.getName())
                        .order(sourcePhase.getOrder())
                        .description(sourcePhase.getDescription())
                        .subphases(new ArrayList<>())
                        .build();

                if (sourcePhase.getSubphases() != null) {
                    sourcePhase.getSubphases().forEach(sourceSubphase -> phaseCopy.getSubphases().add(
                            TemplateSubphase.builder()
                                    .name(sourceSubphase.getName())
                                    .order(sourceSubphase.getOrder())
                                    .referenceUrl(sourceSubphase.getReferenceUrl())
                                    .description(sourceSubphase.getDescription())
                                    .requirements(sourceSubphase.getRequirements())
                                    .build()
                    ));
                }
                copy.getPhases().add(phaseCopy);
            });
        }

        Template savedCopy = templateManagementPort.save(copy);

        List<TemplateLevel1Node> sourceTree = hierarchyQueryPort.findTemplateTree(templateId)
                .map(NormativeHierarchyQueryPort.TemplateNormativeTree::level1Nodes)
                .orElse(List.of());
        if (hierarchyQueryPort.countIndicatorsByTemplateId(templateId) > 0) {
            normativeTreeCloner.cloneFromTemplate(savedCopy.getId(), sourceTree);
        }

        return savedCopy;
    }
}
