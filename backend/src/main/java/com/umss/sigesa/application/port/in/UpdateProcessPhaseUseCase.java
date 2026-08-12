package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.Phase;

import java.util.UUID;

public interface UpdateProcessPhaseUseCase {

    Phase execute(UUID processId, UUID phaseId, String name, Integer order, String description);
}
