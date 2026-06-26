package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessType;
import java.util.UUID;

public interface CreateAccreditationProcessUseCase {
    AccreditationProcess create(UUID templateId, UUID careerId, String period, ProcessType type);
}

