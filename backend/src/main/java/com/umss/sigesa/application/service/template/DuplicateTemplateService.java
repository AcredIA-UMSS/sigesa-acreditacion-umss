package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.DuplicateTemplateUseCase;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
                .build();

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
