package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.UpdateTemplateUseCase;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.Template;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

public class UpdateTemplateService implements UpdateTemplateUseCase {

    private final TemplateManagementPort templateManagementPort;
    private final TemplateStructureValidator validator;

    public UpdateTemplateService(TemplateManagementPort templateManagementPort,
                                 TemplateStructureValidator validator) {
        this.templateManagementPort = templateManagementPort;
        this.validator = validator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Template update(UUID templateId, Template template) {
        Template existing = templateManagementPort.findByIdForEdit(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Plantilla no encontrada con ID: " + templateId));

        validator.validateType(template.getType());
        validator.validateOrders(template);
        validator.validateSubphaseLinks(template);

        template.setId(existing.getId());
        template.setStatus(existing.getStatus());
        template.setCreatedAt(existing.getCreatedAt());
        template.setUpdatedAt(LocalDateTime.now());
        template.setType(template.getType().trim().toUpperCase());

        return templateManagementPort.save(template);
    }
}
