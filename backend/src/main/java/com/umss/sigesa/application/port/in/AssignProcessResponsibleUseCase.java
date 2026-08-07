package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.process.ProcessResponsibleInfo;

import java.util.UUID;

public interface AssignProcessResponsibleUseCase {

    ProcessResponsibleInfo assign(UUID processId, UUID userId, UUID assignedByUserId);
}
