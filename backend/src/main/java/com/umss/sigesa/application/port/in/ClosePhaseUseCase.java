package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.PhaseCompleteResult;

import java.util.UUID;

public interface ClosePhaseUseCase {

    PhaseCompleteResult close(UUID processId, UUID phaseId, UUID actorId, String actorRole);
}
