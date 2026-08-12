package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.CreateTemplateUseCase;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

public class CreateTemplateService implements CreateTemplateUseCase {

    private final TemplateManagementPort templateManagementPort;
    private final TemplateStructureValidator validator;

    public CreateTemplateService(TemplateManagementPort templateManagementPort,
                                 TemplateStructureValidator validator) {
        this.templateManagementPort = templateManagementPort;
        this.validator = validator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Template create(Template template) {
        validator.validateType(template.getType());
        validator.validateOrders(template);
        validator.validateSubphaseLinks(template);

        LocalDateTime now = LocalDateTime.now();
        template.setId(UUID.randomUUID());
        template.setStatus(TemplateStatus.DRAFT);
        template.setType(template.getType().trim().toUpperCase());
        template.setCreatedAt(now);
        template.setUpdatedAt(now);

        return templateManagementPort.save(template);
    }
}
