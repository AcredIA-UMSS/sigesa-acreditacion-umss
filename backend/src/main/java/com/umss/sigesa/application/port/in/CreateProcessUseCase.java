package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.AccreditationProcess;

import java.util.UUID;

public interface CreateProcessUseCase {
    AccreditationProcess createProcess(UUID careerId, UUID templateId);
}

