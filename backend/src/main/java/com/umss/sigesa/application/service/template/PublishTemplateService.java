package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.PublishTemplateUseCase;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

public class PublishTemplateService implements PublishTemplateUseCase {

    private final TemplateManagementPort templateManagementPort;
    private final TemplateStructureValidator validator;

    public PublishTemplateService(TemplateManagementPort templateManagementPort,
                                  TemplateStructureValidator validator) {
        this.templateManagementPort = templateManagementPort;
        this.validator = validator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Template publish(UUID templateId) {
        Template template = templateManagementPort.findByIdForEdit(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Plantilla no encontrada con ID: " + templateId));

        validator.validateForPublish(template);
        template.setStatus(TemplateStatus.PUBLISHED);
        template.setUpdatedAt(LocalDateTime.now());

        return templateManagementPort.save(template);
    }
}
