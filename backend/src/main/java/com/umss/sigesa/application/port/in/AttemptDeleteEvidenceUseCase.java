package com.umss.sigesa.application.port.in;

import java.util.List;
import java.util.UUID;

public interface AttemptDeleteEvidenceUseCase {

    void attemptDelete(UUID evidenceId, UUID actorId, List<String> roles);
}
