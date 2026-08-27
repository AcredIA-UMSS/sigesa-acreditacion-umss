package com.umss.sigesa.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface SubphaseQueryPort {

    Optional<SubphaseContext> findContext(UUID subphaseId);

    record SubphaseContext(UUID subphaseId, UUID careerId, String subphaseName) {
    }
}
