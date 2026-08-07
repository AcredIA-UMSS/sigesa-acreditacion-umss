package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.DeleteTemplateUseCase;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.exception.TemplateInUseException;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteTemplateService implements DeleteTemplateUseCase {

    private final TemplateManagementPort templateManagementPort;

    public DeleteTemplateService(TemplateManagementPort templateManagementPort) {
        this.templateManagementPort = templateManagementPort;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID templateId) {
        Template template = templateManagementPort.findByIdForEdit(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Plantilla no encontrada con ID: " + templateId));

        if (template.getStatus() != TemplateStatus.DRAFT) {
            throw new TemplateInUseException("Solo se pueden eliminar plantillas en estado DRAFT.");
        }

        if (templateManagementPort.existsProcessByTemplateId(templateId)) {
            throw new TemplateInUseException(
                    "La plantilla está referenciada por procesos existentes y no puede eliminarse.");
        }

        templateManagementPort.delete(templateId);
    }
}
