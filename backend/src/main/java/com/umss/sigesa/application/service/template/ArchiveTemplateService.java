package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.ArchiveTemplateUseCase;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

public class ArchiveTemplateService implements ArchiveTemplateUseCase {

    private final TemplateManagementPort templateManagementPort;

    public ArchiveTemplateService(TemplateManagementPort templateManagementPort) {
        this.templateManagementPort = templateManagementPort;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Template archive(UUID templateId) {
        Template template = templateManagementPort.findByIdForEdit(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Plantilla no encontrada con ID: " + templateId));

        template.setStatus(TemplateStatus.ARCHIVED);
        template.setUpdatedAt(LocalDateTime.now());

        return templateManagementPort.save(template);
    }
}
