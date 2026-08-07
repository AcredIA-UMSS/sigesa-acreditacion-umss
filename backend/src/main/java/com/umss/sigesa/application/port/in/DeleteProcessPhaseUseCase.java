package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface DeleteProcessPhaseUseCase {

    void execute(UUID processId, UUID phaseId);
}
