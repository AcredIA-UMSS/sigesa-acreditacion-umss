package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateManagementPort {

    Template save(Template template);

    Optional<Template> findByIdForEdit(UUID id);

    List<Template> findAll();

    List<Template> findByStatus(TemplateStatus status);

    List<Template> findByStatusAndType(TemplateStatus status, String type);

    boolean existsProcessByTemplateId(UUID templateId);

    void delete(UUID templateId);
}
