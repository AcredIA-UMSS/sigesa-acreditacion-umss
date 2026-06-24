package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class DuplicateActiveAssignmentException extends RuntimeException {

    public DuplicateActiveAssignmentException(UUID userId, UUID programId) {
        super("Ya existe una asignación activa para userId=" + userId + " y programId=" + programId);
    }
}
