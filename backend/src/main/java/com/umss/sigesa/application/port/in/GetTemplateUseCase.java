package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Template;

import java.util.UUID;

public interface GetTemplateUseCase {

    Template getById(UUID templateId);
}
