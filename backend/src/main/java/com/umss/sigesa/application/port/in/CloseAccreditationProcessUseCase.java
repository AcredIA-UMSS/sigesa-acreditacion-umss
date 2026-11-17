package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface CloseAccreditationProcessUseCase {

    /**
     * Sets process status to CLOSED and enqueues ProcessClosed dual-channel notifications to [JD].
     * USER OVERRIDE: no EE 100% gate — notifies only after status is CLOSED.
     */
    void close(UUID processId, UUID actorId, String actorRole);
}
