package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface DeleteTemplateUseCase {

    void delete(UUID templateId);
}
