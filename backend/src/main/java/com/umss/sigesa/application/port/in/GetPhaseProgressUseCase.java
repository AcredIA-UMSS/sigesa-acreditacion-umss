package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.process.PhaseProgress;

import java.util.UUID;

public interface GetPhaseProgressUseCase {

    PhaseProgress getProgress(UUID processId, UUID phaseId);
}
