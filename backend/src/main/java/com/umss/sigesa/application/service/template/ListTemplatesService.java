package com.umss.sigesa.application.service.template;

import com.umss.sigesa.application.port.in.ListTemplatesUseCase;
import com.umss.sigesa.application.port.out.TemplateManagementPort;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;

import java.util.List;
import java.util.Optional;

public class ListTemplatesService implements ListTemplatesUseCase {

    private final TemplateManagementPort templateManagementPort;

    public ListTemplatesService(TemplateManagementPort templateManagementPort) {
        this.templateManagementPort = templateManagementPort;
    }

    @Override
    public List<Template> list(Optional<TemplateStatus> status, Optional<String> type) {
        if (status.isPresent() && type.isPresent()) {
            return templateManagementPort.findByStatusAndType(status.get(), type.get().trim().toUpperCase());
        }
        if (status.isPresent()) {
            return templateManagementPort.findByStatus(status.get());
        }
        return templateManagementPort.findAll();
    }
}
