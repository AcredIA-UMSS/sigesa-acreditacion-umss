package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.GetTemplateUseCase;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.Template;

import java.util.UUID;

public class GetTemplateService implements GetTemplateUseCase {

    private final TemplateManagementPort templateManagementPort;

    public GetTemplateService(TemplateManagementPort templateManagementPort) {
        this.templateManagementPort = templateManagementPort;
    }

    @Override
    public Template getById(UUID templateId) {
        return templateManagementPort.findByIdForEdit(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Plantilla no encontrada con ID: " + templateId));
    }
}
