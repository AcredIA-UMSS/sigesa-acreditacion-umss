package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.PhaseProgressResult;

import java.util.UUID;

public interface GetPhaseProgressUseCase {

    PhaseProgressResult getProgress(UUID processId, UUID phaseId);
}
