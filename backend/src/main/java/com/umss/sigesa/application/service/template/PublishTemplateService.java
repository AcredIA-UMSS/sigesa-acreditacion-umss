package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.PublishTemplateUseCase;
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

public class PublishTemplateService implements PublishTemplateUseCase {

    private final TemplateManagementPort templateManagementPort;
    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final TemplateStructureValidator validator;
    private final TemplateNormativeStructureGuard structureGuard;

    public PublishTemplateService(TemplateManagementPort templateManagementPort,
                                  NormativeHierarchyQueryPort hierarchyQueryPort,
                                  TemplateStructureValidator validator,
                                  TemplateNormativeStructureGuard structureGuard) {
        this.templateManagementPort = templateManagementPort;
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.validator = validator;
        this.structureGuard = structureGuard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Template publish(UUID templateId) {
        Template template = templateManagementPort.findByIdForEdit(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Plantilla no encontrada con ID: " + templateId));

        List<TemplateLevel1Node> level1Nodes = hierarchyQueryPort.findTemplateTree(templateId)
                .map(NormativeHierarchyQueryPort.TemplateNormativeTree::level1Nodes)
                .orElse(List.of());

        validator.validateForPublish(template, level1Nodes, structureGuard);
        template.setStatus(TemplateStatus.PUBLISHED);
        template.setUpdatedAt(LocalDateTime.now());

        return templateManagementPort.save(template);
    }
}
