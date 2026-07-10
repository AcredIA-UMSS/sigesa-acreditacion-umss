package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Template;

import java.util.UUID;

public interface ActivateTemplateUseCase {
    Template activate(UUID templateId, String period);
}
