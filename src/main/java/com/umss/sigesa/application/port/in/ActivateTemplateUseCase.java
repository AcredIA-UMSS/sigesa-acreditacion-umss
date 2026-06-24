package com.umss.sigesa.application.port.in;

import java.util.UUID;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessType;

public interface ActivateTemplateUseCase {
    void activate(UUID templateId, String period);
}
